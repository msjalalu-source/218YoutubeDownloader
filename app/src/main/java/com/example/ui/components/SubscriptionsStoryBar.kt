package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recommendation.ChannelProfile
import com.example.ui.theme.YouTubeRed

@Composable
fun SubscriptionsStoryBar(
    channels: List<ChannelProfile>,
    selectedChannelName: String?,
    onSelectChannel: (String?) -> Unit,
    onToggleSubscribe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val subscribedChannels = channels.filter { it.isSubscribed }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(vertical = 8.dp)
    ) {
        // Section Title with Subscriptions Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Subscriptions,
                    contentDescription = null,
                    tint = YouTubeRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "সাবস্ক্রিপশন ফিড",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = YouTubeRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${subscribedChannels.size} চ্যানেল",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = YouTubeRed,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            TextButton(
                onClick = { onSelectChannel(null) },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = if (selectedChannelName != null) "ফিল্টার মুছুন" else "সবগুলো দেখুন",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedChannelName != null) YouTubeRed else MaterialTheme.colorScheme.primary
                )
            }
        }

        // Horizontal Story Avatars Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All Subscriptions" Bubble Button
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(60.dp)
                        .clickable { onSelectChannel(null) }
                        .testTag("subscription_story_all")
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedChannelName == null) YouTubeRed else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subscriptions,
                            contentDescription = "All Subscriptions",
                            tint = if (selectedChannelName == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "সব চ্যানেল",
                        fontSize = 11.sp,
                        fontWeight = if (selectedChannelName == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedChannelName == null) YouTubeRed else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Subscribed channels list
            items(channels) { channel ->
                val isSelected = selectedChannelName == channel.name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(62.dp)
                        .clickable {
                            if (isSelected) {
                                onSelectChannel(null)
                            } else {
                                onSelectChannel(channel.name)
                            }
                        }
                        .testTag("subscription_story_${channel.name}")
                ) {
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Gradient border for channels with new upload or selected
                        val borderModifier = if (isSelected) {
                            Modifier.border(2.dp, YouTubeRed, CircleShape)
                        } else if (channel.isSubscribed && channel.hasNewUpload) {
                            Modifier.border(
                                2.dp,
                                Brush.sweepGradient(listOf(YouTubeRed, Color(0xFFFF9800), YouTubeRed)),
                                CircleShape
                            )
                        } else if (channel.isSubscribed) {
                            Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                        } else {
                            Modifier
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .then(borderModifier)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = channel.avatarUrl,
                                contentDescription = channel.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Red dot for new upload indicator
                        if (channel.isSubscribed && channel.hasNewUpload) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(YouTubeRed)
                                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        } else if (!channel.isSubscribed) {
                            // Plus icon to subscribe
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onToggleSubscribe(channel.name) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Subscribe",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = channel.name,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) YouTubeRed else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
