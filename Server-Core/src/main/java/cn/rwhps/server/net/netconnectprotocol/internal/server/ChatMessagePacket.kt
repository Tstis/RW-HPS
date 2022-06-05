/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

@file:JvmName("ServerPacket")
@file:JvmMultifileClass

package cn.rwhps.server.net.netconnectprotocol.internal.server

import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.util.PacketType
import java.io.IOException

/**
 * 向玩家发送聊天消息
 * @param msg String    : 消息
 * @param sendBy String : 发送人名称
 * @param team Int      : 队伍 (判别颜色)
 * @return Packet       : 生成一个可发送的包
 * @throws IOException  : 未知
 */
@Throws(IOException::class)
internal fun chatMessagePacketInternal(msg: String, sendBy: String, team: Int): Packet {
    val o = GameOutputStream()
    // 包内包含的消息
    o.writeString(msg)
    // 协议版本 (在旧版本中 这里分别为 0 1 2 3)
    o.writeByte(3)
    // 发送人名称是否为空 并写入数据
    o.writeIsString(sendBy)
    // 队伍
    o.writeInt(team)
    // 队伍
    o.writeInt(team)
    return o.createPacket(PacketType.CHAT)
}