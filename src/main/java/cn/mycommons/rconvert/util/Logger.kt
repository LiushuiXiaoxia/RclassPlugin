package cn.mycommons.rconvert.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*


inline fun <reified T> logger(from: T): PluginLog {
    val log = LoggerFactory.getLogger(T::class.java)
    return LoggerImpl(log)
}

interface PluginLog {

    fun info(msg: String)

    fun error(msg: String, e: Throwable)

    fun println(msg: String)
}

class LoggerImpl(
    private val log: Logger
) : PluginLog {

    companion object {
        val home: File by lazy {
            File(System.getProperty("user.home"), "r-plugin").apply {
                mkdirs()
            }
        }

        private val logfile: File by lazy {
            File(home, "log-${SimpleDateFormat("yyyyMMdd.HHmmss").format(Date())}.txt")
        }

        private val logWriter: PrintWriter by lazy { PrintWriter(logfile) }

        fun setup() {

        }

        fun destroy() {
            logWriter.close()
        }
    }

    override fun println(msg: String) {
        info(msg)
    }

    override fun info(msg: String) {
        //  log.info(msg)

        kotlin.io.println(msg)

        val time = SimpleDateFormat("yyyyMMdd.HHmmss").format(Date())
        logWriter.println("$time I [${log.name}] $msg")
        logWriter.flush()
    }

    override fun error(msg: String, e: Throwable) {
        log.error(msg)

        System.err.println(msg)
        System.err.println(e)

        val time = SimpleDateFormat("yyyyMMdd.HHmmss").format(Date())
        logWriter.println("$time E [${log.name}]  $msg")
        logWriter.println("$time E [${log.name}]  $e")
        logWriter.flush()
    }
}