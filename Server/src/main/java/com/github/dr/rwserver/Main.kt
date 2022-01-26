/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver

import com.github.dr.rwserver.command.ClientCommands
import com.github.dr.rwserver.command.LogCommands
import com.github.dr.rwserver.command.ServerCommands
import com.github.dr.rwserver.core.Core.mandatoryExit
import com.github.dr.rwserver.core.Initialization
import com.github.dr.rwserver.core.thread.Threads.newThreadCore
import com.github.dr.rwserver.custom.UpListCustom
import com.github.dr.rwserver.data.base.BaseConfig
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.data.plugin.PluginEventManage.Companion.add
import com.github.dr.rwserver.data.plugin.PluginManage.init
import com.github.dr.rwserver.data.plugin.PluginManage.loadSize
import com.github.dr.rwserver.data.plugin.PluginManage.runInit
import com.github.dr.rwserver.data.plugin.PluginManage.runOnEnable
import com.github.dr.rwserver.data.plugin.PluginManage.runRegisterClientCommands
import com.github.dr.rwserver.data.plugin.PluginManage.runRegisterEvents
import com.github.dr.rwserver.data.plugin.PluginManage.runRegisterServerCommands
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.game.Event
import com.github.dr.rwserver.game.EventType.ServerLoadEvent
import com.github.dr.rwserver.io.output.GameOutputStream
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionPacket
import com.github.dr.rwserver.util.encryption.Autograph
import com.github.dr.rwserver.util.encryption.Base64.decodeString
import com.github.dr.rwserver.util.file.FileUtil.Companion.getFolder
import com.github.dr.rwserver.util.file.FileUtil.Companion.readFileListString
import com.github.dr.rwserver.util.file.FileUtil.Companion.setFilePath
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.io.IoReadConversion.streamBufferRead
import com.github.dr.rwserver.util.log.Log.clog
import com.github.dr.rwserver.util.log.Log.error
import com.github.dr.rwserver.util.log.Log.info
import com.github.dr.rwserver.util.log.Log.set
import com.github.dr.rwserver.util.log.Log.setCopyPrint
import com.github.dr.rwserver.util.log.Log.skipping
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Dr
 */
object Main {
    /*
	 * TODO 防逆向
	 * 设置多个检查点, 定期检查, 如果发现问题就加密或混淆部分数据
	 */
    @JvmStatic
    fun main(args: Array<String>) {
        Initialization()
        set("ALL")
        setCopyPrint(true)
        Logger.getLogger("io.netty").level = Level.OFF
        println(Data.localeUtil.getinput("server.login"))
        clog("Load ing...")


        /*
		 * TODO
		 * Prevent signature modification */
        /* CP #1 */
        if (!Autograph().verify(Main::class.java.protectionDomain.codeSource.location)) {
            skipping("The server was modified and refused to start")
            mandatoryExit()
        }
        setFilePath(if (args.isNotEmpty()) decodeString(args[0]) else null)

        Data.config = BaseConfig.stringToClass()
        Data.core.load()

        //loadCoreJar(if (args.size > 1) decodeString(args[1]) else null)

        clog(Data.localeUtil.getinput("server.hi"))
        clog(Data.localeUtil.getinput("server.project.url"))

        /* 命令加载 */
        ServerCommands(Data.SERVER_COMMAND)
        ClientCommands(Data.CLIENT_COMMAND)
        LogCommands(Data.LOG_COMMAND)
        clog(Data.localeUtil.getinput("server.load.command"))

        /* Event加载 */
        add(Event())
        clog(Data.localeUtil.getinput("server.load.events"))

        /* 初始化Plugin */
        init(getFolder(Data.Plugin_Plugins_Path))
        runOnEnable()
        runRegisterClientCommands(Data.CLIENT_COMMAND)
        runRegisterServerCommands(Data.SERVER_COMMAND)
        runRegisterEvents()

        /* Core Net */
        loadNetCore()

        /* Load Save Unit */
        loadUnitList()

        /* 按键监听 */
        newThreadCore{ buttonMonitoring() }

        /* 加载完毕 */
        Events.fire(ServerLoadEvent())

        /* 初始化Plugin Init */runInit()
        clog(Data.localeUtil.getinput("server.load.end"))
        clog(Data.localeUtil.getinput("server.loadPlugin", loadSize))

        /* 默认直接启动服务器 */
        Data.SERVER_COMMAND.handleMessage(Data.config.DefStartCommand, StrCons { obj: String -> clog(obj) })

        clog("Server Run PID : ${Data.core.pid}")

        UpListCustom(Data.SERVER_COMMAND);
    }

    private fun buttonMonitoring() {
        val bufferedReader = streamBufferRead(System.`in`)
        var count = 0
        while (true) {
            try {
                val str = bufferedReader.readLine()
                val response = Data.SERVER_COMMAND.handleMessage(str, StrCons { obj: String -> clog(obj) })
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
                clog("Error")
                info(e)

                /* nohup Error */
                if (10 < count++) {
                    try {
                        bufferedReader.close()
                    } catch (ignored: Exception) {
                    }
                    return
                }
            }
        }
    }

    fun loadNetCore() {
        NetStaticData.protocolData.setNetConnectPacket(GameVersionPacket(), "2.0.0")
        try {
            val stream: GameOutputStream = Data.utilData
            stream.reset()
            stream.writeInt(1)
            val list = readFileListString(Objects.requireNonNull(
                Main::class.java.getResourceAsStream("/unitData-114")))
            stream.writeInt(list.size())
            var unitData: Array<String>
            for (str in list) {
                unitData = str.split("%#%").toTypedArray()
                stream.writeString(unitData[0])
                stream.writeInt(unitData[1].toInt())
                stream.writeBoolean(true)
                stream.writeBoolean(false)
                stream.writeLong(0)
                stream.writeLong(0)
            }
        } catch (e: Exception) {
            error(e)
        }
        clog("Load OK 1.15 Protocol")
    }

    fun loadUnitList() {
        if (Data.core.unitBase64.size() > 0) {
            try {
                //Data.utilData.buffer.reset();
                val stream: GameOutputStream = Data.utilData
                stream.reset()
                stream.writeInt(1)
                stream.writeInt(Data.core.unitBase64.size())
                var unitData: Array<String>
                for (str in Data.core.unitBase64) {
                    unitData = str.split("%#%").toTypedArray()
                    stream.writeString(unitData[0])
                    stream.writeInt(unitData[1].toInt())
                    stream.writeBoolean(true)
                    if (unitData.size > 2) {
                        stream.writeBoolean(true)
                        stream.writeString(unitData[2])
                    } else {
                        stream.writeBoolean(false)
                    }
                    stream.writeLong(0)
                    stream.writeLong(0)
                }
                clog("Load Mod Ok.")
            } catch (exp: Exception) {
                error("[Server] Load Setting Unit List Error", exp)
            }
        }
    }
}