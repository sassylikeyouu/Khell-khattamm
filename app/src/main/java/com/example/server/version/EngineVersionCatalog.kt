package com.example.server.version

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader

class EngineVersionCatalog(private val context: Context) {
    private val catalog: List<EngineVersion> by lazy {
        try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier("engine_versions", "raw", context.packageName)
            )
            val jsonString = InputStreamReader(inputStream).use { it.readText() }
            val root = JSONObject(jsonString)
            val versionsArray = root.getJSONArray("versions")
            val list = mutableListOf<EngineVersion>()
            
            for (i in 0 until versionsArray.length()) {
                val obj = versionsArray.getJSONObject(i)
                list.add(EngineVersion(
                    id = obj.getString("id"),
                    engineId = obj.getString("engineId"),
                    versionName = obj.getString("versionName"),
                    displayName = obj.getString("displayName"),
                    channel = ReleaseChannel.valueOf(obj.getString("channel")),
                    downloadUrl = obj.getString("downloadUrl"),
                    jarFileName = obj.getString("jarFileName"),
                    requiredJavaVersion = obj.getInt("requiredJavaVersion"),
                    compatibilityLabel = obj.getString("compatibilityLabel"),
                    recommended = obj.getBoolean("recommended")
                ))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllVersions(): List<EngineVersion> = catalog

    fun getVersionsForEngine(engineId: String): List<EngineVersion> {
        return catalog.filter { it.engineId == engineId }
    }

    fun findVersion(versionId: String): EngineVersion? {
        return catalog.find { it.id == versionId }
    }

    fun getDefaultVersion(engineId: String): EngineVersion? {
        val engineVersions = getVersionsForEngine(engineId)
        return engineVersions.find { it.recommended } ?: engineVersions.firstOrNull()
    }
}
