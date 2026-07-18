package com.example.ui.servercreation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainViewModel
import com.example.data.ServerCreationDraft
import com.example.server.template.ServerTemplate
import com.example.server.template.TemplateRegistry
import com.example.server.version.BedrockVersionOption
import com.example.server.version.EngineVersion
import com.example.server.version.EngineVersionCatalog
import kotlinx.coroutines.flow.*

class CreateServerWizardViewModel(application: Application) : AndroidViewModel(application) {
    private val versionCatalog = EngineVersionCatalog(application)

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage = _operationMessage.asStateFlow()

    private fun showMessage(message: String) {
        _operationMessage.value = message
    }

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

    fun selectBedrockVersion(option: BedrockVersionOption) {
        val selectedEngineId = _draft.value.engine?.id ?: return
        val version = versionCatalog.findVersion(option.engineVersionId) ?: return
        
        if (version.engineId != selectedEngineId) return
        if (!version.supportedBedrockVersions.contains(option.bedrockVersion)) return

        updateDraft {
            it.copy(
                bedrockVersion = option.bedrockVersion,
                engineVersionId = option.engineVersionId
            )
        }
    }

    fun getBedrockVersionsForCurrentEngine(): List<BedrockVersionOption> {
        val engineId = _draft.value.engine?.id ?: return emptyList()
        val engineVersions = versionCatalog.getVersionsForEngine(engineId)
        
        val result = mutableListOf<BedrockVersionOption>()

        for (ev in engineVersions) {
            when (ev.compatibilityMode) {
                com.example.server.version.CompatibilityMode.SINGLE_VERSION -> {
                    // Only use the recommended version if it's Single Version
                    ev.recommendedBedrockVersion?.let { bv ->
                        result.add(BedrockVersionOption(
                            bedrockVersion = bv,
                            engineVersionId = ev.id,
                            engineBuildName = ev.displayName,
                            recommended = ev.recommended, // Only marked recommended if the build itself is recommended
                            compatibilityMode = ev.compatibilityMode,
                            compatibilitySummary = ev.compatibilitySummary
                        ))
                    }
                }
                com.example.server.version.CompatibilityMode.MULTI_VERSION -> {
                    // One option for the recommended version
                    ev.recommendedBedrockVersion?.let { bv ->
                        result.add(BedrockVersionOption(
                            bedrockVersion = bv,
                            engineVersionId = ev.id,
                            engineBuildName = ev.displayName,
                            recommended = ev.recommended,
                            compatibilityMode = ev.compatibilityMode,
                            compatibilitySummary = ev.compatibilitySummary
                        ))
                    }
                }
                com.example.server.version.CompatibilityMode.UNKNOWN -> {
                    // No selectable option
                }
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
        val engineId = currentDraft.engine?.id ?: return
        val versionId = currentDraft.engineVersionId ?: return
        val bedrockVersion = currentDraft.bedrockVersion ?: return

        // Strict validation
        val version = versionCatalog.findVersion(versionId)
        if (version == null || version.engineId != engineId || !version.supportedBedrockVersions.contains(bedrockVersion)) {
            showMessage("Selected Minecraft version is not supported by the selected engine build.")
            return
        }

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
