/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.command

import com.github.dr.rwserver.Main
import com.github.dr.rwserver.core.Call
import com.github.dr.rwserver.core.Call.sendMessage
import com.github.dr.rwserver.core.Call.sendSystemMessage
import com.github.dr.rwserver.core.Call.sendTeamData
import com.github.dr.rwserver.core.Call.upDataGameData
import com.github.dr.rwserver.core.Core.exit
import com.github.dr.rwserver.core.NetServer
import com.github.dr.rwserver.core.thread.Threads.newThreadCore
import com.github.dr.rwserver.core.thread.Threads.newThreadService2
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.data.global.Relay
import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.data.plugin.PluginManage.run
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.game.EventType.GameOverEvent
import com.github.dr.rwserver.game.EventType.PlayerBanEvent
import com.github.dr.rwserver.game.Rules
import com.github.dr.rwserver.net.game.ConnectionAgreement
import com.github.dr.rwserver.net.game.StartNet
import com.github.dr.rwserver.net.netconnectprotocol.TypeRelay
import com.github.dr.rwserver.net.netconnectprotocol.TypeRelayRebroadcast
import com.github.dr.rwserver.net.netconnectprotocol.TypeRwHps
import com.github.dr.rwserver.net.netconnectprotocol.realize.*
import com.github.dr.rwserver.plugin.PluginsLoad.PluginLoadData
import com.github.dr.rwserver.plugin.center.PluginCenter
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.ExtractUtil.ipToLong
import com.github.dr.rwserver.util.Time.getTimeFutureMillis
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log.error
import com.github.dr.rwserver.util.log.Log.set
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Dr
 */
class ServerCommands(handler: CommandHandler) {
    private fun registerCore(handler: CommandHandler) {
        handler.register("help", "serverCommands.help") { _: Array<String>?, log: StrCons ->
            log["Commands:"]
            for (command in handler.commandList) {
                if (command.description.startsWith("#")) {
                    log["   " + command.text + (if (command.paramText.isEmpty()) "" else " ") + command.paramText + " - " + command.description.substring(
                        1
                    )]
                } else {
                    log["   " + command.text + (if (command.paramText.isEmpty()) "" else " ") + command.paramText + " - " + Data.localeUtil.getinput(
                        command.description
                    )]
                }
            }
        }

        /*
        @DidNotFinish
        handler.register("stop", "serverCommands.stop") { _: Array<String>?, log: StrCons ->
            log["Stop Server. end"]
            NetServer.closeServer()
        }
         */

        handler.register("version", "serverCommands.version") { _: Array<String>?, log: StrCons ->
            log[localeUtil.getinput("status.versionS", Data.core.javaHeap / 1024 / 1024, Data.SERVER_CORE_VERSION)]
        }
        handler.register("exit", "serverCommands.exit") { _: Array<String>?, _: StrCons ->
            exit()
        }
        handler.register("restart", "serverCommands.restart") { _: Array<String>?, _: StrCons ->
            NetServer.closeServer()
            Data.SERVER_COMMAND.handleMessage("start")
        }
        handler.register("start", "serverCommands.start") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.startNet.size() > 0) {
                log["The server is not closed, please close"]
                return@register
            }

            /* Register Server Protocol Command */
            registerPlayerCommand(handler)

            set(Data.config.Log.uppercase(Locale.getDefault()))
            Data.game = Rules(Data.config)
            Data.game.init()
            newThreadService2({ sendTeamData() } , 0, 2, TimeUnit.SECONDS, "GameTeam")
            newThreadService2({ Call.sendPlayerPing() }, 0, 2, TimeUnit.SECONDS, "GamePing")

