/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.command.ex

import cn.rwhps.server.core.Call
import cn.rwhps.server.core.thread.Threads.newThreadService2
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.player.Player
import cn.rwhps.server.game.event.EventType
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.Time.concurrentSecond
import cn.rwhps.server.util.game.Events
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.exp.ImplementedException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil


/**
 * Vote 为游戏提供一个默认的Vote接口
 * @author Dr
 * @Date 2022/02/04 15:00:05
 */
class Vote {
    private val command: String
    val player: Player
    val targetPlayer: Player?
    private var isTeam: Boolean = false

    private var require: Int = 0
    private var pass: Int = 0

    private var endNoMsg: ()->Unit = {}
    private var endYesMsg: ()->Unit = {}
    private var votePlayerIng: ()->Unit = {}
    private var voteIng: ()->Unit = {}

    private var reciprocal: Int = 60


    private var countDownTask: ScheduledFuture<*>? = null

    private val playerList = Seq<String>()

    // Gameover Suss~
    constructor(command: String, hostPlayer: Player) {
        this.command = command
        this.player = hostPlayer
        this.targetPlayer = null
        preprocessing()
    }
    // Kick Other
    constructor(command: String, hostPlayer: Player, targetPlayer: Player) {
        this.command = command
        this.player = hostPlayer
        this.targetPlayer = targetPlayer
        preprocessing()
    }

    fun toVote(votePlayer: Player, playerPick: String) {
        if (playerList.contains(votePlayer.uuid)) {
            votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.rey"))
            return
        }
        
        val accapt = "y"
        val noAccapt = "n"
        
        if (accapt == playerPick) {
            if (isTeam) {
                if (votePlayer.team == player.team) {
                    this.pass++
                    playerList.add(votePlayer.uuid)
                    votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.y"))
                    votePlayerIng()
                } else {
                    votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.team"))
                }
            } else {
                this.pass++
                playerList.add(votePlayer.uuid)
                votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.y"))
                votePlayerIng()
            }
        } else if (noAccapt == playerPick) {
            if (isTeam) {
                if (votePlayer.team == player.team) {
                    playerList.add(votePlayer.uuid)
                    votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.n"))
                } else {
                    votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.team"))
                }
            } else {
                playerList.add(votePlayer.uuid)
                votePlayer.sendSystemMessage(votePlayer.i18NBundle.getinput("vote.n"))
            }
        }
        inspectEnd()
    }

    private fun preprocessing() {
        // 预处理
        when {
            commandStartData.contains(command) -> commandStartData[command]!!.invoke(this)
            else -> {
                player.sendSystemMessage(player.i18NBundle.getinput("vote.end.err", command))
                clearUp()
            }
        }
    }

    /**
     * 正常投票
     */
    private fun normalDistribution() {
        require = Data.game.playerManage.playerGroup.size()
        endNoMsg = { Call.sendSystemMessageLocal("vote.done.no", command + " " + (targetPlayer?.name ?:""), pass, this.require) }
        endYesMsg = { Call.sendSystemMessageLocal("vote.ok") }
        votePlayerIng = { Call.sendSystemMessage("vote.y.ing", command,pass,this.require) }
        voteIng = { Call.sendSystemMessage("vote.ing", reciprocal) }
        start { Call.sendSystemMessage("vote.start", player.name, command + " " + (targetPlayer?.name ?:"")) }
    }

    /**
     * 团队投票
     */
    private fun teamOnly() {
        val require = AtomicInteger(0)
        Data.game.playerManage.playerGroup.eachBooleanIfs({ e: Player -> e.team == player.team }) { _: Player -> require.getAndIncrement() }
        this.require = require.get()
        endNoMsg = { Call.sendSystemTeamMessageLocal(player.team, "vote.done.no", command + " " + (targetPlayer?.name ?:""),pass,this.require) }
        endYesMsg = { Call.sendSystemTeamMessageLocal(player.team, "vote.ok") }
        votePlayerIng = { Call.sendSystemTeamMessageLocal(player.team,"vote.y.ing", command,pass,this.require) }
        voteIng = { Call.sendSystemTeamMessageLocal(player.team, "vote.ing", reciprocal) }
        start { Call.sendSystemTeamMessageLocal(player.team, "vote.start", player.name, command + " " + (targetPlayer?.name ?:"")) }
    }

    private fun start(run: ()->Unit) {
        val temp = require
        require = if (temp <= 1) {
            player.sendSystemMessage("vote.no1")
            1
        } else if (temp <= 3) {
            2
        } else {
            ceil(temp.toDouble() / 2).toInt()
        }

        playerList.add(player.uuid)

        pass++

        if (pass >= require) {
            end()
        } else {
            countDownTask = newThreadService2(Runnable {
                this.reciprocal -= 10
                voteIng()
                if (this.reciprocal <= 0) {
                    end()
                    countDownTask?.cancel(true)
                }
            }, 10, 10, TimeUnit.SECONDS)
            run()
        }
    }

    private fun inspectEnd() {
        if (this.pass >= this.require) {
            end()
        }
    }

    private fun end() {
        stopTask(countDownTask)
        countDownTask = null

        var error: ImplementedException.VoteImplementedException? = null
        if (this.pass >= this.require) {
            endYesMsg()
            when {
                commandEndData.contains(command) -> commandEndData[command]!!.invoke(this)
                else -> {
                    error = ImplementedException.VoteImplementedException("[Vote End] Server does not implement command: $command")
                    clearUp()
                }
            }
        } else {
            endNoMsg()
        }
        clearUp()
        if (error != null) {
            Log.error(error)
        }
    }

    /**
     * 清理引用
     */
    private fun clearUp() {
        playerList.clear()
        val nullVal: ()->Unit = {}
        endNoMsg = nullVal
        endYesMsg = nullVal
        votePlayerIng = nullVal
        voteIng = nullVal
        Data.vote = null
    }

    private fun stopTask(task: ScheduledFuture<*>?) {
        task?.cancel(true)
    }

    fun stopVote() {
        stopTask(countDownTask)
        countDownTask = null
        clearUp()
    }

    companion object {
        private val commandStartData = mutableMapOf<String, (vote: Vote)->Unit>()
        private val commandEndData = mutableMapOf<String, (vote: Vote)->Unit>()

        init {
            commandStartData["gameover"] = { it.normalDistribution() }
            commandStartData["surrender"] = { it.isTeam = true ; it.teamOnly() }

            commandEndData["gameover"] = { Events.fire(EventType.GameOverEvent()) }
            commandEndData["surrender"] = { Log.clog("Surrender") ; Data.game.playerManage.playerGroup.eachBooleanIfs({ e: Player -> e.team == it.player.team }) { p: Player -> p.con!!.sendSurrender() } }
        }

        fun testVoet(player: Player): Boolean {
            val checkTime = player.lastVoteTime + 60
            return if (checkTime > concurrentSecond()) {
                true
            } else {
                player.lastVoteTime = concurrentSecond()
                false
            }

        }

        @JvmStatic
        fun addVoteFullParticipation(command: String,run: (vote: Vote)->Unit): Boolean {
            return if (commandStartData.contains(command)) {
                false
            } else {
                commandStartData[command] = { run(it) ; it.normalDistribution()}
                true
            }
        }

        @JvmStatic
        fun addVoteTeamOnly(command: String,run: (vote: Vote)->Unit): Boolean {
            return if (commandStartData.contains(command)) {
                false
            } else {
                commandStartData[command] = { run(it) ; it.isTeam = true ; it.teamOnly() }
                true
            }
        }
    }
}