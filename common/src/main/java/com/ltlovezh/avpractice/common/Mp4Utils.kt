package com.anote.android.bach.common.utils

import android.annotation.TargetApi
import android.media.*
import android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM
import android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME
import android.media.MediaFormat.KEY_DURATION
import android.os.Build
import android.text.TextUtils
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Track
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import com.googlecode.mp4parser.authoring.tracks.AppendTrack
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack
import com.ltlovezh.avpractice.common.Logger
import com.ltlovezh.avpractice.common.VideoInfo
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import java.nio.ByteBuffer

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
object Mp4Utils {

    const val TAG = "Mp4Utils"
    const val PARAMS_ERROR = 1000
    const val INPUT_FILE_ERROR = 1100
    const val INPUT_FILE_NO_VIDEO_INFO_ERROR = 1200
    const val INPUT_FILE_NO_VIDEO_TRACK_ERROR = 1300
    const val UNKNOWN_ERROR = 1400
    const val JOIN_SUCCESS = 0

    /**
     * 只有标准的MP4才能合成
     */
    fun joinVideoByMp4Parser(inputPath: String, outputPath: String, duration: Long): Int {
        Logger.d(TAG, "joinVideoByMp4Parser inputPath : $inputPath, outputPath : $outputPath, duration : $duration")
        if (TextUtils.isEmpty(inputPath) || TextUtils.isEmpty(outputPath) || duration <= 0L) {
            return PARAMS_ERROR
        }

        try {
            val startTime = System.currentTimeMillis()

            val inputFile = File(inputPath)
            if (!inputFile.exists() || !inputFile.isFile) {
                return INPUT_FILE_ERROR
            }
            val videoInfo = getVideoInfo(inputPath) ?: return INPUT_FILE_NO_VIDEO_INFO_ERROR
            Logger.i(
                TAG,
                "videoInfo width : ${videoInfo.width}, height : ${videoInfo.height}, duration : ${videoInfo.duration}"
            )

            val repetition = duration / videoInfo.duration
            val remainder = duration % videoInfo.duration
            Logger.i(TAG, "repetition : $repetition, remainder : $remainder")

            val inputMovieList = mutableListOf<Movie>()
            for (index in 0L until repetition) {
                inputMovieList.add(MovieCreator.build(inputPath))
            }
            // 1.分离出完整视频
            val videoTracks = mutableListOf<Track>()
            for (movie in inputMovieList) {
                for (track in movie.tracks) {
                    if (track.handler == "vide") {
                        videoTracks.add(track)
                    }
                }
            }
            // 2.处理不完整视频
            if (remainder > 0L) {
                val croppedMovie = MovieCreator.build(inputPath)
                var croppedTrack: Track? = null
                for (track in croppedMovie.tracks) {
                    if (track.handler == "vide") {
                        croppedTrack = track
                    }
                }

                if (croppedTrack != null) {
                    val sampleDurations = croppedTrack.sampleDurations
                    val timeScale = croppedTrack.trackMetaData.timescale
                    // 找到截取的sample
                    var index = 0
                    var accumulativeDuration = 0L
                    while (accumulativeDuration * 1000 / timeScale < remainder && index < sampleDurations.size) {
                        accumulativeDuration += sampleDurations[index++]
                    }
                    Logger.i(
                        TAG,
                        "index : $index, accumulativeDuration : ${accumulativeDuration * 1000 / timeScale}, sample size : ${sampleDurations.size}, timeScale : $timeScale"
                    )
                    videoTracks.add(CroppedTrack(croppedTrack, 0, index.toLong()))
                }
            }

            val outputMovie = Movie()
            if (videoTracks.isNotEmpty()) {
                outputMovie.addTrack(AppendTrack(*videoTracks.toTypedArray()))
            }

            // 输出文件
            val outputFile = File(outputPath)
            if (outputFile.exists() && outputFile.isFile) {
                outputFile.delete()
            } else if (!outputFile.parentFile.exists()) {
                outputFile.parentFile.mkdirs()
            }

            val outputChannel = RandomAccessFile(outputFile, "rw").channel
            val outputContainer = DefaultMp4Builder().build(outputMovie)
            outputContainer.writeContainer(outputChannel)
            outputChannel.close()

            val cost = System.currentTimeMillis() - startTime
            Logger.i(TAG, "joinVideoByMp4Parser cost : $cost")
            return JOIN_SUCCESS
        } catch (e: Throwable) {
            Logger.e(TAG, e.toString())
            Logger.e(TAG, "joinVideoByMp4Parser error, try to joinVideoByMediaExtractor...")
            return joinVideoByMediaExtractor(inputPath, outputPath, duration)
        }
    }

