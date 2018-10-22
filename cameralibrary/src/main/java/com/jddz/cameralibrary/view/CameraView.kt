package com.jddz.cameralibrary.view

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.Rect
import android.hardware.Camera
import android.util.AttributeSet
import android.view.OrientationEventListener
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.jddz.cameralibrary.utils.ToastUtil
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
*  相机基础类
* @Author cc
* @Date 2018/10/22 15:26
* @version 1.0
*/
class CameraView : SurfaceView, SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private var cameraInfo: Camera.CameraInfo? = null
    private var mCamera: Camera? = null
    // 屏幕旋转角度
    private var mOrientation = 0
    // 记录预览状态
    var isPreview = false
    private lateinit var flowCamera: Flowable<Camera>
    private var disCamera: Disposable? = null
    private var orientationEventListener: OrientationEventListener? = null

    companion object {
        // 屏幕抖动平稳角度
        private const val SHAKE_ANGLE = 4
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        if (safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_BACK)) {
            holder.apply { addCallback(this@CameraView) }
            startOperation()
            flowCamera = Flowable.defer {
                Flowable.fromCallable {
                    mCamera?.apply {
                        parameters?.also { param ->
                            var tempW = 0
                            param.supportedPreviewSizes.forEach {
                                // 4:3比例且最大预览宽度
                                if (it.height.toFloat() / it.width.toFloat() == 0.75f && it.width > tempW) {
                                    tempW = it.width
                                    param.setPreviewSize(it.width, it.height)
                                }
                            }

                            param.supportedPictureSizes.forEach {
                                if (it.width > width && it.height.toFloat() / it.width.toFloat() == 0.75f) {
                                    param.setPictureSize(it.width, it.height)
                                }
                            }

                            param.pictureFormat = ImageFormat.JPEG
                            param.jpegQuality = 100
                            param.jpegThumbnailQuality = 80
                            param.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE

                            requestLayout()
                            parameters = param
                        }
                    }
                }
            }

        }
    }


    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        System.out.println("surfaceChanged")
        stopPreview()
        setParams()
        startPreview()
        isPreview = true
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        System.out.println("surfaceDestroyed")
        stopPreview()
        isPreview = false
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        System.out.println("surfaceCreated")
        setParams()
        startPreview()
        isPreview = true
    }

    override fun onAutoFocus(p0: Boolean, p1: Camera?) {
        mCamera?.cancelAutoFocus()
    }

    // 设置相机默认参数
    private fun setParams() {
        disposable(disCamera)
        disCamera = flowCamera
                .subscribe {
                    disposable(disCamera)
                }
        orientationEventListener?.enable()
    }

    // 自动聚焦
    fun autoFocus(point: Point, autoFocusCallback: Camera.AutoFocusCallback) {
        System.out.println("autoFocus----$point------$isPreview")
        if (isPreview) {
            mCamera?.apply {
                parameters?.also { param ->
                    // 不支持自定义FocusAreas，则使用自动聚焦
                    if (param.maxNumFocusAreas <= 0) {
                        autoFocus(autoFocusCallback)
                        return@apply
                    }

                    val areas = ArrayList<Camera.Area>()
                    var left = point.x / 2 - 300
                    var top = point.y / 2 - 300
                    var right = point.x / 2 + 300
                    var bottom = point.y / 2 + 300
                    left = if (left < -1000) -1000 else left
                    top = if (top < -1000) -1000 else top
                    right = if (right > 1000) 1000 else right
                    bottom = if (bottom > 1000) 1000 else bottom
                    areas.add(Camera.Area(Rect(left, top, right, bottom), 100))
                    param.focusAreas = areas
                    parameters = param
                }

                autoFocus(autoFocusCallback)
            }
        }
    }


    // 监听屏幕旋转角度
    private fun startOperation() {
        System.out.println("startOperation")
        var disposable: Disposable? = null
        // 之前聚焦时的屏幕角度
        var oldRotation = 0
        // 上一次获取的旋转角度
        var lastRotation = 0
        val flowable = Flowable.defer { Flowable.fromCallable { 1 } }
                .delay(1000, TimeUnit.MILLISECONDS)
        orientationEventListener =
                object : OrientationEventListener(context) {
                    override fun onOrientationChanged(rotation: Int) {
                        when (rotation) {
                            in 46..135 -> mOrientation = 90
                            in 136..225 -> mOrientation = 180
                            in 226..315 -> mOrientation = 270
                        }
                        // 屏幕目前抖动角度
                        val sub = Math.abs(lastRotation - rotation)
                        // 记录上一次获取的旋转角度
                        lastRotation = rotation
                        // 屏幕抖动范围在4度以下认为保持平稳，则进行聚焦。
                        if (sub in 2..SHAKE_ANGLE) {
                            val autoRotation = Math.abs(oldRotation - rotation)
                            // 自动聚焦需要旋转的最小角度为5
                            if (autoRotation > SHAKE_ANGLE) {
                                // 重置聚焦时的屏幕角度
                                oldRotation = rotation
                                disposable(disposable)
                                disposable = flowable.subscribe {
                                    autoFocus(Point(width, height), this@CameraView)
                                }
                            }

                        } else if (sub > SHAKE_ANGLE) {
                            // 不平稳则取消之前聚焦请求
                            disposable(disposable)
                        }
                    }
                }
    }

    // 取消订阅
    private fun disposable(disposable: Disposable?) {
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
    }

    // 设置图片保存旋转角度
    private fun setPictureRotation() {
        mCamera?.apply {
            parameters.also { param ->
                var orientation = (cameraInfo?.orientation?.plus(mOrientation + 360))?.rem(360)
                param.setRotation(orientation ?: 0)
                parameters = param
            }
        }
    }


    //    安全打开摄像头
    private fun safeCameraOpen(id: Int): Boolean {

        stopPreviewAndFreeCamera()

        return try {
            val size = Camera.getNumberOfCameras()
            for (i in 0 until size) {
                cameraInfo = Camera.CameraInfo()
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo?.facing == id) {
                    try {
                        mCamera = Camera.open(i)
                        break
                    } catch (e: Exception) {
                        mCamera = null
                        e.printStackTrace()
                        ToastUtil.show(context, e.localizedMessage)
                        false
                    }

                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.show(context, e.localizedMessage)
            false
        }
    }

    //    重置摄像和预览
    fun releaseCameraAndPreview(id: Int) {
        System.out.println("releaseCameraAndPreview")
        safeCameraOpen(id)
        setParams()
        startPreview()
        isPreview = true
    }

    // 开启预览
    private fun startPreview() {
        mCamera?.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
                isPreview = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //    停止预览
    fun stopPreview() {
        mCamera?.apply {
            orientationEventListener?.disable()
            stopPreview()
            isPreview = false
        }
    }

    //    停止预览和释放Camera
    fun stopPreviewAndFreeCamera() {
        mCamera?.apply {
            orientationEventListener?.disable()
            stopPreview()
            isPreview = false
            release()
            mCamera = null
        }
    }


    // 拍照
    fun takePicture(callback: Camera.PictureCallback) {
        setPictureRotation()
        mCamera?.takePicture(null, null, callback)
    }


}