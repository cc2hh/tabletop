package com.jddz.cameralibrary.utils

import android.content.Context
import android.util.DisplayMetrics


/**
 * Created by cc on 2018/10/18.
 *
 * function :
 */
class MeasureUtil {
    companion object {
        fun getScreenWH(context: Context): DisplayMetrics {
            return context.resources.displayMetrics
        }
    }
}