package com.example

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.GoogleSignInDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.QuickDownloadBottomSheet
import com.example.ui.components.YouTubeSearchOverlay
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MediaRepository(database, applicationContext)
        val playbackManager = PlaybackManager()

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel {
                    MainViewModel(repository, playbackManager)
                }
                mainViewModel = viewModel
                BDTubeMainApp(viewModel = viewModel)
            }
        }

        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            mainViewModel?.checkClipboardForMediaLink(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel = null
    }

    private fun handleIncomingIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                mainViewModel?.processSharedLink(sharedText)
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
            intent.dataString?.let { url ->
                mainViewModel?.processSharedLink(url)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BDTubeMainApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val completedDownloads by viewModel.completedDownloads.collectAsStateWithLifecycle()
    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()

    var showFullscreenPlayer by remember { mutableStateOf(false) }

    // Automatic Clipboard Detection on App Resume / Launch / Clip Change
    DisposableEffect(lifecycleOwner, context) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
            viewModel.checkClipboardForMediaLink(context)
        }
        clipboardManager?.addPrimaryClipChangedListener(clipListener)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkClipboardForMediaLink(context)
                scope.launch {
                    delay(300L)
                    viewModel.checkClipboardForMediaLink(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            clipboardManager?.removePrimaryClipChangedListener(clipListener)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.setTab(0) }
                    ) {
                        // Official YouTube Play Button Logo Pill
                        Surface(
                            color = YouTubeRed,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(width = 32.dp, height = 24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "YouTube Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BDTube",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "BD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.Top)
                                .padding(start = 2.dp, top = 2.dp)
                        )
                    }
                },
                actions = {
                    // YouTube Cast Icon
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Cast",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // YouTube Notification Bell
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // YouTube Search Icon (Opens real YouTube Search Overlay)
                    IconButton(
                        onClick = { viewModel.openSearchOverlay() },
                        modifier = Modifier.testTag("top_bar_search_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // YouTube Profile Avatar Circle (Opens Google Account Dialog)
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp, start = 4.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(YouTubeRed, Color(0xFF8A0000))
                                )
                            )
                            .clickable { viewModel.showAuthDialog() }
                            .testTag("top_bar_profile_icon"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.userProfile.displayName.firstOrNull()?.uppercase() ?: "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
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
                // YouTube Mini Player Bar if something is currently active
                if (playbackState.currentMedia != null) {
                    MiniPlayerBar(
                        playbackState = playbackState,
                        onTogglePlayPause = { viewModel.playbackManager.togglePlayPause() },
                        onNext = { viewModel.playbackManager.playNext() },
                        onExpand = { showFullscreenPlayer = true },
                        onClose = { viewModel.playbackManager.closePlayer() }
                    )
                }

                // Authentic YouTube Bottom Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text(
                                text = "হোম",
                                fontSize = 10.sp,
                                fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = Color.Transparent,
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
                                        Badge(containerColor = YouTubeRed) {
                                            Text("${activeDownloads.size}", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (uiState.selectedTab == 1) Icons.Filled.Download else Icons.Outlined.Download,
                                    contentDescription = "Downloads"
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "ডাউনলোড",
                                fontSize = 10.sp,
                                fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = Color.Transparent,
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
                                imageVector = if (uiState.selectedTab == 2) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                contentDescription = "You"
                            )
                        },
                        label = {
                            Text(
                                text = "আপনি",
                                fontSize = 10.sp,
                                fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = Color.Transparent,
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
                    channels = channels,
                    onUrlChanged = { viewModel.onUrlInputChanged(it) },
                    onPasteFromClipboard = { viewModel.checkClipboardForMediaLink(context, force = true) },
                    onExtractClicked = { viewModel.extractAndShowDownloadDialog() },
                    onCategoryChanged = { viewModel.setCategoryFilter(it) },
                    onSelectChannel = { viewModel.setSelectedChannelFilter(it) },
                    onToggleSubscribe = { viewModel.toggleSubscription(it) },
                    onToggleLike = { viewModel.toggleLike(it) },
                    onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                    onTrendingItemClicked = { videoId, _ ->
                        viewModel.extractAndShowDownloadDialog("https://youtu.be/$videoId")
                    },
                    onClearBanner = { viewModel.clearBanner() }
                )

                1 -> DownloadsScreen(
                    activeDownloads = activeDownloads,
                    completedDownloads = completedDownloads,
                    onPlayMedia = { media, list ->
                        viewModel.playOfflineMedia(media, list)
                        showFullscreenPlayer = true
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onAddToPlaylist = { viewModel.openAddToPlaylistDialog(it) },
                    onDeleteMedia = { viewModel.deleteMedia(it) },
                    onPauseDownload = { viewModel.pauseDownload(it) },
                    onResumeDownload = { viewModel.resumeDownload(it) },
                    onNavigateToHome = { viewModel.setTab(0) }
                )

                2 -> SettingsScreen(
                    uiState = uiState,
                    onToggleAutoClipboard = { viewModel.toggleAutoClipboard(it) },
                    onToggleBanglaPriority = { viewModel.togglePrioritizeBanglaAudio(it) },
                    onToggleStrictSafeMode = { viewModel.toggleStrictSafeMode(it) },
                    onOpenAuthDialog = { viewModel.showAuthDialog() }
                )
            }
        }
    }

    // Interactive Full YouTube Search Overlay
    if (uiState.isSearchOverlayOpen) {
        YouTubeSearchOverlay(
            searchQuery = uiState.searchQuery,
            onQueryChange = {
                viewModel.onSearchQueryChanged(it)
                viewModel.onUrlInputChanged(it)
            },
            onSearchSubmit = { query ->
                viewModel.onSearchQueryChanged(query)
                viewModel.onUrlInputChanged(query)
                viewModel.setTab(0)
                if (query.startsWith("http://") || query.startsWith("https://") || query.contains("youtu")) {
                    viewModel.extractAndShowDownloadDialog(query)
                }
            },
            onClose = { viewModel.closeSearchOverlay() },
            onSelectVideo = { videoId, _ ->
                viewModel.extractAndShowDownloadDialog("https://youtu.be/$videoId")
            },
            onSelectAudio = { videoId, _ ->
                viewModel.extractAndShowDownloadDialog("https://youtu.be/$videoId")
            }
        )
    }

    // Google Account & Gmail Login Dialog
    if (uiState.showAuthDialog) {
        GoogleSignInDialog(
            userProfile = uiState.userProfile,
            onDismiss = { viewModel.dismissAuthDialog() },
            onLoginSuccess = { email, name ->
                viewModel.loginWithGoogle(email, name)
            },
            onLogout = {
                viewModel.logout()
            }
        )
    }

    // Modal: Quick Download & Stream Dialog (with Bengali Track First Priority & NewPipe Style Dialog)
    if (uiState.showQuickDownloadModal && uiState.extractedVideoDetails != null) {
        QuickDownloadBottomSheet(
            videoDetails = uiState.extractedVideoDetails!!,
            selectedTab = uiState.selectedDownloadTab,
            selectedVideoOption = uiState.selectedVideoOption,
            selectedAudioOption = uiState.selectedAudioOption,
            onTabSelected = { viewModel.setSelectedDownloadTab(it) },
            onVideoOptionSelected = { viewModel.selectVideoOption(it) },
            onAudioOptionSelected = { viewModel.selectAudioOption(it) },
            onStartDownload = { isAudioOnly, customTitle, threads ->
                viewModel.startDownloadFromModal(isAudioOnly, customTitle, threads)
            },
            onPlayDirectStream = { isAudioOnly ->
                viewModel.playStreamDirectly(isAudioOnly)
                showFullscreenPlayer = true
            },
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
            onAudioPresetChange = { viewModel.playbackManager.setAudioPreset(it) },
            onSleepTimerChange = { viewModel.playbackManager.setSleepTimer(it) },
            onToggleLoop = { viewModel.playbackManager.toggleLoop() },
            onToggleShuffle = { viewModel.playbackManager.toggleShuffle() },
            onPlayMediaItem = { videoId, title ->
                viewModel.extractAndShowDownloadDialog("https://youtu.be/$videoId")
            },
            onToggleSubscribe = { viewModel.toggleSubscription(it) },
            onToggleLike = { viewModel.toggleLike(it) },
            onRecordSkip = { viewModel.recordSkip(it) },
            onRecordComment = { viewModel.recordComment(it) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
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
                                    Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = YouTubeRed)
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
