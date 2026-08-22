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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.KnownBanglaMediaCatalogue
import com.example.data.service.MediaExtractorService
import com.example.recommendation.ChannelProfile
import com.example.recommendation.MLRecommendationEngine
import com.example.recommendation.ScoredMedia
import com.example.ui.MainUiState
import com.example.ui.components.SubscriptionsStoryBar
import com.example.ui.components.YouTubeVideoFeedCard
import com.example.ui.theme.YouTubeRed

@Composable
fun HomeScreen(
    uiState: MainUiState,
    channels: List<ChannelProfile>,
    onUrlChanged: (String) -> Unit = {},
    onPasteFromClipboard: () -> Unit = {},
    onExtractClicked: () -> Unit = {},
    onCategoryChanged: (String) -> Unit = {},
    onSelectChannel: (String?) -> Unit = {},
    onToggleSubscribe: (String) -> Unit = {},
    onToggleLike: (String) -> Unit = {},
    onSearchChanged: (String) -> Unit = {},
    onTrendingItemClicked: (videoId: String, title: String) -> Unit = { _, _ -> },
    onClearBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter videos based on category & adult content restriction
    val isSearchAdultRestricted = remember(uiState.searchQuery) {
        MediaExtractorService.isAdultOrRestrictedContent(uiState.searchQuery)
    }

    // Dynamic ML-Ranked Feed based on watch time, likes, comments, and subscriptions
    val interactionProfile by MLRecommendationEngine.interactionProfile.collectAsState()

    val rankedVideos: List<ScoredMedia> = remember(
        uiState.activeCategoryFilter,
        uiState.selectedChannelFilter,
        uiState.searchQuery,
        interactionProfile,
        isSearchAdultRestricted
    ) {
        if (isSearchAdultRestricted) {
            emptyList()
        } else {
            val baseFeed = MLRecommendationEngine.getRankedHomeFeed(
                categoryFilter = uiState.activeCategoryFilter,
                selectedChannelFilter = uiState.selectedChannelFilter
            )

            if (uiState.searchQuery.isBlank()) {
                baseFeed
            } else {
                baseFeed.filter { item ->
                    item.meta.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    item.meta.author.contains(uiState.searchQuery, ignoreCase = true) ||
                    item.meta.category.contains(uiState.searchQuery, ignoreCase = true)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Banner notification if active
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

        // Subscriptions Feed Story / Channel Bar
        item {
            SubscriptionsStoryBar(
                channels = channels,
                selectedChannelName = uiState.selectedChannelFilter,
                onSelectChannel = onSelectChannel,
                onToggleSubscribe = onToggleSubscribe
            )
        }

        // Adult Restricted Alert if user searched adult terms
        if (isSearchAdultRestricted) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "১৮+ ও প্রাপ্তবয়স্ক কন্টেন্ট রেস্ট্রিক্টেড",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ফ্যামিলি সেফ মোড ও এআই সেফটি শিল্ড সক্রিয় রয়েছে। কোনো অনুপযুক্ত বা ১৮+ ফলাফল প্রদর্শন করা হবে না।",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (rankedVideos.isNotEmpty()) {
            items(rankedVideos, key = { it.meta.videoId }) { scoredItem ->
                YouTubeVideoFeedCard(
                    item = scoredItem.meta,
                    recommendationReason = scoredItem.recommendationReason,
                    isSubscribedChannel = scoredItem.isSubscribedChannel,
                    isLiked = scoredItem.isLiked,
                    onCardClick = { onTrendingItemClicked(scoredItem.meta.videoId, scoredItem.meta.title) },
                    onDownloadClick = { onTrendingItemClicked(scoredItem.meta.videoId, scoredItem.meta.title) },
                    onPlayAudioClick = { onTrendingItemClicked(scoredItem.meta.videoId, scoredItem.meta.title) },
                    onToggleLike = { onToggleLike(scoredItem.meta.videoId) },
                    onToggleSubscribe = { onToggleSubscribe(scoredItem.meta.author) }
                )
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        } else if (!isSearchAdultRestricted) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (uiState.activeCategoryFilter == "subscriptions") "আপনার কোনো সাবস্ক্রাইব করা চ্যানেলের ভিডিও নেই" else "কোনো ভিডিও পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
