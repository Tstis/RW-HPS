/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.core.thread

import cn.rwhps.server.util.IsUtil
import java.util.*
import java.util.concurrent.ScheduledFuture

object TimeTaskData {
    var CallTeamTask: ScheduledFuture<*>? = null
    var CallPingTask: ScheduledFuture<*>? = null
    var PlayerAfkTask: ScheduledFuture<*>? = null

    var CustomUpServerListTask: ScheduledFuture<*>? = null
    var UpServerListTask: ScheduledFuture<*>? = null
    var UpServerListNewTask: ScheduledFuture<*>? = null


    var GameWinOrLoseCheckTask: ScheduledFuture<*>? = null


    var AutoReLoadMapTask: ScheduledFuture<*>? = null

    var CallTickPool: Timer? = null
    var CallTickTask: TimerTask? = null


    /*
     * 这一块为永久区
     * 将不会执行 cancel
     */
    var BlackListCheckTask: ScheduledFuture<*>? = null
    var ServerUploadDataTask: ScheduledFuture<*>? = null
    var ServerUploadData_CheckTimeTask: ScheduledFuture<*>? = null

    fun stopCallTeamTask() {
        CallTeamTask?.cancel(true)
        CallTeamTask = null
    }
    fun stopCallPingTask() {
        CallPingTask?.cancel(true)
        CallPingTask = null
    }
    fun stopPlayerAfkTask() {
        PlayerAfkTask?.cancel(true)
        PlayerAfkTask = null
    }


    fun stopCustomUpServerListTask(run: Runnable? = null) {
        CustomUpServerListTask?.cancel(true)
        if (IsUtil.isNull(CustomUpServerListTask)) run?.run()
        CustomUpServerListTask = null

    }
    fun stopUpServerListTask(run: Runnable? = null) {
        UpServerListTask?.cancel(true)
        if (IsUtil.isNull(UpServerListTask)) run?.run()
        UpServerListTask = null
    }
    fun stopUpServerListNewTask(run: Runnable? = null) {
        UpServerListNewTask?.cancel(true)
        if (IsUtil.isNull(UpServerListNewTask)) run?.run()
        UpServerListNewTask = null
    }
    fun stopAutoReLoadMapTask() {
        AutoReLoadMapTask?.cancel(true)
        AutoReLoadMapTask = null
    }


    fun stopGameWinOrLoseCheckTask() {
        GameWinOrLoseCheckTask?.cancel(true)
        GameWinOrLoseCheckTask = null
    }


    fun stopCallTickTask() {
        CallTickTask?.cancel()
        CallTickPool?.cancel()
        CallTickTask = null
        CallTickPool = null
    }
}