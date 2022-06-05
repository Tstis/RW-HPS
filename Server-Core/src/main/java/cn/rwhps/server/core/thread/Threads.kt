/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.core.thread

import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.Time
import cn.rwhps.server.util.threads.GetNewThreadPool
import cn.rwhps.timetask.task.TimeTaskManage
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * @author RW-HPS/Dr
 */
object Threads {
    private val CORE_THREAD: ExecutorService = GetNewThreadPool.getNewFixedThreadPool(3, "Core-")
    private val CORE_NET_THREAD: ExecutorService = GetNewThreadPool.getNewFixedThreadPool(3, "Core-Net-")
    private val SERVICE = TimeTaskManage()
    private val PLAYER_HEAT_THREAD = GetNewThreadPool.getNewFixedThreadPool(10, "Core-Heat-")

    /** Execute runnable on exit  */
    private val SAVE_POOL = Seq<Runnable>()

    @JvmStatic
	fun close() {
        CORE_THREAD.shutdownNow()
        CORE_NET_THREAD.shutdownNow()
        SERVICE.shutdownNow()
        PLAYER_HEAT_THREAD.shutdownNow()
    }

    internal fun closeNet() {
        CORE_NET_THREAD.shutdownNow()
    }

    @JvmStatic
    fun newThreadPlayerHeat(run: Runnable) {
        PLAYER_HEAT_THREAD.execute(run)
    }

    @JvmStatic
	fun newThreadCore(run: Runnable) {
        CORE_THREAD.execute(run)
    }

    @JvmStatic
    fun newThreadCoreNet(run: Runnable) {
        CORE_NET_THREAD.execute(run)
    }

    @JvmStatic
	fun addSavePool(run: Runnable) {
        SAVE_POOL.add(run)
    }

    @JvmStatic
	fun runSavePool() {
        SAVE_POOL.each { obj: Runnable -> obj.run() }
    }


    /**
     * 创建一个倒数计时器
     * @param run Runnable
     * @param endTime 多少时间后执行
     * @param timeUnit 时间单位
     * @param nameId NameID
     * @return ScheduledFuture<*>
     */
    @JvmStatic
    fun newCountdown(taskFlag: CallTimeTask, endTime: Int, timeUnit: TimeUnit, run: Runnable) {
        SERVICE.addCountdown(
            taskFlag.name,taskFlag.group,taskFlag.description,
            Time.concurrentMillis() + TimeUnit.MILLISECONDS.convert(endTime.toLong(),timeUnit),
            run
        )
    }

    /**
     * 创建一个定时计时器
     * @param run Runnable
     * @param startTime 多长时间后开始
     * @param intervalTime 执行间隔
     * @param timeUnit 时间单位
     * @param nameId NameID
     * @return ScheduledFuture<*>
     */
    @JvmStatic
    fun newTimedTask(taskFlag: CallTimeTask, startTime: Int, intervalTime: Int, timeUnit: TimeUnit, run: Runnable) {
        SERVICE.addTimedTask(
            taskFlag.name,taskFlag.group,taskFlag.description,
            Time.concurrentMillis() + TimeUnit.MILLISECONDS.convert(startTime.toLong(),timeUnit),
            TimeUnit.MILLISECONDS.convert(intervalTime.toLong(),timeUnit),
            run
        )
    }

    @JvmStatic
    fun containsTimeTask(taskFlag: CallTimeTask): Boolean {
        return SERVICE.contains(taskFlag.name,taskFlag.group)
    }

    @JvmStatic
    @JvmOverloads
    fun closeTimeTask(taskFlag: CallTimeTask, run: Runnable? = null): Boolean {
        val flag =  SERVICE.remove(taskFlag.name,taskFlag.group)
        if (flag) {
            run?.run()
        }
        return flag
    }
}