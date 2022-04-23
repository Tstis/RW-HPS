/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server

import cn.rwhps.server.command.CoreCommands
import cn.rwhps.server.command.LogCommands
import cn.rwhps.server.core.Core
import cn.rwhps.server.core.Initialization
import cn.rwhps.server.core.thread.Threads.newThreadCore
import cn.rwhps.server.custom.LoadCoreCustomPlugin
import cn.rwhps.server.data.base.BaseConfig
import cn.rwhps.server.data.base.BaseTestConfig
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
 * @author RW-HPS/Dr
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {

        System.setProperty("file.encoding","UTF-8")

        // 强制 UTF-8 我不愿意解决奇奇怪怪的问题
        if (!System.getProperty("file.encoding").equals("UTF-8",ignoreCase = true)) {
            clog("Please use UTF-8 !!!  -> java -Dfile.encoding=UTF-8 -jar Server.jar")
            clog("For non-UTF8 problems, please solve it yourself")
            Core.mandatoryExit()
        }


        Initialization()

        set("ALL")
        setCopyPrint(true)


        Logger.getLogger("io.netty").level = Level.OFF

        println(Data.i18NBundle.getinput("server.login"))
        clog("Load ing...")

        setFilePath(if (args.isNotEmpty()) decodeString(args[0]) else null)

        Data.config = BaseConfig.stringToClass()
        Data.configTest = BaseTestConfig.stringToClass()
        Data.core.load()

        clog(Data.i18NBundle.getinput("server.hi"))
        clog(Data.i18NBundle.getinput("server.project.url"))

        /* 命令加载 */
        CoreCommands(Data.SERVER_COMMAND)
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
        clog(Data.i18NBundle.getinput("server.loadMod",ModManage.load(getFolder(Data.Plugin_Mods_Path))))
        ModManage.loadUnits()
        //ModManage.test()


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

        Data.config.DockerSupCommand.forEach {
            val responseDockerSupCommand = Data.SERVER_COMMAND.handleMessage(it, StrCons { obj: String -> clog(obj) })
            if (responseDockerSupCommand != null && responseDockerSupCommand.type != CommandHandler.ResponseType.noCommand) {
                if (responseDockerSupCommand.type != CommandHandler.ResponseType.valid) {
                    clog("Please check the command , Unable to use StartCommand inside Config to start the server")
                }
            }
        }
    }

    private fun inputMonitor() {
        val instreamCommandReader = System.`in`
        if (instreamCommandReader == null) {
            Log.fatal("inputMonitor Null & listen command denied")
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

/**
                       ::
                      :;J7, :,                        ::;7:
                      ,ivYi, ,                       ;LLLFS:
                      :iv7Yi                       :7ri;j5PL
                     ,:ivYLvr                    ,ivrrirrY2X,
                    :;r@Wwz.7r:                :ivu@kexianli.
                    :iL7::,:::iiirii:ii;::::,,irvF7rvvLujL7ur
                   ri::,:,::i:iiiiiii:i:irrv177JX7rYXqZEkvv17
                ;i:, , ::::iirrririi:i:::iiir2XXvii;L8OGJr71i
              :,, ,,:   ,::ir@mingyi.irii:i:::j1jri7ZBOS7ivv,
                 ,::,    ::rv77iiiriii:iii:i::,rvLq@huhao.Li
             ,,      ,, ,:ir7ir::,:::i;ir:::i:i::rSGGYri712:
           :::  ,v7r:: ::rrv77:, ,, ,:i7rrii:::::, ir7ri7Lri
          ,     2OBBOi,iiir;r::        ,irriiii::,, ,iv7Luur:
        ,,     i78MBBi,:,:::,:,  :7FSL: ,iriii:::i::,,:rLqXv::
        :      iuMMP: :,:::,:ii;2GY7OBB0viiii:i:iii:i:::iJqL;::
       ,     ::::i   ,,,,, ::LuBBu BBBBBErii:i:i:i:i:i:i:r77ii
      ,       :       , ,,:::rruBZ1MBBqi, :,,,:::,::::::iiriri:
     ,               ,,,,::::i:  @arqiao.       ,:,, ,:::ii;i7:
    :,       rjujLYLi   ,,:::::,:::::::::,,   ,:i,:,,,,,::i:iii
    ::      BBBBBBBBB0,    ,,::: , ,:::::: ,      ,,,, ,,:::::::
    i,  ,  ,8BMMBBBBBBi     ,,:,,     ,,, , ,   , , , :,::ii::i::
    :      iZMOMOMBBM2::::::::::,,,,     ,,,,,,:,,,::::i:irr:i:::,
    i   ,,:;u0MBMOG1L:::i::::::  ,,,::,   ,,, ::::::i:i:iirii:i:i:
    :    ,iuUuuXUkFu7i:iii:i:::, :,:,: ::::::::i:i:::::iirr7iiri::
    :     :rk@Yizero.i:::::, ,:ii:::::::i:::::i::,::::iirrriiiri::,
     :      5BMBBBBBBSr:,::rv2kuii:::iii::,:i:,, , ,,:,:i@petermu.,
          , :r50EZ8MBBBBGOBBBZP7::::i::,:::::,: :,:,::i;rrririiii::
              :jujYY7LS0ujJL7r::,::i::,::::::::::::::iirirrrrrrr:ii:
           ,:  :@kevensun.:,:,,,::::i:i:::::,,::::::iir;ii;7v77;ii;i,
           ,,,     ,,:,::::::i:iiiii:i::::,, ::::iiiir@xingjief.r;7:i,
        , , ,,,:,,::::::::iiiiiiiiii:,:,:::::::::iiir;ri7vL77rrirri::
         :,, , ::::::::i:::i:::i:i::,,,,,:,::i:i:::iir;@Secbone.ii:::
 */