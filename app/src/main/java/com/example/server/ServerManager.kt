package com.example.server

import android.content.Context
import com.example.data.ServerProfile
import com.example.server.engine.ServerEngine
import com.example.server.template.ServerTemplate
import com.example.server.template.TemplateRegistry
import java.io.File
import com.example.server.engine.NukkitEngine
import com.example.server.version.EngineVersion
import com.example.tunnel.TunnelManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class ServerManager(
    private val context: Context,
    private val profileProvider: () -> ServerProfile?,
    private val onLog: (String) -> Unit,
    private val onStatusChange: (ServerStatus) -> Unit
) {
    private var currentEngine: ServerEngine? = null
    var activeTemplate: ServerTemplate = TemplateRegistry.BEDROCK_CLOUDBURST_NUKKIT // Default
    private var onlineMode: Boolean = true // Default: Online Mode
    
    val tunnelManager = TunnelManager(context, onLog)

    private val internalOnStatusChange: (ServerStatus) -> Unit = { status ->
        onStatusChange(status)
        if (status == ServerStatus.STOPPED || status == ServerStatus.FAILED || status == ServerStatus.CRASHED) {
            tunnelManager.stopTunnel()
        }
    }
    
    fun getPublicAddress() = tunnelManager.publicAddress
    
    fun checkIntegrity() {
        val engine = getEngine()
        if (engine is com.example.server.engine.BaseJavaEngine) engine.checkIntegrity()
    }
    
    fun setTemplate(template: ServerTemplate, engineVersion: EngineVersion) {
        if (currentEngine?.getStatus() == ServerStatus.ONLINE) {
            onLog("Cannot change template while server is running.")
            return
        }
        activeTemplate = template
        val profile = profileProvider()
        val serverDir = profile?.let { File(it.serverDirectory) } ?: File(context.filesDir, "minecraft/engines/default")
        
        currentEngine = ServerFactory.createEngine(context, serverDir, template, engineVersion, onLog, internalOnStatusChange)
        currentEngine?.setOnlineMode(onlineMode)
        onLog("Switched to template: ${template.name}")
    }

    fun setOnlineMode(online: Boolean) {
        onlineMode = online
        currentEngine?.setOnlineMode(online)
    }

    private fun getEngine(): ServerEngine {
        if (currentEngine == null) {
            val profile = profileProvider()
            val serverDir = profile?.let { File(it.serverDirectory) } ?: File(context.filesDir, "minecraft/engines/default")
            
            val versionCatalog = com.example.server.version.EngineVersionCatalog(context)
            val versionId = profile?.engineVersionId ?: ""
            
            // Problem 2: Do not silently replace invalid version IDs
            val engineVersion = versionCatalog.findVersion(versionId)
            
            if (engineVersion == null || engineVersion.engineId != activeTemplate.id) {
                if (engineVersion == null) {
                    onLog("ERROR: Selected engine build '$versionId' could not be resolved from the version catalogue.")
                } else {
                    onLog("ERROR: Selected engine build '${engineVersion.displayName}' does not belong to the active engine '${activeTemplate.name}'.")
                }
                onStatusChange(ServerStatus.FAILED)
                // Return a dummy engine
                return object : ServerEngine {
                    override fun startServer(memoryMb: Int) {
                        onLog("ERROR: Server cannot start due to unresolved engine build.")
                    }
                    override fun stopServer() {}
                    override fun restartServer() {}
                    override fun sendCommand(command: String) {}
                    override fun getStatus(): ServerStatus = ServerStatus.FAILED
                    override fun setOnlineMode(online: Boolean) {}
                    override fun installPlugin(url: String, fileName: String) {}
                    override fun backupWorld() {}
                    override fun getServerDir(): File = serverDir
                }
            }
            
            currentEngine = ServerFactory.createEngine(context, serverDir, activeTemplate, engineVersion, onLog, internalOnStatusChange)
            currentEngine?.setOnlineMode(onlineMode)
        }
        return currentEngine!!
    }

    fun switchProfile() {
        if (currentEngine?.getStatus() == ServerStatus.ONLINE) {
            onLog("Warning: Switching profile while server is running. Stopping current engine...")
            stopServer()
        }
        currentEngine = null
    }

    fun startServer(memoryMb: Int = 600, scope: CoroutineScope) {
        scope.launch {
            onLog("[Server] Starting local server mode...")
            onLog("[Server] Starting Minecraft server...")
            getEngine().startServer(memoryMb)
        }
    }

    fun stopServer() {
        currentEngine?.stopServer()
    }

    fun restartServer() {
        currentEngine?.restartServer()
    }
    
    fun sendCommand(command: String) {
        currentEngine?.sendCommand(command)
    }

    fun getServerDir(): File = getEngine().getServerDir()

    fun getProcessId(): Long? = currentEngine?.getProcessId()

    fun getStartedAtMillis(): Long? = currentEngine?.getStartedAtMillis()

    fun getCurrentStatus(): ServerStatus = currentEngine?.getStatus() ?: ServerStatus.STOPPED
}
