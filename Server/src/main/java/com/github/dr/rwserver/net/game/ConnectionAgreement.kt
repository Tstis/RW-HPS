/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.GroupNet
import com.github.dr.rwserver.util.RandomUtil.generateStr
import io.netty.channel.ChannelHandlerContext
import net.udp.ReliableSocket
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

/**
 * @author Dr
 * @date 2021年2月2日星期二 06:31:11
 */
class ConnectionAgreement {
    private val protocolType: ((packet: Packet) -> Unit)
    private val startNet: StartNet?
    private val objectOutStream: Any
    private val udpDataOutputStream: DataOutputStream?
    val useAgreement: String
    @JvmField
    val ip: String
    internal val localPort: Int
    val id: String

    /**
     * TCP Send
     * @param channelHandlerContext Netty-ChannelHandlerContext
     */
    constructor(channelHandlerContext: ChannelHandlerContext, startNet: StartNet) {
        protocolType = { packet: Packet ->
            channelHandlerContext.writeAndFlush(packet)
        }
        this.startNet = startNet
        objectOutStream = channelHandlerContext
        udpDataOutputStream = null
        useAgreement = "TCP"

        val channel = channelHandlerContext.channel()
        ip = convertIp(channel.remoteAddress().toString())
        localPort = (channel.localAddress() as InetSocketAddress).port
        id = generateStr(5)
    }

    /**
     * UDP Send
     * @param socket Socket
     * @throws IOException Error
     */
    constructor(socket: Socket, startNet: StartNet) {
        val socketStream = DataOutputStream(socket.getOutputStream())
        protocolType = { packet: Packet ->
            socketStream.writeInt(packet.bytes.size)
            socketStream.writeInt(packet.type)
            socketStream.write(packet.bytes)
            socketStream.flush()
        }
        this.startNet = startNet
        objectOutStream = socket
        udpDataOutputStream = socketStream
        useAgreement = "UDP"
        ip = convertIp(socket.remoteSocketAddress.toString())
        localPort = socket.localPort
        id = generateStr(5)
    }

    constructor() {
        protocolType = {}
        startNet = null
        objectOutStream = ""
        udpDataOutputStream = null
        useAgreement = "Test"
        ip = ""
        localPort = 0
        id = generateStr(5)
    }

    fun add(groupNet: GroupNet) {
        if (objectOutStream is ChannelHandlerContext) {
            groupNet.add(objectOutStream.channel())
        } else if (objectOutStream is ReliableSocket) {
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
        if (groupNet != null) {
            if (objectOutStream is ChannelHandlerContext) {
                groupNet.remove(objectOutStream.channel())
            } else if (objectOutStream is ReliableSocket) {
                groupNet.remove(this)
            }
        }
        if (objectOutStream is ChannelHandlerContext) {
            objectOutStream.channel().close()
            objectOutStream.close()
        } else if (objectOutStream is ReliableSocket) {
            udpDataOutputStream!!.close()
            objectOutStream.close()
            startNet!!.OVER_MAP.remove(objectOutStream.remoteSocketAddress.toString())
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

    fun convertIp(ipString: String): String {
        return ipString.substring(1, ipString.indexOf(':'))
    }
}