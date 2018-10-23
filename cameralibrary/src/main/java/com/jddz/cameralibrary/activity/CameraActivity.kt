package com.jddz.cameralibrary.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.jddz.cameralibrary.R
import com.jddz.cameralibrary.utils.FileUtils
import com.jddz.cameralibrary.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

/**
 * Created by cc on 2018/10/18.
 *
 * function : 主界面
 *
 */
class CameraActivity : AppCompatActivity() {


    companion object {
        fun startIntent(context: Activity, requestCode: Int) {
            context.startActivityForResult(Intent(context, CameraActivity::class.java), requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // 设置导航栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }


        // 设置预览界面布局宽高比为4:3
        cv_camera.layoutParams.also { param ->
            val heightPixels = resources.displayMetrics.heightPixels
            val widthPixels = resources.displayMetrics.widthPixels
            //
            val min = Math.min(heightPixels, widthPixels) * 4 / 3
            val max = Math.max(heightPixels, widthPixels)
            if (min != max) {
                param.width = min
                ll_camera_opera.layoutParams.width = max - min
            }
            cv_camera.layoutParams = param
        }

        iv_camera.setOnClickListener {
            cv_camera.takePicture(Camera.PictureCallback { p0, p1 ->
                val path = saveFile(p0)
                if (path == "") {
                    p1?.startPreview()
                } else {
                    val intent = Intent(this, ImageActivity::class.java)
                    intent.putExtra("path", path)
                    startActivityForResult(intent, 0)
                }
            })
        }
    }

    // 保存照片
    private fun saveFile(data: ByteArray?): String {
        val name = "${Date().time}.jpg"
        val file = FileUtils.createFile(arrayOf(FileUtils.getDirPath(),
                getString(R.string.save_path), getString(R.string.save_file)), name)

        return try {
            val outputStream = FileOutputStream(file)
            outputStream.write(data)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            ToastUtil.show(this, e.localizedMessage)
            ""
        }
    }

    override fun onPause() {
        super.onPause()
        cv_camera.stopPreview()
    }

    override fun onResume() {
        super.onResume()
        cv_camera.releaseCameraAndPreview(Camera.CameraInfo.CAMERA_FACING_BACK)
    }


    override fun onDestroy() {
        super.onDestroy()
        cv_camera.stopPreviewAndFreeCamera()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            setResult(resultCode, data)
            finish()
        }
    }
}
