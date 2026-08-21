package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.MediaEntity
import com.example.data.repository.MediaRepository
import com.example.player.PlaybackManager
import com.example.ui.MainViewModel
import com.example.ui.components.FullScreenPlayerDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.QuickDownloadBottomSheet
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldCyan
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MediaRepository(database)
        val playbackManager = PlaybackManager()

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel {
                    MainViewModel(repository, playbackManager)
                }
                BDTubeMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BDTubeMainApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val completedDownloads by viewModel.completedDownloads.collectAsStateWithLifecycle()
    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()

    var showFullscreenPlayer by remember { mutableStateOf(false) }

    // Automatic Clipboard Detection on App Resume / Launch
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkClipboardForMediaLink(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(CrimsonRed, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BDTube",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "PRO",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = CrimsonRed,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Text(
                                text = "বিজ্ঞাপনহীন সরাসরি স্ট্রিমার ও ডাউনলোডার",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.checkClipboardForMediaLink(context) },
                        modifier = Modifier.testTag("top_bar_clipboard_check")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Check Clipboard",
                            tint = EmeraldCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column {
                // Mini Player Bar if something is loaded
                if (playbackState.currentMedia != null) {
                    MiniPlayerBar(
                        playbackState = playbackState,
                        onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
                        onNext = { viewModel.playbackManager.playNext() },
                        onExpand = { showFullscreenPlayer = true },
                        onClose = { viewModel.playbackManager.closePlayer() }
                    )
                }

                // Bottom Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("হোম", fontSize = 11.sp, fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setTab(1) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (activeDownloads.isNotEmpty()) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("${activeDownloads.size}", color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (uiState.selectedTab == 1) Icons.Default.Download else Icons.Outlined.Download,
                                    contentDescription = "Downloads"
                                )
                            }
                        },
                        label = { Text("ডাউনলোড", fontSize = 11.sp, fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_downloads")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.setTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == 2) Icons.Default.LibraryMusic else Icons.Outlined.LibraryMusic,
                                contentDescription = "Playlists"
                            )
                        },
                        label = { Text("প্লেলিস্ট", fontSize = 11.sp, fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_playlists")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 3,
                        onClick = { viewModel.setTab(3) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == 3) Icons.Default.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("সেটিংস", fontSize = 11.sp, fontWeight = if (uiState.selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> HomeScreen(
                    uiState = uiState,
                    onUrlChanged = { viewModel.onUrlInputChanged(it) },
                    onPasteFromClipboard = { viewModel.checkClipboardForMediaLink(context) },
                    onExtractClicked = { viewModel.extractAndShowDownloadDialog() },
                    onCategoryChanged = { viewModel.setCategoryFilter(it) },
                    onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                    onTrendingItemClicked = { videoId, _ ->
                        viewModel.extractAndShowDownloadDialog("https://youtu.be/$videoId")
                    },
                    onClearBanner = { viewModel.clearBanner() }
                )

                1 -> DownloadsScreen(
                    activeDownloads = activeDownloads,
                    completedDownloads = completedDownloads,
                    onPlayMedia = { media, list -> viewModel.playOfflineMedia(media, list) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onAddToPlaylist = { viewModel.openAddToPlaylistDialog(it) },
                    onDeleteMedia = { viewModel.deleteMedia(it) },
                    onPauseDownload = { viewModel.pauseDownload(it) },
                    onResumeDownload = { viewModel.resumeDownload(it) },
                    onNavigateToHome = { viewModel.setTab(0) }
                )

                2 -> {
                    val activePlaylist = uiState.activePlaylistDetail
                    val playlistMediaList = if (activePlaylist != null) {
                        viewModel.playbackManager // observe flow dynamically or all media with crossref
                        completedDownloads
                    } else emptyList()

                    PlaylistsScreen(
                        playlists = playlists,
                        activePlaylistDetail = uiState.activePlaylistDetail,
                        playlistItems = playlistMediaList,
                        onOpenCreateDialog = { viewModel.openCreatePlaylistDialog() },
                        onSelectPlaylist = { viewModel.viewPlaylistDetail(it) },
                        onClosePlaylistDetail = { viewModel.closePlaylistDetail() },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onPlayAllInPlaylist = { list ->
                            if (list.isNotEmpty()) viewModel.playOfflineMedia(list[0], list)
                        },
                        onPlayMedia = { media, list -> viewModel.playOfflineMedia(media, list) },
                        onRemoveMediaFromPlaylist = { pId, mId ->
                            viewModel.deletePlaylist(pId)
                        }
                    )
                }

                3 -> SettingsScreen(
                    uiState = uiState,
                    onToggleAutoClipboard = { viewModel.toggleAutoClipboard(it) },
                    onToggleBanglaPriority = { viewModel.togglePrioritizeBanglaAudio(it) }
                )
            }
        }
    }

    // Modal: Quick Download & Stream Dialog (with Bengali Track First Priority)
    if (uiState.showQuickDownloadModal && uiState.extractedVideoDetails != null) {
        QuickDownloadBottomSheet(
            videoDetails = uiState.extractedVideoDetails!!,
            selectedTab = uiState.selectedDownloadTab,
            selectedVideoOption = uiState.selectedVideoOption,
            selectedAudioOption = uiState.selectedAudioOption,
            onTabSelected = { viewModel.setSelectedDownloadTab(it) },
            onVideoOptionSelected = { viewModel.selectVideoOption(it) },
            onAudioOptionSelected = { viewModel.selectAudioOption(it) },
            onStartDownload = { isAudioOnly -> viewModel.startDownloadFromModal(isAudioOnly) },
            onPlayDirectStream = { isAudioOnly -> viewModel.playStreamDirectly(isAudioOnly) },
            onDismiss = { viewModel.dismissDownloadModal() }
        )
    }

    // Full Screen Player Dialog
    if (showFullscreenPlayer && playbackState.currentMedia != null) {
        FullScreenPlayerDialog(
            playbackState = playbackState,
            onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
            onSeekTo = { viewModel.playbackManager.seekTo(it) },
            onSkipForward = { viewModel.playbackManager.skipForward10s() },
            onSkipBackward = { viewModel.playbackManager.skipBackward10s() },
            onNext = { viewModel.playbackManager.playNext() },
            onPrevious = { viewModel.playbackManager.playPrevious() },
            onSpeedChange = { viewModel.playbackManager.setPlaybackSpeed(it) },
            onAudioTrackChange = { viewModel.playbackManager.switchAudioTrack(it) },
            onToggleLoop = { viewModel.playbackManager.toggleLoop() },
            onToggleShuffle = { viewModel.playbackManager.toggleShuffle() },
            onDismiss = { showFullscreenPlayer = false }
        )
    }

    // Dialog: Create New Playlist
    if (uiState.showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.dismissCreatePlaylistDialog() },
            title = { Text("নতুন প্লেলিস্ট তৈরি করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("প্লেলিস্টের নাম") },
                        placeholder = { Text("যেমন: বাংলা লোকগীতি") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_playlist_name")
                    )
                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("বিবরণ (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.createPlaylist(playlistName, playlistDesc) },
                    enabled = playlistName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    modifier = Modifier.testTag("submit_create_playlist")
                ) {
                    Text("তৈরি করুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreatePlaylistDialog() }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Add to Playlist Selector
    if (uiState.showAddToPlaylistDialog && uiState.mediaToAddToPlaylist != null) {
        val media = uiState.mediaToAddToPlaylist!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissAddToPlaylistDialog() },
            title = { Text("প্লেলিস্ট নির্বাচন করুন") },
            text = {
                Column {
                    if (playlists.isEmpty()) {
                        Text("কোনো প্লেলিস্ট তৈরি করা নেই। আগে প্লেলিস্ট তৈরি করুন।")
                    } else {
                        playlists.forEach { pl ->
                            Surface(
                                onClick = { viewModel.addMediaToPlaylist(pl.id, media.id) },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = EmeraldCyan)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(pl.name, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAddToPlaylistDialog() }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
