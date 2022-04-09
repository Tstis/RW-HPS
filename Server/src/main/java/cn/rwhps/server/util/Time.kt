/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Dr
 */
object Time {
    /** 高并发下的效率提升  */
    private val INSTANCE = CurrentTimeMillisClock()

    /**
     * @return 系统计时器的当前值，以纳秒为单位.
     */
    @JvmStatic
    fun nanos(): Long {
        return System.nanoTime()
    }

    /**
     * @return 当前时间与1970年1月1日午夜之间的差值（以毫秒为单位）.
     */
    @JvmStatic
    fun millis(): Long {
        return System.currentTimeMillis()
    }

    /**
     * @return 当前时间与1970年1月1日午夜之间的差值（以毫秒为单位）.
     */
    @JvmStatic
    fun concurrentMillis(): Long {
        return INSTANCE.now
    }

    /**
     * @return 当前时间与1970年1月1日午夜之间的差值（以秒为单位）.
     */
    @JvmStatic
    fun concurrentSecond(): Int {
        return (INSTANCE.now / 1000).toInt()
    }

    /**
     * 获取自上次以来经过的纳秒数
     * @param prevTime - 必须是纳秒
     * @return - 自prevTime以来经过的时间（以纳秒为单位）
     */
    fun getTimeSinceNanos(prevTime: Long): Long {
        return nanos() - prevTime
    }

    /**
     * 获取自上次以来经过的毫秒数
     * @param prevTime - 必须是毫秒
     * @return - 自prevTime以来经过的时间（以毫秒为单位）
     */
    @JvmStatic
    fun getTimeSinceMillis(prevTime: Long): Long {
        return millis() - prevTime
    }

    @JvmStatic
    fun getTimeFutureMillis(addTime: Long): Long {
        return millis() + addTime
    }

    /**
     * 获取JDK当前时间
     */
    @JvmStatic
    val utcMillis: Long get() {
        // 获取JDK当前时间
        val cal = Calendar.getInstance()
        // 取得时间偏移量
        val zoneOffset = cal[Calendar.ZONE_OFFSET]
        // 取得夏令时差
        val dstOffset = cal[Calendar.DST_OFFSET]
        // 从本地时间里扣除这些差量，即可以取得UTC时间
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset))
        return cal.timeInMillis
    }

    @JvmStatic
    fun getUtcMilliFormat(fot: Int): String {
        return format(utcMillis, fot)
    }

    private fun format(gmt: Long, fot: Int): String {
        val ft = arrayOf(
            "yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "dd-MM-yyyy HH:mm:ss",
            "MM-dd-yyyy HH:mm:ss"
        )
        return SimpleDateFormat(ft[fot]).format(Date(gmt))
    }

    private class CurrentTimeMillisClock() {
        @Volatile
        var now: Long
        protected fun scheduleTick() {
            ScheduledThreadPoolExecutor(1) { runnable: Runnable? ->
                val thread = Thread(runnable, "current-time-millis")
                thread.isDaemon = true
                thread
            }.scheduleAtFixedRate({ now = System.currentTimeMillis() }, 100, 100, TimeUnit.MILLISECONDS)
        }

        init {
            now = System.currentTimeMillis()
            scheduleTick()
        }
    }
}