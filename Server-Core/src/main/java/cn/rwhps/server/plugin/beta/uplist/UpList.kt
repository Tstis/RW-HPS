/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.plugin.beta.uplist

import cn.rwhps.server.core.thread.CallTimeTask
import cn.rwhps.server.core.thread.Threads
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.json.Json
import cn.rwhps.server.func.StrCons
import cn.rwhps.server.net.HttpRequestOkHttp
import cn.rwhps.server.plugin.Plugin
import cn.rwhps.server.util.IpUtil
import cn.rwhps.server.util.IsUtil
import cn.rwhps.server.util.StringFilteringUtil.cutting
import cn.rwhps.server.util.encryption.Base64
import cn.rwhps.server.util.game.CommandHandler
import cn.rwhps.server.util.log.Log
import okhttp3.FormBody
import java.text.MessageFormat
import java.util.concurrent.TimeUnit

/**
 * 为 服务器进入官方列表提供API支持
 * 并不打算开放源码 避免被滥用 (已经被滥用了 指漫天AD)
 * API随Server版本而进行版本更新
 * @author RW-HPS/Dr
 */
internal class UpList : Plugin() {
    private var upServerList = false

    /* DATA Cache */
    private lateinit var serverID: String
    private lateinit var addData: String
    private lateinit var openData: String
    private lateinit var updateData: String
    private lateinit var removeData: String

    override fun init() {
        AddLang(this)
    }

    override fun registerCoreCommands(handler: CommandHandler) {
        handler.removeCommand("upserverlist")
        handler.removeCommand("upserverlistnew")

        handler.register("uplist","[command...]","serverCommands.upserverlist") { args: Array<String>?, log: StrCons ->
            if (args != null && args.isNotEmpty()) {
                when (args[0]) {
                    "add" -> { if (args.size > 1) add(log,args[1]) else add(log) }
                    "update" -> update()
                    "remove" -> remove(log)
                    "help" -> log[Data.i18NBundle.getinput("uplist.help")]
                    else -> log["Check UpList Command !"]
                }
            } else {
                log["Check UpList Command ! use 'uplist help'"]
            }
        }
    }

    private fun initUpListData(port: String?): Boolean {
        val formBody = FormBody.Builder()
        formBody.add("Passwd", IsUtil.notIsBlank(Data.game.passwd).toString())
        formBody.add("ServerName",Data.config.ServerName)
        formBody.add("Port", port ?:Data.config.Port.toString())
        formBody.add("MapName",Data.game.maps.mapName)
        formBody.add("PlayerSize",Data.game.playerManage.playerGroup.size().toString())
        formBody.add("PlayerMaxSize",Data.game.maxPlayer.toString())

        var resultUpList = HttpRequestOkHttp.doPost(Data.urlData.readString("Get.UpListData.Bak"), formBody)

        if (resultUpList.isBlank()) {
            resultUpList = HttpRequestOkHttp.doPost(Data.urlData.readString("Get.UpListData"), formBody)
        }

        if (resultUpList.isBlank()) {
            Log.error("[Get UPLIST Data Error] 意外错误 无法初始化")
            return false
        }

        if (resultUpList.startsWith("[-1]")) {
            Log.error("[Get UPLIST Data Error] Please Check API")
            return false
        } else if (resultUpList.startsWith("[-2]")) {
            Log.error("[Get UPLIST Data Error] IP prohibited")
            return false
        }

        val json = Json(resultUpList)
        Log.debug(resultUpList)

        serverID = Base64.decodeString(json.getData("id"))
        addData = Base64.decodeString(json.getData("add"))
        openData = Base64.decodeString(json.getData("open"))
        updateData = Base64.decodeString(json.getData("update"))
        removeData = Base64.decodeString(json.getData("remove"))

        Log.debug(serverID)
        Log.debug(addData)
        Log.debug(openData)
        Log.debug(updateData)

        Log.debug(resultUpList)
        return true
    }

    private fun add(log: StrCons, port: String? = null) {
        if (!upServerList) {
            if (initUpListData(port)) {
                Threads.newThreadCore { upServerList = true ; uplist() }
            }
        } else {
            log["Already on the list"]
        }
    }

