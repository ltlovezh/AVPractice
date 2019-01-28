package com.ltlovezh.avpractice.common

import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileManager {
    companion object {
        const val directory = "ltlovezh"

        fun getPictureDir(context: Context): File {
            val pictureDirPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/$directory"

            val pictureDir = File(pictureDirPath)
            if (!pictureDir.exists()) {
                pictureDir.mkdirs()
            }

            return pictureDir
        }

        fun getVideoDir(context: Context): File {
            val videoDirPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath + "/$directory"

            val videoDir = File(videoDirPath)
            if (!videoDir.exists()) {
                videoDir.mkdirs()
            }

            return videoDir
        }

        fun generateImgName(): String {
            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)  //文件名不能有空格和特殊字符

            return "IMG_${dateFormat.format(Date())}.jpg"
        }

        fun generateVideoName(): String {
            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)  //文件名不能有空格和特殊字符

            return "VID_${dateFormat.format(Date())}.mp4"
        }


        fun getInternalCacheDir(context: Context, dirName: String): File {
            val cacheDirPath = Environment.getExternalStorageDirectory().absolutePath + "/$dirName"
            val cacheDir = File(cacheDirPath)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            return cacheDir
        }


        fun generatePicturePath(context: Context): String {
            return getPictureDir(context).absolutePath + "/" + generateImgName()
        }
    }
}