/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.handler.rudp

import cn.rwhps.server.net.core.server.AbstractNetConnect
import cn.rwhps.server.util.Time.concurrentMillis

/**
 * @author Dr
 */
internal class TimeoutDetection {
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