    private fun uplist() {

        var privateIp = IpUtil.getPrivateIp()
        if (IsUtil.isBlank(privateIp)) {
            privateIp = "10.0.0.1"
        }

        val resultUp = MessageFormat(addData).format(arrayOf(cutting(Data.config.ServerName,10),privateIp, Data.game.maps.mapName))

        val addGs1 = HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", resultUp).contains(serverID)
        val addGs4 = HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", resultUp).contains(serverID)
        if (addGs1 || addGs4) {
            if (addGs1 && addGs4) {
                Log.clog(Data.i18NBundle.getinput("err.yesList"))
            } else {
                Log.clog(Data.i18NBundle.getinput("err.ynList"))
            }
        } else {
            Log.clog(Data.i18NBundle.getinput("err.noList"))
        }

        val checkPortGs1 = HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", openData).contains("true")
        val checkPortGs4 = HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", openData).contains("true")
        if (checkPortGs1 || checkPortGs4) {
            Log.clog(Data.i18NBundle.getinput("err.yesOpen"))
        } else {
            Log.clog(Data.i18NBundle.getinput("err.noOpen"))
        }

        Threads.newTimedTask(CallTimeTask.CustomUpServerListTask, 50, 50, TimeUnit.SECONDS) { update() }
    }

    private fun update() {
        val result0 = MessageFormat(updateData).format(arrayOf(
            cutting(Data.config.ServerName,12),
            if (IsUtil.isBlank(Data.config.Subtitle)) Data.game.maps.mapName else cutting(Data.config.Subtitle,20),
            if (Data.game.isStartGame) "ingame" else "battleroom",
            Data.game.playerManage.playerGroup.size().toString()))
        HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", result0)
        HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", result0)
    }

    private fun remove(log: StrCons) {
        if (upServerList) {
            if (Threads.closeTimeTask(CallTimeTask.CustomUpServerListTask) {
                    HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", removeData)
                    HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", removeData)
                }) {
                upServerList = false
                log["已删除"]
                return
            }
            log["删除失败, 无法停止线程"]
        } else {
            log["未上传 不需要删除"]
        }
    }


    /**
     * Inject multiple languages into the server
     * @author RW-HPS/Dr
     */
    private class AddLang(val plugin: Plugin) {
        init {
            help()
        }

        private fun help() {
            loadCN("uplist.help",
                """
        
        [uplist add] 服务器上传到列表 显示配置文件端口
        [uplist add (port)] 服务器上传到列表 服务器运行配置文件端口 显示自定义端口
        [uplist update] 立刻更新列表服务器信息
        [uplist remove] 取消服务器上传列表
        [uplist help] 获取帮助
        """.trimIndent())
            loadEN("uplist.help",
                """
        
        [uplist add] Server upload to list Show profile port
        [uplist add (port)] Server upload to list Server running profile port Display custom port
        [uplist update] Update list server information immediately
        [uplist remove] Cancel server upload list
        [uplist help] Get Help
        """.trimIndent())
            loadHK("uplist.help",
                """
        
        [uplist add] 服务器上传到列表 显示配置文件端口
        [uplist add (port)] 服务器上传到列表 服务器运行配置文件端口 显示自定义端口
        [uplist update] 立刻更新列表服务器信息
        [uplist remove] 取消服务器上传列表
        [uplist help] 获取帮助
        """.trimIndent())
            loadRU("uplist.help",
                """
        
        [uplist add] Загрузка сервера в список Показать порт профиля
        [uplist add (port)] Загрузка сервера в список Порт запущенного профиля сервера Показать пользовательские порты
        [uplist update] Немедленное обновление информации сервера списка
        [uplist remove] Отмена загрузки сервера в список
        [uplist help] Получить помощь
        """.trimIndent())
        }

        private fun loadCN(k: String, v: String) {
            plugin.loadLang("CN",k,v)
        }
        private fun loadEN(k: String, v: String) {
            plugin.loadLang("EN",k,v)
        }
        private fun loadHK(k: String, v: String) {
            plugin.loadLang("HK",k,v)
        }
        private fun loadRU(k: String, v: String) {
            plugin.loadLang("RU",k,v)
        }
    }
}