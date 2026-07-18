package com.example.server.engine

import android.content.Context
import com.example.server.ServerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

import com.example.server.version.EngineVersion

class PaperEngine(
    context: Context,
    serverDir: File,
    engineVersion: EngineVersion,
    onLog: (String) -> Unit,
    onStatusChange: (ServerStatus) -> Unit
) : BaseJavaEngine(context, serverDir, engineVersion, onLog, onStatusChange) {
    override val serverFolderName = "paper"
    override val serverEngineName = "Paper"
    override val minJavaVersion = 21
    override val maxJavaVersion = 21
}
