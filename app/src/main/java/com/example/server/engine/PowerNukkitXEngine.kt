package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus

class PowerNukkitXEngine(
    context: Context,
    serverDir: java.io.File,
    engineVersionId: String,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersionId, onLog, onStatusChange) {
    override val serverFolderName = "powernukkitx"
    override val serverEngineName = "PowerNukkitX"
    override val minJavaVersion = 21
    override val maxJavaVersion = 21
}
