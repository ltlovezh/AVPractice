//package com.ltlovezh.avpractice.render.filter
//
//import com.ltlovezh.avpractice.render.common.TextureCoordMode
//import com.ltlovezh.avpractice.render.utils.MatrixUtils
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.FloatBuffer
//
///**
// * Author : litao
// * Time : 2019/1/11 - 5:19 PM
// * Description : This is BaseFilter
// */
//abstract class BaseFilter(var textureCoordMode: TextureCoordMode) {
//    companion object {
//        private val TAG = "BaseFilter"
//
//        /**
//         * 单位矩阵
//         */
//        val OM = MatrixUtils.getOriginalMatrix()
//
//        // 顶点坐标(坐标原点在左下角)
//        val VERTEX_POSITION = floatArrayOf(
//            -1.0f, 1.0f,
//            -1.0f, -1.0f,
//            1.0f, 1.0f,
//            1.0f, -1.0f
//        )
//
//        // 渲染窗口是屏幕时，纹理坐标原点在左上角
//        val VERTEX_TEXTURE_IN_SCREEN = floatArrayOf(
//            0.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 0.0f,
//            1.0f, 1.0f
//        )
//
//        // 渲染窗口是FBO时，纹理坐标原点在左下角
//        val VERTEX_TEXTURE_IN_FBO = floatArrayOf(
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f
//        )
//    }
//
//
//    // Program Handle
//    protected var programHandle: Int = 0
//    // vertex position handle
//    protected var aVertexPositionHandle: Int = 0
//    // vertex texture handle
//    protected var aVertexTextureHandle: Int = 0
//    // vertex matrix handle
//    protected var uVertexMatrixHandle: Int = 0
//    // texture handle
//    protected var uTextureHandle: Int = 0
//
//    // 顶点位置坐标Buffer
//    protected var vertexPositionBuffer: FloatBuffer? = null
//
//    // 顶点纹理坐标Buffer
//    protected var vertexTextureBuffer: FloatBuffer? = null
//
//
//    init {
//        initBuffer()
//    }
//
//
//    protected open fun initBuffer() {
//        val pBuffer = ByteBuffer.allocateDirect(VERTEX_POSITION.size * 4)
//        pBuffer.order(ByteOrder.nativeOrder())
//        vertexPositionBuffer = pBuffer.asFloatBuffer()
//        vertexPositionBuffer?.put(VERTEX_POSITION)
//        vertexPositionBuffer?.position(0)
//
//        val textureBuffer = when (textureCoordMode) {
//            TextureCoordMode.TextureCoordInScreen ->
//                VERTEX_TEXTURE_IN_SCREEN
//            else -> VERTEX_TEXTURE_IN_FBO
//        }
//        val tBuffer = ByteBuffer.allocateDirect(textureBuffer.size * 4)
//        tBuffer.order(ByteOrder.nativeOrder())
//        vertexTextureBuffer = tBuffer.asFloatBuffer()
//        vertexTextureBuffer?.put(textureBuffer)
//        vertexTextureBuffer?.position(0)
//    }
//
//
//}