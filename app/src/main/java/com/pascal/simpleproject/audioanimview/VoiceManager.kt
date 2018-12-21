package com.pascal.simpleproject.audioanimview

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File

/**
 * Created by Pascal on 2018/11/16
 */
object VoiceManager {
    private var mediaPlayer: MediaPlayer? = null
    private var playingPath: String? = null
    private var isPausing: Boolean = false
    private var voicePlayListener: VedioPlayListener? = null

    @JvmOverloads
    fun voiceOnclick(context: Context, path: String, voicePlayListener: VedioPlayListener) {

        if (path != null && path.equals(playingPath)) {
            stopAllVoice()
        } else {
            stopAllVoice()
            VoiceManager.voicePlayListener = voicePlayListener //callback要在这里处理。前面的播放要回调后停止
            playVoice(context, path)
        }
    }


    fun voiceOnclickWithPause(context: Context, path: String, voicePlayListener: VedioPlayListener) {

        if (path != null && path.equals(playingPath)) {
            if(isPausing) {
                restartVoice()
            }else{
                pauseVoice()
            }
        } else {
            stopAllVoice()
            VoiceManager.voicePlayListener = voicePlayListener //callback要在这里处理。前面的播放要回调后停止
            playVoice(context, path)
        }
    }

    private fun playVoice(context: Context, path: String) {
        playingPath = path
        if (path.startsWith("http:") || path.startsWith("https:")) {
            playHttpVoice(path)
        } else {
            playFileVoice(context, path)
        }
    }

    private fun playHttpVoice(path: String) {
        mediaPlayer = MediaPlayer()
        if (mediaPlayer != null) {
            mediaPlayer?.setDataSource(path)
            mediaPlayer?.setOnPreparedListener {
                voicePlayListener?.onStart()
                mediaPlayer?.start()
            }
            mediaPlayer?.setOnCompletionListener {
                stopAllVoice()
            }
            mediaPlayer?.prepareAsync()
        }

    }

    private fun playFileVoice(context: Context, path: String) {
        var file = File(path)
        if (file.exists()) {
            val uri = Uri.fromFile(file)
            mediaPlayer = MediaPlayer.create(context, uri)
            if (mediaPlayer != null) {
                voicePlayListener?.onStart()
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    stopAllVoice()
                }
            }
        }
    }

    private fun pauseVoice(){
        isPausing = true
        voicePlayListener?.onPause()
        mediaPlayer?.pause()
    }

    private fun restartVoice(){
        isPausing = false
        voicePlayListener?.onReStart()
        mediaPlayer?.start()
    }


    @JvmOverloads
    fun stopAllVoice() {
        if (mediaPlayer != null) {
            if (mediaPlayer?.isPlaying != null && mediaPlayer?.isPlaying!!) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            voicePlayListener?.onStop()
            mediaPlayer = null
        }
        playingPath = null
    }

    interface VedioPlayListener {
        fun onStart()
        fun onPause()
        fun onReStart()
        fun onStop()
    }

}