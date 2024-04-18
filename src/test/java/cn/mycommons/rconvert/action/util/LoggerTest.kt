package cn.mycommons.rconvert.action.util

import cn.mycommons.rconvert.util.LoggerImpl
import cn.mycommons.rconvert.util.logger
import kotlin.test.Test

class LoggerTest {

    val logger = logger(this)

    @Test
    fun test() {
        logger.info("home = ${LoggerImpl.home}")
    }
}