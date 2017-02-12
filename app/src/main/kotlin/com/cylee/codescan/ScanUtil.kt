package com.cylee.codescan

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by cylee on 16/9/9.
 */
object ScanUtil {
    val format = SimpleDateFormat("yyyyMMddHHmmss")
    fun formatCurrent():String {
        return format.format(Date())
    }
}