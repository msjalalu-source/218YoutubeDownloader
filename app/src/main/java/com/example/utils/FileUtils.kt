package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object FileUtils {
    private const val TAG = "FileUtils"

    fun getDownloadFolder(context: Context): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.let {
            File(it, "BDTube")
        } ?: File(context.filesDir, "BDTube_Downloads")
        
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun sanitizeFileName(title: String, extension: String): String {
        val cleanName = title.replace(Regex("[^a-zA-Z0-9\\u0980-\\u09FF\\s-_]"), "")
            .trim()
            .replace("\\s+".toRegex(), "_")
            .take(40)
            .ifBlank { "Media_${System.currentTimeMillis() % 10000}" }
        return "$cleanName.$extension"
    }

    fun prepareLocalFile(context: Context, title: String, isAudio: Boolean): File {
        val folder = getDownloadFolder(context)
        val ext = if (isAudio) "mp3" else "mp4"
        val fileName = sanitizeFileName(title, ext)
        val file = File(folder, fileName)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating file: ${e.message}")
            }
        }
        return file
    }

    fun openWithExternalApp(context: Context, filePath: String, isVideo: Boolean) {
        try {
            val file = File(filePath)
            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(context, "ফাইলটি স্টোরেজে পাওয়া যায়নি!", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val mimeType = if (isVideo) "video/*" else "audio/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, if (isVideo) "ভিডিও প্লেয়ার নির্বাচন করুন" else "অডিও প্লেয়ার নির্বাচন করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening external player: ${e.message}", e)
            Toast.makeText(context, "বাহ্যিক প্লেয়ারে খুলতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, filePath: String, title: String, isVideo: Boolean) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "শেয়ার করার জন্য ফাইলটি পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val mimeType = if (isVideo) "video/mp4" else "audio/mpeg"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title - BDTube ডাউনলোড")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "ফাইল শেয়ার করুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file: ${e.message}", e)
            Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFreeDiskSpaceBytes(context: Context): Long {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            dir.freeSpace
        } catch (e: Exception) {
            1024L * 1024L * 1024L * 10L // Fallback 10GB
        }
    }

    fun getCacheSizeBytes(context: Context): Long {
        return try {
            val cacheDir = context.cacheDir
            getFolderSize(cacheDir)
        } catch (e: Exception) {
            0L
        }
    }

    private fun getFolderSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return size
    }

    fun clearAppCache(context: Context): Boolean {
        return try {
            val cacheDir = context.cacheDir
            deleteDir(cacheDir)
        } catch (e: Exception) {
            false
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return true
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) return false
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "০ KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            else -> String.format("%.0f KB", kb)
        }
    }
}
