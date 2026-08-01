package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AspectRatio
import com.example.data.model.ClipEntity
import com.example.data.model.ExportedVideoEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TrackEntity
import com.example.data.model.TrackType
import com.example.data.repository.ProjectRepository
import com.example.engine.AiSubtitleConfig
import com.example.engine.AiSubtitleEngine
import com.example.engine.VideoEngine
import com.example.engine.VideoPreviewFrame
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExportConfig(
    val resolution: String = "1080P",
    val fps: Int = 60,
    val bitrateKbps: Int = 12000,
    val format: String = "MP4",
    val platformPresetName: String = "抖音/TikTok (9:16)"
)

data class AiSubtitleUiState(
    val isRecognizing: Boolean = false,
    val progress: Float = 0f,
    val statusText: String = "",
    val language: String = "zh-CN (中文)",
    val isOfflineMode: Boolean = true
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    private val videoEngine = VideoEngine()

    val allProjects: StateFlow<List<ProjectEntity>>
    val allExportedVideos: StateFlow<List<ExportedVideoEntity>>

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)
    val currentProject: StateFlow<ProjectEntity?> = _currentProject.asStateFlow()

    private val _tracks = MutableStateFlow<List<TrackEntity>>(emptyList())
    val tracks: StateFlow<List<TrackEntity>> = _tracks.asStateFlow()

    private val _clips = MutableStateFlow<List<ClipEntity>>(emptyList())
    val clips: StateFlow<List<ClipEntity>> = _clips.asStateFlow()

    private val _selectedClip = MutableStateFlow<ClipEntity?>(null)
    val selectedClip: StateFlow<ClipEntity?> = _selectedClip.asStateFlow()

    val previewState: StateFlow<VideoPreviewFrame> = videoEngine.previewState

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayheadMs = MutableStateFlow(0L)
    val currentPlayheadMs: StateFlow<Long> = _currentPlayheadMs.asStateFlow()

    // Export state
    private val _exportConfig = MutableStateFlow(ExportConfig())
    val exportConfig: StateFlow<ExportConfig> = _exportConfig.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportedResult = MutableStateFlow<ExportedVideoEntity?>(null)
    val exportedResult: StateFlow<ExportedVideoEntity?> = _exportedResult.asStateFlow()

    // AI Subtitle state
    private val _aiSubtitleState = MutableStateFlow(AiSubtitleUiState())
    val aiSubtitleState: StateFlow<AiSubtitleUiState> = _aiSubtitleState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        val database = AppDatabase.getInstance(application)
        repository = ProjectRepository(database.projectDao())

        allProjects = repository.allProjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allExportedVideos = repository.allExportedVideos.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.createSampleDataIfEmpty()
        }
    }

    fun loadProject(projectId: Long) {
        _currentProjectId.value = projectId
        viewModelScope.launch {
            val proj = repository.getProjectById(projectId)
            _currentProject.value = proj

            // Observe tracks
            launch {
                repository.getTracksForProject(projectId).collectLatest { trackList ->
                    _tracks.value = trackList
                    syncPreviewFrame()
                }
            }

            // Observe clips
            launch {
                repository.getClipsForProject(projectId).collectLatest { clipList ->
                    _clips.value = clipList
                    syncPreviewFrame()
                }
            }
        }
    }

    fun createNewProject(title: String = "新视频作品", aspectRatio: AspectRatio = AspectRatio.RATIO_9_16) {
        viewModelScope.launch {
            val newId = repository.createNewProject(title, aspectRatio)
            loadProject(newId)
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_currentProjectId.value == projectId) {
                _currentProjectId.value = null
                _currentProject.value = null
            }
        }
    }

    fun selectClip(clip: ClipEntity?) {
        _selectedClip.value = clip
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val frameIntervalMs = 50L // 20 fps preview refresh
            while (_isPlaying.value) {
                val current = _currentPlayheadMs.value
                val maxMs = previewState.value.totalDurationMs
                if (current >= maxMs) {
                    _currentPlayheadMs.value = 0L
                } else {
                    _currentPlayheadMs.value = (current + frameIntervalMs).coerceAtMost(maxMs)
                }
                syncPreviewFrame()
                delay(frameIntervalMs)
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        syncPreviewFrame()
    }

    fun seekTo(timeMs: Long) {
        _currentPlayheadMs.value = timeMs
        syncPreviewFrame()
    }

    private fun syncPreviewFrame() {
        val proj = _currentProject.value
        val ratio = AspectRatio.entries.find { it.name == proj?.aspectRatioName } ?: AspectRatio.RATIO_9_16
        videoEngine.updatePlayhead(
            timeMs = _currentPlayheadMs.value,
            tracks = _tracks.value,
            clips = _clips.value,
            aspectRatio = ratio,
            isPlaying = _isPlaying.value
        )
    }

    fun changeAspectRatio(aspectRatio: AspectRatio) {
        val proj = _currentProject.value ?: return
        val updated = proj.copy(aspectRatioName = aspectRatio.name, lastModified = System.currentTimeMillis())
        _currentProject.value = updated
        viewModelScope.launch {
            repository.updateProject(updated)
            syncPreviewFrame()
        }
    }

    fun splitClipAtPlayhead() {
        val playhead = _currentPlayheadMs.value
        val selClip = _selectedClip.value ?: _clips.value.find { clip ->
            val track = _tracks.value.find { it.id == clip.trackId }
            track?.trackType == TrackType.VIDEO.name &&
                    playhead > clip.startOffsetMs &&
                    playhead < (clip.startOffsetMs + clip.durationMs)
        } ?: return

        val offsetInClip = playhead - selClip.startOffsetMs
        if (offsetInClip <= 200L || offsetInClip >= (selClip.durationMs - 200L)) return

        val firstPartDuration = offsetInClip
        val secondPartDuration = selClip.durationMs - offsetInClip

        val updatedFirst = selClip.copy(durationMs = firstPartDuration)
        val secondClip = selClip.copy(
            id = 0,
            title = "${selClip.title} (切片2)",
            startOffsetMs = playhead,
            durationMs = secondPartDuration
        )

        viewModelScope.launch {
            repository.updateClip(updatedFirst)
            val pId = _currentProjectId.value ?: return@launch
            repository.addClipToTrack(
                projectId = pId,
                trackType = TrackType.VIDEO,
                title = secondClip.title,
                durationMs = secondClip.durationMs,
                subtitleText = secondClip.subtitleText,
                colorHex = secondClip.colorHex,
                filterId = secondClip.filterId,
                effectId = secondClip.effectId
            )
        }
    }

    fun deleteSelectedClip() {
        val sel = _selectedClip.value ?: return
        viewModelScope.launch {
            repository.deleteClip(sel.id)
            _selectedClip.value = null
        }
    }

    fun applyFilterToSelected(filterId: String) {
        val sel = _selectedClip.value ?: return
        val updated = sel.copy(filterId = filterId)
        _selectedClip.value = updated
        viewModelScope.launch {
            repository.updateClip(updated)
        }
    }

    fun applyEffectToSelected(effectId: String) {
        val sel = _selectedClip.value ?: return
        val updated = sel.copy(effectId = effectId)
        _selectedClip.value = updated
        viewModelScope.launch {
            repository.updateClip(updated)
        }
    }

    fun updateClipSpeed(speed: Float) {
        val sel = _selectedClip.value ?: return
        val updated = sel.copy(speed = speed)
        _selectedClip.value = updated
        viewModelScope.launch {
            repository.updateClip(updated)
        }
    }

    fun updateClipVolume(volume: Float) {
        val sel = _selectedClip.value ?: return
        val updated = sel.copy(volume = volume)
        _selectedClip.value = updated
        viewModelScope.launch {
            repository.updateClip(updated)
        }
    }

    fun addBgmTrack(title: String, durationMs: Long) {
        val pId = _currentProjectId.value ?: return
        viewModelScope.launch {
            repository.addClipToTrack(
                projectId = pId,
                trackType = TrackType.AUDIO,
                title = title,
                durationMs = durationMs,
                colorHex = "#43A047"
            )
        }
    }

    fun addTextClip(text: String, strokeHex: String = "#000000") {
        val pId = _currentProjectId.value ?: return
        viewModelScope.launch {
            repository.addClipToTrack(
                projectId = pId,
                trackType = TrackType.SUBTITLE,
                title = "文字: $text",
                durationMs = 4000L,
                subtitleText = text,
                colorHex = "#FDD835"
            )
        }
    }

    // AI Subtitle Generation
    fun triggerAiSubtitleRecognition(language: String = "zh-CN (中文)", isOfflineMode: Boolean = true) {
        val pId = _currentProjectId.value ?: return
        val totalMs = previewState.value.totalDurationMs

        viewModelScope.launch {
            _aiSubtitleState.value = AiSubtitleUiState(
                isRecognizing = true,
                progress = 0f,
                statusText = "初始化 AI 离线引擎...",
                language = language,
                isOfflineMode = isOfflineMode
            )

            val config = AiSubtitleConfig(language = language, isOfflineMode = isOfflineMode)
            val recognizedSegments = AiSubtitleEngine.recognizeSubtitlesOffline(
                config = config,
                totalDurationMs = totalMs,
                onProgress = { prog, text ->
                    _aiSubtitleState.value = _aiSubtitleState.value.copy(
                        progress = prog,
                        statusText = text
                    )
                }
            )

            // Convert segments to subtitle track clips
            recognizedSegments.forEach { seg ->
                val duration = seg.endMs - seg.startMs
                repository.addClipToTrack(
                    projectId = pId,
                    trackType = TrackType.SUBTITLE,
                    title = "AI字幕: ${seg.text}",
                    durationMs = duration,
                    subtitleText = seg.text,
                    colorHex = "#FDD835"
                )
            }

            _aiSubtitleState.value = _aiSubtitleState.value.copy(
                isRecognizing = false,
                progress = 1.0f,
                statusText = "生成成功！已插入 ${recognizedSegments.size} 条精准打轴字幕"
            )
        }
    }

    // Export Execution
    fun updateExportConfig(
        resolution: String = _exportConfig.value.resolution,
        fps: Int = _exportConfig.value.fps,
        bitrateKbps: Int = _exportConfig.value.bitrateKbps,
        format: String = _exportConfig.value.format,
        platformPresetName: String = _exportConfig.value.platformPresetName
    ) {
        _exportConfig.value = ExportConfig(
            resolution = resolution,
            fps = fps,
            bitrateKbps = bitrateKbps,
            format = format,
            platformPresetName = platformPresetName
        )
    }

    fun startExportRendering() {
        val proj = _currentProject.value ?: return
        val config = _exportConfig.value

        _isExporting.value = true
        _exportProgress.value = 0f
        _exportedResult.value = null

        viewModelScope.launch {
            val totalSteps = 100
            for (step in 1..totalSteps) {
                delay(30) // simulate render speed
                _exportProgress.value = step / 100f
            }

            val estMb = (proj.durationMs / 1000f) * (config.bitrateKbps / 8000f)
            val exported = ExportedVideoEntity(
                projectId = proj.id,
                title = "${proj.title}_${config.resolution}_${config.fps}FPS.${config.format.lowercase()}",
                fileUri = "exported_media_${System.currentTimeMillis()}",
                durationMs = proj.durationMs,
                resolution = config.resolution,
                fps = config.fps,
                bitrateKbps = config.bitrateKbps,
                fileSizeMb = (estMb * 10).toInt() / 10f,
                exportedAt = System.currentTimeMillis()
            )

            repository.insertExportedVideo(exported)
            _exportedResult.value = exported
            _isExporting.value = false
        }
    }

    fun deleteExportedRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteExportedVideo(id)
        }
    }
}
