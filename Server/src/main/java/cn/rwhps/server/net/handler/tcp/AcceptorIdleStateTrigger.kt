/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.handler.tcp

import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.net.handler.rudp.TimeoutDetection
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import java.net.SocketException
import java.util.concurrent.atomic.AtomicInteger

@Sharable
internal class AcceptorIdleStateTrigger : ChannelInboundHandlerAdapter() {
    val connectNum: AtomicInteger = AtomicInteger()

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }

    @Throws(java.lang.Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext?) {
        super.channelRegistered(ctx)
        connectNum.incrementAndGet()
    }

    @Throws(java.lang.Exception::class)
    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        super.channelUnregistered(ctx)
        connectNum.decrementAndGet()
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        //warn("break a link", ctx.channel().id().asLongText())
        clear(ctx)
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.WRITER_IDLE) {
                val con: TypeConnect? = ctx.channel().attr(NewServerHandler.NETTY_CHANNEL_KEY).get()
                if (TimeoutDetection.checkTimeoutDetection(con?.abstractNetConnect)) {
                    clear(ctx)
                }
            }
        } else {
            super.userEventTriggered(ctx, evt)
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        // The remote host forcibly closed an existing connection
        if (cause is SocketException) {
            clear(ctx)
        }
    }

   
    // 清理连接 释放资源
    @param ctx ChannelHandlerContext
    
    private fun clear(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        try {
            val attr = channel.attr(NewServerHandler.NETTY_CHANNEL_KEY)
            val con = attr.get()
            if (con != null) {
                con.abstractNetConnect.disconnect()
            } else {
                channel.close()
                ctx.close()
            }
        } finally {
            //OVER_MAP.remove(channel.id().asLongText())
        }
    }
}