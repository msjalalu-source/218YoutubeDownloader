package com.example

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.data.service.MediaExtractorService
import com.example.recommendation.MLRecommendationEngine

/**
 * BDTubeApplication handles global lifecycle, memory trimming (TRIM_MEMORY_RUNNING_LOW),
 * and lightweight Coil ImageLoader configuration with strict memory bounds to prevent OOM
 * and sustain long-duration lightweight playback.
 */
class BDTubeApplication : Application(), ImageLoaderFactory, ComponentCallbacks2 {

    companion object {
        private const val TAG = "BDTubeApplication"
        lateinit var instance: BDTubeApplication
            private set
    }

    private var appImageLoader: ImageLoader? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerComponentCallbacks(this)
    }

    /**
     * Memory-efficient Coil ImageLoader:
     * - Limits memory cache to 15% of available JVM heap
     * - Configures RGB_565 / Hardware Bitmaps to cut RAM usage by 50%
     * - Sets a 50MB disk cache for instant offline reload
     */
    override fun newImageLoader(): ImageLoader {
        val loader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15) // Max 15% heap for image cache
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50MB disk cache
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(false) // Disable heavy crossfade animation to save GPU/CPU
            .allowRgb565(true) // Reduce 32-bit ARGB to 16-bit RGB where appropriate to save 50% RAM
            .build()

        appImageLoader = loader
        return loader
    }

    /**
     * Automatic Memory Trimming:
     * When Android signals low RAM (TRIM_MEMORY_RUNNING_LOW / CRITICAL / COMPLETE),
     * immediate purge of in-memory caches occurs to safeguard the OS and prevent background kills.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory received level: $level")

        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // Critical memory pressure: full cache purge
                appImageLoader?.memoryCache?.clear()
                MediaExtractorService.clearMemoryCache()
                MLRecommendationEngine.clearCalculatedCaches()
                System.gc()
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // Moderate pressure: trim caches
                appImageLoader?.memoryCache?.clear()
                MediaExtractorService.trimMemoryCache()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        appImageLoader?.memoryCache?.clear()
        MediaExtractorService.clearMemoryCache()
        MLRecommendationEngine.clearCalculatedCaches()
        System.gc()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterComponentCallbacks(this)
    }
}
