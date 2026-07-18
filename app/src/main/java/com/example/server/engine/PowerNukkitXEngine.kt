package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus

import com.example.server.version.EngineVersion

class PowerNukkitXEngine(
    context: Context,
    serverDir: java.io.File,
    engineVersion: EngineVersion,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersion, onLog, onStatusChange) {
    override val serverFolderName = "powernukkitx"
    override val serverEngineName = "PowerNukkitX"
    override fun getEngineId(): String = "bedrock_power_nukkit_x"
    override val minJavaVersion = 21
    override val maxJavaVersion = 21
}
