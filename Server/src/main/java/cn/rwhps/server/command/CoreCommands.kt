/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.command

import cn.rwhps.server.core.Call
import cn.rwhps.server.core.Core
import cn.rwhps.server.core.Initialization
import cn.rwhps.server.core.thread.Threads
import cn.rwhps.server.core.thread.TimeTaskData
import cn.rwhps.server.data.base.BaseConfig
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.data.mods.ModManage
import cn.rwhps.server.data.plugin.PluginManage
import cn.rwhps.server.func.StrCons
import cn.rwhps.server.game.Rules
import cn.rwhps.server.net.StartNet
import cn.rwhps.server.net.core.IRwHps
import cn.rwhps.server.net.core.ServiceLoader
import cn.rwhps.server.plugin.PluginsLoad
import cn.rwhps.server.plugin.center.PluginCenter
import cn.rwhps.server.util.game.CommandHandler
import cn.rwhps.server.util.log.Log
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
                    log["   " + command.text + (if (command.paramText.isEmpty()) "" else " ") + command.paramText + " - " + command.description.substring(1)]
                } else {
                    if ("HIDE" == command.description) {
                        continue
                    }
                    log["   " + command.text + (if (command.paramText.isEmpty()) "" else " ") + command.paramText + " - " + Data.i18NBundle.getinput(command.description)]
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
        handler.register("setlanguage","[HK/CN/RU/EN]" ,"serverCommands.setlanguage") { arg: Array<String>, _: StrCons ->
            Initialization.initServerLanguage(Data.core.settings,arg[0])
        }

        handler.register("reloadconfig", "serverCommands.reloadconfig") { Data.config = BaseConfig.stringToClass() }
        handler.register("reloadmods", "serverCommands.reloadmods") { _: Array<String>?, log: StrCons ->
            log[Data.i18NBundle.getinput("server.loadMod",ModManage.reLoadMods())]
        }
        handler.register("exit", "serverCommands.exit") { Core.exit() }
    }

    private fun registerInfo(handler: CommandHandler) {
        handler.register("plugins", "serverCommands.plugins") { _: Array<String>?, log: StrCons ->
            PluginManage.run { e: PluginsLoad.PluginLoadData? ->
                log[localeUtil.getinput("plugin.info", e!!.name, e.description, e.author, e.version)]
            }
        }
        handler.register("mods", "serverCommands.mods") { _: Array<String>?, log: StrCons ->
            ModManage.getModsList().each {
                log[localeUtil.getinput("mod.info", it)]
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

            NetStaticData.ServerNetType = IRwHps.NetType.ServerProtocol
            NetStaticData.RwHps =
                ServiceLoader.getService(ServiceLoader.ServiceType.IRwHps,"IRwHps", IRwHps.NetType::class.java)
                    .newInstance(IRwHps.NetType.ServerProtocol) as IRwHps

            TimeTaskData.CallTeamTask = Threads.newThreadService2({ Call.sendTeamData() }, 0, 2, TimeUnit.SECONDS)
            TimeTaskData.CallPingTask = Threads.newThreadService2({ Call.sendPlayerPing() }, 0, 2, TimeUnit.SECONDS)
/*
            NetStaticData.protocolData.setTypeConnect(TypeRwHpsBeta());
            NetStaticData.protocolData.setNetConnectProtocol(GameVersionServerBeta(ConnectionAgreement()),157);
            NetStaticData.protocolData.setNetConnectPacket(GameVersionPacketBeta(),"3.0.0");*/
            //NetStaticData.protocolData.setNetConnectProtocol(new GameVersionFFA(null),151);
            handler.handleMessage("startnetservice")
        }/*
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

            handler.handleMessage("startnetservice")
        }*/
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

            NetStaticData.ServerNetType = IRwHps.NetType.RelayProtocol
            NetStaticData.RwHps =
                ServiceLoader.getService(ServiceLoader.ServiceType.IRwHps,"IRwHps", IRwHps.NetType::class.java)
                    .newInstance(IRwHps.NetType.RelayProtocol) as IRwHps

            handler.handleMessage("startnetservice 5201 5500")
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

            NetStaticData.ServerNetType = IRwHps.NetType.RelayMulticastProtocol
            NetStaticData.RwHps =
                ServiceLoader.getService(ServiceLoader.ServiceType.IRwHps,"IRwHps", IRwHps.NetType::class.java)
                    .newInstance(IRwHps.NetType.RelayMulticastProtocol) as IRwHps

            handler.handleMessage("startnetservice 5200 5500")
        }


        handler.register("startnetservice", "[sPort] [ePort]","HIDE") { arg: Array<String>?, _: StrCons? ->
            val startNetTcp = StartNet()
            //val startNetTcp = StartNet(StartGamePortDivider::class.java)
            NetStaticData.startNet.add(startNetTcp)
            Threads.newThreadCoreNet {
                if (arg != null && arg.size > 1) {
                    startNetTcp.openPort(Data.config.Port, arg[0].toInt(), arg[1].toInt())
                } else {
                    startNetTcp.openPort(Data.config.Port)
                }
            }
            /*
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
            }*/
        }
    }

    companion object {
        private val localeUtil = Data.i18NBundle
    }

    init {
        registerCore(handler)
        registerCorex(handler)
        registerInfo(handler)
        registerStartServer(handler)


        // Test (孵化器）
        handler.register("log", "[a...]", "serverCommands.exit") { _: Array<String>, _: StrCons ->
        }
        handler.register("logg", "<1>", "serverCommands.exit") { arg: Array<String>, _: StrCons ->
        }
        handler.register("kc", "<1>", "serverCommands.exit") { arg: Array<String>, _: StrCons ->
            val site = arg[0].toInt() - 1
            val player = Data.game.playerManage.getPlayerArray(site)
            player!!.con!!.disconnect()
        }
    }
}