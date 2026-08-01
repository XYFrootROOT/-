package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClipEntity
import com.example.data.model.TrackEntity
import com.example.data.model.TrackType
import com.example.engine.VideoEngine
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MagentaAccent
import com.example.ui.theme.StudioDarkCanvas
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioDarkSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.TrackAudioColor
import com.example.ui.theme.TrackEffectColor
import com.example.ui.theme.TrackSubtitleColor
import com.example.ui.theme.TrackVideoColor

@Composable
fun MultiTrackTimelineView(
    tracks: List<TrackEntity>,
    clips: List<ClipEntity>,
    currentPlayheadMs: Long,
    totalDurationMs: Long,
    selectedClip: ClipEntity?,
    onSeekTo: (Long) -> Unit,
    onSelectClip: (ClipEntity?) -> Unit,
    onAddTrackClip: (TrackType) -> Unit,
    modifier: Modifier = Modifier
) {
    var timelineScale by remember { mutableFloatStateOf(1.0f) } // scale multiplier
    val pxPerSec = (60 * timelineScale).coerceIn(30f, 150f)
    val timelineWidthDp = maxOf(400.dp, ((totalDurationMs / 1000f) * pxPerSec).dp)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkCanvas)
    ) {
        // Timeline Header Controls (Playhead Scrubber & Zoom Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioDarkSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "时间轴",
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = VideoEngine.formatMsToTimecode(currentPlayheadMs),
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Timeline Scrub Slider
            Slider(
                value = currentPlayheadMs.toFloat(),
                onValueChange = { onSeekTo(it.toLong()) },
                valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1000f),
                colors = SliderDefaults.colors(
                    thumbColor = CyanAccent,
                    activeTrackColor = CyanAccent,
                    inactiveTrackColor = Color(0xFF2E3146)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .testTag("timeline_scrub_slider")
            )

            // Timeline Zoom Scale Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { timelineScale = (timelineScale - 0.2f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { timelineScale = (timelineScale + 0.2f).coerceAtMost(2.5f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Main Stacked Tracks Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Track Headers Sidebar (Left side)
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
                    .background(StudioDarkSurfaceVariant)
                    .padding(top = 24.dp)
            ) {
                TrackType.entries.forEach { tType ->
                    val icon = when (tType) {
                        TrackType.VIDEO -> Icons.Default.Movie
                        TrackType.AUDIO -> Icons.Default.MusicNote
                        TrackType.SUBTITLE -> Icons.Default.Subtitles
                        TrackType.EFFECT -> Icons.Default.AutoAwesome
                        TrackType.STICKER -> Icons.Default.GraphicEq
                    }
                    val label = when (tType) {
                        TrackType.VIDEO -> "视频主轨"
                        TrackType.AUDIO -> "音频配乐"
                        TrackType.SUBTITLE -> "AI 字幕"
                        TrackType.EFFECT -> "特效滤镜"
                        TrackType.STICKER -> "贴纸画中画"
                    }
                    val color = when (tType) {
                        TrackType.VIDEO -> TrackVideoColor
                        TrackType.AUDIO -> TrackAudioColor
                        TrackType.SUBTITLE -> TrackSubtitleColor
                        TrackType.EFFECT -> TrackEffectColor
                        TrackType.STICKER -> GoldAccent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = label,
                            color = TextSecondaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            // Scrollable Timeline Canvas with Playhead Line
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier.width(timelineWidthDp)
                ) {
                    // Time Ruler Bar
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(StudioDarkSurface)
                    ) {
                        val durationSec = totalDurationMs / 1000
                        for (sec in 0..durationSec.toInt()) {
                            val x = (sec * pxPerSec)
                            val isMajor = sec % 2 == 0
                            val tickHeight = if (isMajor) 14f else 8f
                            drawLine(
                                color = if (isMajor) CyanAccent else Color.Gray,
                                start = Offset(x, size.height - tickHeight),
                                end = Offset(x, size.height),
                                strokeWidth = if (isMajor) 2f else 1f
                            )
                        }
                    }

                    // Render Tracks in fixed order
                    TrackType.entries.forEach { tType ->
                        val trackObj = tracks.find { it.trackType == tType.name }
                        val trackClips = if (trackObj != null) {
                            clips.filter { it.trackId == trackObj.id }
                        } else emptyList()

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(0.5.dp, Color(0xFF1E2130))
                                .background(Color(0xFF0F1017))
                        ) {
                            trackClips.forEach { clip ->
                                val clipStartDp = ((clip.startOffsetMs / 1000f) * pxPerSec).dp
                                val clipWidthDp = maxOf(36.dp, ((clip.durationMs / 1000f) * pxPerSec).dp)
                                val isSelected = selectedClip?.id == clip.id

                                val clipBgColor = when (tType) {
                                    TrackType.VIDEO -> TrackVideoColor
                                    TrackType.AUDIO -> TrackAudioColor
                                    TrackType.SUBTITLE -> TrackSubtitleColor
                                    TrackType.EFFECT -> TrackEffectColor
                                    TrackType.STICKER -> GoldAccent
                                }

                                Card(
                                    modifier = Modifier
                                        .offset { IntOffset(clipStartDp.roundToPx(), 0) }
                                        .width(clipWidthDp)
                                        .height(42.dp)
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.5.dp,
                                            color = if (isSelected) CyanAccent else Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { onSelectClip(clip) }
                                        .testTag("clip_item_${clip.id}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = clipBgColor.copy(alpha = 0.85f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (clip.subtitleText.isNotEmpty()) clip.subtitleText else clip.title,
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Vertical Playhead Needle Line (Red / Cyan vertical indicator across all tracks)
                val playheadXDp = ((currentPlayheadMs / 1000f) * pxPerSec).dp
                Box(
                    modifier = Modifier
                        .offset { IntOffset(playheadXDp.roundToPx(), 0) }
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MagentaAccent)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopCenter)
                            .background(MagentaAccent, CircleShape)
                    )
                }
            }
        }
    }
}
