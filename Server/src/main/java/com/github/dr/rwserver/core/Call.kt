/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.core

import com.github.dr.rwserver.core.thread.Threads.newThreadService
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.game.EventType.GameOverEvent
import com.github.dr.rwserver.game.GameCommand
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log.error
import java.io.IOException
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

/**
 * @author Dr
 */
object Call {
    @JvmStatic
    fun sendMessage(player: Player, text: String) {
        try {
            NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket(text, player.name, player.team))
        } catch (e: IOException) {
            error("[ALL] Send Player Chat Error", e)
        }
    }

    @JvmStatic
    fun sendMessageLocal(player: Player, text: String, vararg obj: Any) {
        Data.game.playerManage.playerGroup.each { e: Player -> e.sendMessage(player, e.localeUtil.getinput(text, *obj)) }
    }

    @JvmStatic
    fun sendTeamMessage(team: Int, player: Player, text: String) {
        Data.game.playerManage.playerGroup.eachBooleanIfs({ e: Player -> e.team == team }) { p: Player -> p.sendMessage(player, "[TEAM] $text") }
    }

    @JvmStatic
    fun sendSystemTeamMessageLocal(team: Int, text: String, vararg obj: Any) {
        Data.game.playerManage.playerGroup.eachBooleanIfs({ e: Player -> e.team == team }) { p: Player -> p.sendSystemMessage("[TEAM] " + p.localeUtil.getinput(text, *obj)) }
    }

    @JvmStatic
    fun sendSystemMessage(text: String) {
        try {
            NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(text))
        } catch (e: IOException) {
            error("[ALL] Send System Chat Error", e)
        }
    }

    @JvmStatic
    fun sendSystemMessageLocal(text: String, vararg obj: Any) {
        Data.game.playerManage.playerGroup.each { e: Player -> e.sendSystemMessage(e.localeUtil.getinput(text, *obj)) }
    }

    @JvmStatic
    fun sendSystemMessage(text: String, vararg obj: Any) {
        Data.game.playerManage.playerGroup.each { e: Player -> e.sendSystemMessage(e.localeUtil.getinput(text, *obj)) }
    }

    @JvmStatic
    fun sendTeamData() {
        if (Data.game.gamePaused) {
            return
        }
        try {
            val enc = NetStaticData.protocolData.abstractNetPacket.getTeamDataPacket()
            Data.game.playerManage.playerGroup.each { e: Player -> e.con!!.sendTeamData(enc) }
        } catch (e: IOException) {
            error("[ALL] Send Team Error", e)
        }
    }

    @JvmStatic
    fun sendPlayerPing() {
        Data.game.playerManage.playerGroup.each { e: Player -> e.con!!.ping() }
    }

    @JvmStatic
    fun upDataGameData() {
        Data.game.playerManage.playerGroup.each { e: Player ->
            try {
                e.con!!.sendServerInfo(false)
            } catch (err: IOException) {
                error("[ALL] Send System Info Error", err)
            }
        }
    }

    @JvmStatic
    fun killAllPlayer() {
        Data.game.playerManage.playerGroup.each { e: Player ->
            try {
                e.con!!.sendKick("Game Over")
            } catch (err: IOException) {
                error("[ALL] Kick All Player Error", e)
            }
        }
    }

    @JvmStatic
    fun disAllPlayer() {
        Data.game.playerManage.playerGroup.each { e: Player -> e.con!!.disconnect() }
    }

    @JvmStatic
    fun testPreparationPlayer() {
        val timer = Timer()
        timer.schedule(RandyTask(timer), 0, 100)
    }

    /**
     * 检测玩家是否全部收到StartGame-Packet
     * @property loadTime 失败次数
     * @property loadTimeMaxTry 尝试最大次数
     * @property start 是否收到
     */
    private class RandyTask(private val timer: Timer) : TimerTask() {
        private var loadTime = 0
        private val loadTimeMaxTry = 30
        private var start = true

        override fun run() {
            start = true

            Data.game.playerManage.playerGroup.each { p: Player ->
                if (!p.start) {
                    loadTime += 1
                    start = false
                }
            }

            if (loadTime > loadTimeMaxTry) {
                sendSystemMessageLocal("start.testNo")
                val timerNew = Timer()
                timerNew.schedule(SendGameTickCommand(timerNew), 0, 150)
                stop()
            }

            if (start) {
                sendSystemMessageLocal("start.testYes")
                val timerNew = Timer()
                timerNew.schedule(SendGameTickCommand(timerNew), 0, 150)
                stop()
            }
        }

        private fun stop() {
            cancel()
            timer.cancel()
        }
    }

    // Welcome to Bugs RW-HPS ! ---- 来自于 1.2.0-M2到5.2.0-M1-DEV才被修复的Bug
    /**
     *
     * @property timer Timer
     * @property time Int
     * @property oneSay Boolean
     * @property gameOverTask ScheduledFuture<*>?
     * @property forcedReturn Boolean
     * @constructor
     */
    private class SendGameTickCommand(private val timer: Timer) : TimerTask() {
        private var time = 0
        private var oneSay = true

        private var gameOverTask: ScheduledFuture<*>? = null
        @Volatile
        private var forcedReturn = false

        override fun run() {
            // 检测人数是否符合Gameover
            val playerSize = Data.game.playerManage.playerGroup.size()
            if (playerSize == 0) {
                gr()
                return
            }

            if (playerSize <= 1) {
                if (oneSay) {
                    oneSay = false
                    sendSystemMessageLocal("gameOver.oneMin")
                    gameOverTask = newThreadService({gr()}, 1, TimeUnit.MINUTES)
                }
            } else {
                if (gameOverTask != null) {
                    oneSay = true
                    gameOverTask!!.cancel(true)
                    gameOverTask = null
                }
            }

            if (Data.game.gamePaused || forcedReturn) {
                return
            }

            time += 10
            when (val size = Data.game.gameCommandCache.size) {
                0 -> {
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getTickPacket(time))
                    } catch (e: IOException) {
                        error("[ALL] Send Tick Failed", e)
                    }
                }
                1 -> {
                    val gameCommand = Data.game.gameCommandCache.poll()
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getGameTickCommandPacket(time, gameCommand))
                    } catch (e: IOException) {
                        error("[ALL] Send Game Tick Error", e)
                    }
                }
                else -> {
                    val comm = Seq<GameCommand>(size)
                    IntStream.range(0, size).mapToObj { Data.game.gameCommandCache.poll() }.forEach { value: GameCommand -> comm.add(value) }
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getGameTickCommandsPacket(time, comm))
                    } catch (e: IOException) {
                        error("[ALL] Send Game Ticks Error", e)
                    }
                }
            }
        }

        fun gr() {
            if (forcedReturn) {
                cancel()
                timer.cancel()
                return
            }

            forcedReturn = true
            gameOverTask?.cancel(true)
            gameOverTask = null

            cancel()
            timer.cancel()

            Events.fire(GameOverEvent())
        }
    }
}