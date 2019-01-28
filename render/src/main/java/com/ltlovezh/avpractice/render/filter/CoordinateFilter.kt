package com.ltlovezh.avpractice.render.filter

import android.content.res.Resources
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Author : litao
 * Time : 2019/1/7 - 10:28 AM
 * Description : This is CoordinateFilter
 */
class CoordinateFilter(res: Resources) : AFilter(res) {

    private var attributeColor = 0

    companion object {
        // 坐标轴的两条线
        private val INE_POSITION = floatArrayOf(
            -1f, 0f, 1f, 0f,

            0f, -1f, 0f, 1f
        )
        private val LINE_COLOR = floatArrayOf(
            1.0f, 0.0f, 0.0f, 1.0f, // 红色
            0.0f, 1.0f, 0.0f, 1.0f, // 绿色

            1.0f, 0.0f, 0.0f, 1.0f, // 红色
            0.0f, 1.0f, 0.0f, 1.0f
        )// 绿色
    }


    override fun initBuffer() {
        val a = ByteBuffer.allocateDirect(INE_POSITION.size * 4)
        a.order(ByteOrder.nativeOrder())
        mVerBuffer = a.asFloatBuffer()
        mVerBuffer.put(INE_POSITION)
        mVerBuffer.position(0)

        val b = ByteBuffer.allocateDirect(LINE_COLOR.size * 4)
        b.order(ByteOrder.nativeOrder())
        mTexBuffer = b.asFloatBuffer()
        mTexBuffer.put(LINE_COLOR)
        mTexBuffer.position(0)
    }

    override fun onCreate() {
        createProgramByAssetsFile("shader/coordinate_vertex.glsl", "shader/coordinate_fragment.glsl")
    }

    override fun onSizeChanged(width: Int, height: Int) {

    }

    override fun createProgram(vertex: String?, fragment: String?) {
        super.createProgram(vertex, fragment)
        attributeColor = GLES20.glGetAttribLocation(mProgram, "aColor")
    }

    override fun onBindTexture() {

    }

    override fun onDraw() {
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, mVerBuffer)
        GLES20.glEnableVertexAttribArray(attributeColor)
        GLES20.glVertexAttribPointer(attributeColor, 4, GLES20.GL_FLOAT, false, 0, mTexBuffer)
        GLES20.glLineWidth(10f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 4)
        GLES20.glDisableVertexAttribArray(mHPosition)
        GLES20.glDisableVertexAttribArray(attributeColor)
    }
}