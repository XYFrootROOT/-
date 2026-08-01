package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.JianYingTheme
import com.example.ui.viewmodel.EditorViewModel

enum class ScreenState {
    HOME,
    EDITOR,
    EXPORT
}

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JianYingTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.HOME) }

                val projects by viewModel.allProjects.collectAsState()
                val exportedVideos by viewModel.allExportedVideos.collectAsState()

                Crossfade(
                    targetState = currentScreen,
                    modifier = Modifier.fillMaxSize(),
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        ScreenState.HOME -> {
                            HomeScreen(
                                projects = projects,
                                exportedVideos = exportedVideos,
                                onCreateNewProject = {
                                    viewModel.createNewProject("新创作短视频")
                                    currentScreen = ScreenState.EDITOR
                                },
                                onOpenProject = { pId ->
                                    viewModel.loadProject(pId)
                                    currentScreen = ScreenState.EDITOR
                                },
                                onDeleteProject = { pId ->
                                    viewModel.deleteProject(pId)
                                },
                                onDeleteExportedVideo = { eId ->
                                    viewModel.deleteExportedRecord(eId)
                                },
                                onOpenAiSubtitleQuickTool = {
                                    if (projects.isNotEmpty()) {
                                        viewModel.loadProject(projects.first().id)
                                    } else {
                                        viewModel.createNewProject("AI字幕智能样片")
                                    }
                                    currentScreen = ScreenState.EDITOR
                                }
                            )
                        }

                        ScreenState.EDITOR -> {
                            EditorScreen(
                                viewModel = viewModel,
                                onBackToHome = { currentScreen = ScreenState.HOME },
                                onStartExportRender = { currentScreen = ScreenState.EXPORT }
                            )
                        }

                        ScreenState.EXPORT -> {
                            ExportScreen(
                                viewModel = viewModel,
                                onBackToHome = { currentScreen = ScreenState.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}

