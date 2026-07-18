package com.example.server.version

data class InstalledEngineVersion(
    val engineId: String,
    val versionId: String,
    val versionName: String,
    val jarFileName: String,
    val installedAt: Long
)
