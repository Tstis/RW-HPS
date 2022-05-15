/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.core

import cn.rwhps.server.game.event.EventGlobalType
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.net.GroupNet
import cn.rwhps.server.net.handler.rudp.PackagingSocket
import cn.rwhps.server.util.game.Events
import cn.rwhps.server.util.log.Log
import io.netty.channel.ChannelHandlerContext
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketException
import java.util.*

/**
 * @author RW-HPS/Dr
 * @date 2021年2月2日星期二 06:31:11
 */
class ConnectionAgreement {
    private val protocolType: ((packet: Packet) -> Unit)
    private val objectOutStream: Any
    private val udpDataOutputStream: DataOutputStream?

    val isClosed: ()->Boolean
    val useAgreement: String
    @JvmField
    val ip: String
    internal val localPort: Int
    val id: String = UUID.randomUUID().toString()

    /**
     * TCP Send
     * @param channelHandlerContext Netty-ChannelHandlerContext
     */
    internal constructor(channelHandlerContext: ChannelHandlerContext) {
        protocolType = { packet: Packet ->
            channelHandlerContext.writeAndFlush(packet)
        }
        objectOutStream = channelHandlerContext
        udpDataOutputStream = null
        useAgreement = "TCP"
        isClosed = { false }

        val channel = channelHandlerContext.channel()
        ip = convertIp(channel.remoteAddress().toString())
        localPort = (channel.localAddress() as InetSocketAddress).port
    }

    /**
     * UDP Send
     * @param socket Socket
     * @throws IOException Error
     */
    internal constructor(socket: PackagingSocket) {
        val socketStream = DataOutputStream(socket.outputStream)
        protocolType = { packet: Packet ->
            socketStream.writeInt(packet.bytes.size)
            socketStream.writeInt(packet.type)
            socketStream.write(packet.bytes)
            socketStream.flush()
        }
        objectOutStream = socket
        udpDataOutputStream = socketStream
        useAgreement = "UDP"
        isClosed = { socket.isClosed }

        ip = convertIp(socket.remoteSocketAddressString)
        localPort = socket.localPort
    }

    constructor() {
        protocolType = {}
        objectOutStream = ""
        udpDataOutputStream = null
        useAgreement = "Test"
        isClosed = { false }

        ip = ""
        localPort = 0
    }

    fun add(groupNet: GroupNet) {
        if (objectOutStream is ChannelHandlerContext) {
            groupNet.add(objectOutStream.channel())
        } else if (objectOutStream is PackagingSocket) {
            groupNet.add(this)
        }
    }

    /**
     * 接管Send逻辑
     * 整合不同协议的发送逻辑
     * @param packet MsgPacket
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun send(packet: Packet) {
        protocolType(packet)
    }

    @Throws(IOException::class)
    fun close(groupNet: GroupNet?) {
        //Events.fire(EventGlobalType.NewCloseEvent(this))
        IPData.remove(ip)
        if (groupNet != null) {
            if (objectOutStream is ChannelHandlerContext) {
                groupNet.remove(objectOutStream.channel())
            } else if (objectOutStream is PackagingSocket) {
                groupNet.remove(this)
            }
        }
        if (objectOutStream is ChannelHandlerContext) {
            objectOutStream.channel().close()
            objectOutStream.close()
        } else if (objectOutStream is PackagingSocket) {
            try {
                udpDataOutputStream!!.close()
                objectOutStream.close()
            } catch (e : SocketException) {
                Log.debug("[RUDP Close] Passive")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other == null || javaClass != other.javaClass) {
            false
        } else id == (other as ConnectionAgreement).id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    private fun convertIp(ipString: String): String {
        return ipString.substring(1, ipString.indexOf(':'))
    }

    companion object {
        val IPData = Seq<String>()
    }
}