    /**
     * 通过MediaExtractor合成
     */
    private fun joinVideoByMediaExtractor(inputPath: String, outputPath: String, duration: Long): Int {
        Logger.d(
            TAG,
            "joinVideoByMediaExtractor inputPath : $inputPath, outputPath : $outputPath, duration : $duration"
        )
        if (TextUtils.isEmpty(inputPath) || TextUtils.isEmpty(outputPath) || duration <= 0L) {
            return PARAMS_ERROR
        }

        try {
            val startTime = System.currentTimeMillis()

            val inputFile = File(inputPath)
            if (!inputFile.exists() || !inputFile.isFile) {
                return INPUT_FILE_ERROR
            }
            val videoInfo = getVideoInfo(inputPath) ?: return INPUT_FILE_NO_VIDEO_INFO_ERROR
            Logger.i(
                TAG,
                "videoInfo width : ${videoInfo.width}, height : ${videoInfo.height}, duration : ${videoInfo.duration}"
            )

            val repetition = duration / videoInfo.duration
            val remainder = duration % videoInfo.duration
            Logger.i(TAG, "repetition : $repetition, remainder : $remainder")

            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(inputPath)

            var oldVideoTrackIndex = selectTrack(videoExtractor, "video/")
            if (oldVideoTrackIndex < 0) {
                return INPUT_FILE_NO_VIDEO_TRACK_ERROR
            }
            videoExtractor.selectTrack(oldVideoTrackIndex)
            val videoFormat = videoExtractor.getTrackFormat(oldVideoTrackIndex)
            val frameRate = videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
            val frameInterval = 1000 * 1000 / frameRate
            Logger.i(TAG, "frameRate : $frameRate, pts interval : $frameInterval")
            // 修正持续时长
            videoFormat.setLong(KEY_DURATION, duration * 1000)

            // 启动合成
            val mediaMuxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val newVideoTrackIndex = mediaMuxer.addTrack(videoFormat)
            mediaMuxer.start()

            var maxPts = 0L
            val firstSampleTime = videoExtractor.sampleTime
            val sampleBuffer = ByteBuffer.allocate(1024 * 1000)
            for (index in 0 until repetition) {
                var frameNum = 0
                while (true) {
                    val sampleSize = videoExtractor.readSampleData(sampleBuffer, 0)
                    if (sampleSize < 0) {
                        break
                    }
                    val sampleTime = videoExtractor.sampleTime
                    if (index == 0L && sampleTime > maxPts) {
                        maxPts = sampleTime
                    }
                    val bufferInfo = MediaCodec.BufferInfo()
                    bufferInfo.size = sampleSize
                    bufferInfo.offset = 0
                    bufferInfo.flags = videoExtractor.sampleFlags
                    bufferInfo.presentationTimeUs = (sampleTime + index * maxPts + index * frameInterval)
//                    // 读取是按照DTS来读的，这里按照pts递增来输入，画面会抖动
//                    bufferInfo.presentationTimeUs = (firstSampleTime + frameNum * frameInterval + index * maxPts + index * frameInterval)
                    val iskeyFrame: Boolean = ((videoExtractor.sampleFlags and BUFFER_FLAG_KEY_FRAME) != 0)
                    val isEndFrame: Boolean = ((videoExtractor.sampleFlags and BUFFER_FLAG_END_OF_STREAM) != 0)

                    Logger.i(
                        TAG,
                        "index : $index, frameNum : ${frameNum++}, sampleTime : $sampleTime, presentationTimeUs : ${bufferInfo.presentationTimeUs}," +
                                " sampleSize : $sampleSize, iskeyFrame : $iskeyFrame, isEndFrame : $isEndFrame"
                    )

                    mediaMuxer.writeSampleData(newVideoTrackIndex, sampleBuffer, bufferInfo)
                    videoExtractor.advance()
                }
                // 重新定位到起始位置
                videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            }

            if (remainder > 0L) {
                var frameNum = 0
                // 重新定位到起始位置
                videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                while (true) {
                    val sampleSize = videoExtractor.readSampleData(sampleBuffer, 0)
                    if (sampleSize < 0) {
                        break
                    }
                    val sampleTime = videoExtractor.sampleTime
                    val bufferInfo = MediaCodec.BufferInfo()
                    bufferInfo.size = sampleSize
                    bufferInfo.offset = 0
                    bufferInfo.flags = videoExtractor.sampleFlags
                    bufferInfo.presentationTimeUs = (sampleTime + repetition * maxPts + repetition * frameInterval)
//                    bufferInfo.presentationTimeUs = (firstSampleTime + frameNum * frameInterval + repetition * maxPts + repetition * frameInterval)
                    val iskeyFrame: Boolean = ((videoExtractor.sampleFlags and BUFFER_FLAG_KEY_FRAME) != 0)
                    val isEndFrame: Boolean = ((videoExtractor.sampleFlags and BUFFER_FLAG_END_OF_STREAM) != 0)

                    Logger.i(
                        TAG,
                        "final index : $repetition, frameNum : ${frameNum++}, sampleTime : $sampleTime, presentationTimeUs : ${bufferInfo.presentationTimeUs}," +
                                " sampleSize : $sampleSize, iskeyFrame : $iskeyFrame, isEndFrame : $isEndFrame"
                    )

                    mediaMuxer.writeSampleData(newVideoTrackIndex, sampleBuffer, bufferInfo)
                    videoExtractor.advance()
                    // todo 这里firstSampleTime的使用，有待验证
                    if ((sampleTime - firstSampleTime) >= remainder * 1000) {
                        break
                    }
                }
            }

            videoExtractor.release()
            mediaMuxer.stop()
            mediaMuxer.release()
            val cost = System.currentTimeMillis() - startTime
            Logger.i(TAG, "joinVideoByMediaExtractor cost : $cost")

            return JOIN_SUCCESS
        } catch (e: Exception) {
            Logger.e(TAG, "joinVideoByMp4Parser error : " + e.toString())
            return UNKNOWN_ERROR
        }
    }

    fun selectTrack(extractor: MediaExtractor, mimePrefix: String): Int {
        val numTracks = extractor.trackCount

        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith(mimePrefix)) {
                return i
            }
        }

        return -1
    }

    fun getVideoInfo(inputPath: String): VideoInfo? {
        if (TextUtils.isEmpty(inputPath)) {
            return null
        }

        try {
            val videoInfo = VideoInfo()
            videoInfo.path = inputPath
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(inputPath)

            val rotation = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val videoFrameNum =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)

            // 主要信息
            videoInfo.width = if (width == null) {
                -1
            } else {
                Integer.parseInt(width)
            }
            videoInfo.height = if (height == null) {
                -1
            } else {
                Integer.parseInt(height)
            }
            videoInfo.duration = if (duration == null) {
                -1
            } else {
                Integer.parseInt(duration)
            }
            videoInfo.rotation = if (rotation == null) {
                -1
            } else {
                Integer.parseInt(rotation)
            }

            return videoInfo
        } catch (e: Exception) {
            Logger.e(TAG, e.toString())
        }

        return null
    }

}