package com.jddz.cameralibrary.utils

import android.content.Context
import android.widget.Toast

/**
 * Created by cc on 2018/9/27.
 *
 * function : toast工具类
 */

class ToastUtil {
    companion object {
        fun show(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
