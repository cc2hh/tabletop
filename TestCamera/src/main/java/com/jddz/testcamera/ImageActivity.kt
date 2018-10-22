package com.jddz.testcamera

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.jddz.testcamera.utils.FileUtils
import kotlinx.android.synthetic.main.activity_image.*

/**
 * Created by cc on 2018/10/18.
 *
 * function : 拍照返回的结果界面
 *
 */
class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val path = intent.getStringExtra("path")
        Glide.with(this)
                .load(path)
                .into(iv_image)
        btn_image_cancel.setOnClickListener {
            FileUtils.deleteFile(path)
            finish()
        }
        btn_image_save.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("path", path))
            finish()
        }
    }
}
