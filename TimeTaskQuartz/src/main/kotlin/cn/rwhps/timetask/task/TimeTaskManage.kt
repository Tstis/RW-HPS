/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.timetask.task

import cn.rwhps.timetask.run.RunnableRun
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import java.util.*


class TimeTaskManage {
    private val scheduler: Scheduler = StdSchedulerFactory().scheduler

    init {
        this.scheduler.start()
    }

    fun shutdownNow() {
        scheduler.shutdown()
    }

    /**
     * 创建一个定时计时器
     * @param name 名字
     * @param group 组
     * @param description 描述
     * @param startTime 何时开始
     * @param runnable Runnable
     */
    fun addCountdown(name: String, group: String, description: String, startTime: Long, runnable: Runnable) {
        val key = JobKey(name, group)
        val jobDetail = JobBuilder.newJob(RunnableRun::class.java).withIdentity(key).build()
        val dataMap = JobDataMap()
        dataMap["run"] = {
            runnable.run()
            remove(key)
        }

        val trigger: Trigger = TriggerBuilder.newTrigger()
            .usingJobData(dataMap)
            .withDescription(description)
            .withIdentity(name, group)
            //.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(time).withRepeatCount(0))
            .startAt(Date(startTime))
            .build()
        scheduler.scheduleJob(jobDetail, trigger)
    }

    /**
     * 创建一个定时计时器
     * @param name 名字
     * @param group 组
     * @param description 描述
     * @param intervalTime 执行间隔
     * @param startTime 何时开始
     * @param runnable Runnable
     */
    fun addTimedTask(name: String, group: String, description: String, startTime: Long, intervalTime: Long, runnable: Runnable) {
        val key = JobKey(name, group)
        val jobDetail = JobBuilder.newJob(RunnableRun::class.java).withIdentity(key).build()
        val dataMap = JobDataMap()
        dataMap["run"] = { runnable.run() }

        val trigger: Trigger = TriggerBuilder.newTrigger()
            .usingJobData(dataMap)
            .withDescription(description)
            .withIdentity(name, group)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(intervalTime).repeatForever())
            .startAt(Date(startTime))
            .build()
        scheduler.scheduleJob(jobDetail, trigger)
    }

    fun contains(jobKey: JobKey): Boolean {
        return scheduler.checkExists(jobKey)
    }
    fun contains(name: String, group: String): Boolean {
        return contains(JobKey(name,group))
    }


    fun remove(jobKey: JobKey): Boolean {
        return scheduler.deleteJob(jobKey)
    }
    fun remove(name: String, group: String): Boolean {
        return remove(JobKey(name,group))
    }

}