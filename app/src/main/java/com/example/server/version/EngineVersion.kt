package com.example.server.version

enum class ReleaseChannel {
    STABLE,
    SNAPSHOT,
    PREVIEW
}

data class EngineVersion(
    val id: String,
    val engineId: String,
    val versionName: String,
    val displayName: String,
    val channel: ReleaseChannel,
    val downloadUrl: String,
    val jarFileName: String,
    val requiredJavaVersion: Int,
    val compatibilityLabel: String,
    val recommended: Boolean
)
