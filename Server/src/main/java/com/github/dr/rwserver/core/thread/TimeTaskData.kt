/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.core.thread

import com.github.dr.rwserver.util.IsUtil
import java.util.concurrent.ScheduledFuture

object TimeTaskData {
    @JvmField
    var CallTeamTask: ScheduledFuture<*>? = null
    @JvmField
    var CallPingTask: ScheduledFuture<*>? = null
    @JvmField
    var PlayerAfkTask: ScheduledFuture<*>? = null

    @JvmField
    var CustomUpServerListTask: ScheduledFuture<*>? = null
    @JvmField
    var UpServerListTask: ScheduledFuture<*>? = null
    @JvmField
    var UpServerListNewTask: ScheduledFuture<*>? = null

    @JvmField
    var GameWinOrLoseCheckTask: ScheduledFuture<*>? = null

    @JvmField
    var AutoReLoadMapTask: ScheduledFuture<*>? = null


    /*
     * 这一块为永久区
     * 将不会执行 cancel
     */
    @JvmField
    var BlackListCheckTask: ScheduledFuture<*>? = null
    @JvmField
    var ServerUploadDataTask: ScheduledFuture<*>? = null
    @JvmField
    var ServerUploadData_CheckTimeTask: ScheduledFuture<*>? = null

    @JvmStatic
    fun stopCallTeamTask() {
        CallTeamTask?.cancel(true)
        CallTeamTask = null
    }
    @JvmStatic
    fun stopCallPingTask() {
        CallPingTask?.cancel(true)
        CallPingTask = null
    }
    @JvmStatic
    fun stopPlayerAfkTask() {
        PlayerAfkTask?.cancel(true)
        PlayerAfkTask = null
    }

    @JvmStatic
    fun stopCustomUpServerListTask(run: Runnable? = null) {
        CustomUpServerListTask?.cancel(true)
        if (IsUtil.isNull(CustomUpServerListTask)) run?.run()
        CustomUpServerListTask = null

    }
    @JvmStatic
    fun stopUpServerListTask(run: Runnable? = null) {
        UpServerListTask?.cancel(true)
        if (IsUtil.isNull(UpServerListTask)) run?.run()
        UpServerListTask = null
    }
    @JvmStatic
    fun stopUpServerListNewTask(run: Runnable? = null) {
        UpServerListNewTask?.cancel(true)
        if (IsUtil.isNull(UpServerListNewTask)) run?.run()
        UpServerListNewTask = null
    }
    @JvmStatic
    fun stopAutoReLoadMapTask() {
        AutoReLoadMapTask?.cancel(true)
        AutoReLoadMapTask = null
    }

    @JvmStatic
    fun stopGameWinOrLoseCheckTask() {
        GameWinOrLoseCheckTask?.cancel(true)
        GameWinOrLoseCheckTask = null
    }
}