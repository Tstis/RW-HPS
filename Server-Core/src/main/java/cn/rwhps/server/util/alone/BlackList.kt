/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.alone

import cn.rwhps.server.core.thread.CallTimeTask
import cn.rwhps.server.core.thread.Threads.newTimedTask
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.Time.getTimeFutureMillis
import cn.rwhps.server.util.Time.millis
import java.util.concurrent.TimeUnit

class BlackList {
    private val blackList = Seq<BlackData>(false, 16)
    fun addBlackList(str: String) {
        blackList.add(BlackData(str, getTimeFutureMillis(3600 * 1000L)))
    }

    fun containsBlackList(str: String): Boolean {
        val result = BooleanArray(1)
        blackList.each { e: BlackData ->
            if (e.ip == str) {
                result[0] = true
            }
        }
        return result[0]
    }

    private class BlackData(val ip: String, val time: Long) {
        override fun toString(): String {
            return ip
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            return if (other == null || javaClass != other.javaClass) {
                false
            } else ip == other.toString()
        }

        override fun hashCode(): Int {
            return ip.hashCode()
        }
    }

    init {
        newTimedTask(CallTimeTask.BlackListCheckTask, 0, 1, TimeUnit.HOURS){
            val time = millis()
            blackList.each({ it.time < time }) { value: BlackData -> blackList.remove(value) }
        }
    }
}