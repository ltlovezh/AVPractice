package com.ltlovezh.avpractice.image.animated

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.facebook.animated.gif.GifFrame
import com.facebook.animated.webp.WebPFrame
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.fresco.animation.drawable.AnimatedDrawable2
import com.facebook.imagepipeline.animated.base.AnimatedImage
import com.facebook.imagepipeline.image.CloseableAnimatedImage
import com.facebook.imagepipeline.image.ImageInfo
import java.lang.RuntimeException
import com.facebook.animated.gif.GifImage
import com.facebook.drawee.interfaces.DraweeController
import com.ltlovezh.avpractice.common.Logger
import com.ltlovezh.avpractice.common.structure.LruLinkedHashMap

/**
 * Author : litao
 * Time : 2019/1/21 - 4:01 PM
 * Description : This is AnimatedImageExtractor
 * 提取gif和动态webp的图像帧
 */
class AnimatedImageExtractor(val context: Context) {

    companion object {
        const val TAG = "AnimatedImageExtractor"
    }

    private var imageUri: Uri? = null
    var imageFrameListener: ImageFrameListener? = null

    private val clearPaint: Paint = Paint()
    private var animatedBitmap: Bitmap? = null
    private var animatedCanvas: Canvas? = null

    private var globalAnimatedDrawable: AnimatedDrawable2? = null
    private var globalAnimatedImage: AnimatedImage? = null
    private var draweeController: DraweeController? = null

    private var lruLinkedHashMap = LruLinkedHashMap<String, Bitmap>(5)

