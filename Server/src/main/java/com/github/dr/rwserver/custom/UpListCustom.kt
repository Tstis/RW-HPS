/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.custom

import com.github.dr.rwserver.core.thread.Threads.newThreadCore
import com.github.dr.rwserver.core.thread.Threads.newThreadService2
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.json.Json
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.net.HttpRequestOkHttp
import com.github.dr.rwserver.util.ExtractUtil
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.IsUtil.notIsBlank
import com.github.dr.rwserver.util.ReExp
import com.github.dr.rwserver.util.encryption.Base64
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.log.Log.clog
import com.github.dr.rwserver.util.log.Log.error
import okhttp3.FormBody
import java.text.MessageFormat
import java.util.concurrent.TimeUnit

/**
 * This class is used for the official list on the server
 * Make small restrictions when most of the source code is publicly available
 * Do not want players to use this server for advertising
 * @author Dr
 */
class UpListCustom(handler: CommandHandler) {
    //private val tk = Data.config.readString("token", "")
    private var upServerList = false

    /* DATA Cache */
    private lateinit var serverID: String
    private lateinit var addData: String
    private lateinit var openData: String
    private lateinit var updateData: String
    private lateinit var removeData: String

    init {
        handler.removeCommand("upserverlist")
        handler.removeCommand("upserverlistnew")
        handler.register("upserverlist", "serverCommands.upserverlist") { _: Array<String>?, log: StrCons ->
            if (!upServerList) {
                initUpListData()
                newThreadCore {
                    upServerList = true
                    uplist()
                }
            } else {
                log["已上传 不需要再次上传"]
            }
        }

        handler.register("removeupserverlist", "serverCommands.removeupserverlist") { _: Array<String>?, log: StrCons ->
            if (upServerList) {
                HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", removeData)
                HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", removeData)
                upServerList = false
                log["已删除"]
            } else {
                log["未上传 不需要删除"]
            }
        }
    }

    private fun initUpListData() {
        val formBody = FormBody.Builder()
        formBody.add("Passwd", notIsBlank(Data.game.passwd).toString())
        formBody.add("ServerName",Data.core.serverName)
        formBody.add("Port", Data.config.Port.toString())
        formBody.add("MapName",Data.game.maps.mapName)
        formBody.add("PlayerSize",Data.game.playerManage.playerGroup.size().toString())
        formBody.add("PlayerMaxSize",Data.game.maxPlayer.toString())

        val resultUpList = HttpRequestOkHttp.doPost("https://api.data.der.kim/UpList/v3/upList", formBody)

        if (resultUpList.startsWith("[-1]")) {
            error("[Get UPLIST Data Error] Please Check API")
            return
        } else if (resultUpList.startsWith("[-2]")) {
            error("[Get UPLIST Data Error] IP prohibited")
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

    private fun uplist() {
        var privateIp = ExtractUtil.getPrivateIp()
        if (IsUtil.isBlank(privateIp)) {
            privateIp = "10.0.0.1"
        }

        val resultUp = MessageFormat(addData).format(arrayOf(Data.core.serverName,privateIp,Data.game.maps.mapName))
        val addGs1 = HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", resultUp).contains(serverID)
        val addGs4 = HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", resultUp).contains(serverID)
        if (addGs1 || addGs4) {
            if (addGs1 && addGs4) {
                clog(Data.localeUtil.getinput("err.yesList"))
            } else {
                clog(Data.localeUtil.getinput("err.ynList"))
            }
        } else {
            clog(Data.localeUtil.getinput("err.noList"))
        }

        val checkPortGs1 = HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", openData).contains("true")
        val checkPortGs4 = HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", openData).contains("true")
        if (checkPortGs1 || checkPortGs4) {
            clog(Data.localeUtil.getinput("err.yesOpen"))
        } else {
            clog(Data.localeUtil.getinput("err.noOpen"))
        }

        newThreadService2({
            val pingdata = object : ReExp() {
                @Throws(Exception::class)
                override fun runs(): Any {
                    val result0 = MessageFormat(updateData).format(arrayOf(
                        Data.core.serverName,
                        if (IsUtil.isBlank(Data.config.Subtitle)) Data.game.maps.mapName else Data.config.Subtitle,
                        if (Data.game.isStartGame) "ingame" else "battleroom",
                        Data.game.playerManage.playerGroup.size().toString()))
                    HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", result0)
                    HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", result0)

                    return "Y"
                }

                override fun defruns(): Any? {
                    return null
                }
            }.setSleepTime(10).setRetryFreq(3).execute()
            if (pingdata == null) {
                Log.warn("错误 请检查网络")
            }
        }, 50, 50, TimeUnit.SECONDS, "UPLIST")
    }
}