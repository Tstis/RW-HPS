/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server

import cn.rwhps.server.command.ClientCommands
import cn.rwhps.server.command.CoreCommands
import cn.rwhps.server.command.LogCommands
import cn.rwhps.server.core.Initialization
import cn.rwhps.server.core.thread.Threads.newThreadCore
import cn.rwhps.server.custom.LoadCoreCustomPlugin
import cn.rwhps.server.data.base.BaseConfig
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.mods.ModManage
import cn.rwhps.server.data.plugin.PluginEventManage.Companion.add
import cn.rwhps.server.data.plugin.PluginManage
import cn.rwhps.server.data.plugin.PluginManage.init
import cn.rwhps.server.data.plugin.PluginManage.loadSize
import cn.rwhps.server.data.plugin.PluginManage.runInit
import cn.rwhps.server.data.plugin.PluginManage.runOnEnable
import cn.rwhps.server.data.plugin.PluginManage.runRegisterEvents
import cn.rwhps.server.func.StrCons
import cn.rwhps.server.game.Event
import cn.rwhps.server.game.EventGlobal
import cn.rwhps.server.game.event.EventGlobalType.ServerLoadEvent
import cn.rwhps.server.util.encryption.Base64.decodeString
import cn.rwhps.server.util.file.FileUtil.Companion.getFolder
import cn.rwhps.server.util.file.FileUtil.Companion.setFilePath
import cn.rwhps.server.util.game.CommandHandler
import cn.rwhps.server.util.game.Events
import cn.rwhps.server.util.io.IoReadConversion
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.Log.clog
import cn.rwhps.server.util.log.Log.info
import cn.rwhps.server.util.log.Log.set
import cn.rwhps.server.util.log.Log.setCopyPrint
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Welcome to Bugs RW-HPS !
 * Welcome to the RW-HPS Team !
 */

/**
 * @author Dr
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Initialization()

        set("ALL")
        setCopyPrint(true)


        Logger.getLogger("io.netty").level = Level.OFF

        println(Data.i18NBundle.getinput("server.login"))
        clog("Load ing...")

        setFilePath(if (args.isNotEmpty()) decodeString(args[0]) else null)

        Data.config = BaseConfig.stringToClass()
        Data.core.load()

        clog(Data.i18NBundle.getinput("server.hi"))
        clog(Data.i18NBundle.getinput("server.project.url"))

        /* 命令加载 */
        CoreCommands(Data.SERVER_COMMAND)
        ClientCommands(Data.CLIENT_COMMAND)
        LogCommands(Data.LOG_COMMAND)
        clog(Data.i18NBundle.getinput("server.load.command"))

        /* Event加载 */
        add(Event())
        add(EventGlobal())
        clog(Data.i18NBundle.getinput("server.load.events"))

        /* 初始化Plugin */
        init(getFolder(Data.Plugin_Plugins_Path))
        LoadCoreCustomPlugin()

        runOnEnable()
        runRegisterEvents()
        PluginManage.runRegisterCoreCommands(Data.SERVER_COMMAND)
        PluginManage.runRegisterClientCommands(Data.CLIENT_COMMAND)


        /* Load Mod */
        ModManage.loadCore()
        clog(Data.i18NBundle.getinput("server.loadMod",ModManage.load(getFolder(Data.Plugin_Mods_Path))))
        ModManage.loadUnits()


        /* 按键监听 */
        newThreadCore{ inputMonitor() }

        /* 加载完毕 */
        Events.fire(ServerLoadEvent())

        /* 初始化Plugin Init */
        runInit()
        clog(Data.i18NBundle.getinput("server.load.end"))
        clog(Data.i18NBundle.getinput("server.loadPlugin", loadSize))

        /* 默认直接启动服务器 */
        val response = Data.SERVER_COMMAND.handleMessage(Data.config.DefStartCommand, StrCons { obj: String -> clog(obj) })
        if (response != null && response.type != CommandHandler.ResponseType.noCommand) {
            if (response.type != CommandHandler.ResponseType.valid) {
                clog("Please check the command , Unable to use StartCommand inside Config to start the server")
            }
        }

        clog("Server Run PID : ${Data.core.pid}")
    }

    private fun inputMonitor() {
        val instreamCommandReader = System.`in`
        if (instreamCommandReader == null) {
            Log.fatal("inputMonitor Null")
            return
        }
        val bufferedReader = IoReadConversion.streamBufferRead(instreamCommandReader)
        while (true) {
            try {
                val str = bufferedReader.readLine()
                val response = Data.SERVER_COMMAND.handleMessage(str,
                    StrCons { obj: String -> clog(obj) })
                if (response != null && response.type != CommandHandler.ResponseType.noCommand) {
                    if (response.type != CommandHandler.ResponseType.valid) {
                        val text = when (response.type) {
                            CommandHandler.ResponseType.manyArguments -> {
                                "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText
                            }
                            CommandHandler.ResponseType.fewArguments -> {
                                "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText
                            }
                            else -> {
                                "Unknown command. Check help"
                            }
                        }
                        clog(text)
                    }
                }
            } catch (e: Exception) {
                clog("InputMonitor Error")
                info(e)
            }
        }
    }
}

// 你的身体是为你服务的 而不是RW-HPS(?)  !