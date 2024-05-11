package cn.mycommons.rconvert.action

import kotlin.test.Test

class ConvertKitTest {

    @Test
    fun test() {
        listOf(
            "R.string.im_name",
            "com.bili.liba.R.string.im_name",
            "com.bili.libb.R.string.im_name",
            "com.bili.libc.R.string.im_name",

            "R.string.app_name",
            "com.bili.liba.R.string.app_name",
            "com.bili.libb.R.string.app_name",
            "com.bili.libc.R.string.app_name",

            "R.drawable.animationlist_tv_loading",
            "com.bili.liba.R.drawable.animationlist_tv_loading",
            "com.bili.libb.R.drawable.animationlist_tv_loading",
            "com.bili.libc.R.drawable.animationlist_tv_loading",
        ).forEach {
            val t = ConvertKit.findTarget(it)
            println("$it -> $t")
        }
    }
}