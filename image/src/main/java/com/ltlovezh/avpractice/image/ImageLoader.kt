package com.anote.android.common.widget.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.image.CloseableAnimatedImage
import com.facebook.imagepipeline.image.CloseableBitmap
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import com.ltlovezh.avpractice.common.Logger
import com.ltlovezh.avpractice.common.thread.CommonExecutors

/**
 * Author : litao
 * Time : 2019/1/23 - 9:11 PM
 * Description : This is ImageLoader
 * 通过Fresco加载不同协议的图片，获取Bitmap
 */
class ImageLoader(val context: Context) {
    companion object {
        const val TAG = "ImageLoader"
    }

    fun loadImage(imageUri: Uri, imageLoaderListener: ImageLoaderListener) {
        val imageRequest = ImageRequest.fromUri(imageUri)
        val imagePipeline = Fresco.getImagePipeline()
        val dataSource = imagePipeline.fetchDecodedImage(imageRequest, context)

        if (dataSource.hasResult()) {
            copyBitmap(imageUri, dataSource.result, imageLoaderListener)
        } else {
            dataSource.subscribe(object : BaseDataSubscriber<CloseableReference<CloseableImage>>() {
                override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                    Logger.e(ImageFormat.TAG, "onFailureImpl")
                    imageLoaderListener.onLoadResult(imageUri, null)
                }

                override fun onNewResultImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                    if (dataSource == null) {
                        imageLoaderListener.onLoadResult(imageUri, null)
                        return
                    }

                    copyBitmap(imageUri, dataSource.result, imageLoaderListener)
                }
            }, CommonExecutors.limitedTreadPoolExecutor)
        }
    }

    private fun copyBitmap(
        imageUri: Uri,
        closeableReference: CloseableReference<CloseableImage>?,
        imageLoaderListener: ImageLoaderListener
    ) {
        try {
            if (closeableReference == null || !closeableReference.isValid) {
                imageLoaderListener.onLoadResult(imageUri, null)
                return
            }

            val closeableImage = closeableReference.get()
            if (closeableImage is CloseableBitmap && !closeableImage.underlyingBitmap.isRecycled) { // 静态图
                val originBitmap = closeableImage.underlyingBitmap
                val resultBitmap = originBitmap.copy(originBitmap.config, false) ?: with(originBitmap) {
                    val canvasBitmap =
                        Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(canvasBitmap)
                    canvas.drawBitmap(this, 0f, 0f, null)
                    canvasBitmap
                }

                imageLoaderListener.onLoadResult(imageUri, resultBitmap)
            } else if (closeableImage is CloseableAnimatedImage && closeableImage.image!!.frameCount >= 1) { // 动图首帧
                val animatedImageFrame = closeableImage.image!!.getFrame(0)
                val frameBitmap =
                    Bitmap.createBitmap(animatedImageFrame.width, animatedImageFrame.height, Bitmap.Config.ARGB_8888)
                animatedImageFrame.renderFrame(animatedImageFrame.width, animatedImageFrame.height, frameBitmap)

                imageLoaderListener.onLoadResult(imageUri, frameBitmap)
            } else {
                imageLoaderListener.onLoadResult(imageUri, null)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "copyBitmap error : $e")
            imageLoaderListener.onLoadResult(imageUri, null)
        } finally {
            CloseableReference.closeSafely(closeableReference)
        }
    }

    interface ImageLoaderListener {
        fun onLoadResult(imageUri: Uri, bitmap: Bitmap?)
    }
}