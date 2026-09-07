package com.example.audio

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object AudioExportManager {

    /**
     * Saves generated audio bytes directly to the user's device Downloads folder.
     * Uses Scoped Storage (MediaStore) for modern Android, requiring no special runtime permissions.
     */
    suspend fun saveAudioToDownloads(
        context: Context,
        audioBytes: ByteArray,
        baseFileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val safeName = baseFileName.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_").take(30)
            val fileName = "SuaraAI_${safeName}_${System.currentTimeMillis()}.wav"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SuaraAI")
                }

                val uri: Uri? = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { os: OutputStream ->
                        os.write(audioBytes)
                    }
                    Result.success("Disimpan ke folder Download/SuaraAI/$fileName")
                } else {
                    // Fallback to internal/cache
                    Result.failure(Exception("Gagal membuka lokasi penyimpanan Download"))
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, "SuaraAI").apply { mkdirs() }
                val targetFile = File(appDir, fileName)
                FileOutputStream(targetFile).use { fos ->
                    fos.write(audioBytes)
                }
                Result.success("Disimpan ke folder Download/SuaraAI/$fileName")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Shares audio directly via WhatsApp, Telegram, Email, or any audio player
     */
    fun shareAudio(
        context: Context,
        audioBytes: ByteArray,
        title: String
    ) {
        try {
            val cacheDir = File(context.cacheDir, "shared_audio").apply { mkdirs() }
            val safeTitle = title.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_").take(25)
            val tempFile = File(cacheDir, "SuaraAI_${safeTitle}.wav")
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Audio Suara AI - $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Audio Suara AI").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.util.Log.e("AudioExportManager", "Failed to share audio", e)
        }
    }

    /**
     * Shares an existing audio file path
     */
    fun shareAudioFile(
        context: Context,
        filePath: String,
        title: String
    ) {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Audio Suara AI - $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Audio Suara AI").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.util.Log.e("AudioExportManager", "Failed to share audio file", e)
        }
    }
}
