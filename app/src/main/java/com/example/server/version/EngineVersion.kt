package com.example.server.version

enum class ReleaseChannel {
    STABLE,
    SNAPSHOT,
    PREVIEW
}

enum class CompatibilityMode {
    SINGLE_VERSION,
    MULTI_VERSION,
    UNKNOWN
}

data class BedrockVersionOption(
    val bedrockVersion: String,
    val engineVersionId: String,
    val engineBuildName: String,
    val recommended: Boolean,
    val compatibilityMode: CompatibilityMode,
    val compatibilitySummary: String?
)

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
    val recommended: Boolean,
    val supportedBedrockVersions: List<String> = emptyList(),
    val recommendedBedrockVersion: String? = null,
    val compatibilityMode: CompatibilityMode = CompatibilityMode.SINGLE_VERSION,
    val compatibilitySummary: String? = null
)
