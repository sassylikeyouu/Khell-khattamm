package com.example.server.version

import org.json.JSONObject
import java.io.File
import java.util.jar.JarFile

object InstalledEngineVersionRepository {
    private const val META_PATH = ".minehost/engine-installation.json"

    fun read(serverDir: File): InstalledEngineVersion? {
        val metaFile = File(serverDir, META_PATH)
        if (!metaFile.exists()) return null

        return try {
            val json = JSONObject(metaFile.readText())
            InstalledEngineVersion(
                engineId = json.optString("engineId", ""),
                versionId = json.getString("versionId"),
                versionName = json.optString("versionName", ""),
                jarFileName = json.optString("jarName", ""), // Match previous key if any
                installedAt = json.optLong("installedAt", 0L)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun write(serverDir: File, installed: InstalledEngineVersion): Boolean {
        return try {
            val metaDir = File(serverDir, ".minehost").apply { mkdirs() }
            val metaFile = File(metaDir, "engine-installation.json")
            val json = JSONObject().apply {
                put("engineId", installed.engineId)
                put("versionId", installed.versionId)
                put("versionName", installed.versionName)
                put("jarName", installed.jarFileName)
                put("installedAt", installed.installedAt)
            }
            metaFile.writeText(json.toString(2))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun matches(
        serverDir: File,
        selectedVersion: EngineVersion
    ): Boolean {
        val metadata = read(serverDir) ?: return false
        
        // Metadata exists and parses successfully.
        // metadata.engineId == selectedVersion.engineId
        // metadata.versionId == selectedVersion.id
        // metadata.jarFileName == selectedVersion.jarFileName
        
        if (metadata.engineId != selectedVersion.engineId) return false
        if (metadata.versionId != selectedVersion.id) return false
        if (metadata.jarFileName != selectedVersion.jarFileName) return false
        
        // The expected JAR exists.
        val jarFile = File(serverDir, selectedVersion.jarFileName)
        if (!jarFile.exists()) return false
        
        // The JAR passes validation.
        return validateJar(jarFile)
    }

    fun validateJar(jarFile: File): Boolean {
        if (!jarFile.exists()) return false
        if (jarFile.length() < 1_000_000) return false // 1MB
        
        return try {
            // Check for ZIP magic bytes (implicit in JarFile opening)
            // The file can be opened using java.util.jar.JarFile.
            JarFile(jarFile).use { jar ->
                // It contains at least one .class file.
                var hasClass = false
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.endsWith(".class")) {
                        hasClass = true
                        break
                    }
                }
                hasClass
            }
        } catch (e: Exception) {
            false
        }
    }
}
