package com.example.ui.servercreation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainViewModel
import com.example.data.ServerCreationDraft
import com.example.server.template.ServerTemplate
import com.example.server.template.TemplateRegistry
import com.example.server.version.EngineVersion
import com.example.server.version.EngineVersionCatalog
import kotlinx.coroutines.flow.*

class CreateServerWizardViewModel(application: Application) : AndroidViewModel(application) {
    private val versionCatalog = EngineVersionCatalog(application)

    private val _draft = MutableStateFlow(CreateServerDraft(
        engine = TemplateRegistry.BEDROCK_CLOUDBURST_NUKKIT,
        engineVersionId = versionCatalog.getDefaultVersion(TemplateRegistry.BEDROCK_CLOUDBURST_NUKKIT.id)?.id,
        bedrockVersion = versionCatalog.getDefaultVersion(TemplateRegistry.BEDROCK_CLOUDBURST_NUKKIT.id)?.recommendedBedrockVersion
    ))
    val draft: StateFlow<CreateServerDraft> = _draft.asStateFlow()

    private val _currentStep = MutableStateFlow(WizardStep.BASICS)
    val currentStep: StateFlow<WizardStep> = _currentStep.asStateFlow()

    fun updateDraft(update: (CreateServerDraft) -> CreateServerDraft) {
        _draft.update(update)
    }

    fun setDraft(newDraft: CreateServerDraft) {
        _draft.value = newDraft
    }

    fun selectEngine(template: ServerTemplate) {
        val defaultEngineVersion = versionCatalog.getDefaultVersion(template.id)
        val defaultBedrockVersion = defaultEngineVersion?.recommendedBedrockVersion ?: defaultEngineVersion?.supportedBedrockVersions?.firstOrNull()
        
        updateDraft {
            it.copy(
                engine = template,
                engineVersionId = defaultEngineVersion?.id,
                bedrockVersion = defaultBedrockVersion
            )
        }
    }

    fun selectBedrockVersion(bedrockVersion: String) {
        val engineId = _draft.value.engine?.id ?: return
        val allEngineVersions = versionCatalog.getVersionsForEngine(engineId)
        
        // Find compatible engine build
        val compatibleBuild = allEngineVersions.filter { 
            it.supportedBedrockVersions.contains(bedrockVersion) 
        }.let { compatible ->
            compatible.find { it.recommended } ?: compatible.find { it.channel == com.example.server.version.ReleaseChannel.STABLE } ?: compatible.firstOrNull()
        }

        if (compatibleBuild != null) {
            updateDraft {
                it.copy(
                    bedrockVersion = bedrockVersion,
                    engineVersionId = compatibleBuild.id
                )
            }
        }
    }

    fun getBedrockVersionsForCurrentEngine(): List<Pair<String, EngineVersion>> {
        val engineId = _draft.value.engine?.id ?: return emptyList()
        val engineVersions = versionCatalog.getVersionsForEngine(engineId)
        
        val result = mutableListOf<Pair<String, EngineVersion>>()
        val seenBedrockVersions = mutableSetOf<String>()

        // For each engine version, extract its supported bedrock versions
        // We want to show unique bedrock versions. 
        // If multiple engine versions support the same bedrock version, 
        // we'll associate it with the "best" engine version (recommended/stable).
        
        val allSupportedBedrockVersions = engineVersions.flatMap { it.supportedBedrockVersions }.distinct()
        
        for (bv in allSupportedBedrockVersions) {
            val compatibleBuild = engineVersions.filter { it.supportedBedrockVersions.contains(bv) }
                .let { compatible ->
                    compatible.find { it.recommended } ?: compatible.find { it.channel == com.example.server.version.ReleaseChannel.STABLE } ?: compatible.firstOrNull()
                }
            if (compatibleBuild != null) {
                result.add(bv to compatibleBuild)
            }
        }
        
        return result
    }

    fun getEngineVersion(versionId: String): EngineVersion? {
        return versionCatalog.findVersion(versionId)
    }

    fun nextStep(): Boolean {
        val steps = WizardStep.entries
        val currentIndex = steps.indexOf(_currentStep.value)
        if (currentIndex < steps.size - 1) {
            _currentStep.value = steps[currentIndex + 1]
            return true
        }
        return false
    }

    fun previousStep(): Boolean {
        val steps = WizardStep.entries
        val currentIndex = steps.indexOf(_currentStep.value)
        if (currentIndex > 0) {
            _currentStep.value = steps[currentIndex - 1]
            return true
        }
        return false
    }

    val canContinue = combine(_currentStep, _draft) { step, draft ->
        when (step) {
            WizardStep.BASICS -> draft.serverName.isNotBlank()
            WizardStep.ENGINE -> draft.engine != null
            WizardStep.VERSION -> {
                val bedrockVersion = draft.bedrockVersion
                val engineVersionId = draft.engineVersionId
                if (bedrockVersion.isNullOrBlank() || engineVersionId == null) return@combine false
                
                val version = versionCatalog.findVersion(engineVersionId)
                version != null && version.engineId == draft.engine?.id && version.supportedBedrockVersions.contains(bedrockVersion)
            }
            else -> true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun createServer(mainViewModel: MainViewModel, onDone: () -> Unit) {
        val currentDraft = _draft.value
        val engineId = currentDraft.engine?.id ?: TemplateRegistry.BEDROCK_CLOUDBURST_NUKKIT.id
        val versionId = currentDraft.engineVersionId ?: versionCatalog.getDefaultVersion(engineId)?.id ?: return
        val bedrockVersion = currentDraft.bedrockVersion ?: ""

        val creationDraft = ServerCreationDraft(
            name = currentDraft.serverName,
            engineId = engineId,
            engineVersionId = versionId,
            bedrockVersion = bedrockVersion,
            memoryMb = currentDraft.memoryMb,
            maxPlayers = currentDraft.maxPlayers,
            port = currentDraft.port,
            levelName = if (currentDraft.worldName.isNotBlank()) currentDraft.worldName else "world"
        )
        mainViewModel.createServer(creationDraft, currentDraft.artworkUri)
        onDone()
    }
}
