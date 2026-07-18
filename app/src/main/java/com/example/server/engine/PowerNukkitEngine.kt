package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus

import com.example.server.version.EngineVersion

class PowerNukkitEngine(
    context: Context,
    serverDir: java.io.File,
    engineVersion: EngineVersion,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersion, onLog, onStatusChange) {
    override val serverFolderName = "powernukkit"
    override val serverEngineName = "PowerNukkit"
    override val minJavaVersion = 8
    override val maxJavaVersion = 21
}
