/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.command

import com.github.dr.rwserver.command.ex.Vote
import com.github.dr.rwserver.core.Call.sendMessageLocal
import com.github.dr.rwserver.core.Call.sendSystemMessageLocal
import com.github.dr.rwserver.core.Call.sendTeamData
import com.github.dr.rwserver.core.Call.sendTeamMessage
import com.github.dr.rwserver.core.Call.testPreparationPlayer
import com.github.dr.rwserver.core.Call.upDataGameData
import com.github.dr.rwserver.core.thread.Threads.newThreadService
import com.github.dr.rwserver.core.thread.TimeTaskData
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.game.EventType.GameStartEvent
import com.github.dr.rwserver.game.GameMaps.MapType
import com.github.dr.rwserver.util.IsUtil.notIsBlank
import com.github.dr.rwserver.util.IsUtil.notIsNumeric
import com.github.dr.rwserver.util.Time.getTimeFutureMillis
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log.error
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Dr
 */
class ClientCommands(handler: CommandHandler) {
    private val localeUtil = Data.localeUtil
    private fun isAdmin(player: Player): Boolean {
        if (player.isAdmin) {
            return true
        }
        player.sendSystemMessage(player.localeUtil.getinput("err.noAdmin"))
        return false
    }

    init {
        handler.register("help", "clientCommands.help") { _: Array<String>?, player: Player ->
            val str = StringBuilder(16)
            for (command in handler.commandList) {
                if (command.description.startsWith("#")) {
                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
                        .append(command.paramText).append(" - ").append(command.description.substring(1))
                } else {
                    if ("HIDE" == command.description) {
                        continue
                    }
                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
                        .append(command.paramText).append(" - ").append(player.localeUtil.getinput(command.description))
                        .append(LINE_SEPARATOR)
                }
            }
            player.sendSystemMessage(str.toString())
        }
        handler.register("map", "<MapNumber...>", "clientCommands.map") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (Data.game.isStartGame || Data.game.mapLock) {
                    return@register
                }
                val response = StringBuilder(args[0])
                var i = 1
                val lens = args.size
                while (i < lens) {
                    response.append(" ").append(args[i])
                    i++
                }
                val inputMapName = response.toString().replace("'", "").replace(" ", "").replace("-", "").replace("_", "")
                val mapPlayer = Data.MapsMap[inputMapName]
                if (mapPlayer != null) {
                    val data = mapPlayer.split("@").toTypedArray()
                    Data.game.maps.mapName = data[0]
                    Data.game.maps.mapPlayer = data[1]
                    Data.game.maps.mapType = MapType.defaultMap
                } else {
                    if (Data.game.mapsData.size == 0) {
                        return@register
                    }
                    if (notIsNumeric(inputMapName)) {
                        player.sendSystemMessage(localeUtil.getinput("err.noNumber"))
                        return@register
                    }
                    val name = Data.game.mapsData.keys().toSeq()[inputMapName.toInt()]
                    val data = Data.game.mapsData[name]
                    Data.game.maps.mapData = data
                    Data.game.maps.mapType = data.mapType
                    Data.game.maps.mapName = name
                    Data.game.maps.mapPlayer = ""
                    player.sendSystemMessage(player.localeUtil.getinput("map.custom.info"))
                }
                upDataGameData()
            }
        }
        handler.register("maps", "[page]", "clientCommands.maps") { _: Array<String>?, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(localeUtil.getinput("err.startGame"))
                return@register
            }
            if (Data.game.mapsData.size == 0) {
                return@register
            }
            val response = StringBuilder()
            val i = AtomicInteger(0)
            response.append(localeUtil.getinput("maps.top"))
            Data.game.mapsData.keys().forEach { k: String ->
                response.append(localeUtil.getinput("maps.info", i.get(), k)).append(LINE_SEPARATOR)
                i.getAndIncrement()
            }
            player.sendSystemMessage(response.toString())
        }
        handler.register("afk", "clientCommands.afk") { _: Array<String>?, player: Player ->
            if (!Data.game.isAfk) {
                player.sendSystemMessage(localeUtil.getinput("ban.comm", "afk"))
                return@register
            }
            if (player.isAdmin) {
                player.sendSystemMessage(localeUtil.getinput("afk.adminNo"))
            } else {
                if (Data.game.isStartGame) {
                    player.sendSystemMessage(localeUtil.getinput("err.startGame"))
                    return@register
                }
                if (TimeTaskData.PlayerAfkTask != null) {
                    return@register
                }
                val admin = AtomicBoolean(true)
                Data.game.playerManage.playerGroup.each({ p: Player -> p.isAdmin },{ _: Player -> admin.set(false) })
                if (admin.get() && Data.config.OneAdmin) {
                    player.isAdmin = true
                    upDataGameData()
                    sendSystemMessageLocal("afk.end.noAdmin", player.name)
                    return@register
                }
                TimeTaskData.PlayerAfkTask = newThreadService({
                    Data.game.playerManage.playerGroup.each(
                        { p: Player -> p.isAdmin }) { i: Player ->
                        i.isAdmin = false
                        player.isAdmin = true
                        upDataGameData()
                        sendSystemMessageLocal("afk.end.ok", player.name)
                        TimeTaskData.stopPlayerAfkTask()
                    }
                }, 30, TimeUnit.SECONDS)
                sendMessageLocal(player, "afk.start", player.name)
            }
        }
        handler.register("give", "<PlayerSerialNumber>", "clientCommands.give") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (Data.game.isStartGame) {
                    player.sendSystemMessage(localeUtil.getinput("err.startGame"))
                    return@register
                }
                if (notIsNumeric(args[0])) {
                    player.sendSystemMessage(localeUtil.getinput("err.noNumber"))
                    return@register
                }
                val playerSite = args[0].toInt() - 1
                val newAdmin = Data.game.playerManage.getPlayerArray(playerSite)
                if (notIsBlank(newAdmin)) {
                    player.isAdmin = false
                    newAdmin!!.isAdmin = true
                    upDataGameData()
                    sendMessageLocal(player, "give.ok", player.name)
                } else {
                    sendMessageLocal(player, "give.noPlayer", player.name)
                }
            }
        }
        handler.register("nosay", "<on/off>", "clientCommands.noSay") { args: Array<String>, player: Player ->
            player.noSay = "on" == args[0]
            player.sendSystemMessage(localeUtil.getinput("server.noSay", if (player.noSay) "开启" else "关闭"))
        }
        handler.register("am", "<on/off>", "clientCommands.am") { args: Array<String>, player: Player ->
            Data.game.playerManage.amTeam = "on" == args[0]
            if (Data.game.playerManage.amTeam) {
                Data.game.playerManage.amYesPlayerTeam()
            } else {
                Data.game.playerManage.amNoPlayerTeam()
            }
            player.sendSystemMessage(localeUtil.getinput("server.amTeam", if (Data.game.playerManage.amTeam) "开启" else "关闭"))
        }
        handler.register("income", "<income>", "clientCommands.income") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (Data.game.isStartGame) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                    return@register
                }
                Data.game.income = args[0].toFloat()
                upDataGameData()
            }
        }
        handler.register("status", "clientCommands.status") { _: Array<String>?, player: Player ->
            player.sendSystemMessage(
                player.localeUtil.getinput(
                    "status.version",
                    Data.game.playerManage.playerGroup.size(),
                    Data.core.admin.bannedIPs.size(),
                    Data.SERVER_CORE_VERSION,
                    player.con!!.version
                )
            )
        }
        handler.register("kick", "<PlayerSerialNumber>", "clientCommands.kick") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (isAdmin(player)) {
                if (notIsNumeric(args[0])) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                    return@register
                }
                val site = args[0].toInt() - 1
                val kickPlayer = Data.game.playerManage.getPlayerArray(site)
                if (kickPlayer != null) {
                    kickPlayer.kickTime = getTimeFutureMillis(60 * 1000L)
                    try {
                        kickPlayer.kickPlayer(localeUtil.getinput("kick.you"))
                    } catch (e: IOException) {
                        error("[Player] Send Kick Player Error", e)
                    }
                }
            }
        }
        handler.register("sync",  "clientCommands.sync") { _: Array<String>?, player: Player ->
            run {
                if (Data.game.playerManage.playerGroup.size() == 1) {
                    player.sendSystemMessage("Only one player, Ban Sync")
                } else {
                    player.sync()
                }
            }
        }


        handler.register("i", "<i...>", "HIDE") { _: Array<String>?, _: Player -> }

        /* QC */
        handler.register("credits", "<money>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (notIsNumeric(args[0])) {
                    player.sendSystemMessage(localeUtil.getinput("err.noNumber"))
                    return@register
                }
                when (args[0].toInt()) {
                    0 -> Data.game.credits = 1
                    1000 -> Data.game.credits = 2
                    2000 -> Data.game.credits = 3
                    5000 -> Data.game.credits = 4
                    10000 -> Data.game.credits = 5
                    50000 -> Data.game.credits = 6
                    100000 -> Data.game.credits = 7
                    200000 -> Data.game.credits = 8
                    4000 -> Data.game.credits = 0
                    else -> {
                    }
                }
                upDataGameData()
            }
        }
        handler.register("nukes", "<boolean>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                Data.game.noNukes = !java.lang.Boolean.parseBoolean(args[0])
                upDataGameData()
            }
        }
        handler.register("addai", "HIDE") { _: Array<String>?, player: Player -> player.sendSystemMessage(player.localeUtil.getinput("err.nosupr")) }
        handler.register("fog", "<type>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                Data.game.mist = if ("off" == args[0]) 0 else if ("basic" == args[0]) 1 else 2
                upDataGameData()
            }
        }
        handler.register("sharedcontrol", "<boolean>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                Data.game.sharedControl = java.lang.Boolean.parseBoolean(args[0])
                upDataGameData()
            }
        }
        handler.register("startingunits", "<type>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (notIsNumeric(args[0])) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                    return@register
                }
                //Data.game.initUnit = (type == 1) ? 1 : (type == 2) ? 2 : (type ==3) ? 3 : (type == 4) ? 4 : 100;
                Data.game.initUnit = args[0].toInt()
                upDataGameData()
            }
        }
        handler.register("start", "clientCommands.start") { _: Array<String>?, player: Player ->
            if (isAdmin(player)) {
                if (TimeTaskData.PlayerAfkTask != null) {
                    TimeTaskData.stopPlayerAfkTask()
                    sendMessageLocal(player, "afk.clear", player.name)
                }
                if (Data.config.StartMinPlayerSize > Data.game.playerManage.playerGroup.size()) {
                    player.sendSystemMessage(player.localeUtil.getinput("start.playerNo", Data.config.StartMinPlayerSize))
                    return@register
                }
                if (Data.game.maps.mapData != null) {
                    Data.game.maps.mapData!!.readMap()
                }

                val enc = NetStaticData.protocolData.abstractNetPacket.getTeamDataPacket()

                Data.game.playerManage.playerGroup.each { e: Player ->
                    try {
                        e.con!!.sendTeamData(enc)
                        e.con!!.sendStartGame()
                        e.lastMoveTime = System.currentTimeMillis()
                    } catch (err: IOException) {
                        error("Start Error", err)
                    }
                }
                /*
                if (Data.config.WinOrLose) {
                }*/
                Data.game.isStartGame = true
                Data.game.playerManage.updateControlIdentifier()
                testPreparationPlayer()
                Events.fire(GameStartEvent())
            }
        }
        handler.register("t", "<text...>", "clientCommands.t") { args: Array<String>, player: Player ->
            val response = StringBuilder(args[0])
            var i = 1
            val lens = args.size
            while (i < lens) {
                response.append(" ").append(args[i])
                i++
            }
            sendTeamMessage(player.team, player, response.toString())
        }
        handler.register("surrender", "clientCommands.surrender") { _: Array<String>?, player: Player ->
            if (Data.game.isStartGame) {
				if (Data.vote == null) {
                    if (Vote.testVoet(player)) {
                        Data.vote = Vote("surrender",player)
                    } else {
                        player.sendSystemMessage("[警告] 您正在尝试投降, 如果您确认要进行投票进行投降, 那么 请再点击一次投降")
                    }
				} else {
					Data.vote!!.toVote(player,"y")
				}
                //player.con!!.sendSurrender()
            } else {
                player.sendSystemMessage(player.localeUtil.getinput("err.noStartGame"))
            }
        }
        handler.register("teamlock", "clientCommands.teamlock") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                Data.game.lockTeam = "on" == args[0]
            }
        }
        handler.register("killme", "clientCommands.killMe") { _: Array<String>?, player: Player ->
            if (Data.game.isStartGame) {
                player.con!!.sendSurrender()
            } else {
                player.sendSystemMessage(player.localeUtil.getinput("err.noStartGame"))
            }
        }
        // TODO Vote
        /*
        handler.register("vote", "<gameover/kick> [player-site]", "clientCommands.vote") { args: Array<String>, player: Player ->
            when (args[0].lowercase(Locale.getDefault())) {
                "gameover" -> Data.Vote = Vote(player, args[0])
                "kick" -> if (args.size > 1 && isNumeric(args[1])) {
                    Data.Vote = Vote(player, args[0], args[1])
                } else {
                    player.sendSystemMessage(player.localeUtil.getinput("err.commandError"))
                }
                else -> player.sendSystemMessage(player.localeUtil.getinput("err.command"))
            }
        }

         */
        handler.register("move", "<PlayerSerialNumber> <ToSerialNumber> <?>", "HIDE") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (isAdmin(player)) {
                if (notIsNumeric(args[0]) && notIsNumeric(args[1]) && notIsNumeric(args[2])) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                    return@register
                }
                val oldSite = args[0].toInt() - 1
                val newSite = args[1].toInt() - 1
                //int newSite = 0;
                val team = args[2].toInt()
                if (newSite >= 0) {
                    if (oldSite < Data.game.maxPlayer && newSite < Data.game.maxPlayer) {
                        val od = Data.game.playerManage.getPlayerArray(oldSite)
                        val nw = Data.game.playerManage.getPlayerArray(newSite)
                        if (od == null) {
                            return@register
                        }
                        if (nw == null) {
                            Data.game.playerManage.removePlayerArray(oldSite)
                            od.site = newSite
                            if (team > -1) {
                                od.team = team
                            }
                            Data.game.playerManage.setPlayerArray(newSite,od)
                        } else {
                            od.site = newSite
                            nw.site = oldSite
                            if (team > -1) {
                                od.team = team
                            }
                            Data.game.playerManage.setPlayerArray(newSite,od)
                            Data.game.playerManage.setPlayerArray(oldSite,nw)
                        }
                        sendTeamData()
                    }
                } else if (newSite == -3) {
                    val od = Data.game.playerManage.getPlayerArray(oldSite) ?: return@register
                    od.team = -3
                    sendTeamData()
                }
            }
        }
        handler.register("self_move", "<ToSerialNumber> <?>", "HIDE") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (Data.game.lockTeam) {
                return@register
            }
            if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
                player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                return@register
            }
            val newSite = args[0].toInt() - 1
            val team = args[1].toInt()
            if (newSite >= 0) {
                if (newSite < Data.game.maxPlayer) {
                val newSitePlayer = Data.game.playerManage.getPlayerArray(newSite)
                    if (newSitePlayer == null) {
                        Data.game.playerManage.removePlayerArray(player)
                        player.site = newSite
                        if (team > -1) {
                            player.team = team
                        }
                        Data.game.playerManage.setPlayerArray(newSite,player)
                        sendTeamData()
                    }
                }
            } else if (newSite == -3) {
                player.team = -3
                sendTeamData()
            }
        }
        handler.register("team", "<PlayerSiteNumber> <ToTeamNumber>", "HIDE") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (isAdmin(player)) {
                if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                    return@register
                }
                val playerSite = args[0].toInt() - 1
                val newSite = args[1].toInt() - 1
                if (playerSite < Data.game.maxPlayer && newSite < Data.game.maxPlayer) {
                    if (newSite > -1) {
                        val newTeamPlayer = Data.game.playerManage.getPlayerArray(playerSite)
                        if (newTeamPlayer != null) {
                            newTeamPlayer.team = newSite
                        }
                    }
                    sendTeamData()
                }
            }
        }
        handler.register("self_team", "<ToTeamNumber>", "HIDE") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (Data.game.lockTeam) {
                return@register
            }
            if (notIsNumeric(args[0])) {
                player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                return@register
            }
            val newSite = args[0].toInt() - 1
            if (newSite < Data.game.maxPlayer) {
                player.team = newSite
                sendTeamData()
            }
        }
    }
}