package com.example.server.version

import com.example.data.ServerProfile

object EngineCompatibilityValidator {
    fun validate(profile: ServerProfile, versionCatalog: EngineVersionCatalog): Result {
        val version = versionCatalog.findVersion(profile.engineVersionId)
            ?: return Result(false, "Selected engine build could not be resolved from the version catalogue.")

        if (version.engineId != profile.engineId) {
            return Result(false, "Engine mismatch: profile expects ${profile.engineId} but build belongs to ${version.engineId}.")
        }

        if (profile.bedrockVersion.isBlank()) {
            return Result(false, "Minecraft version is missing in the server profile.")
        }

        if (!version.supportedBedrockVersions.contains(profile.bedrockVersion)) {
            return Result(false, "Selected Minecraft version is not supported by the selected engine build.")
        }

        return Result(true, "OK")
    }

    data class Result(val success: Boolean, val message: String)
}
