package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus

class PowerNukkitEngine(
    context: Context,
    serverDir: java.io.File,
    engineVersionId: String,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersionId, onLog, onStatusChange) {
    override val serverFolderName = "powernukkit"
    override val serverEngineName = "PowerNukkit"
    override val minJavaVersion = 8
    override val maxJavaVersion = 21
}
