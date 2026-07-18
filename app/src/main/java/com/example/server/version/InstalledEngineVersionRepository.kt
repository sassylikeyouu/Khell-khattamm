package com.example.server.version

import org.json.JSONObject
import java.io.File
import java.util.jar.JarFile

object InstalledEngineVersionRepository {
    private const val META_PATH = ".minehost/engine-installation.json"

    fun metadataExists(serverDir: File): Boolean {
        return File(serverDir, META_PATH).exists()
    }

    fun metadataIsReadable(serverDir: File): Boolean {
        return try {
            val file = File(serverDir, META_PATH)
            if (!file.exists()) return false
            JSONObject(file.readText())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun read(serverDir: File): InstalledEngineVersion? {
        val metaFile = File(serverDir, META_PATH)
        if (!metaFile.exists()) return null

        return try {
            val json = JSONObject(metaFile.readText())
            val jarFileName = when {
                json.has("jarFileName") -> json.optString("jarFileName")
                json.has("jarName") -> json.optString("jarName")
                else -> ""
            }
            InstalledEngineVersion(
                engineId = json.optString("engineId", ""),
                versionId = json.getString("versionId"),
                versionName = json.optString("versionName", ""),
                jarFileName = jarFileName,
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
            val tempFile = File(metaDir, "engine-installation.json.tmp")
            
            val json = JSONObject().apply {
                put("engineId", installed.engineId)
                put("versionId", installed.versionId)
                put("versionName", installed.versionName)
                put("jarFileName", installed.jarFileName)
                put("installedAt", installed.installedAt)
            }
            
            tempFile.writeText(json.toString(2))
            if (tempFile.renameTo(metaFile)) {
                true
            } else {
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun matches(
        serverDir: File,
        selectedVersion: EngineVersion
    ): Boolean {
        val metadata = read(serverDir) ?: return false
        
        if (metadata.engineId != selectedVersion.engineId) return false
        if (metadata.versionId != selectedVersion.id) return false
        if (metadata.jarFileName != selectedVersion.jarFileName) return false
        
        val jarFile = File(serverDir, selectedVersion.jarFileName)
        if (!jarFile.exists()) return false
        
        return validateJar(jarFile)
    }

    fun validateJar(jarFile: File): Boolean {
        if (!jarFile.exists()) return false
        if (jarFile.length() < 1_000_000) return false // 1MB
        
        return try {
            JarFile(jarFile).use { jar ->
                // ZIP/JAR magic bytes are valid if we opened it.
                
                // Manifest exists and Main-Class is present.
                val manifest = jar.manifest
                val mainClass = manifest?.mainAttributes?.getValue(java.util.jar.Attributes.Name.MAIN_CLASS)
                if (mainClass.isNullOrBlank()) return false

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
