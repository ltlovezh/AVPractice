package com.anote.android.common.widget.image

import android.content.Context
import android.net.Uri
import com.facebook.common.memory.PooledByteBuffer
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imageformat.DefaultImageFormats
import com.facebook.imageformat.ImageFormat
import com.facebook.imagepipeline.image.EncodedImage
import com.facebook.imagepipeline.request.ImageRequest
import com.ltlovezh.avpractice.common.Logger
import com.ltlovezh.avpractice.common.thread.CommonExecutors

/**
 * Author : litao
 * Time : 2019/1/22 - 4:27 PM
 * Description : This is ImageFormat
 */
class ImageFormat(val context: Context) {
    companion object {
        const val TAG = "ImageFormat"
    }

    fun parseImageFormat(imageUri: Uri, imageFormatListener: ImageFormatListener) {
        val imageRequest = ImageRequest.fromUri(imageUri)
        val imagePipeline = Fresco.getImagePipeline()
        val dataSource = imagePipeline.fetchEncodedImage(imageRequest, context)

        if (dataSource.hasResult()) {
            judgeImageFormat(imageUri, dataSource.result, imageFormatListener)
        } else {
            dataSource.subscribe(object : BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
                override fun onFailureImpl(dataSource: DataSource<CloseableReference<PooledByteBuffer>>?) {
                    Logger.e(TAG, "onFailureImpl")
                    imageFormatListener.onImageFormat(imageUri, ImageFormat.UNKNOWN, true)
                }

                override fun onNewResultImpl(dataSource: DataSource<CloseableReference<PooledByteBuffer>>?) {
                    if (dataSource == null) {
                        imageFormatListener.onImageFormat(imageUri, ImageFormat.UNKNOWN, true)
                        return
                    }

                    judgeImageFormat(imageUri, dataSource.result, imageFormatListener)
                }
            }, CommonExecutors.limitedTreadPoolExecutor)
        }
    }

    private fun judgeImageFormat(
        imageUri: Uri,
        closeableReference: CloseableReference<PooledByteBuffer>?,
        imageFormatListener: ImageFormatListener
    ) {
        try {
            if (closeableReference == null || !closeableReference.isValid) {
                imageFormatListener.onImageFormat(imageUri, ImageFormat.UNKNOWN, true)
                return
            }

            val encodedImage = EncodedImage(closeableReference)
            encodedImage.parseMetaData()
            val imageFormat = encodedImage.imageFormat
            val staticImage = when (imageFormat) {
                DefaultImageFormats.GIF, DefaultImageFormats.WEBP_ANIMATED -> false
                else -> true
            }
            Logger.d(TAG, "imageUri : $imageUri, imageFormat : $imageFormat, staticImage : $staticImage")
            imageFormatListener.onImageFormat(imageUri, imageFormat, staticImage)
        } catch (e: Throwable) {
            Logger.e(TAG, "judgeImageFormat error : $e")
            imageFormatListener.onImageFormat(imageUri, ImageFormat.UNKNOWN, true)
        } finally {
            CloseableReference.closeSafely(closeableReference)
        }
    }

    interface ImageFormatListener {
        /**
         * imageFormat : 图片格式
         * staticImage ： true : 静图， false ：动图
         */
        fun onImageFormat(imageUri: Uri, imageFormat: ImageFormat, staticImage: Boolean)
    }

}