    init {
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    /**
     * 必须在主线程调用
     */
    fun parse(uri: Uri) {
        mainThread(Runnable {
            imageUri = uri

            // 1.释放上一个图片资源
            destroy()
            // 2.加载动图资源
            loadUri()
        })
    }


    private fun loadUri() {
        val localDraweeController = Fresco.newDraweeControllerBuilder().setUri(imageUri)
            .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                    super.onFinalImageSet(id, imageInfo, animatable)
                    if (imageInfo is CloseableAnimatedImage && animatable is AnimatedDrawable2) { // 动图资源
                        globalAnimatedImage = imageInfo.image
                        globalAnimatedDrawable = animatable

                        Logger.d(
                            TAG,
                            "id : $id, animated type : ${if (globalAnimatedImage is GifImage) {
                                "GIF"
                            } else {
                                "WEBP"
                            }}, image width : ${imageInfo?.width}, height : ${imageInfo?.height}, " +
                                    "drawable width : ${animatable.intrinsicWidth}, height : ${animatable.intrinsicHeight}, " +
                                    "frameCount : ${globalAnimatedDrawable?.frameCount}"
                        )

                        imageFrameListener?.onImageParseSuccess()
                    } else {
                        imageFrameListener?.onImageParseFailure(
                            50,
                            "not animated image, imageInfo : $imageInfo, animatable : $animatable"
                        )
                    }
                }

                override fun onSubmit(id: String?, callerContext: Any?) {
                    super.onSubmit(id, callerContext)
                    Logger.d(TAG, "onSubmit, id : $id")
                }

                override fun onFailure(id: String?, throwable: Throwable?) {
                    super.onFailure(id, throwable)
                    Logger.e(TAG, "onFailure, id : $id, throwable : $throwable")
                    imageFrameListener?.onImageParseFailure(55, "onFailure, id : $id, throwable : $throwable")
                }

                override fun onRelease(id: String?) {
                    super.onRelease(id)
                    Logger.d(TAG, "onRelease, id : $id")
                }

            }).build()

        draweeController = localDraweeController
        localDraweeController.hierarchy = GenericDraweeHierarchyBuilder(context.resources).build()
        localDraweeController.onAttach()
    }


    fun isValid(): Boolean {
        return globalAnimatedImage != null && globalAnimatedDrawable != null
    }

    fun getWidth(): Int {
        return globalAnimatedImage?.width ?: -1
    }

    fun getHeight(): Int {
        return globalAnimatedImage?.height ?: -1
    }

    fun getFrameCount(): Int {
        return globalAnimatedImage?.frameCount ?: -1
    }

    fun getDuration(): Int {
        return globalAnimatedImage?.duration ?: -1
    }

    fun getFrameDurations(): IntArray? {
        return globalAnimatedImage?.frameDurations ?: null
    }

    /**
     * 获取某一完整帧的详细信息
     */
    fun getFullFrame(frameIndex: Int): ImageFrame? {
        val localAnimatedDrawable = globalAnimatedDrawable
        localAnimatedDrawable ?: return null
        val localAnimatedImage = globalAnimatedImage
        localAnimatedImage ?: return null
        val localAnimationBackend = localAnimatedDrawable.animationBackend
        localAnimationBackend ?: return null

        if (frameIndex in 0 until localAnimatedDrawable.frameCount) {
            if (animatedBitmap == null) {
                animatedBitmap = Bitmap.createBitmap(
                    localAnimatedImage.width,
                    localAnimatedImage.height,
                    Bitmap.Config.ARGB_8888
                )
                animatedCanvas = Canvas(animatedBitmap)
            } else {
                animatedCanvas?.drawPaint(clearPaint)
            }

            val frameDrawn =
                localAnimationBackend.drawFrame(localAnimatedDrawable, animatedCanvas, frameIndex)
            if (!frameDrawn) {
                throw RuntimeException("frameDrawn, index : $frameIndex")
                return null
            }

            val animatedFrame = localAnimatedImage.getFrame(frameIndex)
            val frameInfo = localAnimatedImage.getFrameInfo(frameIndex)

            return ImageFrame(
                animatedBitmap!!,
                animatedFrame.durationMs,
                frameInfo.width,
                frameInfo.height,
                frameInfo.xOffset,
                frameInfo.yOffset,
                when (animatedFrame) {
                    is GifFrame -> animatedFrame.disposalMode
                    is WebPFrame -> -1
                    else -> -1
                }
            )
        }

        return null
    }

    /**
     * 获取某一真实帧的详细信息
     */
    fun getRealFrame(frameIndex: Int): ImageFrame? {
        val localAnimatedDrawable = globalAnimatedDrawable
        localAnimatedDrawable ?: return null
        val localAnimatedImage = globalAnimatedImage
        localAnimatedImage ?: return null
        val localAnimationBackend = localAnimatedDrawable.animationBackend
        localAnimationBackend ?: return null

        if (frameIndex in 0 until localAnimatedImage.frameCount) {
            val animatedFrame = localAnimatedImage.getFrame(frameIndex)
            val frameInfo = localAnimatedImage.getFrameInfo(frameIndex)

            val cacheKey = getCacheKey(frameInfo.width, frameInfo.height)
            var bitmap = lruLinkedHashMap[cacheKey]
            if (bitmap == null || bitmap.isRecycled) {
                bitmap = Bitmap.createBitmap(frameInfo.width, frameInfo.height, Bitmap.Config.ARGB_8888)
                lruLinkedHashMap[cacheKey] = bitmap
            } else {
                bitmap.eraseColor(Color.TRANSPARENT)
            }

            animatedFrame.renderFrame(frameInfo.width, frameInfo.height, bitmap)

            return ImageFrame(
                bitmap!!,
                animatedFrame.durationMs,
                frameInfo.width,
                frameInfo.height,
                frameInfo.xOffset,
                frameInfo.yOffset,
                when (animatedFrame) {
                    is GifFrame -> animatedFrame.disposalMode
                    is WebPFrame -> -1
                    else -> -1
                }
            )
        }

        return null
    }

    private fun getCacheKey(width: Int, height: Int): String {
        return "$width-$height"
    }

    /**
     * 必须在主线程调用
     */
    fun destroy() {
        mainThread(Runnable {
            try {
                for ((key, value) in lruLinkedHashMap) {
                    if (value == null || value.isRecycled) {
                        continue
                    }
                    value.recycle()
                }
                globalAnimatedImage?.dispose()
                animatedBitmap?.recycle()
                draweeController?.onDetach()
                animatedBitmap = null
                globalAnimatedImage = null
            } catch (e: Throwable) {
                Logger.e(TAG, "destroy error : $e, Thread : ${Thread.currentThread().name}")
            }
        })
    }


    /**
     * 保证主线程调用
     */
    private fun mainThread(runnable: Runnable) {
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            runnable.run()
        } else {
            Handler(Looper.getMainLooper()).post(runnable)
        }
    }

    interface ImageFrameListener {
        fun onImageParseSuccess()
        // 错误码范围在50~69
        fun onImageParseFailure(error: Int, errorMsg: String)
    }

    data class ImageFrame(
        val bitmap: Bitmap,
        val DurationMs: Int,
        val frameWidth: Int,
        val frameHeight: Int,
        val frameXOffset: Int,
        val frameYOffset: Int,
        val disposalMethod: Int
    )
}