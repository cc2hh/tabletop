package com.jddz.cameralibrary.view

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import com.jddz.cameralibrary.R
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Created by cc on 2018/10/17.
 *
 * function : 自定义聚焦展示view
 */
class FocusView : ImageView {

    // 获取图片失败
    private val noId = -1
    // 聚焦失败展示图片
    private var failImg = noId
    // 聚焦成功展示图片
    private var successImg = noId
    // 聚焦时展示图片
    private var focusImg = noId

    private var flowable: Flowable<Int>
    private var disposable: Disposable? = null
    private var mAnimation: Animation

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mAnimation = AnimationUtils.loadAnimation(context, R.anim.focusview_show)
        visibility = View.GONE

        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.FocusView)!!
        failImg = typedArray.getResourceId(R.styleable.FocusView_focus_fail_id, noId)
        successImg = typedArray.getResourceId(R.styleable.FocusView_focus_success_id, noId)
        focusImg = typedArray.getResourceId(R.styleable.FocusView_focus_focusing_id, noId)
        typedArray.recycle()

        // 三种状态图片不能为空
        if (failImg == noId || successImg == noId || focusImg == noId) {
            throw RuntimeException("animation is null")
        }

        flowable = Flowable.defer { Flowable.fromCallable { 1 } }
    }

    // 开始聚焦
    fun startFocus(point: Point) {
        val params = layoutParams as RelativeLayout.LayoutParams
        // 根据触摸点坐标设置聚焦view的位置
        params.leftMargin = point.x - width / 2
        params.topMargin = point.y - height / 2
        layoutParams = params

        visibility = View.VISIBLE
        setImageResource(focusImg)
        startAnimation(mAnimation)
        // 注销之前隐藏view任务
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
        // 延迟3s隐藏聚焦view
        disposable = flowable.delay(3, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    visibility = View.GONE
                    disposable?.dispose()
                }


    }

    // 聚焦结果回调
    fun onFocusCallBack(success: Boolean) {
        setImageResource(if (success) successImg else failImg)
        // 注销之前隐藏view任务
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }

        // 延迟1s隐藏view
        disposable = flowable.delay(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    visibility = View.GONE
                    disposable?.dispose()
                }
    }

}