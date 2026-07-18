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
                val id = obj.getString("id")
                val engineId = obj.getString("engineId")
                val downloadUrl = obj.getString("downloadUrl")
                val jarFileName = obj.getString("jarFileName")
                val compatibilityMode = try { 
                    CompatibilityMode.valueOf(obj.optString("compatibilityMode", "SINGLE_VERSION")) 
                } catch(e: Exception) { 
                    CompatibilityMode.SINGLE_VERSION 
                }
                
                val recommendedBedrockVersion = if (obj.isNull("recommendedBedrockVersion")) null else obj.optString("recommendedBedrockVersion", null as String?)
                val compatibilitySummary = if (obj.isNull("compatibilitySummary")) null else obj.optString("compatibilitySummary", null as String?)

                val supportedArray = obj.optJSONArray("supportedBedrockVersions")
                val supportedList = mutableListOf<String>()
                if (supportedArray != null) {
                    for (j in 0 until supportedArray.length()) {
                        val v = supportedArray.getString(j)
                        if (v.isNotBlank()) supportedList.add(v)
                    }
                }

                // Validation
                if (id.isBlank() || engineId.isBlank() || downloadUrl.isBlank() || jarFileName.isBlank()) {
                    android.util.Log.e("EngineVersionCatalog", "Rejecting malformed entry: $id")
                    continue
                }
                if (list.any { it.id == id }) {
                    android.util.Log.e("EngineVersionCatalog", "Rejecting duplicate ID: $id")
                    continue
                }
                if (recommendedBedrockVersion != null && !supportedList.contains(recommendedBedrockVersion)) {
                    android.util.Log.e("EngineVersionCatalog", "Rejecting entry: recommendedBedrockVersion not in supported list for $id")
                    continue
                }
                if (compatibilityMode == CompatibilityMode.SINGLE_VERSION && supportedList.size > 1) {
                    android.util.Log.e("EngineVersionCatalog", "Rejecting entry: SINGLE_VERSION but multiple supported versions for $id")
                    continue
                }

                list.add(EngineVersion(
                    id = id,
                    engineId = engineId,
                    versionName = obj.getString("versionName"),
                    displayName = obj.getString("displayName"),
                    channel = ReleaseChannel.valueOf(obj.getString("channel")),
                    downloadUrl = downloadUrl,
                    jarFileName = jarFileName,
                    requiredJavaVersion = obj.getInt("requiredJavaVersion"),
                    compatibilityLabel = obj.getString("compatibilityLabel"),
                    recommended = obj.getBoolean("recommended"),
                    supportedBedrockVersions = supportedList,
                    recommendedBedrockVersion = recommendedBedrockVersion,
                    compatibilityMode = compatibilityMode,
                    compatibilitySummary = compatibilitySummary
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
