/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.command

import com.github.dr.rwserver.core.Call
import com.github.dr.rwserver.core.Core
import com.github.dr.rwserver.core.thread.Threads
import com.github.dr.rwserver.core.thread.TimeTaskData
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.data.plugin.PluginManage
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.game.Rules
import com.github.dr.rwserver.net.StartNet
import com.github.dr.rwserver.net.core.ConnectionAgreement
import com.github.dr.rwserver.net.netconnectprotocol.TypeRelay
import com.github.dr.rwserver.net.netconnectprotocol.TypeRelayRebroadcast
import com.github.dr.rwserver.net.netconnectprotocol.TypeRwHps
import com.github.dr.rwserver.net.netconnectprotocol.realize.*
import com.github.dr.rwserver.plugin.PluginsLoad
import com.github.dr.rwserver.plugin.center.PluginCenter
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.log.Log
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Dr
 */
class CoreCommands(handler: CommandHandler) {
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
        @NeedHelp
        handler.register("stop", "serverCommands.stop") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.startNet.size() == 0) {
                log["Server does not start"]
                return@register
            }
            NetServer.closeServer()
        }*/

        handler.register("version", "serverCommands.version") { _: Array<String>?, log: StrCons ->
            log[localeUtil.getinput("status.versionS", Data.core.javaHeap / 1024 / 1024, Data.SERVER_CORE_VERSION)]
        }
        handler.register("exit", "serverCommands.exit") { _: Array<String>?, _: StrCons ->
            Core.exit()
        }
    }

    private fun registerInfo(handler: CommandHandler) {
        handler.register("plugins", "serverCommands.plugins") { _: Array<String>?, log: StrCons ->
            PluginManage.run { e: PluginsLoad.PluginLoadData? ->
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
                response.append(localeUtil.getinput("maps.info", i.get(), k)).append(Data.LINE_SEPARATOR)
                i.getAndIncrement()
            }
            log[response.toString()]
        }
    }

    private fun registerCorex(handler: CommandHandler) {
        handler.register("plugin", "<TEXT...>", "serverCommands.upserverlist") { arg: Array<String>, log: StrCons ->
            PluginCenter.pluginCenter.command(arg[0], log)
        }
    }

    private fun registerStartServer(handler: CommandHandler) {
        handler.register("start", "serverCommands.start") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.startNet.size() > 0) {
                log["The server is not closed, please close"]
                return@register
            }

            /* Register Server Protocol Command */
            ServerCommands(handler)

            Log.set(Data.config.Log.uppercase(Locale.getDefault()))
            Data.game = Rules(Data.config)
            Data.game.init()
            TimeTaskData.CallTeamTask = Threads.newThreadService2({ Call.sendTeamData() }, 0, 2, TimeUnit.SECONDS)
            TimeTaskData.CallPingTask = Threads.newThreadService2({ Call.sendPlayerPing() }, 0, 2, TimeUnit.SECONDS)

            NetStaticData.protocolData.setTypeConnect(TypeRwHps(GameVersionServer(ConnectionAgreement())))
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")
/*
            NetStaticData.protocolData.setTypeConnect(TypeRwHpsBeta());
            NetStaticData.protocolData.setNetConnectProtocol(GameVersionServerBeta(ConnectionAgreement()),157);
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacketBeta(),"3.0.0");*/
            //NetStaticData.protocolData.setNetConnectProtocol(new GameVersionFFA(null),151);
            Threads.newThreadCoreNet {
                val startNet = StartNet()
                NetStaticData.startNet.add(startNet)
                startNet.openPort(Data.config.Port)
            }
            if (Data.config.UDPSupport) {
                Threads.newThreadCoreNet {
                    try {
                        val startNet = StartNet()
                        NetStaticData.startNet.add(startNet)
                        startNet.startUdp(Data.config.Port)
                    } catch (e: Exception) {
                        Log.error(e)
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
            ServerCommands(handler)

            Log.set(Data.config.Log.uppercase(Locale.getDefault()))
            Data.game = Rules(Data.config)
            Data.game.init()
            TimeTaskData.CallTeamTask = Threads.newThreadService2({ Call.sendTeamData() }, 0, 2, TimeUnit.SECONDS)
            TimeTaskData.CallPingTask = Threads.newThreadService2({ Call.sendPlayerPing() }, 0, 2, TimeUnit.SECONDS)

            NetStaticData.protocolData.setTypeConnect(TypeRwHps(GameVersionFFA(ConnectionAgreement())))
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")

            Threads.newThreadCoreNet {
                val startNet = StartNet()
                NetStaticData.startNet.add(startNet)
                startNet.openPort(Data.config.Port)
            }
            if (Data.config.UDPSupport) {
                Threads.newThreadCoreNet {
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
        handler.register("startrelay", "serverCommands.start") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.startNet.size() > 0) {
                log["The server is not closed, please close"]
                return@register
            }

            /* Register Relay Protocol Command */
            RelayCommands(handler)

            Log.set(Data.config.Log.uppercase(Locale.getDefault()))
            Data.game = Rules(Data.config)
            Data.game.init()

            NetStaticData.protocolData.setTypeConnect(TypeRelay(GameVersionRelay(ConnectionAgreement())))
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")

            val startNetTcp = StartNet()
            NetStaticData.startNet.add(startNetTcp)
            Threads.newThreadCoreNet { startNetTcp.openPort(Data.config.Port, 5201, 5500) }
            if (Data.config.UDPSupport) {
                Threads.newThreadCoreNet {
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
        handler.register("startrelaytest", "serverCommands.start") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.startNet.size() > 0) {
                log["The server is not closed, please close"]
                return@register
            }

            /* Register Relay Protocol Command */
            RelayCommands(handler)

            Log.set(Data.config.Log.uppercase(Locale.getDefault()))
            Data.game = Rules(Data.config)
            Data.game.init()

            NetStaticData.protocolData.setTypeConnect(TypeRelayRebroadcast(GameVersionRelayRebroadcast(ConnectionAgreement())))
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")

            val startNetTcp = StartNet()
            NetStaticData.startNet.add(startNetTcp)
            Threads.newThreadCoreNet { startNetTcp.openPort(Data.config.Port, 5200, 5500) }
            if (Data.config.UDPSupport) {
                Threads.newThreadCoreNet {
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

    companion object {
        private val localeUtil = Data.localeUtil
    }

    init {
        registerCore(handler)
        registerCorex(handler)
        registerInfo(handler)
        registerStartServer(handler)

        handler.register("log", "[a...]", "serverCommands.exit") { arg: Array<String>, _: StrCons ->
            Data.LOG_COMMAND.handleMessage(arg[0], null)
        }
        handler.register("logg", "<1> <2>", "serverCommands.exit") { arg: Array<String>, _: StrCons ->
            Data.LOG_COMMAND.handleMessage(arg[0] + " " + arg[1], null)
        }
        handler.register("kc", "<1>", "serverCommands.exit") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            player!!.con!!.disconnect()
        }
    }
}