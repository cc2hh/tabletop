package com.jddz.testcamera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.WindowManager
import com.jddz.cameralibrary.utils.ToastUtil
import com.jddz.testcamera.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.sql.Date
import java.util.*

/**
 * Created by cc on 2018/10/18.
 *
 * function : 主界面
 *
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 设置导航栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)

        // 设置预览界面布局宽高比为4:3
        cv_main.layoutParams.also { param ->
            val heightPixels = resources.displayMetrics.heightPixels
            val widthPixels = resources.displayMetrics.widthPixels
            //
            val min = Math.min(heightPixels, widthPixels) * 4 / 3
            val max = Math.max(heightPixels, widthPixels)
            if (min != max) {
                param.width = min
                ll_main_opera.layoutParams.width = max - min
            }
            cv_main.layoutParams = param
        }

        iv_main.setOnClickListener {
            cv_main.takePicture(Camera.PictureCallback { p0, p1 ->
                val path = saveFile(p0)
                if (path == "") {
                    p1?.startPreview()
                } else {
                    val intent = Intent(this, ImageActivity::class.java)
                    intent.putExtra("path", path)
                    startActivity(intent)
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
        cv_main.stopPreview()
    }

    override fun onResume() {
        super.onResume()
        cv_main.releaseCameraAndPreview(Camera.CameraInfo.CAMERA_FACING_BACK)
    }


    override fun onDestroy() {
        super.onDestroy()
        cv_main.stopPreviewAndFreeCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            0 -> grantResults.filter { it != PackageManager.PERMISSION_GRANTED }.also {
                if (it.isNotEmpty()) {
                    ToastUtil.show(this, it.toString())
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
