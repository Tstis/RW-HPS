/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.http

import cn.rwhps.server.net.handler.tcp.GamePortWebSocket
import cn.rwhps.server.util.log.exp.VariableException

/**
 * @author Dr
 */
object WebData {
    private val getData: MutableMap<String, WebGet> = HashMap()
    private val postData: MutableMap<String, WebPost> = HashMap()
    private val webSocketData: MutableMap<String, GamePortWebSocket> = HashMap()

    @JvmStatic
    fun addWebGetInstance(url: String, webGet: WebGet) {
        if (getData.containsKey(url)) {
            throw VariableException.RepeatAddException("[AddWebGetInstance] Repeat Add")
        }
        getData[url] = webGet
    }
    @JvmStatic
    fun addWebPostInstance(url: String, webPost: WebPost) {
        if (postData.containsKey(url)) {
            throw VariableException.RepeatAddException("[AddWebPostInstance] Repeat Add")
        }
        postData[url] = webPost
    }
    @JvmStatic
    fun addWebSocketInstance(url: String, webSocket: WebSocket) {
        if (webSocketData.containsKey(url)) {
            throw VariableException.RepeatAddException("[AddWebSocketInstance] Repeat Add")
        }
        webSocketData[url] = GamePortWebSocket(webSocket)
    }

    /**
     * 根据URL 获取实例
     *
     * 通配符优先级低于指定位置 当无法访问指定位置时 尝试使用通配符
     * 如果找不到通配符 将会抛出默认404
     * 建议插件使用 ~/插件名/ 进行开发或兼容
     *
     * @param url String
     * @param sendWeb SendWeb
     */
    internal fun runWebGetInstance(url: String, sendWeb: SendWeb) {
        val wildcard = "${url.substring(0,url.lastIndexOf("/")+1)}*"
        var getUrl = url
        var urlData = ""

        if (url.contains("?")) {
            getUrl = url.substring(0,url.lastIndexOf("?")-1)
            if (url.length > url.lastIndexOf("?")+1) {
                urlData = url.substring(url.lastIndexOf("?"+1))
            }
        }

        if (getData.containsKey(getUrl)) {
            getData[getUrl]?.get(getUrl,urlData,sendWeb)
        } else if (getData.containsKey(wildcard)) {
            getData[wildcard]!!.get(getUrl,urlData,sendWeb)
        } else {
            sendWeb.send404()
        }
    }
    internal fun runWebPostInstance(url: String, data: String, sendWeb: SendWeb) {
        val wildcard = "${url.substring(0,url.lastIndexOf("/")+1)}*"
        var getUrl = url
        var urlData = ""

        if (url.contains("?")) {
            getUrl = url.substring(0,url.lastIndexOf("?")-1)
            if (url.length > url.lastIndexOf("?")+1) {
                urlData = url.substring(url.lastIndexOf("?"+1))
            }
        }

        if (postData.containsKey(getUrl)) {
            postData[getUrl]!!.get(getUrl,urlData,data,sendWeb)
        } else if (postData.containsKey(wildcard)) {
            postData[wildcard]!!.get(getUrl,urlData,data,sendWeb)
        } else {
            sendWeb.send404()
        }
    }
    internal fun runWebSocketInstance(url: String): GamePortWebSocket? {
        if (webSocketData.containsKey(url)) {
            return webSocketData[url]!!
        }
        return null
    }
}