package com.pascal.simpleproject.audioanimview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.pascal.simpleproject.R
import java.math.BigDecimal

/**
 * Created by Pascal on 2018/12/16
 */
class AudioItemAnimView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var itemPaint: Paint = Paint()
    private var playThread: Thread? = null
    private var isPlaying: Boolean = false
    private val drawCount = 30   //一次动画多少帧
    private val onceTime = 300 / drawCount //每一帧的时间
    private var patchValue = 2f//补充差值,UI给出的数值跟实际效果有一点的差距
    private var isUp = true
    private var itemColor: Int = 0
    private var itemWidth: Float = 0f
    private var step: Float = 0f
    private var audioWidth = 0
    private var data1: MutableList<Float> = mutableListOf(4f, 12f, 4f, 12f, 20f, 12f, 4f, 12f, 20f, 28f, 20f, 12f, 4f, 12f, 4f, 12f, 20f, 12f, 4f)
    private var data2: MutableList<Float> = mutableListOf(16f, 18f, 16f, 18f, 22f, 18f, 16f, 18f, 22f, 12f, 22f, 18f, 16f, 18f, 22f, 18f, 16f, 18f, 16f)
    private var drawData = mutableListOf<DrawBean>()  //播放的数组

    init {
        initAttrs(context, attrs)
        initPaint()
        initView()
    }

    private fun initPaint() {
        itemPaint.color = itemColor
        itemPaint.strokeWidth = itemWidth
    }

    private fun initAttrs(context: Context, attrs: AttributeSet) {
        val typeArray = context.theme.obtainStyledAttributes(attrs,
                R.styleable.AudioAnimViewStyle, 0, 0)
        itemColor = typeArray.getColor(R.styleable.AudioAnimViewStyle_itemColor, Color.WHITE)
        itemWidth = typeArray.getDimension(R.styleable.AudioAnimViewStyle_itemWidth, 6f)
        patchValue = typeArray.getFloat(R.styleable.AudioAnimViewStyle_patchValue, 3f)

        step = typeArray.getDimension(R.styleable.AudioAnimViewStyle_step, 9f)
    }

    override fun onDraw(canvas: Canvas?) {
        val viewHeight = height.toFloat()
        drawPlayItem(canvas, viewHeight)
    }

    private fun drawPlayItem(canvas: Canvas?, viewHeight: Float) {
        synchronized(drawData) {
            for (i in 0 until drawData.size) {
                val totalStep = (i + 1) * step
                if ((totalStep + step) > audioWidth) {
                    break
                }
                canvas?.drawLine(totalStep, viewHeight / 2 - drawData[i].drawData*patchValue ,
                        totalStep, viewHeight / 2 + drawData[i].drawData* patchValue, itemPaint)
                if (isUp) {
                    drawData[i].drawData = BigDecimal(drawData[i].drawData.toString()).add(BigDecimal(drawData[i].diff.toString())).toFloat()
                } else {
                    drawData[i].drawData = BigDecimal(drawData[i].drawData.toString()).subtract(BigDecimal(drawData[i].diff.toString())).toFloat()
                }
            }
        }
    }

    fun initView() {
        initDrawData()
        postInvalidate()
    }

    private fun initDrawData() {
        synchronized(drawData) {
            drawData.clear()
            var addFirstData = true
            while ((drawData.size * step) < audioWidth) {
                if (addFirstData) {
                    addData(data1, data2)
                } else {
                    addData(data2, data1)
                }
                addFirstData = !addFirstData
            }
        }
    }

    private fun addData(list: MutableList<Float>, minOrMax: MutableList<Float>) {
        if (list.size != minOrMax.size) {
            return
        }
        for (i in 0 until list.size) {
            val item = DrawBean(list[i], minOrMax[i], (minOrMax[i] - list[i]) / drawCount)
            drawData.add(item)
        }
    }

    private fun startDraw() {
        if (drawData.isNullOrEmpty()) {
            return
        }
        if (drawData[0].drawData <= data1[0]) {
            isUp = true
        } else if (drawData[0].drawData >= data2[0]) {
            isUp = false
        }
        postInvalidate()
    }

    fun startAnimiton() {
        initDrawData()
        isPlaying = true
        playThread = Thread(playRunnable)
        playThread?.start()
    }

    fun stopPlay() {
        isPlaying = false
    }

    private val playRunnable = Runnable {
        try {
            while (isPlaying) {
                startDraw()
                Thread.sleep(onceTime.toLong())
            }
            if (!isPlaying) {
                initView()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (audioWidth == 0) {
            audioWidth = measuredWidth
            initView()
        }
    }

    data class DrawBean(
            var drawData: Float,//播放的数组
            var minOrMax: Float,//存放 drawData 对应的最大值或最小值，单纯方便计算 diffData
            var diff: Float? //存放每一帧动画step
    )
}