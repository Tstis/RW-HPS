/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.core

import com.github.dr.rwserver.net.game.*
import com.github.dr.rwserver.util.log.exp.ImplementedException
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

@Sharable
open class AbstractNet(protected val startNet: StartNet): ChannelInitializer<SocketChannel>() {
    private val idleStateTrigger: AcceptorIdleStateTrigger = AcceptorIdleStateTrigger(startNet)
    private var newServerHandler: NewServerHandler = NewServerHandler(startNet)

    protected fun addTimeOut(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
        channelPipeline.addLast(idleStateTrigger)
    }

    protected fun addPacketDecoderAndEncoder(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(PacketDecoder())
        channelPipeline.addLast(PacketEncoder())
    }

    protected fun addNewServerHandler(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(newServerHandler)
    }
    protected fun addNewServerHandlerExecutorGroup(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(startNet.ioGroup,newServerHandler)
    }

    internal fun getConnectSize(): Int {
        return idleStateTrigger.connectNum.get()
    }

    override fun initChannel(socketChannel: SocketChannel) {
        throw ImplementedException("Need to implement")
    }

}