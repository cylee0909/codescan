package com.cylee.codescan

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Environment
import com.cylee.androidlib.base.BaseApplication
import com.cylee.codescan2.BuildConfig
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by cylee on 16/9/6.
 */
class App : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        redirectLog()
    }

    //测试包或者非release包将日志输出到文件中，方便查问题
    private fun redirectLog() {
        if (BuildConfig.DEBUG) {
            val defHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
                var osw: OutputStreamWriter? = null
                try {
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd/HH:mm:ss", Locale.CHINA)
                    val log = File(Environment.getExternalStorageDirectory(), "scan_crash.log")
                    if (!log.exists()) {
                        log.createNewFile()
                    }

                    osw = OutputStreamWriter(FileOutputStream(log, false))
                    var baos = ByteArrayOutputStream()
                    val pw = PrintWriter(baos)
                    pw.println("\n============================================================\n")
                    pw.println(simpleDateFormat.format(Date()))
                    ex.printStackTrace(pw)
                    pw.flush()
                    var content = baos.toString("utf-8")

                    var clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager;
                    var textCd = ClipData.newPlainText("crash_log",content)
                    clipboard.setPrimaryClip(textCd);

                    osw!!.write(content)
                    osw.flush()
                } catch (e: Exception) {
                } finally {
                    if (osw != null) {
                        try {
                            osw.close()
                        } catch (e: Exception) {
                        }

                    }
                }
                defHandler?.uncaughtException(thread, ex)
            }
        }
    }
}
