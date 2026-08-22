package com.example.recommendation

import androidx.compose.runtime.Immutable
import com.example.data.service.KnownBanglaMediaCatalogue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min

/**
 * Channel metadata for YouTube Subscriptions Feed
 */
@Immutable
data class ChannelProfile(
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val isSubscribed: Boolean = true,
    val subscriberCount: String = "১.২M",
    val hasNewUpload: Boolean = true
)

/**
 * User interaction profile tracking for on-device Machine Learning Recommendation
 */
@Immutable
data class UserInteractionProfile(
    val categoryWatchSeconds: Map<String, Long> = mapOf(
        "bangla_hits" to 1800L,
        "islamic" to 1200L,
        "podcasts" to 600L,
        "soundcloud" to 400L,
        "all" to 300L
    ),
    val channelWatchSeconds: Map<String, Long> = mapOf(
        "বাংলা টিউনস স্টুডিও" to 900L,
        "কোক স্টুডিও বাংলা স্পেশাল" to 1200L,
        "কলরব শিল্পীগোষ্ঠী" to 800L
    ),
    val videoWatchSeconds: Map<String, Long> = emptyMap(),
    val likedVideoIds: Set<String> = setOf("dQw4w9WgXcQ", "3JZ_D3ELwOQ"),
    val dislikedOrSkippedVideoIds: Set<String> = emptySet(),
    val commentedVideoIds: Set<String> = setOf("dQw4w9WgXcQ"),
    val subscribedChannelNames: Set<String> = setOf(
        "কোক স্টুডিও বাংলা স্পেশাল",
        "কলরব শিল্পীগোষ্ঠী",
        "বাংলা টিউনস স্টুডিও",
        "বিডি পডকাস্ট আনপ্লাগড"
    ),
    val totalWatchTimeSeconds: Long = 4300L
)

/**
 * Scored media item after running through ML Recommendation Model
 */
@Immutable
data class ScoredMedia(
    val meta: KnownBanglaMediaCatalogue.KnownMeta,
    val score: Float,
    val recommendationReason: String,
    val isSubscribedChannel: Boolean,
    val isLiked: Boolean
)

/**
 * High-performance on-device Machine Learning & Recommendation Algorithm Engine
 *
 * Algorithm weights:
 * 1. Category Affinity (Watch Time & Frequency): 35%
 * 2. Creator Loyalty & Subscription Status: 25%
 * 3. Explicit Engagement (Likes, Comments, Shares): 20%
 * 4. Watch Time Depth & Completion Rate: 15%
 * 5. Negative Feedback (Fast Skips < 15s or Dislikes): -30% Penalty
 * 6. Fresh Discovery Exploration: 5%
 */
object MLRecommendationEngine {

    private val _interactionProfile = MutableStateFlow(UserInteractionProfile())
    val interactionProfile: StateFlow<UserInteractionProfile> = _interactionProfile.asStateFlow()

    val defaultChannels = listOf(
        ChannelProfile(
            name = "কোক স্টুডিও বাংলা স্পেশাল",
            handle = "@cokestudiobangla",
            avatarUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=150&auto=format&fit=crop",
            isSubscribed = true,
            subscriberCount = "৩.৪M",
            hasNewUpload = true
        ),
        ChannelProfile(
            name = "বাংলা টিউনস স্টুডিও",
            handle = "@banglatunes",
            avatarUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=150&auto=format&fit=crop",
            isSubscribed = true,
            subscriberCount = "১.৮M",
            hasNewUpload = true
        ),
        ChannelProfile(
            name = "কলরব শিল্পীগোষ্ঠী",
            handle = "@kalarab_official",
            avatarUrl = "https://images.unsplash.com/photo-1564769625905-50e93615e769?w=150&auto=format&fit=crop",
            isSubscribed = true,
            subscriberCount = "৪.২M",
            hasNewUpload = true
        ),
        ChannelProfile(
            name = "বিডি পডকাস্ট আনপ্লাগড",
            handle = "@bdpodcast",
            avatarUrl = "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=150&auto=format&fit=crop",
            isSubscribed = true,
            subscriberCount = "৮৫০K",
            hasNewUpload = false
        ),
        ChannelProfile(
            name = "MrBeast",
            handle = "@mrbeast",
            avatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop",
            isSubscribed = false,
            subscriberCount = "৩০০M",
            hasNewUpload = true
        ),
        ChannelProfile(
            name = "বাউল একাডেমি ঢাকা",
            handle = "@baulacademy",
            avatarUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop",
            isSubscribed = false,
            subscriberCount = "৬২০K",
            hasNewUpload = false
        ),
        ChannelProfile(
            name = "ডিজে রনি বিডি রিমিক্স",
            handle = "@djronybd",
            avatarUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=150&auto=format&fit=crop",
            isSubscribed = false,
            subscriberCount = "৪১০K",
            hasNewUpload = false
        )
    )

