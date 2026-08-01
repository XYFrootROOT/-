package com.example.engine

import com.example.data.model.AspectRatio
import com.example.data.model.ClipEntity
import com.example.data.model.TrackEntity
import com.example.data.model.TrackType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VideoPreviewFrame(
    val currentPlayheadMs: Long = 0L,
    val totalDurationMs: Long = 15000L,
    val isPlaying: Boolean = false,
    val activeVideoClip: ClipEntity? = null,
    val activeFilterId: String = "none",
    val activeEffectId: String = "none",
    val activeSubtitles: List<ClipEntity> = emptyList(),
    val activeAudioClips: List<ClipEntity> = emptyList(),
    val activeStickers: List<ClipEntity> = emptyList(),
    val activeTransition: String = "none",
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val zoomScale: Float = 1.0f
)

class VideoEngine {

    private val _previewState = MutableStateFlow(VideoPreviewFrame())
    val previewState: StateFlow<VideoPreviewFrame> = _previewState.asStateFlow()

    fun updatePlayhead(
        timeMs: Long,
        tracks: List<TrackEntity>,
        clips: List<ClipEntity>,
        aspectRatio: AspectRatio,
        isPlaying: Boolean = false
    ) {
        val totalMs = clips.maxOfOrNull { it.startOffsetMs + it.durationMs }?.coerceAtLeast(5000L) ?: 15000L
        val clampedTime = timeMs.coerceIn(0L, totalMs)

        // Find active video clip
        val videoTrackIds = tracks.filter { it.trackType == TrackType.VIDEO.name && !it.isMuted }.map { it.id }
        val activeVideo = clips.find { clip ->
            clip.trackId in videoTrackIds &&
                    clampedTime >= clip.startOffsetMs &&
                    clampedTime < (clip.startOffsetMs + clip.durationMs)
        }

        // Active subtitles
        val textTrackIds = tracks.filter { it.trackType == TrackType.SUBTITLE.name && !it.isMuted }.map { it.id }
        val activeTexts = clips.filter { clip ->
            clip.trackId in textTrackIds &&
                    clampedTime >= clip.startOffsetMs &&
                    clampedTime < (clip.startOffsetMs + clip.durationMs)
        }

        // Active audio
        val audioTrackIds = tracks.filter { it.trackType == TrackType.AUDIO.name && !it.isMuted }.map { it.id }
        val activeAudios = clips.filter { clip ->
            clip.trackId in audioTrackIds &&
                    clampedTime >= clip.startOffsetMs &&
                    clampedTime < (clip.startOffsetMs + clip.durationMs)
        }

        // Active effects & filters
        val effectTrackIds = tracks.filter { it.trackType == TrackType.EFFECT.name && !it.isMuted }.map { it.id }
        val activeEffectClip = clips.find { clip ->
            clip.trackId in effectTrackIds &&
                    clampedTime >= clip.startOffsetMs &&
                    clampedTime < (clip.startOffsetMs + clip.durationMs)
        }

        // Active filter from video clip or effect clip
        val activeFilter = activeVideo?.filterId ?: "none"
        val activeEffect = activeEffectClip?.effectId ?: activeVideo?.effectId ?: "none"
        val activeTrans = activeVideo?.transitionType ?: "none"

        _previewState.value = VideoPreviewFrame(
            currentPlayheadMs = clampedTime,
            totalDurationMs = totalMs,
            isPlaying = isPlaying,
            activeVideoClip = activeVideo,
            activeFilterId = activeFilter,
            activeEffectId = activeEffect,
            activeSubtitles = activeTexts,
            activeAudioClips = activeAudios,
            activeTransition = activeTrans,
            aspectRatio = aspectRatio
        )
    }

    companion object {
        fun formatMsToTimecode(timeMs: Long): String {
            val totalSeconds = timeMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val millis = (timeMs % 1000) / 10
            return String.format("%02d:%02d.%02d", minutes, seconds, millis)
        }
    }
}
