package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus

import com.example.server.version.EngineVersion

class CloudburstEngine(
    context: Context,
    serverDir: java.io.File,
    engineVersion: EngineVersion,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersion, onLog, onStatusChange) {
    override val serverFolderName = "cloudburst"
    override val serverEngineName = "Cloudburst"
    override val minJavaVersion = 21
    override val maxJavaVersion = 21
}
