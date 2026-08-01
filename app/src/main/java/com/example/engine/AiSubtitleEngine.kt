package com.example.engine

import kotlinx.coroutines.delay
import java.util.UUID

data class SubtitleSegment(
    val id: String = UUID.randomUUID().toString(),
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val speaker: String = "主讲",
    val confidence: Float = 0.98f
)

data class AiSubtitleConfig(
    val language: String = "zh-CN (中文)",
    val isOfflineMode: Boolean = true,
    val autoRemovePunctuation: Boolean = true,
    val enableHighPrecision: Boolean = true
)

object AiSubtitleEngine {

    val sampleSpeechTranscripts = listOf(
        listOf(
            SubtitleSegment(startMs = 200L, endMs = 3200L, text = "大家好，欢迎来到本期精彩视频！"),
            SubtitleSegment(startMs = 3500L, endMs = 6800L, text = "今天我们使用剪映Studio展示全能多轨道剪辑。"),
            SubtitleSegment(startMs = 7100L, endMs = 10500L, text = "内置AI智能字幕识别，支持离线秒级打轴。"),
            SubtitleSegment(startMs = 10800L, endMs = 14200L, text = "一键适配抖音、小红书和B站全平台分辨率！")
        ),
        listOf(
            SubtitleSegment(startMs = 300L, endMs = 2800L, text = "Welcome to the future of AI video editing."),
            SubtitleSegment(startMs = 3100L, endMs = 5900L, text = "Multi-track timeline with instant real-time preview."),
            SubtitleSegment(startMs = 6200L, endMs = 9500L, text = "Auto subtitles recognition and custom filter effects."),
            SubtitleSegment(startMs = 9800L, endMs = 13800L, text = "One-click 4K export ready for TikTok & YouTube!")
        )
    )

    /**
     * Fast Offline AI Subtitle Recognition Simulation
     */
    suspend fun recognizeSubtitlesOffline(
        config: AiSubtitleConfig,
        totalDurationMs: Long,
        onProgress: (Float, String) -> Unit
    ): List<SubtitleSegment> {
        onProgress(0.1f, "正在加载离线 AI 语音模型...")
        delay(300)
        onProgress(0.3f, "提取音频波形特征与人声沉寂区间...")
        delay(400)
        onProgress(0.6f, "离线神经网络语音转文字处理中...")
        delay(400)
        onProgress(0.9f, "智能对齐时间轴与去噪优化...")
        delay(200)
        onProgress(1.0f, "识别完成！已自动生成精准对齐字幕")

        val baseList = if (config.language.contains("zh")) {
            sampleSpeechTranscripts[0]
        } else {
            sampleSpeechTranscripts[1]
        }

        // Adjust segment duration if project is longer
        if (totalDurationMs > 15000L) {
            val ratio = totalDurationMs.toFloat() / 15000L
            return baseList.map { seg ->
                seg.copy(
                    startMs = (seg.startMs * ratio).toLong(),
                    endMs = (seg.endMs * ratio).toLong()
                )
            }
        }
        return baseList
    }
}
