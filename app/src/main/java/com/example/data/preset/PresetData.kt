package com.example.data.preset

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TrackAudioColor
import com.example.ui.theme.TrackEffectColor
import com.example.ui.theme.TrackStickerColor
import com.example.ui.theme.TrackSubtitleColor
import com.example.ui.theme.TrackVideoColor

data class PresetFilter(
    val id: String,
    val name: String,
    val category: String,
    val colorOverlay: Color,
    val contrast: Float,
    val saturation: Float,
    val brightness: Float
)

data class PresetEffect(
    val id: String,
    val name: String,
    val category: String,
    val iconName: String,
    val description: String
)

data class PresetTransition(
    val id: String,
    val name: String,
    val iconName: String
)

data class PresetAudio(
    val id: String,
    val name: String,
    val artist: String,
    val durationMs: Long,
    val genre: String,
    val rhythmBpm: Int
)

data class PresetMediaClip(
    val id: String,
    val title: String,
    val durationMs: Long,
    val category: String,
    val primaryColorHex: String,
    val subtitleText: String,
    val tags: List<String>
)

object PresetData {

    val filters = listOf(
        PresetFilter("none", "原图", "基础", Color.Transparent, 1.0f, 1.0f, 1.0f),
        PresetFilter("film_warm", "港风胶片", "胶片", Color(0x22FFA726), 1.15f, 1.2f, 1.05f),
        PresetFilter("cyberpunk", "赛博朋克", "复古", Color(0x3300E5FF), 1.3f, 1.4f, 0.95f),
        PresetFilter("vivid_sunset", "落日暖阳", "风景", Color(0x22FF5722), 1.1f, 1.3f, 1.1f),
        PresetFilter("vintage_bw", "经典黑白", "黑白", Color(0x55888888), 1.4f, 0.0f, 1.0f),
        PresetFilter("cool_breeze", "清透海盐", "日系", Color(0x2200BCD4), 1.05f, 1.1f, 1.15f),
        PresetFilter("cinematic_green", "电影青绿", "电影", Color(0x22009688), 1.25f, 1.15f, 0.9f),
        PresetFilter("neon_night", "霓虹夜景", "夜景", Color(0x33AB47BC), 1.35f, 1.5f, 0.95f)
    )

    val effects = listOf(
        PresetEffect("glitch_shake", "故障抖动", "画面特效", "Flash", "科技感RGB通道错位抖动"),
        PresetEffect("vhs_tape", "复古录像带", "画面特效", "Videocam", "带磁带噪点与红绿重影效果"),
        PresetEffect("light_leak", "浪漫光晕", "光影", "WbSunny", "边缘温暖镜头漫反射边缘光"),
        PresetEffect("neon_glow", "霓虹边框", "氛围", "AutoAwesome", "智能轮廓高亮流动荧光线条"),
        PresetEffect("star_dust", "星芒粒子", "氛围", "Grain", "梦幻金色漫天飘落星光"),
        PresetEffect("zoom_bounce", "镜头冲击", "动效", "ZoomIn", "节奏强劲的跟拍镜头快速冲弹")
    )

    val transitions = listOf(
        PresetTransition("fade", "叠化溶解", "Fade"),
        PresetTransition("zoom_in", "运镜推近", "ZoomIn"),
        PresetTransition("glitch_cut", "故障转场", "Electric"),
        PresetTransition("wipe_right", "右向擦除", "ArrowForward"),
        PresetTransition("spin_blur", "旋转模糊", "Autorenew")
    )

    val presetAudios = listOf(
        PresetAudio("bgm_cyber", "Cybernetic Pulse (赛博律动)", "Studio AI Beats", 15000L, "电子", 128),
        PresetAudio("bgm_chill", "Summer Chill Vibe (夏日清凉)", "Chillout Wave", 20000L, "轻音乐", 95),
        PresetAudio("bgm_epic", "Epic Cinematic Trailer (史诗预告片)", "Orchestra Modern", 18000L, "史诗", 140),
        PresetAudio("bgm_vlog", "Daily Vlog Acoustic (日常木吉他)", "Sunny Afternoon", 12000L, "Vlog", 110)
    )

    val stockClips = listOf(
        PresetMediaClip(
            id = "clip_cyber_city",
            title = "赛博都市夜景",
            durationMs = 6000L,
            category = "城市",
            primaryColorHex = "#00E5FF",
            subtitleText = "欢迎来到未来数字都市",
            tags = listOf("夜景", "霓虹", "科技")
        ),
        PresetMediaClip(
            id = "clip_beach_sunset",
            title = "海滩日落余晖",
            durationMs = 5000L,
            category = "自然",
            primaryColorHex = "#FF7043",
            subtitleText = "浪花拍打沙滩，感受落日余晖",
            tags = listOf("海滩", "日落", "唯美")
        ),
        PresetMediaClip(
            id = "clip_coffee_vlog",
            title = "精选手冲咖啡",
            durationMs = 4000L,
            category = "生活",
            primaryColorHex = "#8D6E63",
            subtitleText = "清晨的一杯手冲咖啡，开启美好一天",
            tags = listOf("咖啡", "美食", "Vlog")
        ),
        PresetMediaClip(
            id = "clip_neon_dance",
            title = "街舞狂欢快剪",
            durationMs = 5000L,
            category = "潮流",
            primaryColorHex = "#E91E63",
            subtitleText = "全场跟随节奏一起嗨翻",
            tags = listOf("舞蹈", "卡点", "潮流")
        )
    )

    val aiSubtitleTemplates = listOf(
        "自动辨识高能片段，卡点生成精美弹幕字幕",
        "识别口播语音，支持中英双语离线字幕打轴",
        "一键提取Vlog独白，智能去除停顿词与无声片段",
        "花字字幕样式一键套用：抖音爆款、电影字幕、小红书贴纸"
    )
}
