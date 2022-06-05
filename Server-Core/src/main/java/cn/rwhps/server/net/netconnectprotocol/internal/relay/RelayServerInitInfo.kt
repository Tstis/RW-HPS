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
 * 让客户端认为这是 RELAY 服务器
 *  用 RELAY 的初始化
 * @return Packet       : 生成一个可发送的包
 * @throws IOException  : 未知
 */
@Throws(IOException::class)
internal fun relayServerInitInfo(): Packet {
    val o = GameOutputStream()
    o.writeByte(0)
    // RELAY Version
    o.writeInt(151)
    // ?
    o.writeInt(1)
    // ?
    o.writeBoolean(false)
    return o.createPacket(PacketType.RELAY_VERSION_INFO)
}