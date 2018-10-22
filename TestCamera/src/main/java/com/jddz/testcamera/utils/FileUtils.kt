package com.jddz.testcamera.utils

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Created by cc on 2018/10/18.
 *
 * function :
 */
class FileUtils {

    companion object {

        /**
         * 获取缓存路径
         *
         * @param context 当前活动
         * @return 缓存路径
         */
        fun getCacheDirPath(context: Context): String {
            return if (Environment.MEDIA_MOUNTED == Environment
                            .getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                context.externalCacheDir.path
            } else {
                context.cacheDir.path
            }
        }

        /**
         * 获取存储根文件
         *
         * @return 缓存路径
         */
        fun getDirPath(): String {
            return if (Environment.MEDIA_MOUNTED == Environment
                            .getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                Environment.getExternalStorageDirectory().absolutePath
            } else {
                ""
            }
        }

        /**
         *  创建路径
         */
        fun createPath(filePath: Array<String>): String {
            val path = StringBuilder("")
            for (i in filePath.indices) {
                path.append(filePath[i]).append(File.separator)
                val temp = File(path.toString())
                // 判断路径存在否
                if (!temp.exists()) {
                    temp.mkdirs()
                }
            }
            return path.toString()
        }

        /**
         *  创建文件
         */
        fun createFile(path: Array<String>, name: String): File {
            val file = File(createPath(path) + name)
            if (!file.exists()) {
                file.createNewFile()
            }
            return file
        }

        /**
         * 删除文件
         *
         */
        fun deleteFile(path: String): Boolean {
            if (path == null || path.trim().isEmpty()) {
                return true
            }
            val file = File(path)
            if (!file.exists()) {
                return true
            }
            if (file.isFile) {
                return file.delete()
            }
            if (!file.isDirectory) {
                return false
            }
            for (f in file.listFiles()!!) {
                if (f.isFile) {
                    f.delete()
                } else if (f.isDirectory) {
                    deleteFile(f.absolutePath)
                }
            }
            return file.delete()
        }
    }
}
