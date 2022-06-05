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

import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.util.PacketType
import java.io.IOException

/**
 * 使玩家客户端弹出一个可输入的窗口
 *   输入的数据明文返回
 * @param msg String    : 提示的信息
 * @return Packet       : 生成一个可发送的包
 * @throws IOException  : 未知
 */
@Throws(IOException::class)
fun relayServerTypeInternal(msg: String): Packet {
    val o = GameOutputStream()
    // 理论上是随机数？
    o.writeByte(1)
    o.writeInt(5) //可能和-AX一样
    // Msg
    o.writeString(msg)
    return o.createPacket(PacketType.RELAY_117) /// -> 118
}

/**
 * 解析 [relayServerTypeInternal] 返回的数据
 * @param packet Packet : 返回的包
 * @return String       : 解析出的数据 (玩家输入的明文)
 * @throws IOException  : 未知
 */
@Throws(IOException::class)
fun relayServerTypeReplyInternal(packet: Packet): String {
    GameInputStream(packet).use { inStream ->
        // 跳过前面没用的数据
        inStream.skip(5)
        // 读取数据 并去掉首尾空格
        return inStream.readString().trim { it <= ' ' }
    }
}