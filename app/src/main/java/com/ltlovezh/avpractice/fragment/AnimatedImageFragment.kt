package com.ltlovezh.avpractice.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ltlovezh.avpractice.R
import com.ltlovezh.avpractice.common.FileManager
import com.ltlovezh.avpractice.common.Logger
import com.ltlovezh.avpractice.common.bitmap.BitmapUtil
import com.ltlovezh.avpractice.image.animated.AnimatedImageExtractor
import kotlinx.android.synthetic.main.animated_image_fragment_layout.*
import java.io.File

/**
 * Author : litao
 * Time : 2019/1/28 - 3:00 PM
 * Description : This is AnimatedImageFragment
 */
class AnimatedImageFragment : BaseFragment() {

    companion object {
        const val TAG = "AnimatedImageFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.animated_image_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        test_full_frame.setOnClickListener {
            var animatedImageExtractor = AnimatedImageExtractor(context!!)
            animatedImageExtractor.imageFrameListener = object : AnimatedImageExtractor.ImageFrameListener {
                override fun onImageParseSuccess() {
                    Logger.d(TAG, "onImageParseSuccess")
                    for (index in 0 until animatedImageExtractor.getFrameCount()) {
                        val imageFrame = animatedImageExtractor.getFullFrame(index)
                        imageFrame ?: continue
                        Logger.d(
                            TAG,
                            "frameIndex : $index, frameDuration : ${imageFrame.DurationMs}, " +
                                    "frameWidth : ${imageFrame.frameWidth}, frameHeight : ${imageFrame.frameHeight}, " +
                                    "xOffset : ${imageFrame.frameXOffset}, yOffset : ${imageFrame.frameYOffset}, " +
                                    "disposalMethod : ${imageFrame.disposalMethod}, Thread : ${Thread.currentThread().name}"
                        )

                        val savePath = "${FileManager.getPictureDir(context!!)}${File.separator}full_frame_$index.jpg"

                        BitmapUtil.saveBitmapToFile(imageFrame.bitmap, savePath)
                    }

                    animatedImageExtractor.destroy()
                }

                override fun onImageParseFailure(error: Int, errorMsg: String) {
                    Logger.d(TAG, "onImageParseFailure")
                }
            }

            animatedImageExtractor.parse(Uri.parse("https://frescolib.org/static/sample-images/fresco_logo_anim_full_frames_with_pause_m.gif"))
        }

        test_real_frame.setOnClickListener {
            var animatedImageExtractor = AnimatedImageExtractor(context!!)
            animatedImageExtractor.imageFrameListener = object : AnimatedImageExtractor.ImageFrameListener {
                override fun onImageParseSuccess() {
                    Logger.d(TAG, "onImageParseSuccess")
                    for (index in 0 until animatedImageExtractor.getFrameCount()) {
                        val imageFrame = animatedImageExtractor.getRealFrame(index)
                        imageFrame ?: continue
                        Logger.d(
                            TAG,
                            "frameIndex : $index, frameDuration : ${imageFrame.DurationMs}, " +
                                    "frameWidth : ${imageFrame.frameWidth}, frameHeight : ${imageFrame.frameHeight}, " +
                                    "xOffset : ${imageFrame.frameXOffset}, yOffset : ${imageFrame.frameYOffset}, " +
                                    "disposalMethod : ${imageFrame.disposalMethod}, Thread : ${Thread.currentThread().name}"
                        )

                        val savePath = "${FileManager.getPictureDir(context!!)}${File.separator}real_frame_$index.jpg"

                        BitmapUtil.saveBitmapToFile(imageFrame.bitmap, savePath)
                    }

                    animatedImageExtractor.destroy()
                }

                override fun onImageParseFailure(error: Int, errorMsg: String) {
                    Logger.d(TAG, "onImageParseFailure")
                }
            }

            animatedImageExtractor.parse(Uri.parse("https://frescolib.org/static/sample-images/fresco_logo_anim_full_frames_with_pause_m.gif"))
        }
    }

}