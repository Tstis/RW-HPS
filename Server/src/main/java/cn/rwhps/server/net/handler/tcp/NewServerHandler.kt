/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.handler.tcp

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.game.event.EventGlobalType.NewConnectEvent
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.util.IpUtil
import cn.rwhps.server.util.game.Events
import cn.rwhps.server.util.log.Log.debug
import cn.rwhps.server.util.log.Log.error
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
                    type = NetStaticData.RwHps.typeConnect.getTypeConnect(connectionAgreement)
                    attr.setIfAbsent(type)

                    if (Data.core.admin.bannedIP24.contains(IpUtil.ipToLong24(type.abstractNetConnect.ip))) {
                        type.abstractNetConnect.disconnect()
                        return
                    }

                    val newConnectEvent = NewConnectEvent(connectionAgreement)
                    Events.fire(newConnectEvent)
                    if (newConnectEvent.result) {
                        return
                    }
                }

                ctx.executor().execute {
                    if (type.abstractNetConnect.isConnectServer) {
                        type.abstractNetConnect.connectServer!!.send(msg)
                        return@execute
                    }
                    try {
                        type.typeConnect(msg)
                    } catch (e: Exception) {
                        debug(e = e)
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

        error(RuntimeException())
        error(cause == null)
        error(cause!!)
        error(cause.message!!)
        error(cause.localizedMessage!!)
        error(cause.cause!!)
    }

    companion object {
        @JvmField
        val NETTY_CHANNEL_KEY = AttributeKey.valueOf<TypeConnect>("User-Net")!!
    }
}