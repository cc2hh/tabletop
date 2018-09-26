package com.jddz.cameralibrary

import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException
import java.util.jar.Attributes

/**
 * Created by cc on 2018/9/21.
 *
 * function :
 */
class CameraView : SurfaceView, SurfaceHolder.Callback {


    private lateinit var mHolder: SurfaceHolder
    private var mCamera: Camera? = null
    private var mSupportedPreviewSizes: MutableList<Camera.Size>? = null

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
//        setCamera()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        mCamera?.stopPreview()
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        mCamera?.apply {
            parameters?.also { param ->
                width
                height
                param.previewSize
                param.setPreviewSize(param.supportedPreviewSizes[0].width, param.supportedPreviewSizes[0].height)
                requestLayout()
                parameters = param
            }
            mCamera?.setPreviewDisplay(holder)
            startPreview()
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        safeCameraOpen(0)
        holder.apply { addCallback(this@CameraView) }
    }

    //    安全打开摄像头
    private fun safeCameraOpen(id: Int): Boolean {

        return try {
            releaseCameraAndPreview()
            val size = Camera.getNumberOfCameras()
            for (i in 0..size) {
                val cameraInfo = Camera.CameraInfo()
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        mCamera = Camera.open(i)
                        break
                    } catch (e: Exception) {
                        mCamera = null
                        return false
                    }

                }
            }
            true
        } catch (e: Exception) {
//            Log.e()
            e.printStackTrace()
            false
        }
    }

    //    重置摄像和预览
    private fun releaseCameraAndPreview() {

        setCamera(null)
        mCamera?.also { camera ->
            camera.release()
            mCamera = null
        }
    }


    //    设置Camera
    private fun setCamera(camera: Camera?) {

        if (mCamera == camera) {
            return
        }

        stopPreviewAndFreeCamera()

        mCamera = camera

        mCamera?.apply {
            mSupportedPreviewSizes = parameters.supportedPreviewSizes
            requestLayout()

            try {
                setPreviewDisplay(mHolder)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            startPreview()
        }
    }

    //    停止预览和释放Camera
    private fun stopPreviewAndFreeCamera() {
        mCamera?.apply {
            stopPreview()
            release()
            mCamera = null
        }
    }


    fun takePicture(callback: Camera.PictureCallback) {
        mCamera?.takePicture(null, null, callback)
    }

}