package com.jddz.testcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.bumptech.glide.Glide
import com.jddz.cameralibrary.activity.CameraActivity
import com.jddz.cameralibrary.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_main.*

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

        iv_main.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            val path = data?.getStringExtra("path")
            Glide.with(this)
                    .load(path)
                    .into(iv_main)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            0 -> grantResults.filter { it != PackageManager.PERMISSION_GRANTED }.also {
                if (it.isNotEmpty()) {
                    ToastUtil.show(this, it.toString())
                } else {
                    CameraActivity.startIntent(this, 0)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
