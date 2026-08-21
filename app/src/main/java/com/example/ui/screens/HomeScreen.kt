package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.KnownBanglaMediaCatalogue
import com.example.ui.MainUiState
import com.example.ui.components.YouTubeFilterChips
import com.example.ui.components.YouTubeShortsShelf
import com.example.ui.components.YouTubeVideoFeedCard
import com.example.ui.theme.YouTubeRed

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onUrlChanged: (String) -> Unit,
    onPasteFromClipboard: () -> Unit,
    onExtractClicked: () -> Unit,
    onCategoryChanged: (String) -> Unit = {},
    onSearchChanged: (String) -> Unit = {},
    onTrendingItemClicked: (videoId: String, title: String) -> Unit = { _, _ -> },
    onClearBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter videos based on category
    val allVideos = KnownBanglaMediaCatalogue.sampleTrending
    val filteredVideos = remember(uiState.activeCategoryFilter, uiState.searchQuery) {
        allVideos.filter { video ->
            val matchesCategory = when (uiState.activeCategoryFilter) {
                "all" -> true
                "bangla_hits" -> video.category == "bangla_hits"
                "podcasts" -> video.category == "podcasts"
                "islamic" -> video.category == "islamic"
                "soundcloud" -> video.category == "soundcloud"
                else -> true
            }
            val matchesSearch = uiState.searchQuery.isBlank() ||
                    video.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    video.author.contains(uiState.searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. YouTube-style Smart Link Paste & Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )

                        TextField(
                            value = uiState.inputUrlText,
                            onValueChange = {
                                onUrlChanged(it)
                                onSearchChanged(it)
                            },
                            placeholder = {
                                Text(
                                    text = "YouTube বা SoundCloud লিঙ্ক পেস্ট করুন...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = YouTubeRed,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("url_input_field")
                        )

                        if (uiState.inputUrlText.isNotBlank()) {
                            IconButton(onClick = {
                                onUrlChanged("")
                                onSearchChanged("")
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Direct Clipboard Paste Button
                        Surface(
                            onClick = onPasteFromClipboard,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("paste_clipboard_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Extract / Download Action Pill
                        Button(
                            onClick = onExtractClicked,
                            enabled = !uiState.isExtracting && uiState.inputUrlText.isNotBlank(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YouTubeRed,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("extract_media_button")
                        ) {
                            if (uiState.isExtracting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ডাউনলোড",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Banner notification if active
        if (uiState.bannerMessage != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, YouTubeRed.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = YouTubeRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.bannerMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = onClearBanner,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. YouTube Category / Filter Chips
        item {
            YouTubeFilterChips(
                selectedCategory = uiState.activeCategoryFilter,
                onSelectCategory = onCategoryChanged
            )
        }

        // 4. YouTube First Video Card
        if (filteredVideos.isNotEmpty()) {
            item {
                val firstItem = filteredVideos.first()
                YouTubeVideoFeedCard(
                    item = firstItem,
                    onCardClick = { onTrendingItemClicked(firstItem.videoId, firstItem.title) },
                    onDownloadClick = { onTrendingItemClicked(firstItem.videoId, firstItem.title) },
                    onPlayAudioClick = { onTrendingItemClicked(firstItem.videoId, firstItem.title) }
                )
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // 5. YouTube Shorts Shelf (Between video 1 and rest)
        item {
            YouTubeShortsShelf(
                onShortClick = { id ->
                    onTrendingItemClicked("dQw4w9WgXcQ", "বাংলা সেরা ট্রেন্ডিং শর্টস")
                }
            )
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // 6. Remaining YouTube Video Feed Cards
        if (filteredVideos.size > 1) {
            items(filteredVideos.drop(1)) { item ->
                YouTubeVideoFeedCard(
                    item = item,
                    onCardClick = { onTrendingItemClicked(item.videoId, item.title) },
                    onDownloadClick = { onTrendingItemClicked(item.videoId, item.title) },
                    onPlayAudioClick = { onTrendingItemClicked(item.videoId, item.title) }
                )
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
