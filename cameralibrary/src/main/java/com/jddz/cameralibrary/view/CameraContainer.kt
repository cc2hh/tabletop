package com.jddz.cameralibrary.view

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.jddz.cameralibrary.R
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.cameracontainer.view.*
import java.util.concurrent.TimeUnit

/**
 * Created by cc on 2018/10/17.
 *
 * function : 相机扩展布局
 */
class CameraContainer : RelativeLayout {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        View.inflate(context, R.layout.cameracontainer, this)

        CameraView
        var disposable: Disposable? = null
        val flowable = Flowable.defer { Flowable.fromCallable { 1 } }
                .delay(500, TimeUnit.MILLISECONDS)
        // 触摸监听
        setOnTouchListener { _, motionEvent ->
            when (motionEvent.action.and(MotionEvent.ACTION_MASK)) {
                MotionEvent.ACTION_DOWN ->
                    if (disposable?.isDisposed == false) {
                        disposable?.dispose()
                    }
                MotionEvent.ACTION_UP -> {
                    val point = Point(motionEvent.x.toInt(), motionEvent.y.toInt())
                    disposable = flowable.subscribe {
                        camera.autoFocus(point, Camera.AutoFocusCallback { bool, camera ->
                            System.out.println("setOnTouchListener----$bool")
                            // 手动聚焦则取消自动聚焦效果
                            camera.cancelAutoFocus()
                            focus.onFocusCallBack(bool)
                            disposable?.dispose()
                        })
                    }
                    focus.startFocus(point)
                }
            }
            true
        }
    }

    // 拍照
    fun takePicture(callback: Camera.PictureCallback) {
        camera.takePicture(callback)
    }

    //    停止预览
    fun stopPreview() {
        camera.stopPreview()
    }

    //    停止预览和释放Camera
    fun stopPreviewAndFreeCamera() {
        camera.stopPreviewAndFreeCamera()
    }

    //    重置摄像和预览
    fun releaseCameraAndPreview(id: Int) {
        camera.releaseCameraAndPreview(id)
    }

}