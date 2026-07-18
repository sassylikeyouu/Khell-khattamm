package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus

import com.example.server.version.EngineVersion

class NukkitMOTEngine(
    context: Context,
    serverDir: java.io.File,
    engineVersion: EngineVersion,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersion, onLog, onStatusChange) {
    override val serverFolderName = "nukkit-mot"
    override val serverEngineName = "Nukkit-MOT"
    override val minJavaVersion = 25
    override val maxJavaVersion = 25
}
