package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.YouTubeRed

data class ShortItem(
    val id: String,
    val title: String,
    val views: String,
    val thumbUrl: String
)

@Composable
fun YouTubeShortsShelf(
    onShortClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleShorts = listOf(
        ShortItem(
            id = "short_1",
            title = "বাংলা সেরা গানের রিমিক্স টিউন #Shorts",
            views = "১.৮M ভিউজ",
            thumbUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop"
        ),
        ShortItem(
            id = "short_2",
            title = "মায়াবী বাঁশির মধুর সুর - ভাইরাল ক্লিপ",
            views = "৯৫০K ভিউজ",
            thumbUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop"
        ),
        ShortItem(
            id = "short_3",
            title = "ইসলামিক উপদেশ ও সুন্দর নাশিদ শর্টস",
            views = "২.৪M ভিউজ",
            thumbUrl = "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=500&auto=format&fit=crop"
        ),
        ShortItem(
            id = "short_4",
            title = "সাউন্ডক্লাউড ভাইরাল বাস বুস্টেড ডপ বিট",
            views = "৬২০K ভিউজ",
            thumbUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop"
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Shorts Header with Official Shorts Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = YouTubeRed,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Shorts",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• ট্রেন্ডিং শর্ট ভিডিও",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Row of 9:16 Shorts
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sampleShorts) { item ->
                Surface(
                    onClick = { onShortClick(item.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E1E1E),
                    modifier = Modifier
                        .width(135.dp)
                        .height(230.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.thumbUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Gradient protection for text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                    )
                                )
                        )

                        // Text content
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.views,
                                color = Color(0xFFDDDDDD),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
