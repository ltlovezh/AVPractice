package com.ltlovezh.avpractice

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig
import com.facebook.imagepipeline.image.ImmutableQualityInfo
import com.facebook.imagepipeline.image.QualityInfo
import com.ltlovezh.avpractice.render.ContextHolder

/**
 * Author : litao
 * Time : 2019/1/6 - 8:56 PM
 * Description : This is BaseApplication
 */
class BaseApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ContextHolder.context = this
    }

    override fun onCreate() {
        super.onCreate()
        initFresco()
    }
    private fun initFresco() {

        val imagePipelineConfigBuilder = ImagePipelineConfig.newBuilder(this)
        imagePipelineConfigBuilder.setBitmapsConfig(Bitmap.Config.RGB_565) // 若不是要求忒高清显示应用，就用使用RGB_565吧（默认是ARGB_8888)
            .setDownsampleEnabled(true) // 在解码时改变图片的大小，支持PNG、JPG以及WEBP格式的图片，与ResizeOptions配合使用

            // 设置Jpeg格式的图片支持渐进式显示
            .setProgressiveJpegConfig(object : ProgressiveJpegConfig {
                override fun getNextScanNumberToDecode(scanNumber: Int): Int {
                    return scanNumber + 2
                }

                override fun getQualityInfo(scanNumber: Int): QualityInfo {
                    val isGoodEnough = scanNumber >= 5
                    return ImmutableQualityInfo.of(scanNumber, isGoodEnough, false)
                }
            })
            .build()
        val imagePipelineConfig = imagePipelineConfigBuilder.build()

        Fresco.initialize(this, imagePipelineConfig)
    }

}