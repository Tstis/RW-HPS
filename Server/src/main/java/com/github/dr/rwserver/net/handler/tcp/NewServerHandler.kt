/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.handler.tcp

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.game.EventGlobalType.NewConnectEvent
import com.github.dr.rwserver.io.packet.Packet
import com.github.dr.rwserver.net.core.ConnectionAgreement
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.util.ExtractUtil
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log.debug
import com.github.dr.rwserver.util.log.Log.error
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.AttributeKey

@Sharable
internal class NewServerHandler : SimpleChannelInboundHandler<Any?>() {

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg == null) {
            return
        }

        try {
            if (msg is Packet) {
                val attr = ctx.channel().attr(NETTY_CHANNEL_KEY)
                var type = attr.get()

                if (type == null) {
                    val connectionAgreement = ConnectionAgreement(ctx)
                    type = NetStaticData.protocolData.typeConnect.getTypeConnect(connectionAgreement)
                    attr.setIfAbsent(type)

                    val newConnectEvent = NewConnectEvent(connectionAgreement)
                    Events.fire(newConnectEvent)
                    if (newConnectEvent.result) {
                        return
                    }
                }
                if (Data.core.admin.bannedIP24.contains(ExtractUtil.ipToLong(type.abstractNetConnect.ip))) {
                    type.abstractNetConnect.disconnect()
                    return
                }

                ctx.executor().execute {
                    if (type.abstractNetConnect.isConnectServer) {
                        type.abstractNetConnect.connectServer!!.send(msg)
                        return@execute
                    }
                    try {
                        type.typeConnect(msg)
                    } catch (e: Exception) {
                        debug(e)
                        //type.disconnect()
                    }
                }
            }
        } catch (ss: Exception) {
            error(ss)
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        //ctx.close()
/*
        error(RuntimeException())
        error(cause == null)
        error(cause!!)
        error(cause.message!!)
        error(cause.localizedMessage!!)
        error(cause.cause!!)*/
    }

    companion object {
        @JvmField
        val NETTY_CHANNEL_KEY = AttributeKey.valueOf<TypeConnect>("User-Net")!!
    }
}