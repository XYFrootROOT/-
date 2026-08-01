package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TrackType {
    VIDEO,
    AUDIO,
    SUBTITLE,
    EFFECT,
    STICKER
}

enum class AspectRatio(
    val label: String,
    val widthRatio: Float,
    val heightRatio: Float,
    val platformTag: String
) {
    RATIO_9_16("9:16", 9f, 16f, "抖音 / TikTok / Reels"),
    RATIO_16_9("16:9", 16f, 9f, "Bilibili / YouTube"),
    RATIO_1_1("1:1", 1f, 1f, "Instagram / 朋友圈"),
    RATIO_3_4("3:4", 3f, 4f, "小红书 / Red"),
    RATIO_21_9("21:9", 21f, 9f, "电影院宽银幕")
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val coverUri: String = "",
    val durationMs: Long = 15000L,
    val aspectRatioName: String = AspectRatio.RATIO_9_16.name,
    val lastModified: Long = System.currentTimeMillis(),
    val resolution: String = "1080P",
    val fps: Int = 30
)

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val trackType: String = TrackType.VIDEO.name,
    val index: Int = 0,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false
)

@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: Long,
    val title: String,
    val colorHex: String = "#1E88E5",
    val startOffsetMs: Long = 0L,
    val durationMs: Long = 5000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val filterId: String = "none",
    val effectId: String = "none",
    val transitionType: String = "none",
    val subtitleText: String = "",
    val strokeColorHex: String = "#000000",
    val rotation: Float = 0f,
    val scale: Float = 1.0f
)

@Entity(tableName = "exported_videos")
data class ExportedVideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val fileUri: String,
    val durationMs: Long,
    val resolution: String,
    val fps: Int,
    val bitrateKbps: Int,
    val fileSizeMb: Float,
    val exportedAt: Long = System.currentTimeMillis()
)
