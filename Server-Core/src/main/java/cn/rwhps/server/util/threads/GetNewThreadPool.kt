/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.threads

import java.util.concurrent.*

/**
 * @author RW-HPS/Dr
 */
object GetNewThreadPool {
    fun getNewFixedThreadPool(nThreads: Int, name: String): ThreadPoolExecutor {
        return ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue(), ThreadFactoryName.nameThreadFactory(name))
    }

    fun getNewScheduledThreadPool(corePoolSize: Int, name: String): ScheduledExecutorService {
        return ScheduledThreadPoolExecutor(corePoolSize, ThreadFactoryName.nameThreadFactory(name))
    }
}