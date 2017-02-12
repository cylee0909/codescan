package cn.csnbgsh.herbarium

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.view.View
import android.widget.Toast

/**
 * Created by cylee on 16/4/2.
 */
inline fun <T> Activity.bind(id:Int):T {
    return this.findViewById(id) as T
}

inline  fun <T> View.bind(id:Int):T {
    return this.findViewById(id) as T
}

inline fun toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(EX.context, message, length).show()
}

inline fun Number.dp2px():Int {
    return Math.round(EX.context.resources.displayMetrics.density * this.toInt())
}

object EX {
    lateinit var context:Context
}
