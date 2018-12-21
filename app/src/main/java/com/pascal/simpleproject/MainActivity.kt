package com.pascal.simpleproject

import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.pascal.simpleproject.audioanimview.VoiceManager
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

/**
 * Created by Pascal on 2018/12/21
 */
class MainActivity:AppCompatActivity(){
    private var recordThread:Thread ?= null
    private var playThread:Thread ?= null
    private var isRecording:Boolean = false
    private var isPlay:Boolean = false
    private var mMediaRecorder: MediaRecorder?=null
    private var mFileName: String = "friend_record_voice.aac"
    private var mFileDir = Environment.getExternalStorageDirectory().absolutePath + "/Afriend"
    private var mFilePath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        record.setOnClickListener{
            if(!isRecording){
                requstPermisson()
                record.text = "暂停录音"
            }else{
                stopRecord()
                audioAnimView.resetPlay()
                record.text = "录音"
            }

        }
        play.setOnClickListener{
            mFilePath?.run {
                VoiceManager.stopAllVoice()
                audioAnimView.resetPlay()
                VoiceManager.voiceOnclick(this@MainActivity,this, object :VoiceManager.VedioPlayListener{
                    override fun onStart() {
                        isPlay = true
                        playThread = Thread(playRunable)
                        playThread?.start()
                    }

                    override fun onPause() {
                    }

                    override fun onReStart() {
                    }

                    override fun onStop() {
                        isPlay = false
                        audioAnimView.resetPlay()
                    }
                })
            }
        }
    }


    private fun requstPermisson() {
        if (AndPermission.hasPermissions(this, Permission.RECORD_AUDIO, Permission.WRITE_EXTERNAL_STORAGE)) {
            startRecord()
        }else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.RECORD_AUDIO, Permission.WRITE_EXTERNAL_STORAGE)
                    .onGranted {
                    }.onDenied {
                        Toast.makeText(this,"请允许地址授权",Toast.LENGTH_LONG).show()
                    }.start()
        }
    }

    private fun startRecord() {
        audioAnimView.clearData()
        recordThread = Thread(recordRunable)
        isRecording = true
        prepareVideoRecorder()
        audioAnimView.setFinishListener {
            isPlay = false
        }
        recordThread?.start()
    }
    private fun stopRecord() {
        if (!isRecording)
            return
        isRecording = false
        mMediaRecorder ?.run {
            this.reset()   // clear recorder configuration
            this.release() // release the recorder object
        }
    }

    private fun prepareVideoRecorder(): Boolean {
        setFileNameAndPath()

        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.run {
            this.setAudioSource(MediaRecorder.AudioSource.MIC)
            this.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            this.setOutputFile(mFilePath)
            this.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            this.setAudioChannels(1)
            this.setAudioSamplingRate(44100)
            this.setAudioEncodingBitRate(192000)
            try {
                this.prepare()
                this.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    private fun setFileNameAndPath() {
        var file = File(mFileDir)
        if (!file.exists())
            file.mkdirs()
        mFilePath = "$mFileDir/$mFileName"
        file = File(mFilePath)
        file.createNewFile()
    }

    private val recordRunable = Runnable {
        try {
            while (isRecording) {
                audioAnimView.addData(getDecibel(mMediaRecorder))
                Thread.sleep(30)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val playRunable = Runnable {
        try {
            while (isPlay) {
                audioAnimView.startPlay()
                Thread.sleep(30)
            }
            playThread?.interrupt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /**
     * 获取分贝
     * */
    private fun getDecibel(mMediaRecorder: MediaRecorder?): Int {
        mMediaRecorder?.run {
            val ratio = mMediaRecorder.maxAmplitude.toDouble()
            var db = 0.0
            if (ratio > 1)
                db = 20 * Math.log10(ratio)
            if(db <= 0){
                db = 45.0
            }
            return Math.abs((db.toInt() -40)*3)
        }
        return 30
    }
}