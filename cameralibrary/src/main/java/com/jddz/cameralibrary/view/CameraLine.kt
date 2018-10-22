package com.jddz.cameralibrary.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.jddz.cameralibrary.R

/**
 * Created by cc on 2018/10/18.
 *
 * function : 相机辅助线
 *
 */
class CameraLine : View {


    companion object {
        // 默认参考线的宽度
        const val strokeWidth = 1f
    }

    // 获取属性失败
    private val noId = -1
    // 画笔
    private var mPaint = Paint()
    // 显示、隐藏参考线，默认显示
    private var lineIsShow = true

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.CameraLine)!!
        val lineIsWide = typedArray.getBoolean(R.styleable.CameraLine_lineIsWide, false)
        lineIsShow = typedArray.getBoolean(R.styleable.CameraLine_lineIsShow, true)
        val lineColor = typedArray.getColor(R.styleable.CameraLine_lineColor, noId)
        val lineCrossColor = typedArray.getColor(R.styleable.CameraLine_lineCrossColor, noId)
        val lineWidth = typedArray.getDimension(R.styleable.CameraLine_lineWidth, noId.toFloat())
        val lineCrossWidth = typedArray.getDimension(R.styleable.CameraLine_lineCrossWidth, noId.toFloat())
        val lineCrossLength = typedArray.getDimension(R.styleable.CameraLine_lineCrossLength, noId.toFloat())

        typedArray.recycle()

        mPaint.apply {
            color = if (lineColor == noId) {
                ContextCompat.getColor(context, R.color.lineColor)
            } else {
                lineColor
            }
            strokeWidth = if (lineWidth == noId.toFloat()) {
                CameraLine.strokeWidth
            } else {
                lineWidth
            }
            // 抗锯齿
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (lineIsShow) {
//            val width = MeasureUtil.getScreenWH(context).widthPixels
//            val height = MeasureUtil.getScreenWH(context).heightPixels

            var itemW = width.toFloat() / 3
            var itemH = height.toFloat() / 3

            for (i in 1..2) {
                canvas?.drawLine(itemW * i, 0f, itemW * i, height.toFloat(), mPaint)
                canvas?.drawLine(0f, itemH * i, width.toFloat(), itemH * i, mPaint)
            }
        }
    }

}