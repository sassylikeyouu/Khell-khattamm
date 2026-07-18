package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus

class NukkitMOTEngine(
    context: Context,
    serverDir: java.io.File,
    engineVersionId: String,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersionId, onLog, onStatusChange) {
    override val serverFolderName = "nukkit-mot"
    override val serverEngineName = "Nukkit-MOT"
    override val minJavaVersion = 25
    override val maxJavaVersion = 25
}
