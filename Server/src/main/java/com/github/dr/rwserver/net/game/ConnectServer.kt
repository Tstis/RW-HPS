/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.game.EventType
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.io.input.GameInputStream
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionServer
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.game.Events
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

/**
 * Create a repeater
 * @property ip ip
 * @property port port
 * @constructor
 */
class ConnectServer(private val ip: String, private val port: Int, private val abstractNetConnect: GameVersionServer) {
    private lateinit var channel: Channel


    init {
        Thread {
            val client = Bootstrap()
            //第1步 定义线程组，处理读写和链接事件，没有了accept事件
            val group: EventLoopGroup = NioEventLoopGroup()
            try {
                client.group(group)
                //第2步 绑定客户端通道
                client.channel(NioSocketChannel::class.java)
                //第3步 给NIoSocketChannel初始化handler， 处理读写事件
                client.handler(object : ChannelInitializer<NioSocketChannel>() {
                    //通道是NioSocketChannel
                    @Throws(Exception::class)
                    override fun initChannel(ch: NioSocketChannel) {
                        ch.pipeline().addLast(PacketDecoder())
                        ch.pipeline().addLast(PacketEncoder())
                        ch.pipeline().addLast(AcceptServerData(this@ConnectServer,abstractNetConnect))
                    }
                })
                //连接服务器
                val future: ChannelFuture = client.connect(ip, port).sync()

                this@ConnectServer.channel = future.channel()
                future.channel().writeAndFlush(NetStaticData.protocolData.abstractNetPacket.getPlayerConnectPacket())

                //当通道关闭了，就继续往下走
                future.channel().closeFuture().sync()
            } finally {
                group.shutdownGracefully()
            }
        }.start()

        abstractNetConnect.connectServer = this
    }

    fun send(packet: Packet) {
        channel.writeAndFlush(packet)
    }

    fun close() {
        channel.close()
    }

    private class AcceptServerData(private val connectServer: ConnectServer,private val abstractNetConnect: GameVersionServer): ChannelInboundHandlerAdapter() {
        @Throws(java.lang.Exception::class)
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is Packet) {
                if (msg.type == 161) {
                    val stream = GameInputStream(msg)
                    stream.readString()
                    stream.skip(12)
                    stream.readString()
                    stream.readString()
                    val player = abstractNetConnect.player
                    connectServer.send(NetStaticData.protocolData.abstractNetPacket.getPlayerRegisterPacket(
                        player.name,
                        player.uuid,
                        "",
                        stream.readInt()
                    ))
                    if (IsUtil.notIsBlank(player)) {
                        Data.game.playerManage.playerGroup.remove(player)
                        if (!Data.game.isStartGame) {
                            Data.game.playerManage.playerAll.remove(player)
                            player.clear()
                            Data.game.playerManage.removePlayerArray(player)
                        }
                        Events.fire(EventType.PlayerLeaveEvent(player))
                    }
                    abstractNetConnect.isConnectServer = true
                } else {
                    abstractNetConnect.sendPacket(msg)
                }
            }
        }
    }
}