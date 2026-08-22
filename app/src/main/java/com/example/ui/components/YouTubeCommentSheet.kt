package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.ui.theme.YouTubeRed

data class CommentItem(
    val id: String,
    val userName: String,
    val timeAgo: String,
    val commentText: String,
    val likesCount: Int = 12,
    val isLiked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeCommentSheet(
    videoTitle: String,
    onDismiss: () -> Unit,
    onCommentSubmitted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentsList by remember {
        mutableStateOf(
            listOf(
                CommentItem("1", "Md. Rafiqul Islam", "২ ঘন্টা আগে", "অসাধারণ একটি গান! বাংলা ট্র্যাকের সাউন্ড কোয়ালিটি অনেক সুন্দর হয়েছে।", 24),
                CommentItem("2", "Nabila Tabassum", "৫ ঘন্টা আগে", "বাংলা ভাষা প্রথম প্রায়োরিটি দেওয়ার ফিচারটি সত্যিই দারুণ হয়েছে।", 18),
                CommentItem("3", "Tanvir Ahmed", "১ দিন আগে", "মাশাল্লাহ! মন জুড়িয়ে গেল। আরও নতুন রিলিজের অপেক্ষায় রইলাম।", 45),
                CommentItem("4", "Sultana Razia", "২ দিন আগে", "ব্যাকগ্রাউন্ডে অডিও শোনার সিস্টেমটা খুবই স্মুথ কাজ করছে।", 9)
            )
        )
    }

    var commentInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "মন্তব্যসমূহ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${commentsList.size}",
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = videoTitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Comments List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(commentsList, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.userName.firstOrNull()?.toString() ?: "U",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.userName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.timeAgo,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.commentText,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input field to add comment (trains ML recommendation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    placeholder = {
                        Text("একটি মন্তব্য লিখুন...", fontSize = 13.sp)
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = YouTubeRed
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field")
                )

                IconButton(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            val newComment = CommentItem(
                                id = System.currentTimeMillis().toString(),
                                userName = "আপনি (User)",
                                timeAgo = "এইমাত্র",
                                commentText = commentInput.trim(),
                                likesCount = 1
                            )
                            commentsList = listOf(newComment) + commentsList
                            onCommentSubmitted(commentInput.trim())
                            commentInput = ""
                        }
                    },
                    enabled = commentInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (commentInput.isNotBlank()) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
