/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.command

/**
 * @author Dr
 */
object BetaGame {
    internal fun CheckGameWin() {
        /*
        // BETA
        TimeTaskData.GameWinOrLoseCheckTask = newThreadService2({

            // 获取当前时间
            val time = System.currentTimeMillis()
            // 获取判断时间
            val time2: Int = Data.config.WinOrLoseTime shr 2
            val intSet = IntSet(16)

            // 玩家是否死亡
            Data.game.playerManage.playerGroup.eachBooleanIfs({ d: Player -> !d.dead }) { e: Player ->
                // 玩家距离最后一次移动单位的时间
                val breakTime = time - e.lastMoveTime
                // 超过判定时间是警告一次 ; 达到判定的两倍即发送自杀包
                if (breakTime > Data.config.WinOrLoseTime) {
                    // 直接让玩家自杀
                    e.con!!.sendSurrender()
                } else if (breakTime > time2) {
                    // 给出提示
                    e.sendSystemMessage(e.localeUtil.getinput("winOrLose.time"))
                }
                intSet.add(e.team)
            }
            if (intSet.size <= 1) {
                val winTeam = intSet.iterator().next()
                Data.game.playerManage.playerGroup.eachBooleanIfs({ p: Player -> p.team == winTeam }) { c: Player -> info(c.name) }
                Events.fire(GameOverEvent())
            }
        }, 10, 10, TimeUnit.SECONDS)*/
    }
}