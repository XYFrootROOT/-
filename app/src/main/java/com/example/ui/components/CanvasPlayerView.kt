package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preset.PresetData
import com.example.engine.VideoEngine
import com.example.engine.VideoPreviewFrame
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MagentaAccent
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioDarkSurfaceHeader
import kotlin.math.sin

@Composable
fun CanvasPlayerView(
    frame: VideoPreviewFrame,
    onTogglePlayPause: () -> Unit,
    onStepPrevFrame: () -> Unit,
    onStepNextFrame: () -> Unit,
    onAspectRatioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aspectRatio = frame.aspectRatio
    val activeClip = frame.activeVideoClip
    val filter = PresetData.filters.find { it.id == frame.activeFilterId }
    val effect = PresetData.effects.find { it.id == frame.activeEffectId }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkSurfaceHeader)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Player Viewport Container with Aspect Ratio constraint
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val ratioVal = aspectRatio.widthRatio / aspectRatio.heightRatio

            Box(
                modifier = Modifier
                    .aspectRatio(ratioVal, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF2E3146), RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                // Background Simulated Blur / Gradient Frame
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    filter?.colorOverlay ?: Color(0xFF1E2640),
                                    Color(0xFF0D0E12)
                                )
                            )
                        )
                )

                // Simulated Render Canvas Content
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val timeMs = frame.currentPlayheadMs
                    val phase = (timeMs / 100f)

                    // Draw animated background pattern based on active clip title
                    val primaryColor = filter?.colorOverlay ?: CyanAccent
                    val secondaryColor = if (effect != null) MagentaAccent else GoldAccent

                    val path = Path()
                    path.moveTo(0f, height * 0.5f)
                    for (x in 0..width.toInt() step 10) {
                        val y = height * 0.5f + sin((x + phase) * 0.02f) * 40f
                        path.lineTo(x.toFloat(), y)
                    }
                    drawPath(
                        path = path,
                        color = primaryColor.copy(alpha = 0.3f),
                        style = Stroke(width = 6.dp.toPx())
                    )

                    // Draw video grid lines
                    for (i in 1..3) {
                        val x = width * (i / 4f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(x, 0f),
                            end = Offset(x, height)
                        )
                        val y = height * (i / 4f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(width, y)
                        )
                    }

                    // Draw Special Effect Overlays
                    if (frame.activeEffectId == "glitch_shake") {
                        val offsetGlitch = (sin(phase) * 15f)
                        drawLine(
                            color = MagentaAccent.copy(alpha = 0.6f),
                            start = Offset(0f, height * 0.3f + offsetGlitch),
                            end = Offset(width, height * 0.3f + offsetGlitch),
                            strokeWidth = 12f
                        )
                        drawLine(
                            color = CyanAccent.copy(alpha = 0.6f),
                            start = Offset(0f, height * 0.7f - offsetGlitch),
                            end = Offset(width, height * 0.7f - offsetGlitch),
                            strokeWidth = 10f
                        )
                    } else if (frame.activeEffectId == "neon_glow") {
                        drawRect(
                            color = CyanAccent.copy(alpha = 0.25f),
                            size = size,
                            style = Stroke(width = 16f)
                        )
                    }
                }

                // Active Clip Title / Media Badge
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video",
                        tint = CyanAccent.copy(alpha = 0.8f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeClip?.title ?: "主视频片段 Preview",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (filter != null && filter.id != "none") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "滤镜: ${filter.name}",
                            color = CyanAccent,
                            fontSize = 12.sp
                        )
                    }
                    if (effect != null && effect.id != "none") {
                        Text(
                            text = "特效: ${effect.name}",
                            color = MagentaAccent,
                            fontSize = 12.sp
                        )
                    }
                }

                // Subtitle Overlay (Bottom of Viewport Canvas)
                if (frame.activeSubtitles.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        frame.activeSubtitles.forEach { sub ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.75f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    GoldAccent.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = sub.subtitleText,
                                    color = GoldAccent,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Top Viewport Information Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { onAspectRatioClick() }
                            .testTag("aspect_ratio_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = "Aspect Ratio",
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = aspectRatio.label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Audio Wave Indicator if playing audio
                    if (frame.activeAudioClips.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Audio Wave",
                                tint = Color(0xFF43A047),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "配乐同步中",
                                color = Color(0xFF43A047),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Center Floating Play/Pause Controls Overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = !frame.isPlaying,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .padding(8.dp)
                        ) {
                            IconButton(
                                onClick = onStepPrevFrame,
                                modifier = Modifier.testTag("step_prev_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Step Prev",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(CyanAccent, CircleShape)
                                    .clickable { onTogglePlayPause() }
                                    .testTag("play_pause_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (frame.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onStepNextFrame,
                                modifier = Modifier.testTag("step_next_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Step Next",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Timecode Overlay at Bottom Right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${VideoEngine.formatMsToTimecode(frame.currentPlayheadMs)} / ${VideoEngine.formatMsToTimecode(frame.totalDurationMs)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
