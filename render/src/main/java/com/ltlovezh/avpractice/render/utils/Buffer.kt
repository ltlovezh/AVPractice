package com.ltlovezh.avpractice.render.utils

import android.graphics.Bitmap
import android.opengl.GLES20
import android.text.TextUtils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Author : litao
 * Time : 2019/1/6 - 9:26 PM
 * Description : This is Buffer
 */
object Buffer {

    /**
     * 必须在OpenGL线程调用
     */
    fun saveFrame(path: String, width: Int, height: Int): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }

        val outputFile = File(path)
        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        val buffer = ByteBuffer.allocateDirect(width * height * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
        buffer.rewind()


        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(FileOutputStream(outputFile))
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(buffer)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bmp.recycle()
            return true
        } finally {
            bos?.close()
        }

        return false
    }
}