package com.ltlovezh.avpractice.render.filter

import android.content.res.Resources
import android.opengl.GLES20
import android.os.Looper
import android.util.SparseArray
import com.ltlovezh.avpractice.common.Logger
import com.ltlovezh.avpractice.render.utils.MatrixUtils
import com.ltlovezh.avpractice.render.utils.OpenGlUtils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Arrays

/**
 * Description:
 */
abstract class AFilter(protected var mRes: Resources) {


    companion object {
        private val TAG = "BaseFilter"

        /**
         * 单位矩阵
         */
        val OM = MatrixUtils.getOriginalMatrix()
    }

    /**
     * 程序句柄
     */
    protected var mProgram: Int = 0
    /**
     * 顶点坐标句柄
     */
    protected var mHPosition: Int = 0
    /**
     * 纹理坐标句柄
     */
    protected var mHCoord: Int = 0
    /**
     * 总变换矩阵句柄
     */
    protected var mHMatrix: Int = 0
    /**
     * 默认纹理贴图句柄
     */
    protected var mHTexture: Int = 0

    /**
     * 顶点坐标Buffer
     */
    protected lateinit var mVerBuffer: FloatBuffer

    /**
     * 纹理坐标Buffer
     */
    protected lateinit var mTexBuffer: FloatBuffer

    /**
     * 索引坐标Buffer
     */

    var flag = 0

    var matrix = Arrays.copyOf(OM, 16)

    var textureType = 0      //默认使用Texture2D0
    var textureId = 0
    //顶点坐标
    private val pos = floatArrayOf(
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, 1.0f,
        1.0f, -1.0f
    )

    // 纹理坐标，该纹理坐标是绘制到FBO使用的，若是绘制到Android屏幕，则需要使用FinalFilter中的纹理坐标
    // 原因是Android屏幕的左上角是坐标原点，而绘制到FBO时，左下角是坐标原点，Y坐标正好是反过来的。

    // 纹理坐标原点在左下角
    //    private float[] coord = {
    //            0.0f, 1.0f,
    //            0.0f, 0.0f,
    //            1.0f, 1.0f,
    //            1.0f, 0.0f,
    //    };

