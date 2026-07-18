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
        engineVersionId = versionCatalog.getDefaultVersion(TemplateRegistry.BEDROCK_CLOUDBURST_NUKKIT.id)?.id
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
        val defaultVersion = versionCatalog.getDefaultVersion(template.id)
        updateDraft {
            it.copy(
                engine = template,
                engineVersionId = defaultVersion?.id
            )
        }
    }

    fun selectVersion(version: EngineVersion) {
        val selectedEngineId = _draft.value.engine?.id ?: return
        if (version.engineId != selectedEngineId) return

        updateDraft {
            it.copy(engineVersionId = version.id)
        }
    }

    fun getVersionsForCurrentEngine(): List<EngineVersion> {
        val engineId = _draft.value.engine?.id ?: return emptyList()
        return versionCatalog.getVersionsForEngine(engineId)
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
            WizardStep.VERSION -> draft.engineVersionId != null && 
                                versionCatalog.findVersion(draft.engineVersionId)?.engineId == draft.engine?.id
            else -> true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun createServer(mainViewModel: MainViewModel, onDone: () -> Unit) {
        val currentDraft = _draft.value
        val engineId = currentDraft.engine?.id ?: TemplateRegistry.BEDROCK_CLOUDBURST_NUKKIT.id
        val versionId = currentDraft.engineVersionId ?: versionCatalog.getDefaultVersion(engineId)?.id ?: return

        val creationDraft = ServerCreationDraft(
            name = currentDraft.serverName,
            engineId = engineId,
            engineVersionId = versionId,
            memoryMb = currentDraft.memoryMb,
            maxPlayers = currentDraft.maxPlayers,
            port = currentDraft.port,
            levelName = if (currentDraft.worldName.isNotBlank()) currentDraft.worldName else "world"
        )
        mainViewModel.createServer(creationDraft, currentDraft.artworkUri)
        onDone()
    }
}
