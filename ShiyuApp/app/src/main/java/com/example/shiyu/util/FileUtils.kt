package com.example.shiyu.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    private const val MAX_LONGEST_SIDE = 1920
    private const val TARGET_MAX_SIZE = 1024 * 1024L // 1MB
    private const val INITIAL_QUALITY = 85
    private const val MIN_QUALITY = 20
    private const val QUALITY_STEP = 5

    fun getImagesDir(context: Context): File {
        val dir = File(context.filesDir, "images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun generateFileName(extension: String = "jpg"): String {
        val timestamp = System.currentTimeMillis()
        val random = (0..9999).random()
        return "${timestamp}_${random}.$extension"
    }

    fun saveImage(context: Context, uri: Uri, fileName: String? = null): String? {
        return try {
            val dir = getImagesDir(context)
            val name = fileName ?: generateFileName()
            val destFile = File(dir, name)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String? = null): String? {
        return try {
            val dir = getImagesDir(context)
            val name = fileName ?: generateFileName()
            val destFile = File(dir, name)

            FileOutputStream(destFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_QUALITY, output)
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getFileSize(filePath: String): Long {
        return try {
            val file = File(filePath)
            if (file.exists()) file.length() else 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun compressImage(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(input, null, options)

                val width = options.outWidth
                val height = options.outHeight
                var inSampleSize = 1

                while (width / inSampleSize > maxWidth || height / inSampleSize > maxHeight) {
                    inSampleSize *= 2
                }

                options.inJustDecodeBounds = false
                options.inSampleSize = inSampleSize

                context.contentResolver.openInputStream(uri)?.use { input2 ->
                    BitmapFactory.decodeStream(input2, null, options)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Compress image to under target size (default 1MB)
     * Scales down large images first, then iteratively reduces quality
     */
    fun compressImageForUpload(
        context: Context,
        sourcePath: String,
        targetMaxSize: Long = TARGET_MAX_SIZE
    ): File? {
        return try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) return null

            val bitmap = BitmapFactory.decodeFile(sourcePath) ?: return null
            val scaledBitmap = scaleDownBitmap(bitmap)

            val dir = getImagesDir(context)
            val destFile = File(dir, "upload_${generateFileName()}")
            var quality = INITIAL_QUALITY
            var outputStream = ByteArrayOutputStream()

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            while (outputStream.size() > targetMaxSize && quality > MIN_QUALITY) {
                quality -= QUALITY_STEP
                outputStream.reset()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            FileOutputStream(destFile).use { output ->
                output.write(outputStream.toByteArray())
            }

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Compress image from Uri for upload
     */
    fun compressImageForUpload(
        context: Context,
        uri: Uri,
        targetMaxSize: Long = TARGET_MAX_SIZE
    ): File? {
        return try {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return null

            val scaledBitmap = scaleDownBitmap(bitmap)

            val dir = getImagesDir(context)
            val destFile = File(dir, "upload_${generateFileName()}")
            var quality = INITIAL_QUALITY
            var outputStream = ByteArrayOutputStream()

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            while (outputStream.size() > targetMaxSize && quality > MIN_QUALITY) {
                quality -= QUALITY_STEP
                outputStream.reset()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            FileOutputStream(destFile).use { output ->
                output.write(outputStream.toByteArray())
            }

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Scale down bitmap if longest side exceeds MAX_LONGEST_SIDE
     */
    private fun scaleDownBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_LONGEST_SIDE && height <= MAX_LONGEST_SIDE) {
            return bitmap
        }

        val ratio = minOf(
            MAX_LONGEST_SIDE.toFloat() / width,
            MAX_LONGEST_SIDE.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Check if file needs compression (over 1MB)
     */
    fun needsCompression(filePath: String): Boolean {
        return getFileSize(filePath) > TARGET_MAX_SIZE
    }
}
