/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.http

import cn.rwhps.server.data.global.Data
import io.netty.channel.Channel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

/**
 * @author Dr
 */
class SendWeb(
    private val channel: Channel,
    private val request: HttpRequest
) {
    private var cacheData: ByteArray? = null
    var status: HttpResponseStatus = HttpResponseStatus.OK
    private val hand  = "RW-HPS Web [Version: ${Data.SERVER_CORE_VERSION}]"

    fun setData(bytes: ByteArray) {
        cacheData = bytes
    }
    fun setData(string: String) {
        cacheData = string.toByteArray(Data.UTF_8)
    }

    fun send404() {
        status = HttpResponseStatus.NOT_FOUND
        cacheData = """
        <p>[404] File not found
            <br>The project is based on 
                <strong>RW-HPS</strong>
                <br>
                    <a href="https://github.com/RW-HPS/RW-HPS">RW-HPS Github</a>
                </p>
        """.trimIndent().toByteArray(Data.UTF_8)
        send()
    }

    fun send() {
        if (cacheData == null) {
            throw NullPointerException()
        }
        val defaultFullHttpResponse = DefaultFullHttpResponse(request.protocolVersion(), status)
        defaultFullHttpResponse.headers().set("server",hand)

        defaultFullHttpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, cacheData!!.size)
        defaultFullHttpResponse.content().writeBytes(cacheData)

        channel.writeAndFlush(defaultFullHttpResponse)

        channel.close()
    }

}