package com.ltlovezh.avpractice.common.bitmap

import android.graphics.Bitmap
import android.media.ExifInterface
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.TextUtils
import com.ltlovezh.avpractice.common.Logger
import java.io.*

/**
 * Author : litao
 * Time : 2019/1/15 - 11:54 AM
 * Description : This is BitmapUtil
 */
object BitmapUtil {
    private const val TAG = "BitmapUtil"

    fun saveBitmapToFile(bitmap: Bitmap, filePath: String) {
        saveBitmapToFile(bitmap, File(filePath))
    }

    fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        saveBitmapToFile(bitmap, file, Bitmap.CompressFormat.JPEG, 100)
    }

    fun saveBitmapToFile(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat, quality: Int) {
        var bos: BufferedOutputStream? = null
        try {
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            bos = BufferedOutputStream(FileOutputStream(file))
            bitmap.compress(format, quality, bos)
        } catch (e: Exception) {
            Logger.e(
                TAG,
                "saveBitmapToFile error : $e"
            )
        } finally {
            try {
                bos?.close()
            } catch (e: Exception) {
                Logger.e(
                    TAG,
                    "saveBitmapToFile finally error : $e"
                )
            }
        }
    }

    fun getPictureDegree(path: String?): Int {
        if (TextUtils.isEmpty(path)) {
            return 0
        }

        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation =
                exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: Exception) {
            Logger.e(
                TAG,
                "getPictureDegree error : $e"
            )

        }
        return degree
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getPictureDegree(inputStream: InputStream?): Int {
        inputStream ?: return 0

        var degree = 0
        try {
            val exifInterface = ExifInterface(inputStream)
            val orientation =
                exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: Exception) {
            Logger.e(
                TAG,
                "getPictureDegree error : $e"
            )

        }
        return degree
    }
}