            NetStaticData.protocolData.setTypeConnect(TypeRwHps(GameVersionServer(ConnectionAgreement())))
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")
/*
            NetStaticData.protocolData.setTypeConnect(TypeRwHpsBeta());
            NetStaticData.protocolData.setNetConnectProtocol(GameVersionServerBeta(ConnectionAgreement()),157);
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacketBeta(),"3.0.0");*/
            //NetStaticData.protocolData.setNetConnectProtocol(new GameVersionFFA(null),151);
            newThreadCore {
                val startNet = StartNet()
                NetStaticData.startNet.add(startNet)
                startNet.openPort(Data.config.Port)
            }
            if (Data.config.UDPSupport) {
                newThreadCore {
                    try {
                        val startNet = StartNet()
                        NetStaticData.startNet.add(startNet)
                        startNet.startUdp(Data.config.Port)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        handler.register("startffa", "serverCommands.start.ffa") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.startNet.size() > 0) {
                log["The server is not closed, please close"]
                return@register
            }

            /* Register Server Protocol Command */
            registerPlayerCommand(handler)

            set(Data.config.Log.uppercase(Locale.getDefault()))
            Data.game = Rules(Data.config)
            Data.game.init()
            newThreadService2({ sendTeamData() } , 0, 2, TimeUnit.SECONDS, "GameTeam")
            newThreadService2({ Call.sendPlayerPing() }, 0, 2, TimeUnit.SECONDS, "GamePing")
            NetStaticData.protocolData.setTypeConnect(TypeRwHps(GameVersionFFA(ConnectionAgreement())))
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")
            newThreadCore {
                val startNet = StartNet()
                NetStaticData.startNet.add(startNet)
                startNet.openPort(Data.config.Port)
            }
            if (Data.config.UDPSupport) {
                newThreadCore {
                    try {
                        val startNet = StartNet()
                        NetStaticData.startNet.add(startNet)
                        startNet.startUdp(Data.config.Port)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        handler.register("startrelay", "serverCommands.start") { _: Array<String>?, _: StrCons ->
            set(Data.config.Log.uppercase(Locale.getDefault()))
            Data.game = Rules(Data.config)
            Data.game.init()

            NetStaticData.protocolData.setTypeConnect(TypeRelay(GameVersionRelay(ConnectionAgreement())))
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")

            val startNetTcp = StartNet()
            NetStaticData.startNet.add(startNetTcp)
            newThreadCore { startNetTcp.openPort(Data.config.Port) }
            if (Data.config.UDPSupport) {
                newThreadCore {
                    try {
                        val startNet = StartNet()
                        NetStaticData.startNet.add(startNet)
                        startNet.startUdp(Data.config.Port)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        handler.register("startrelaytest", "serverCommands.start") { _: Array<String>?, _: StrCons ->
            set(Data.config.Log.uppercase(Locale.getDefault()))
            Data.game = Rules(Data.config)
            Data.game.init()
            
            NetStaticData.protocolData.setTypeConnect(TypeRelayRebroadcast(GameVersionRelayRebroadcast(ConnectionAgreement())))
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")

            val startNetTcp = StartNet()
            NetStaticData.startNet.add(startNetTcp)
            newThreadCore { startNetTcp.openPort(Data.config.Port) }
            if (Data.config.UDPSupport) {
                newThreadCore {
                    try {
                        val startNet = StartNet()
                        NetStaticData.startNet.add(startNet)
                        startNet.startUdp(Data.config.Port)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun registerInfo(handler: CommandHandler) {
        handler.register("plugins", "serverCommands.plugins") { _: Array<String>?, log: StrCons ->
            run { e: PluginLoadData? ->
                log[localeUtil.getinput("plugin.info", e!!.name, e.description, e.author, e.version)]
            }
        }
        handler.register("mods", "serverCommands.mods") { _: Array<String>?, log: StrCons ->
            val seqCache = Seq<String>()
            Data.core.unitBase64.each { str ->
                val unitData = str.split("%#%")
                if (unitData.size > 2 && !seqCache.contains(unitData[2])) {
                    seqCache.add(unitData[2])
                    log[localeUtil.getinput("mod.info", unitData[2])]
                }
            }
        }
        handler.register("maps", "serverCommands.maps") { _: Array<String>?, log: StrCons ->
            val response = StringBuilder()
            val i = AtomicInteger(0)
            Data.game.mapsData.keys().forEach { k: String? ->
                response.append(localeUtil.getinput("maps.info", i.get(), k)).append(LINE_SEPARATOR)
                i.getAndIncrement()
            }
            log[response.toString()]
        }
    }

    private fun registerPlayerCommand(handler: CommandHandler) {
        handler.register("players", "serverCommands.players") { _: Array<String>?, log: StrCons ->
            if (Data.game.playerManage.playerGroup.size() == 0) {
                log["No players are currently in the server."]
            } else {
                log["Players: {0}", Data.game.playerManage.playerGroup.size()]
                val data = StringBuilder()
                for (player in Data.game.playerManage.playerGroup) {
                    data.append(LINE_SEPARATOR)
                        .append(player.name)
                        .append(" / ")
                        .append("ID: ").append(player.uuid)
                        .append(" / ")
                        .append("IP: ").append(player.con!!.ip)
                        .append(" / ")
                        .append("Protocol: ").append(player.con!!.useConnectionAgreement)
                        .append(" / ")
                        .append("Admin: ").append(player.isAdmin)
                }
                log[data.toString()]
            }
        }
        handler.register("say", "<text...>", "serverCommands.say") { arg: Array<String>, _: StrCons ->
            val response = StringBuilder(arg[0])
            var i = 1
            val lens = arg.size
            while (i < lens) {
                response.append(" ").append(arg[i])
                i++
            }
            if (Data.config.SingleUserRelay) {
                try {
                    NetStaticData.relay.groupNet.broadcast(
                        NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(
                            response.toString().replace("<>", "")
                        )
                    )
                } catch (ignored: IOException) {
                }
            }
            sendSystemMessage(response.toString().replace("<>", ""))
        }
        handler.register("gameover", "serverCommands.gameover") { _: Array<String>?, _: StrCons ->
            Events.fire(GameOverEvent())
        }
        handler.register("clearbanip", "serverCommands.clearbanip") { _: Array<String>?, _: StrCons ->
            Data.core.admin.bannedIPs.clear()
        }
        handler.register("admin", "<add/remove> <PlayerPosition>", "serverCommands.admin") { arg: Array<String>, log: StrCons ->
            if (Data.game.isStartGame) {
                log[localeUtil.getinput("err.startGame")]
                return@register
            }
            if (!("add" == arg[0] || "remove" == arg[0])) {
                log["Second parameter must be either 'add' or 'remove'."]
                return@register
            }
            val add = "add" == arg[0]
            val site = arg[1].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                if (add) {
                    Data.core.admin.addAdmin(player.uuid)
                } else {
                    Data.core.admin.removeAdmin(player.uuid)
                }
                player.isAdmin = add
                try {
                    player.con!!.sendServerInfo(false)
                } catch (e: IOException) {
                    error("[Player] Send Server Info Error", e)
                }
                sendTeamData()
                log["Changed admin status of player: {0}", player.name]
            }
        }
        handler.register("clearbanuuid", "serverCommands.clearbanuuid") { _: Array<String>?, _: StrCons ->
            Data.core.admin.bannedUUIDs.clear()
        }
        handler.register("clearbanall", "serverCommands.clearbanall") { _: Array<String>?, _: StrCons ->
            Data.core.admin.bannedIPs.clear()
            Data.core.admin.bannedUUIDs.clear()
        }
        handler.register("ban", "<PlayerSerialNumber>", "serverCommands.ban") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                Events.fire(PlayerBanEvent(player))
            }
        }
        handler.register("mute", "<PlayerSerialNumber> [Time(s)]", "serverCommands.mute") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                //Data.game.playerData[site].muteTime = getLocalTimeFromU(Long.parseLong(arg[1])*1000L);
                player.muteTime = getTimeFutureMillis(43200 * 1000L)
            }
        }
        handler.register("kick", "<PlayerSerialNumber> [time]", "serverCommands.kick") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                player.kickTime = if (arg.size > 1) getTimeFutureMillis(
                    arg[1].toInt() * 1000L
                ) else getTimeFutureMillis(60 * 1000L)
                try {
                    player.con!!.sendKick(localeUtil.getinput("kick.you"))
                } catch (e: IOException) {
                    error("[Player] Send Kick Player Error", e)
                }
            }
        }
        handler.register("isafk", "<off/on>", "serverCommands.isAfk") { arg: Array<String>, _: StrCons ->
            if (Data.config.OneAdmin) {
                Data.game.isAfk = "on" == arg[0]
            }
        }
        handler.register("maplock", "<off/on>", "serverCommands.isAfk") { arg: Array<String>, _: StrCons ->
            Data.game.mapLock = "on" == arg[0]
        }
        handler.register("kill", "<PlayerSerialNumber>", "serverCommands.kill") { arg: Array<String>, log: StrCons ->
            if (Data.game.isStartGame) {
                val site = arg[0].toInt() - 1
                val player = Data.game.playerManage.getPlayerArray(site)
                if (player != null) {
                    player.con!!.sendSurrender()
                }
            } else {
                log[localeUtil.getinput("err.noStartGame")]
            }
        }
        handler.register("giveadmin", "<PlayerSerialNumber...>", "serverCommands.giveadmin") { arg: Array<String>, _: StrCons ->
            Data.game.playerManage.playerGroup.each(
                { p: Player -> p.isAdmin }) { i: Player ->
                val player = Data.game.playerManage.getPlayerArray(arg[0].toInt())
                if (player != null) {
                    i.isAdmin = false
                    player.isAdmin = true
                    upDataGameData()
                    sendMessage(player, localeUtil.getinput("give.ok", player.name))
                }
            }
        }
        handler.register("clearmuteall", "serverCommands.clearmuteall") { _: Array<String>?, _: StrCons ->
            Data.game.playerManage.playerGroup.each { e: Player -> e.muteTime = 0 }
        }
        handler.register("cleanmods", "serverCommands.cleanmods") { _: Array<String>?, _: StrCons ->
            Data.core.unitBase64.clear()
            Data.core.save()
            Main.loadNetCore()
        }
        handler.register("reloadmaps", "serverCommands.reloadmaps") { _: Array<String>?, log: StrCons ->
            val size = Data.game.mapsData.size
            Data.game.mapsData.clear()
            Data.game.checkMaps()
            log["Reload {0}:{1}", size, Data.game.mapsData.size]
        }
    }

    private fun registerRelayCommand(handler: CommandHandler) {
        handler.register("players", "serverCommands.players") { _: Array<String>?, log: StrCons ->
            if (Data.game.playerManage.playerGroup.size() == 0) {
                log["No players are currently in the server."]
            } else {
                log["Players: {0}", Data.game.playerManage.playerGroup.size()]
                val data = StringBuilder()
                for (player in Data.game.playerManage.playerGroup) {
                    data.append(LINE_SEPARATOR)
                        .append(player.name)
                        .append(" / ")
                        .append("ID: ").append(player.uuid)
                        .append(" / ")
                        .append("IP: ").append(player.con!!.ip)
                        .append(" / ")
                        .append("Protocol: ").append(player.con!!.useConnectionAgreement)
                        .append(" / ")
                        .append("Admin: ").append(player.isAdmin)
                }
                log[data.toString()]
            }
        }
        handler.register("ban", "<PlayerSerialNumber>", "serverCommands.ban") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            if (player != null) {
                Events.fire(PlayerBanEvent(player))
            }
        }
    }

    private fun registerCorex(handler: CommandHandler) {
        handler.register("plugin", "<TEXT...>", "serverCommands.upserverlist") { arg: Array<String>, log: StrCons ->
            PluginCenter.pluginCenter.command(
                arg[0], log
            )
        }
    }

    companion object {
        private val localeUtil = Data.localeUtil
    }

    init {
        registerCore(handler)
        registerCorex(handler)
        registerInfo(handler)
        handler.register("log", "[a...]", "serverCommands.exit") { arg: Array<String>, _: StrCons ->
            Data.LOG_COMMAND.handleMessage(
                arg[0], null
            )
        }
        handler.register("logg", "<1> <2>", "serverCommands.exit") { arg: Array<String>, _: StrCons ->
            Data.LOG_COMMAND.handleMessage(
                arg[0] + " " + arg[1], null
            )
        }
        handler.register("kc", "<1>", "serverCommands.exit") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            player!!.con!!.disconnect()
        }

        handler.register("banrelay", "<id>", "") { arg: Array<String>, _: StrCons ->
            val relay = Relay.getRelay(arg[0])
            relay.groupNet.disconnect()
            relay.sendMsg("您被管理员Ban 请勿占用公共资源")
            val ip = relay.admin!!.ip
            Data.core.admin.bannedIP24.add(ipToLong(ip))
            relay.admin!!.disconnect()
            println("OK $ip")
        }
    }
}