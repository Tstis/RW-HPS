/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.handler.tcp

import cn.rwhps.server.net.http.WebSocket
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import java.util.function.Consumer

@ChannelHandler.Sharable
class GamePortWebSocket(private val webSocket: WebSocket) : SimpleChannelInboundHandler<TextWebSocketFrame>() {
    val connected: MutableSet<Channel> = HashSet(4)


    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame) {
        webSocket.ws(this,ctx.channel(),msg.text())
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        connected.remove(ctx.channel())
        super.channelInactive(ctx)
    }

    fun broadCast(msg: String?) {
        connected.forEach(Consumer { x: Channel ->
            x.writeAndFlush(TextWebSocketFrame(msg))
        })
    }

    fun hasChannel(channel: Channel): Boolean {
        return connected.contains(channel)
    }
}