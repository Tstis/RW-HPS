/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.net.core.TypeConnect
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import java.util.concurrent.atomic.AtomicInteger

@Sharable
internal class AcceptorIdleStateTrigger(private val startNet: StartNet) : ChannelInboundHandlerAdapter() {
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
        //warn("断开一个链接", ctx.channel().id().asLongText())

        startNet.clear(ctx)
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.WRITER_IDLE) {
                val con: TypeConnect? = ctx.channel().attr(NewServerHandler.NETTY_CHANNEL_KEY).get()
                if (TimeoutDetection.checkTimeoutDetection(con?.abstractNetConnect)) {
                    startNet.clear(ctx)
                }
            }
        } else {
            super.userEventTriggered(ctx, evt)
        }
    }
}