    // 纹理坐标原点在左上角
    private val coord = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    )

    private var mBools: SparseArray<BooleanArray>? = null
    private var mInts: SparseArray<IntArray>? = null
    private var mFloats: SparseArray<FloatArray>? = null

    val outputTexture: Int
        get() = -1


    init {
        initBuffer()
    }

    fun create() {
        onCreate()
    }

    fun setSize(width: Int, height: Int) {
        onSizeChanged(width, height)
    }

    fun draw() {
        onClear()

        onUseProgram()

        onSetExpandData()

        onBindTexture()

        onDraw()
    }

    fun setFloat(type: Int, vararg params: Float) {
        if (mFloats == null) {
            mFloats = SparseArray()
        }
        mFloats!!.put(type, params)
    }

    fun setInt(type: Int, vararg params: Int) {
        if (mInts == null) {
            mInts = SparseArray()
        }
        mInts!!.put(type, params)
    }

    fun setBool(type: Int, vararg params: Boolean) {
        if (mBools == null) {
            mBools = SparseArray()
        }
        mBools!!.put(type, params)
    }

    fun getBool(type: Int, index: Int): Boolean {
        if (mBools == null) return false
        val b = mBools!!.get(type)
        return !(b == null || b.size <= index) && b[index]
    }

    fun getInt(type: Int, index: Int): Int {
        if (mInts == null) return 0
        val b = mInts!!.get(type)
        return if (b == null || b.size <= index) {
            0
        } else b[index]
    }

    fun getFloat(type: Int, index: Int): Float {
        if (mFloats == null) return 0f
        val b = mFloats!!.get(type)
        return if (b == null || b.size <= index) {
            0f
        } else b[index]
    }

    /**
     * 实现此方法，完成程序的创建，可直接调用createProgram来实现
     */
    protected abstract fun onCreate()

    protected abstract fun onSizeChanged(width: Int, height: Int)

    protected open fun createProgram(vertex: String?, fragment: String?) {
        mProgram = uCreateGlProgram(vertex, fragment)
        if (mProgram <= 0) {
            val info = "createProgram error , Program : $mProgram"
            Logger.e(TAG, info)
        }
        mHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mHCoord = GLES20.glGetAttribLocation(mProgram, "vCoord")
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        mHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
    }

    protected fun createProgramByAssetsFile(vertex: String, fragment: String) {
        createProgram(OpenGlUtils.uRes(vertex), OpenGlUtils.uRes(fragment))
    }

    /**
     * Buffer初始化
     */
    protected open fun initBuffer() {
        val a = ByteBuffer.allocateDirect(32)
        a.order(ByteOrder.nativeOrder())
        mVerBuffer = a.asFloatBuffer()
        mVerBuffer.put(pos)
        mVerBuffer.position(0)
        val b = ByteBuffer.allocateDirect(32)
        b.order(ByteOrder.nativeOrder())
        mTexBuffer = b.asFloatBuffer()
        mTexBuffer.put(coord)
        mTexBuffer.position(0)
    }

    protected fun onUseProgram() {
        GLES20.glUseProgram(mProgram)
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected open fun onDraw() {
        GLES20.glEnableVertexAttribArray(mHPosition)

        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, mVerBuffer)

        GLES20.glEnableVertexAttribArray(mHCoord)

        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(mHPosition)
        GLES20.glDisableVertexAttribArray(mHCoord)
    }

    /**
     * 清除画布
     */
    protected open fun onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        // 清除颜色和深度缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    /**
     * 设置其他扩展数据
     */
    protected fun onSetExpandData() {
        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, matrix, 0)
    }

    /**
     * 绑定默认纹理
     */
    protected open fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glUniform1i(mHTexture, textureType)
    }

    //创建GL程序
    fun uCreateGlProgram(vertexSource: String?, fragmentSource: String?): Int {
        val vertex = uLoadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertex == 0) return 0
        val fragment = uLoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragment == 0) return 0
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertex)
            GLES20.glAttachShader(program, fragment)
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                glError(2, "Could not link program : " + GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    /**
     * 加载shader
     */
    fun uLoadShader(shaderType: Int, source: String?): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (0 != shader) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                glError(
                    1,
                    "Could not compile shader : " + shaderType + ", error msg:" + GLES20.glGetShaderInfoLog(shader)
                )
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    fun glError(code: Int, msg: String) {
        val info = "glError : $code, msg : $msg"
        Logger.e(TAG, info)
    }

    /**
     * For GLSurfaceView
     * Note that when the EGL context is lost, all OpenGL resources associated
     * with that context will be automatically deleted. You do not need to call
     * the corresponding "glDelete" methods such as glDeleteTextures to
     * manually delete these lost resources.
     */
    fun onDestroy() {
        // OpenGL资源的释放,目前GLSurfaceView会自动释放OpenGL资源，不需要我们自己去释放,但是在渲染期间，替换Filter时，还是需要释放资源的。
        // 在非EGL线程释放OpenGL资源，在4.4的手机上居然会影响到硬件加速，真是奇葩...
        try {
            if (canGLDelete() && mProgram > 0) {
                GLES20.glDeleteProgram(mProgram)
                mProgram = 0
            }
        } catch (e: Exception) {
            Logger.e(TAG, e.toString())
        }

    }

    protected fun canGLDelete(): Boolean {
        val threadName = Thread.currentThread().name
        val mainThread = Looper.getMainLooper() == Looper.myLooper()
        Logger.i(TAG, "canGLDelete threadName : $threadName, mainThread : $mainThread")
        return threadName.toUpperCase().contains("GLTHREAD") || !mainThread
    }


}
