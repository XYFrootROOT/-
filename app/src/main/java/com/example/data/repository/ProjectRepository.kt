package com.example.data.repository

import com.example.data.dao.ProjectDao
import com.example.data.model.AspectRatio
import com.example.data.model.ClipEntity
import com.example.data.model.ExportedVideoEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TrackEntity
import com.example.data.model.TrackType
import com.example.data.preset.PresetData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val allExportedVideos: Flow<List<ExportedVideoEntity>> = dao.getAllExportedVideos()

    suspend fun getProjectById(id: Long): ProjectEntity? = dao.getProjectById(id)

    fun getTracksForProject(projectId: Long): Flow<List<TrackEntity>> =
        dao.getTracksForProject(projectId)

    fun getClipsForProject(projectId: Long): Flow<List<ClipEntity>> =
        dao.getClipsForProject(projectId)

    suspend fun updateProject(project: ProjectEntity) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(projectId: Long) {
        dao.deleteClipsForProject(projectId)
        dao.deleteTracksForProject(projectId)
        dao.deleteProjectById(projectId)
    }

    suspend fun insertExportedVideo(video: ExportedVideoEntity): Long {
        return dao.insertExportedVideo(video)
    }

    suspend fun deleteExportedVideo(id: Long) {
        dao.deleteExportedVideo(id)
    }

    suspend fun addClipToTrack(
        projectId: Long,
        trackType: TrackType,
        title: String,
        durationMs: Long,
        subtitleText: String = "",
        colorHex: String = "#1E88E5",
        filterId: String = "none",
        effectId: String = "none"
    ) {
        val tracks = dao.getTracksForProject(projectId).firstOrNull() ?: emptyList()
        var targetTrack = tracks.find { it.trackType == trackType.name }
        
        if (targetTrack == null) {
            val newTrackId = dao.insertTrack(
                TrackEntity(
                    projectId = projectId,
                    trackType = trackType.name,
                    index = tracks.size
                )
            )
            targetTrack = TrackEntity(id = newTrackId, projectId = projectId, trackType = trackType.name)
        }

        val existingClips = dao.getClipsForProject(projectId).firstOrNull() ?: emptyList()
        val trackClips = existingClips.filter { it.trackId == targetTrack.id }
        val startOffset = if (trackClips.isEmpty()) 0L else trackClips.maxOf { it.startOffsetMs + it.durationMs }

        val newClip = ClipEntity(
            trackId = targetTrack.id,
            title = title,
            colorHex = colorHex,
            startOffsetMs = startOffset,
            durationMs = durationMs,
            filterId = filterId,
            effectId = effectId,
            subtitleText = subtitleText
        )
        dao.insertClip(newClip)

        // Update project duration if needed
        val project = dao.getProjectById(projectId)
        if (project != null) {
            val totalMaxMs = maxOf(project.durationMs, startOffset + durationMs)
            dao.updateProject(project.copy(durationMs = totalMaxMs, lastModified = System.currentTimeMillis()))
        }
    }

    suspend fun updateClip(clip: ClipEntity) {
        dao.updateClip(clip)
    }

    suspend fun deleteClip(clipId: Long) {
        dao.deleteClip(clipId)
    }

    suspend fun createNewProject(
        title: String = "未命名草稿",
        aspectRatio: AspectRatio = AspectRatio.RATIO_9_16
    ): Long {
        val projectId = dao.insertProject(
            ProjectEntity(
                title = title,
                aspectRatioName = aspectRatio.name,
                durationMs = 15000L,
                coverUri = "preset_cover_1"
            )
        )

        // Create 4 standard tracks
        val videoTrackId = dao.insertTrack(TrackEntity(projectId = projectId, trackType = TrackType.VIDEO.name, index = 0))
        val audioTrackId = dao.insertTrack(TrackEntity(projectId = projectId, trackType = TrackType.AUDIO.name, index = 1))
        val textTrackId = dao.insertTrack(TrackEntity(projectId = projectId, trackType = TrackType.SUBTITLE.name, index = 2))
        val effectTrackId = dao.insertTrack(TrackEntity(projectId = projectId, trackType = TrackType.EFFECT.name, index = 3))

        // Populating sample initial clips
        val clips = listOf(
            ClipEntity(
                trackId = videoTrackId,
                title = "赛博都市夜景",
                colorHex = "#1E88E5",
                startOffsetMs = 0L,
                durationMs = 6000L,
                filterId = "cyberpunk"
            ),
            ClipEntity(
                trackId = videoTrackId,
                title = "海滩日落余晖",
                colorHex = "#26A69A",
                startOffsetMs = 6000L,
                durationMs = 5000L,
                filterId = "vivid_sunset"
            ),
            ClipEntity(
                trackId = videoTrackId,
                title = "街舞狂欢快剪",
                colorHex = "#8E24AA",
                startOffsetMs = 11000L,
                durationMs = 4000L,
                filterId = "film_warm"
            ),
            ClipEntity(
                trackId = audioTrackId,
                title = "Cybernetic Pulse.mp3",
                colorHex = "#43A047",
                startOffsetMs = 0L,
                durationMs = 15000L,
                volume = 0.8f
            ),
            ClipEntity(
                trackId = textTrackId,
                title = "AI字幕：欢迎来到剪映Studio",
                colorHex = "#FDD835",
                startOffsetMs = 500L,
                durationMs = 3500L,
                subtitleText = "欢迎来到剪映Studio全能视频剪辑"
            ),
            ClipEntity(
                trackId = textTrackId,
                title = "AI字幕：支持多轨道与AI智能字幕",
                colorHex = "#FDD835",
                startOffsetMs = 4200L,
                durationMs = 4000L,
                subtitleText = "支持多轨道、AI字幕与离线实时导出"
            ),
            ClipEntity(
                trackId = textTrackId,
                title = "AI字幕：一键导出主流社交媒体格式",
                colorHex = "#FDD835",
                startOffsetMs = 8500L,
                durationMs = 4500L,
                subtitleText = "一键导出抖音、小红书与B站多种比例"
            ),
            ClipEntity(
                trackId = effectTrackId,
                title = "特效：故障抖动",
                colorHex = "#D81B60",
                startOffsetMs = 0L,
                durationMs = 3000L,
                effectId = "glitch_shake"
            ),
            ClipEntity(
                trackId = effectTrackId,
                title = "特效：霓虹边框",
                colorHex = "#AB47BC",
                startOffsetMs = 11000L,
                durationMs = 4000L,
                effectId = "neon_glow"
            )
        )

        dao.insertClips(clips)
        return projectId
    }

    suspend fun createSampleDataIfEmpty() {
        val existing = dao.getAllProjects().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val proj1 = createNewProject("抖音Vlog爆款短视频", AspectRatio.RATIO_9_16)
            val proj2 = createNewProject("B站4K横屏极客评测", AspectRatio.RATIO_16_9)
            
            // Add a sample exported video item
            dao.insertExportedVideo(
                ExportedVideoEntity(
                    projectId = proj1,
                    title = "抖音Vlog爆款短视频_1080P_60FPS.mp4",
                    fileUri = "sample_exported_1",
                    durationMs = 15000L,
                    resolution = "1080P",
                    fps = 60,
                    bitrateKbps = 12000,
                    fileSizeMb = 21.4f
                )
            )
        }
    }
}
