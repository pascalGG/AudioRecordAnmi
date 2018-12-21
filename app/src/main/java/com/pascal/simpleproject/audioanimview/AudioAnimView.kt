package com.pascal.simpleproject.audioanimview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.pascal.simpleproject.R

/**
 * Created by Pascal on 2018/12/16
 */
class AudioAnimView(context: Context, attrs: AttributeSet) : View(context, attrs)  {
    private var linePaint: Paint = Paint()
    private var moveLinePaint:Paint = Paint()
    private var itemPaint:Paint = Paint()
    private var recordPostion = 0f //录音的步长
    private var playPostion = 0f //播放的步长
    private var mutableList = mutableListOf<Int>()
    private val RECORD_MODE = 0
    private val PLAY_MODE = 1
    private var DRAW_MODE = RECORD_MODE
    private var finishCallback: (() -> Unit) ?= null

    init {
        initAttrs(context,attrs)
        initPaint()
    }

    fun initPaint() {
        linePaint.color = lineColor
        linePaint.strokeWidth = lineWidth

        moveLinePaint.color = moveColor
        moveLinePaint.strokeWidth = moveWidth
        
        itemPaint.color = itemColor
        itemPaint.strokeWidth = itemWidth
    }

    private var lineColor: Int = 0
    private var lineWidth: Float = 0f
    private var moveColor: Int = 0
    private var moveWidth: Float = 0f
    private var itemColor: Int = 0
    private var itemWidth: Float = 0f
    private var horizontalLineOffset:Float = 0f
    private var step:Float = 0f


    private fun initAttrs(context: Context, attrs: AttributeSet){
        val typeArray = context.theme.obtainStyledAttributes(attrs,
                R.styleable.AudioAnimViewStyle, 0, 0)
        lineColor = typeArray.getColor(R.styleable.AudioAnimViewStyle_lineColor,Color.WHITE)
        lineWidth = typeArray.getDimension(R.styleable.AudioAnimViewStyle_lineWidth,3f)

        moveColor = typeArray.getColor(R.styleable.AudioAnimViewStyle_moveLineColor,Color.WHITE)
        moveWidth = typeArray.getDimension(R.styleable.AudioAnimViewStyle_moveLineWidth,3f)


        itemColor = typeArray.getColor(R.styleable.AudioAnimViewStyle_itemColor,Color.WHITE)
        itemWidth = typeArray.getDimension(R.styleable.AudioAnimViewStyle_itemWidth,6f)

        step = typeArray.getDimension(R.styleable.AudioAnimViewStyle_step,9f)
        horizontalLineOffset = typeArray.getDimension(R.styleable.AudioAnimViewStyle_horizontalLineOffset,0F)
    }
    override fun onDraw(canvas: Canvas?) {
        val viewHeight = height.toFloat()
        val viewWidth = width.toFloat()
        drawLine(canvas, viewHeight, viewWidth)
        when(DRAW_MODE){
            RECORD_MODE ->{
                drawMoveLine(canvas, viewHeight)
                drawItem(canvas, viewHeight)
            }
            PLAY_MODE ->{
                drawPlayMoveLine(canvas, viewHeight)
                drawPlayItem(canvas, viewHeight,viewWidth)
            }
        }
    }

    private fun drawItem(canvas: Canvas?, viewHeight: Float){
        for(i in mutableList.size-1 downTo 0){
            val totalStep = (mutableList.size - i ) * step
            canvas?.drawLine(recordPostion - totalStep, viewHeight/2 - mutableList[i],
                    recordPostion - totalStep, viewHeight/2 + mutableList[i], itemPaint)
        }
    }

    private fun drawMoveLine(canvas: Canvas?, viewHeight: Float) {
        canvas?.drawLine(recordPostion, 0f, recordPostion, viewHeight, moveLinePaint)
    }

    private fun drawLine(canvas: Canvas?, viewHeight: Float, viewWidth: Float) {
        canvas?.drawLine(0f, viewHeight / 2, viewWidth, viewHeight / 2, linePaint)
    }

    private fun drawPlayItem(canvas: Canvas?, viewHeight: Float, viewWidth: Float){

        for(i in 0 until  mutableList.size ){
            val totalStep = i * step
            canvas?.drawLine(viewWidth/2+playPostion + horizontalLineOffset+ totalStep, viewHeight/2 - mutableList[i],
                        viewWidth/2+playPostion + horizontalLineOffset + totalStep, viewHeight/2 + mutableList[i], itemPaint)
        }
    }

    private fun drawPlayMoveLine(canvas: Canvas?, viewHeight: Float) {
        canvas?.drawLine(width/2.toFloat()+horizontalLineOffset, 0f, width/2.toFloat()+horizontalLineOffset, viewHeight, moveLinePaint)
    }


    fun setFinishListener(finishCallback: (() -> Unit)){
        this.finishCallback = finishCallback
    }

    fun addData(item: Int) {
        DRAW_MODE = RECORD_MODE
        if (recordPostion < width/2)
            recordPostion += step
        mutableList.add(item)
        postInvalidate()
    }

    fun getData():MutableList<Int>{
        return mutableList
    }

    fun startPlay(){
        playPostion -= step
        DRAW_MODE = PLAY_MODE

        val totalStep = mutableList.size * step
        if(Math.abs(playPostion) > totalStep){
            finishCallback?.invoke()

            return
        }
        postInvalidate()
    }

    fun initData(list:MutableList<Int>){
        playPostion = 0f
        mutableList = list
        DRAW_MODE = PLAY_MODE
        postInvalidate()
    }

    fun resetPlay(){
        initData(mutableList)
    }

    fun clearData(){
        recordPostion = 0f
        DRAW_MODE = RECORD_MODE
        mutableList.clear()
        postInvalidate()
    }
}