    private val _channels = MutableStateFlow(defaultChannels)
    val channels: StateFlow<List<ChannelProfile>> = _channels.asStateFlow()

    /**
     * Records watch time progression
     */
    fun recordWatchProgress(
        videoId: String,
        category: String,
        author: String,
        durationSeconds: Long,
        totalDurationSeconds: Long
    ) {
        val current = _interactionProfile.value
        val newCatTime = (current.categoryWatchSeconds[category] ?: 0L) + durationSeconds
        val newChanTime = (current.channelWatchSeconds[author] ?: 0L) + durationSeconds
        val newVidTime = (current.videoWatchSeconds[videoId] ?: 0L) + durationSeconds

        _interactionProfile.value = current.copy(
            categoryWatchSeconds = current.categoryWatchSeconds + (category to newCatTime),
            channelWatchSeconds = current.channelWatchSeconds + (author to newChanTime),
            videoWatchSeconds = current.videoWatchSeconds + (videoId to newVidTime),
            totalWatchTimeSeconds = current.totalWatchTimeSeconds + durationSeconds
        )
    }

    /**
     * Records like interaction
     */
    fun toggleLike(videoId: String): Boolean {
        val current = _interactionProfile.value
        val isLiked = current.likedVideoIds.contains(videoId)
        val updatedLikes = if (isLiked) {
            current.likedVideoIds - videoId
        } else {
            current.likedVideoIds + videoId
        }
        val updatedDislikes = current.dislikedOrSkippedVideoIds - videoId

        _interactionProfile.value = current.copy(
            likedVideoIds = updatedLikes,
            dislikedOrSkippedVideoIds = updatedDislikes
        )
        return !isLiked
    }

    /**
     * Records skip / negative interaction
     */
    fun recordSkipOrDislike(videoId: String) {
        val current = _interactionProfile.value
        _interactionProfile.value = current.copy(
            dislikedOrSkippedVideoIds = current.dislikedOrSkippedVideoIds + videoId,
            likedVideoIds = current.likedVideoIds - videoId
        )
    }

    /**
     * Records comment / active engagement
     */
    fun recordComment(videoId: String) {
        val current = _interactionProfile.value
        _interactionProfile.value = current.copy(
            commentedVideoIds = current.commentedVideoIds + videoId
        )
    }

    /**
     * Toggles subscription to a channel
     */
    fun toggleSubscription(channelName: String): Boolean {
        val current = _interactionProfile.value
        val isSubbed = current.subscribedChannelNames.contains(channelName)
        val updatedSubs = if (isSubbed) {
            current.subscribedChannelNames - channelName
        } else {
            current.subscribedChannelNames + channelName
        }

        _interactionProfile.value = current.copy(subscribedChannelNames = updatedSubs)

        // Update channels flow
        _channels.value = _channels.value.map { channel ->
            if (channel.name.equals(channelName, ignoreCase = true)) {
                channel.copy(isSubscribed = !isSubbed)
            } else {
                channel
            }
        }

        return !isSubbed
    }

