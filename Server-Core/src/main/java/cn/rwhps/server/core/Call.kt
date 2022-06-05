/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.core

import cn.rwhps.server.core.thread.CallTimeTask
import cn.rwhps.server.core.thread.Threads
import cn.rwhps.server.core.thread.Threads.newCountdown
import cn.rwhps.server.core.thread.TimeTaskData
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.data.player.Player
import cn.rwhps.server.game.event.EventType.GameOverEvent
import cn.rwhps.server.io.packet.GameCommandPacket
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.game.Events
import cn.rwhps.server.util.log.Log.error
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

/**
 * @author RW-HPS/Dr
 */
object Call {
    @JvmStatic
    fun sendMessage(player: Player, text: String) {
        try {
            NetStaticData.groupNet.broadcast(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(text, player.name, player.team))
        } catch (e: IOException) {
            error("[ALL] Send Player Chat Error", e)
        }
    }

    @JvmStatic
    fun sendMessageLocal(player: Player, text: String, vararg obj: Any) {
        Data.game.playerManage.playerGroup.each { e: Player -> e.sendMessage(player, e.i18NBundle.getinput(text, *obj)) }
    }

    @JvmStatic
    fun sendTeamMessage(team: Int, player: Player, text: String) {
        Data.game.playerManage.playerGroup.eachBooleanIfs({ e: Player -> e.team == team }) { p: Player -> p.sendMessage(player, "[TEAM] $text") }
    }

    @JvmStatic
    fun sendSystemTeamMessageLocal(team: Int, text: String, vararg obj: Any) {
        Data.game.playerManage.playerGroup.eachBooleanIfs({ e: Player -> e.team == team }) { p: Player -> p.sendSystemMessage("[TEAM] " + p.i18NBundle.getinput(text, *obj)) }
    }

    @JvmStatic
    fun sendSystemMessage(text: String) {
        try {
            NetStaticData.groupNet.broadcast(NetStaticData.RwHps.abstractNetPacket.getSystemMessagePacket(text))
        } catch (e: IOException) {
            error("[ALL] Send System Chat Error", e)
        }
    }

    @JvmStatic
    fun sendSystemMessageLocal(text: String, vararg obj: Any) {
        Data.game.playerManage.playerGroup.each { e: Player -> e.sendSystemMessage(e.i18NBundle.getinput(text, *obj)) }
    }

    @JvmStatic
    fun sendSystemMessage(text: String, vararg obj: Any) {
        Data.game.playerManage.playerGroup.each { e: Player -> e.sendSystemMessage(e.i18NBundle.getinput(text, *obj)) }
    }

    @JvmStatic
    fun sendTeamData() {
        if (Data.game.gameReConnectPaused) {
            return
        }
        try {
            val enc = NetStaticData.RwHps.abstractNetPacket.getTeamDataPacket()
            Data.game.playerManage.playerGroup.each { e: Player -> e.con!!.sendTeamData(enc) }
        } catch (e: IOException) {
            error("[ALL] Send Team Error", e)
        }
    }

    @JvmStatic
    fun sendPlayerPing() {
        Data.game.playerManage.playerGroup.each { e: Player -> e.con!!.sendPing() }
    }

    @JvmStatic
    @JvmOverloads
    fun upDataGameData(unitData: Boolean = false) {
        Data.game.playerManage.playerGroup.each { e: Player ->
            try {
                e.con!!.sendServerInfo(unitData)
            } catch (err: IOException) {
                error("[ALL] Send System Info Error", err)
            }
        }
    }

    @JvmStatic
    fun killAllPlayer() {
        Data.game.playerManage.playerGroup.each { e: Player ->
            try {
                e.kickPlayer("Game Over")
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

                TimeTaskData.CallTickTask = SendGameTickCommand()
                timerNew.schedule(TimeTaskData.CallTickTask, 0, 150)
                TimeTaskData.CallTickPool = timerNew

                stop()
            }

            if (start) {
                sendSystemMessageLocal("start.testYes")
                val timerNew = Timer()

                TimeTaskData.CallTickTask = SendGameTickCommand()
                timerNew.schedule(TimeTaskData.CallTickTask, 0, 150)
                TimeTaskData.CallTickPool = timerNew

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
     * @property oneSay Boolean
     * @property forcedReturn Boolean
     * @constructor
     */
    private class SendGameTickCommand : TimerTask() {
        private var oneSay = true

        @Volatile
        private var forcedReturn = false

        override fun run() {
            //Data.game.playerManage.playerGroup[0].sync()

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
                    newCountdown(CallTimeTask.GameOverTask, 1, TimeUnit.MINUTES) {gr()}
                }
            } else {
                if (Threads.containsTimeTask(CallTimeTask.GameOverTask)) {
                    oneSay = true
                    Threads.closeTimeTask(CallTimeTask.GameOverTask)
                }
            }

            if (Data.game.gameReConnectPaused || forcedReturn || Data.game.gamePaused) {
                return
            }

            val time = Data.game.tickGame.getAndAdd(10)
            when (val size = Data.game.gameCommandCache.size) {
                0 -> {
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.RwHps.abstractNetPacket.getTickPacket(time))
                    } catch (e: IOException) {
                        error("[ALL] Send Tick Failed", e)
                    }
                }
                1 -> {
                    val gameCommand = Data.game.gameCommandCache.poll()
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.RwHps.abstractNetPacket.getGameTickCommandPacket(time, gameCommand))
                    } catch (e: IOException) {
                        error("[ALL] Send Game Tick Error", e)
                    }
                }
                else -> {
                    val comm = Seq<GameCommandPacket>(size)
                    IntStream.range(0, size).mapToObj { Data.game.gameCommandCache.poll() }.forEach { value: GameCommandPacket -> comm.add(value) }
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.RwHps.abstractNetPacket.getGameTickCommandsPacket(time, comm))
                    } catch (e: IOException) {
                        error("[ALL] Send Game Ticks Error", e)
                    }
                }
            }
        }

        override fun cancel(): Boolean {
            Threads.closeTimeTask(CallTimeTask.GameOverTask)
            return super.cancel()
        }

        fun gr() {
            if (forcedReturn) {
                cancel()
                TimeTaskData.stopCallTickTask()
                return
            }
            forcedReturn = true
            Threads.closeTimeTask(CallTimeTask.GameOverTask)

            cancel()
            TimeTaskData.stopCallTickTask()

            Events.fire(GameOverEvent())
        }
    }
}