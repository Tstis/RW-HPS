/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.command.ex

import com.github.dr.rwserver.core.Call
import com.github.dr.rwserver.core.thread.Threads.newThreadService
import com.github.dr.rwserver.core.thread.Threads.newThreadService2
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.game.EventType
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.game.Events
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil


/**
 * Vote
 * @author Dr
 * @Date 2022/02/04 15:00:05
 */
class Vote {
    private val command: String
    private val player: Player
    private val targetPlayer: Player?
    private var isTeam: Boolean = false

    private var require: Int = 0
    private var pass: Int = 0

    private var endNoMsg: ()->Unit = {}
    private var endYesMsg: ()->Unit = {}
    private var votePlayerIng: ()->Unit = {}
    private var voteIng: ()->Unit = {}

    private var reciprocal: Int = 60


    private var countDownTask: ScheduledFuture<*>? = null
    private var voteTimeTask: ScheduledFuture<*>? = null

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
            votePlayer.sendSystemMessage(votePlayer.localeUtil.getinput("vote.rey"))
            return
        }
        
        val accapt = "y"
        val noAccapt = "n"
        
        if (accapt == playerPick) {
            if (isTeam) {
                if (votePlayer.team == player.team) {
                    this.pass++
                    playerList.add(votePlayer.uuid)
                    votePlayer.sendSystemMessage(votePlayer.localeUtil.getinput("vote.y"))
                    votePlayerIng()
                } else {
                    votePlayer.sendSystemMessage(votePlayer.localeUtil.getinput("vote.team"))
                }
            } else {
                this.pass++
                playerList.add(votePlayer.uuid)
                votePlayer.sendSystemMessage(votePlayer.localeUtil.getinput("vote.y"))
                votePlayerIng()
            }
        } else if (noAccapt == playerPick) {
            if (isTeam) {
                if (votePlayer.team == player.team) {
                    playerList.add(votePlayer.uuid)
                    votePlayer.sendSystemMessage(votePlayer.localeUtil.getinput("vote.n"))
                } else {
                    votePlayer.sendSystemMessage(votePlayer.localeUtil.getinput("vote.team"))
                }
            } else {
                playerList.add(votePlayer.uuid)
                votePlayer.sendSystemMessage(votePlayer.localeUtil.getinput("vote.n"))
            }
        }
        inspectEnd()
    }

    private fun preprocessing() {
        // 预处理
        when (command) {
            "gameover" -> normalDistribution()
            "surrender" -> {
                isTeam = true
                teamOnly()
            }
            else -> {
                player.sendSystemMessage(player.localeUtil.getinput("vote.end.err", command))
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
        votePlayerIng = { Call.sendSystemMessage("vote.y.ing", command,pass,reciprocal) }
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
        votePlayerIng = { Call.sendSystemTeamMessageLocal(player.team,"vote.y.ing", command,pass,reciprocal) }
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
            }, 10, 10, TimeUnit.SECONDS)
            voteTimeTask = newThreadService(Runnable {
                countDownTask!!.cancel(true)
                end()
            }, 58, TimeUnit.SECONDS)
            run()
        }
    }

    private fun inspectEnd() {
        if (this.pass >= this.require) {
            end()
        }
    }

    private fun end() {
        if (this.pass >= this.require) {
            endYesMsg()

            stopTask(countDownTask)
            stopTask(voteTimeTask)
            countDownTask = null
            voteTimeTask = null

            when (command) {
                "gameover" -> gameover()
                "surrender" -> surrender()
                else -> {}
            }
        } else {
            endNoMsg()
        }
        clearUp()
    }

    /**
     * 清理引用
     */
    private fun clearUp() {
        playerList.clear()
        endNoMsg = {}
        endYesMsg = {}
        votePlayerIng = {}
        voteIng = {}
        Data.vote = null
    }

    private fun gameover() {
        Events.fire(EventType.GameOverEvent())
    }

    private fun surrender() {
        Data.game.playerManage.playerGroup.eachBooleanIfs({ e: Player -> e.team == player.team }) { p: Player -> p.con!!.sendSurrender() }
    }


    private fun stopTask(task: ScheduledFuture<*>?) {
        task?.cancel(true)
    }
}