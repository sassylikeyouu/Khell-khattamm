package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class PaperEngine(
    context: Context,
    serverDir: File,
    engineVersionId: String,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersionId, onLog, onStatusChange) {
    override val serverFolderName = "paper"
    override val serverEngineName = "Paper"
    override val minJavaVersion = 21
    override val maxJavaVersion = 21
}
