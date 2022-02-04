/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import com.github.dr.rwserver.util.Time.concurrentMillis
import com.github.dr.rwserver.util.threads.ThreadFactoryName
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Dr
 */
internal class TimeoutDetection(s: Int, startNet: StartNet) {
    private val namedFactory = ThreadFactoryName.nameThreadFactory("TimeoutDetection-")
    private val Service: ScheduledExecutorService = ScheduledThreadPoolExecutor(1, namedFactory)
    private val scheduledFuture: ScheduledFuture<*> = Service.scheduleAtFixedRate(CheckTime(startNet), 0, s.toLong(), TimeUnit.SECONDS)

    private class CheckTime(private val startNet: StartNet) : Runnable {
        override fun run() {
            startNet.OVER_MAP.each { k: String, v: TypeConnect ->
                val con = v.abstractNetConnect
                if (checkTimeoutDetection(con)) {
                    con.disconnect()
                    startNet.OVER_MAP.remove(k)
                }
            }
        }
    }

    companion object {
        internal fun checkTimeoutDetection(abstractNetConnect: AbstractNetConnect?): Boolean {
            if (abstractNetConnect == null) {
                return true
            }

            return if (abstractNetConnect.inputPassword) {
                /* 60s无反应判定close */
                concurrentMillis() > abstractNetConnect.lastReceivedTime + 60 * 1000L
            } else {
                concurrentMillis() > abstractNetConnect.lastReceivedTime + 180 * 1000L
            }

        }
    }
}