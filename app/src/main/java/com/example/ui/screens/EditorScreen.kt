package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatio
import com.example.ui.components.AiSubtitleSheet
import com.example.ui.components.CanvasPlayerView
import com.example.ui.components.ExportConfigSheet
import com.example.ui.components.FilterEffectSheet
import com.example.ui.components.MultiTrackTimelineView
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MagentaAccent
import com.example.ui.theme.StudioDarkCanvas
import com.example.ui.theme.StudioDarkSurface
import com.example.ui.theme.StudioDarkSurfaceHeader
import com.example.ui.theme.StudioDarkSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.EditorViewModel

data class ToolbarAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val badgeColor: Color = CyanAccent
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBackToHome: () -> Unit,
    onStartExportRender: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProject = viewModel.currentProject
    val tracks = viewModel.tracks
    val clips = viewModel.clips
    val selectedClip = viewModel.selectedClip
    val previewFrame = viewModel.previewState
    val aiSubtitleState = viewModel.aiSubtitleState
    val exportConfig = viewModel.exportConfig

    var showAiSubtitleSheet by remember { mutableStateOf(false) }
    var showFilterEffectSheet by remember { mutableStateOf(false) }
    var showExportConfigSheet by remember { mutableStateOf(false) }
    var showAspectRatioDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val toolbarActions = listOf(
        ToolbarAction("split", "分割", Icons.Default.ContentCut, CyanAccent),
        ToolbarAction("ai_subtitle", "AI字幕", Icons.Default.AutoAwesome, GoldAccent),
        ToolbarAction("filter_fx", "滤镜/特效", Icons.Default.Filter, MagentaAccent),
        ToolbarAction("speed", "变速", Icons.Default.Speed, CyanAccent),
        ToolbarAction("bgm", "配乐", Icons.Default.MusicNote, Color(0xFF43A047)),
        ToolbarAction("text", "添加文字", Icons.Default.TextFields, GoldAccent),
        ToolbarAction("aspect", "画布比例", Icons.Default.CropFree, CyanAccent),
        ToolbarAction("delete", "删除", Icons.Default.Delete, Color.Red)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDarkCanvas,
        topBar = {
            // Editor Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioDarkSurfaceHeader)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackToHome, modifier = Modifier.testTag("editor_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimaryDark
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentProject.value?.title ?: "剪辑工作台",
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${previewFrame.value.aspectRatio.label} · ${exportConfig.value.resolution} ${exportConfig.value.fps}帧",
                        color = TextSecondaryDark,
                        fontSize = 10.sp
                    )
                }

                // Primary One-Click Export Button
                Button(
                    onClick = { showExportConfigSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("top_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "导出",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Preview Canvas Area
            CanvasPlayerView(
                frame = previewFrame.value,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onStepPrevFrame = { viewModel.seekTo(previewFrame.value.currentPlayheadMs - 100L) },
                onStepNextFrame = { viewModel.seekTo(previewFrame.value.currentPlayheadMs + 100L) },
                onAspectRatioClick = { showAspectRatioDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.42f)
            )

            // Multi-Track Timeline Container Area
            MultiTrackTimelineView(
                tracks = tracks.value,
                clips = clips.value,
                currentPlayheadMs = previewFrame.value.currentPlayheadMs,
                totalDurationMs = previewFrame.value.totalDurationMs,
                selectedClip = selectedClip.value,
                onSeekTo = { viewModel.seekTo(it) },
                onSelectClip = { viewModel.selectClip(it) },
                onAddTrackClip = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.46f)
            )

            // Bottom Quick Action Toolbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(StudioDarkSurface)
                    .padding(vertical = 4.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                ) {
                    items(toolbarActions.size) { index ->
                        val action = toolbarActions[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    when (action.id) {
                                        "split" -> viewModel.splitClipAtPlayhead()
                                        "ai_subtitle" -> showAiSubtitleSheet = true
                                        "filter_fx" -> showFilterEffectSheet = true
                                        "speed" -> showSpeedDialog = true
                                        "bgm" -> viewModel.addBgmTrack("Cyber Pulse.mp3", 15000L)
                                        "text" -> viewModel.addTextClip("双击编辑字幕")
                                        "aspect" -> showAspectRatioDialog = true
                                        "delete" -> viewModel.deleteSelectedClip()
                                    }
                                }
                                .testTag("toolbar_${action.id}")
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.title,
                                tint = action.badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = action.title,
                                color = TextPrimaryDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheets
    if (showAiSubtitleSheet) {
        AiSubtitleSheet(
            isRecognizing = aiSubtitleState.value.isRecognizing,
            progress = aiSubtitleState.value.progress,
            statusText = aiSubtitleState.value.statusText,
            onTriggerRecognition = { lang, offline ->
                viewModel.triggerAiSubtitleRecognition(lang, offline)
            },
            onDismiss = { showAiSubtitleSheet = false }
        )
    }

    if (showFilterEffectSheet) {
        FilterEffectSheet(
            activeFilterId = previewFrame.value.activeFilterId,
            activeEffectId = previewFrame.value.activeEffectId,
            onSelectFilter = { viewModel.applyFilterToSelected(it) },
            onSelectEffect = { viewModel.applyEffectToSelected(it) },
            onDismiss = { showFilterEffectSheet = false }
        )
    }

    if (showExportConfigSheet) {
        ExportConfigSheet(
            config = exportConfig.value,
            onUpdateConfig = { res, fps, bitrate, fmt, platform ->
                viewModel.updateExportConfig(res, fps, bitrate, fmt, platform)
            },
            onStartExport = {
                onStartExportRender()
            },
            onDismiss = { showExportConfigSheet = false }
        )
    }

    if (showAspectRatioDialog) {
        ModalBottomSheet(onDismissRequest = { showAspectRatioDialog = false }, containerColor = StudioDarkSurface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("选择画布分辨率比例:", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                AspectRatio.entries.forEach { ratio ->
                    val isSel = previewFrame.value.aspectRatio == ratio
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.changeAspectRatio(ratio)
                                showAspectRatioDialog = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ratio.label,
                            color = if (isSel) CyanAccent else TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(60.dp)
                        )
                        Text(
                            text = ratio.platformTag,
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    if (showSpeedDialog) {
        var currentSpeed by remember { mutableStateOf(selectedClip.value?.speed ?: 1.0f) }
        ModalBottomSheet(onDismissRequest = { showSpeedDialog = false }, containerColor = StudioDarkSurface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("调节片段播放速度: ${currentSpeed}x", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                Slider(
                    value = currentSpeed,
                    onValueChange = {
                        currentSpeed = (it * 10).toInt() / 10f
                        viewModel.updateClipSpeed(currentSpeed)
                    },
                    valueRange = 0.5f..3.0f,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                )
            }
        }
    }
}
