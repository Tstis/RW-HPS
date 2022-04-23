/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.log.Log.error
import io.netty.channel.Channel
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor
import java.io.IOException
import java.util.concurrent.Executors

/**
 * @author RW-HPS/Dr
 */
class GroupNet {
    private val channelGroup: ChannelGroup = DefaultChannelGroup( GlobalEventExecutor.INSTANCE)
    private val singleTcpThreadExecutor = Executors.newSingleThreadExecutor()
    private val singleUdpThreadExecutor = Executors.newSingleThreadExecutor()
    private val protocol = Seq<ConnectionAgreement>(8)

    /**
     * 加入一个用户到群发列表
     * @param channel 通道
     */
    fun add(channel: Channel) {
        channelGroup.add(channel)
    }

    /**
     * 加入一个用户到群发列表
     * @param connectionAgreement UDP通道
     */
    fun add(connectionAgreement: ConnectionAgreement) {
        protocol.add(connectionAgreement)
    }

    fun broadcast(msg: Any) {
        if (Data.config.SingleUserRelay) {
            return
        }
        broadcast(msg, null)
    }

    /**
     * 群发一条消息
     * @param msg 消息
     */
    fun broadcast(msg: Any, str: String?) {
        singleTcpThreadExecutor.execute { channelGroup.writeAndFlush(msg) }
        singleUdpThreadExecutor.execute {
            protocol.each { e: ConnectionAgreement ->
                try {
                    e.send((msg as Packet))
                } catch (ioException: IOException) {
                    protocol.remove(e)
                    try {
                        e.close(this)
                    } catch (exception: IOException) {
                        error("[Server] UDP GrepNet Error Close", e)
                    }
                }
            }
        }
    }

    /**
     * 删除一个TCP通道
     * @param channel Channel
     */
    fun remove(channel: Channel) {
        channelGroup.remove(channel)
    }

    /**
     * 删除一个UDP通道
     * @param connectionAgreement Protocol
     */
    fun remove(connectionAgreement: ConnectionAgreement) {
        protocol.remove(connectionAgreement)
    }

    /**
     * 刷新操作
     * @return 是否成功
     */
    fun flush(): ChannelGroup {
        return channelGroup.flush()
    }

    fun disconnect() {
        channelGroup.disconnect()
    }
}