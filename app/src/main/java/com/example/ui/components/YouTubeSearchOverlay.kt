package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.KnownBanglaMediaCatalogue
import com.example.data.service.MediaExtractorService
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeSearchOverlay(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onClose: () -> Unit,
    onSelectVideo: (videoId: String, title: String) -> Unit,
    onSelectAudio: (videoId: String, title: String) -> Unit,
    onVoiceSearchRequest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    // Trending & Bengali suggestions
    val quickSuggestions = listOf(
        "বাংলা নতুন গান ২০২৬",
        "Arijit Singh Bangla Songs",
        "হৃদয় কাঁপানো ইসলামিক নাশিদ",
        "Bangla Coke Studio Hits",
        "Tahsan Romantic Playlist",
        "Bengali Folk Song Studio",
        "সুরের ভুবন ইসলামিক গজল",
        "Habib Wahid Top Tracks",
        "SoundCloud Lo-Fi Bengali Beats"
    )

    // Filtered results based on typed query
    val searchResults: List<KnownBanglaMediaCatalogue.KnownMeta> = remember(searchQuery) {
        val clean = searchQuery.trim()
        if (clean.isBlank()) {
            emptyList()
        } else {
            KnownBanglaMediaCatalogue.sampleTrending.filter { item ->
                item.title.contains(clean, ignoreCase = true) ||
                item.author.contains(clean, ignoreCase = true) ||
                item.category.contains(clean, ignoreCase = true)
            }
        }
    }

    val isSearchAdultRestricted = remember(searchQuery) {
        MediaExtractorService.isAdultOrRestrictedContent(searchQuery)
    }

    LaunchedEffect(Unit) {
        delay(150)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // ignore
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. YouTube Authentic Top Search Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Back Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("search_overlay_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Search Input Field Container
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            placeholder = {
                                Text(
                                    text = "YouTube এ খুঁজুন বা লিঙ্ক পেস্ট করুন...",
                                    fontSize = 14.sp,
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
                                .focusRequester(focusRequester)
                                .testTag("search_overlay_input")
                        )

                        if (searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // YouTube Voice Search Mic Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onVoiceSearchRequest,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("voice_search_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = YouTubeRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                thickness = 0.8.dp
            )

            // 2. Search Content & Suggestions
            if (isSearchAdultRestricted) {
                // Safety restriction block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFE53935).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "১৮+ ও প্রাপ্তবয়স্ক কন্টেন্ট রেস্ট্রিক্টেড",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "BDTube এ ফ্যামিলি ও এআই সেফটি শিল্ড সক্রিয় রয়েছে। যেকোনো প্রাপ্তবয়স্ক বা ক্ষতিকর কন্টেন্ট স্বয়ংক্রিয়ভাবে ফিল্টার করা হয়।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else if (searchQuery.isBlank()) {
                // Default Trending & Suggestions List like real YouTube
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        Text(
                            text = "জনপ্রিয় ও ট্রেন্ডিং অনুসন্ধান",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(quickSuggestions) { query ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onQueryChange(query)
                                    onSearchSubmit(query)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = query,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.NorthWest,
                                contentDescription = "Fill query",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Interactive Search Live Suggestions & Video Matches
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (searchResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "সরাসরি ম্যাচিং ফলাফল (${searchResults.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = YouTubeRed,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        items(searchResults) { video ->
                            YouTubeVideoFeedCard(
                                item = video,
                                onCardClick = {
                                    onSelectVideo(video.videoId, video.title)
                                    onClose()
                                },
                                onDownloadClick = {
                                    onSelectVideo(video.videoId, video.title)
                                    onClose()
                                },
                                onPlayAudioClick = {
                                    onSelectAudio(video.videoId, video.title)
                                    onClose()
                                }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                thickness = 0.5.dp
                            )
                        }
                    } else {
                        // Prompt to search / extract this query
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = YouTubeRed,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "\"$searchQuery\" অনুসন্ধান করুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "সরাসরি লিঙ্ক হলে এক্সট্র্যাক্ট করে ডাউনলোড উইন্ডো চালু করতে বাটনে চাপুন।",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        onSearchSubmit(searchQuery)
                                        onClose()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("লিংক এক্সট্র্যাক্ট বা সার্চ করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
