package com.oshurmamadov.data.common

import android.media.MediaMetadataRetriever
import com.oshurmamadov.domain.util.OSEnvironment
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler
import nl.bravobit.ffmpeg.FFmpeg
import okhttp3.ResponseBody
import java.io.File

/**
 * Data level extension class
 */

fun ResponseBody.convertBodyToString(): String {
    return this.byteStream().bufferedReader().use { it.readText()}
}

fun String.decodeUriComponent(decode: (url: String) -> String): String {
    return decode(this)
}

fun MediaMetadataRetriever.getVideoDuration(url: String): Pair<String, String> {
    var result = Pair("", "") //TODO think about it
    try {
        this.setDataSource(url, HashMap<String, String>())
        val duration = this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val mimeType = this.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).split("/")[1] // TODO check for jush_null
        System.out.println("MediaMetadataRetriever videoDuration: $duration  mimeType: $mimeType")

        result = Pair(duration, mimeType)
    }catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

fun File.getOutputMediaFile(environment: OSEnvironment, format: String): File? {
    val mediaStorageDir = environment.getExternalStoragePublicDirectory()
    if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null
    }
    return File(mediaStorageDir.path + File.separator + this.path + "." + format)
}

fun FFmpeg.basicInit() {
    try {
//        this.loadBinary(object : LoadBinaryResponseHandler(){
//            override fun onSuccess() {
//                super.onSuccess()
//                System.out.println("FFMPEG init success")
//            }
//
//            override fun onFailure() {
//                super.onFailure()
//                System.out.println("FFMPEG init failure")
//            }
//        })
        if (isSupported)
            System.out.println("FFMPEG init success")
        else
            System.out.println("FFMPEG init failure")

    }catch (e : java.lang.Exception) {
        e.printStackTrace()
    }
}

fun FFmpeg.trimVideo(start: String, end: String, videoFile: File, mFormat: String): String {
    var status = EMPTY
    val format = ".$mFormat"
    val newVideoFilePath = videoFile.parent + File.separator + videoFile.name.replace(format, EMPTY) +
            DEFAULT_VIDEO_NAME_SUFFIX + format
//    val cmd = arrayOf(
//            FFMPEG_CMD_SS,
//            start,
//            FFMPEG_CMD_I,
//            videoFile.absolutePath,
//            FFMPEG_CMD_TO,
//            end,
//            "-c",//FFMPEG_CMD_C,
//            FFMPEG_CMD_COPY,
//            newVideoFilePath)
    val cmd = arrayOf("-i", videoFile.absolutePath, "-ss", start, "-t", end, "-async", "1", newVideoFilePath)
  //  try {
        execute(cmd, object : ExecuteBinaryResponseHandler() {

            override fun onStart() {
                System.out.println("FFMPEG trimming onStart")
            }

            override fun onProgress(message: String?) {}

            override fun onFailure(message: String?) {
                System.out.println("FFMPEG trimming failure " + message)
                status = EMPTY
            }

            override fun onSuccess(message: String?) {
                System.out.println("FFMPEG trimming success")
                status = newVideoFilePath
            }

            override fun onFinish() {
                System.out.println("FFMPEG trimming onFinish")
            }
        })
//    } catch (e: Exception) {
//        e.printStackTrace()
//        status = EMPTY
//    }
    return status
}
