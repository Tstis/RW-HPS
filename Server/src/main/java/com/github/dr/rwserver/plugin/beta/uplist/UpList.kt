/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.plugin.beta.uplist

import com.github.dr.rwserver.core.thread.Threads
import com.github.dr.rwserver.core.thread.TimeTaskData
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.json.Json
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.net.HttpRequestOkHttp
import com.github.dr.rwserver.plugin.Plugin
import com.github.dr.rwserver.util.ExtractUtil
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.encryption.Base64
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.log.Log
import okhttp3.FormBody
import java.text.MessageFormat
import java.util.concurrent.TimeUnit

/**
 * 为 服务器进入官方列表提供API支持
 * 并不打算开放源码 避免被滥用 (已经被滥用了 指漫天AD)
 * API随Server版本而进行版本更新
 * @author Dr
 */
class UpList : Plugin() {
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
            if (args != null && args.size > 0) {
                when (args[0]) {
                    "add" -> { if (args.size > 1) add(log,args[1]) else add(log) }
                    "update" -> update()
                    "remove" -> remove(log)
                    "help" -> log[Data.localeUtil.getinput("uplist.help")]
                    else -> log["Check UpList Command !"]
                }
            } else {
                log["Check UpList Command ! use 'uplist help'"]
            }
        }
    }

    private fun initUpListData(port: String?) {
        val formBody = FormBody.Builder()
        formBody.add("Passwd", IsUtil.notIsBlank(Data.game.passwd).toString())
        formBody.add("ServerName",Data.core.serverName)
        formBody.add("Port", port ?:Data.config.Port.toString())
        formBody.add("MapName",Data.game.maps.mapName)
        formBody.add("PlayerSize",Data.game.playerManage.playerGroup.size().toString())
        formBody.add("PlayerMaxSize",Data.game.maxPlayer.toString())

        val resultUpList = HttpRequestOkHttp.doPost(Data.urlData.readString("Get.UpListData.Bak"), formBody)

        if (resultUpList.startsWith("[-1]")) {
            Log.error("[Get UPLIST Data Error] Please Check API")
            return
        } else if (resultUpList.startsWith("[-2]")) {
            Log.error("[Get UPLIST Data Error] IP prohibited")
            return
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
    }

    private fun add(log: StrCons,port: String? = null) {
        if (!upServerList) {
            initUpListData(port)
            Threads.newThreadCore { upServerList = true ; uplist() }
        } else {
            log["Already on the list"]
        }
    }

    private fun uplist() {

        var privateIp = ExtractUtil.getPrivateIp()
        if (IsUtil.isBlank(privateIp)) {
            privateIp = "10.0.0.1"
        }

        val resultUp = MessageFormat(addData).format(arrayOf(Data.core.serverName,privateIp, Data.game.maps.mapName))

        val addGs1 = HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", resultUp).contains(serverID)
        val addGs4 = HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", resultUp).contains(serverID)
        if (addGs1 || addGs4) {
            if (addGs1 && addGs4) {
                Log.clog(Data.localeUtil.getinput("err.yesList"))
            } else {
                Log.clog(Data.localeUtil.getinput("err.ynList"))
            }
        } else {
            Log.clog(Data.localeUtil.getinput("err.noList"))
        }

        val checkPortGs1 = HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", openData).contains("true")
        val checkPortGs4 = HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", openData).contains("true")
        if (checkPortGs1 || checkPortGs4) {
            Log.clog(Data.localeUtil.getinput("err.yesOpen"))
        } else {
            Log.clog(Data.localeUtil.getinput("err.noOpen"))
        }

        TimeTaskData.CustomUpServerListTask = Threads.newThreadService2({update()}, 50, 50, TimeUnit.SECONDS)
    }

    private fun update() {
        val result0 = MessageFormat(updateData).format(arrayOf(
            Data.core.serverName,
            if (IsUtil.isBlank(Data.config.Subtitle)) Data.game.maps.mapName else Data.config.Subtitle,
            if (Data.game.isStartGame) "ingame" else "battleroom",
            Data.game.playerManage.playerGroup.size().toString()))
        HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", result0)
        HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", result0)
    }

    private fun remove(log: StrCons) {
        if (upServerList) {
            HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", removeData)
            HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", removeData)
            upServerList = false
            TimeTaskData.stopCustomUpServerListTask()
            log["已删除"]
        } else {
            log["未上传 不需要删除"]
        }
    }
}