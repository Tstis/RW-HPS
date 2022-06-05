/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

@file:JvmName("RelayPacket")
@file:JvmMultifileClass

package cn.rwhps.server.net.netconnectprotocol.internal.relay

import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.util.PacketType
import java.io.IOException

/**
 * 使用官方协议跳转到新服务器
 *  客户端主动连接
 * @param ip String     : IP
 * @return Packet       : 生成一个可发送的包
 * @throws IOException  : 未知
 */
@Throws(IOException::class)
internal fun fromRelayJumpsToAnotherServer(ip: String): Packet {
    val o = GameOutputStream()
    // 包内包含的消息
    o.writeByte(0)
    // 协议版本? 我不知道
    o.writeInt(3)
    // Debug
    o.writeBoolean(false)
    // For
    o.writeInt(1)
    o.writeString(ip)

    return o.createPacket(PacketType.PACKET_RECONNECT_TO)
}