    /**
     * Machine Learning Multi-Factor Scoring Formula
     */
    fun calculateRelevanceScore(
        item: KnownBanglaMediaCatalogue.KnownMeta,
        profile: UserInteractionProfile
    ): Pair<Float, String> {
        var score = 50.0f // Baseline discovery score
        var primaryReason = "জনপ্রিয় ভিডিও"

        // 1. Category Affinity (35% weight)
        val categoryTime = profile.categoryWatchSeconds[item.category] ?: 0L
        val totalTime = max(1L, profile.totalWatchTimeSeconds)
        val categoryAffinity = (categoryTime.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
        val categoryScore = categoryAffinity * 35f
        score += categoryScore
        if (categoryAffinity > 0.25f) {
            primaryReason = "আপনার পছন্দসই ক্যাটাগরি (${categoryNameBangla(item.category)})"
        }

        // 2. Creator Loyalty & Subscription (25% weight)
        val isSubscribed = profile.subscribedChannelNames.any { it.equals(item.author, ignoreCase = true) }
        val channelTime = profile.channelWatchSeconds[item.author] ?: 0L
        if (isSubscribed) {
            score += 25f
            primaryReason = "সাবস্ক্রাইব করা চ্যানেল • নতুন রিলিজ"
        } else if (channelTime > 300L) {
            score += 15f
            primaryReason = "এই চ্যানেলের ভিডিও আপনি পূর্বে দেখেছেন"
        }

        // 3. User Explicit Actions (20% weight)
        if (profile.likedVideoIds.contains(item.videoId)) {
            score += 20f
            primaryReason = "আপনার লাইক করা ভিডিও তালিকা থেকে"
        }
        if (profile.commentedVideoIds.contains(item.videoId)) {
            score += 10f
        }

        // 4. Watch Time Depth (15% weight)
        val videoWatchedSecs = profile.videoWatchSeconds[item.videoId] ?: 0L
        if (videoWatchedSecs > 60L) {
            val completion = (videoWatchedSecs.toFloat() / max(1L, item.durationSeconds).toFloat()).coerceIn(0f, 1f)
            score += completion * 15f
        }

        // 5. Negative Skip Penalty (-30% weight)
        if (profile.dislikedOrSkippedVideoIds.contains(item.videoId)) {
            score -= 40f
            primaryReason = "কম রেকমেন্ড করা হচ্ছে"
        }

        // 6. Bengali Language Priority Bonus
        if (item.category == "bangla_hits" || item.title.contains("বাংলা")) {
            score += 5f
        }

        return Pair(score, primaryReason)
    }

    private var cachedRankedFeed: List<ScoredMedia>? = null
    private var lastRankedCacheKey: String = ""

    fun clearCalculatedCaches() {
        cachedRankedFeed = null
        lastRankedCacheKey = ""
    }

    /**
     * Returns ML-Ranked Feed for Home Screen
     */
    fun getRankedHomeFeed(
        categoryFilter: String = "all",
        selectedChannelFilter: String? = null
    ): List<ScoredMedia> {
        val profile = _interactionProfile.value
        val cacheKey = "$categoryFilter|$selectedChannelFilter|${profile.likedVideoIds.size}|${profile.subscribedChannelNames.size}|${profile.totalWatchTimeSeconds}"
        
        if (cacheKey == lastRankedCacheKey && cachedRankedFeed != null) {
            return cachedRankedFeed!!
        }

        val allVideos = KnownBanglaMediaCatalogue.sampleTrending

        val filtered = when {
            selectedChannelFilter != null -> {
                allVideos.filter { it.author.equals(selectedChannelFilter, ignoreCase = true) }
            }
            categoryFilter == "subscriptions" -> {
                allVideos.filter { video ->
                    profile.subscribedChannelNames.any { it.equals(video.author, ignoreCase = true) }
                }
            }
            categoryFilter != "all" -> {
                allVideos.filter { it.category == categoryFilter }
            }
            else -> allVideos
        }

        val result = filtered.map { meta ->
            val (score, reason) = calculateRelevanceScore(meta, profile)
            val isSubbed = profile.subscribedChannelNames.any { it.equals(meta.author, ignoreCase = true) }
            val isLiked = profile.likedVideoIds.contains(meta.videoId)
            ScoredMedia(
                meta = meta,
                score = score,
                recommendationReason = reason,
                isSubscribedChannel = isSubbed,
                isLiked = isLiked
            )
        }.sortedByDescending { it.score }

        lastRankedCacheKey = cacheKey
        cachedRankedFeed = result
        return result
    }

    /**
     * Returns ML-Ranked Suggested Videos next to the currently playing video in Player
     */
    fun getSuggestedVideosForPlayer(
        currentVideoId: String,
        currentCategory: String,
        currentAuthor: String
    ): List<ScoredMedia> {
        val profile = _interactionProfile.value
        val allVideos = KnownBanglaMediaCatalogue.sampleTrending.filter { it.videoId != currentVideoId }

        return allVideos.map { meta ->
            val (baseScore, baseReason) = calculateRelevanceScore(meta, profile)
            var boostedScore = baseScore
            var reason = baseReason

            // Up-rank videos from the same creator or category for player suggestions
            if (meta.author.equals(currentAuthor, ignoreCase = true)) {
                boostedScore += 30f
                reason = "একই শিল্পীর আরও ভিডিও"
            } else if (meta.category.equals(currentCategory, ignoreCase = true)) {
                boostedScore += 20f
                reason = "একই ক্যাটাগরির পরবর্তী গান"
            }

            val isSubbed = profile.subscribedChannelNames.any { it.equals(meta.author, ignoreCase = true) }
            val isLiked = profile.likedVideoIds.contains(meta.videoId)

            ScoredMedia(
                meta = meta,
                score = boostedScore,
                recommendationReason = reason,
                isSubscribedChannel = isSubbed,
                isLiked = isLiked
            )
        }.sortedByDescending { it.score }
    }

    private fun categoryNameBangla(cat: String): String {
        return when (cat) {
            "bangla_hits" -> "বাংলা হিট গান"
            "islamic" -> "ইসলামিক নাশিদ"
            "podcasts" -> "পডকাস্ট"
            "soundcloud" -> "সাউন্ডক্লাউড"
            else -> "জনপ্রিয়"
        }
    }
}
