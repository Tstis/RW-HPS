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
import cn.rwhps.server.io.output.CompressOutputStream
import cn.rwhps.server.io.packet.GameCommandPacket
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.PacketType
import java.io.IOException

/**
 * 向玩家发生更新Tick包
 * @param tick Int      : TICK
 * @return Packet       : 生成一个可发送的包
 * @throws IOException  : 未知
 */
@Throws(IOException::class)
internal fun gameTickPacketInternal(tick: Int): Packet {
    val o = GameOutputStream()
    // 游戏TICK
    o.writeInt(tick)
    // 可读取的按键包长度
    // 因为不存在按键包 所以这个包只会让客户端更新Tick
    o.writeInt(0)
    return o.createPacket(PacketType.PACKET_TICK)
}

/**
 * 向玩家发生指定Tick下的单个按键包(单位动作包)
 * @param tick Int              : TICK
 * @param cmd GameCommandPacket : 按键包
 * @return Packet               : 生成一个可发送的包
 * @throws IOException          : 未知
 */
@Throws(IOException::class)
internal fun gameTickCommandPacketInternal(tick: Int, cmd: GameCommandPacket): Packet {
    val o = GameOutputStream()
    // 游戏TICK
    o.writeInt(tick)
    // 可读取的按键包长度
    o.writeInt(1)
    val enc = CompressOutputStream.getGzipOutputStream("c", false)
    enc.writeBytes(cmd.bytes)
    o.flushEncodeData(enc)
    return o.createPacket(PacketType.PACKET_TICK)
}

/**
 * 向玩家发生指定Tick下的多个按键包(单位动作包)
 * @param tick Int                      : TICK
 * @param cmd Seq<GameCommandPacket>    : 多个按键包列表
 * @return Packet                       : 生成一个可发送的包
 * @throws IOException                  : 未知
 */
@Throws(IOException::class)
internal fun gameTickCommandsPacketInternal(tick: Int, cmd: Seq<GameCommandPacket>): Packet {
    val o = GameOutputStream()
    // 游戏TICK
    o.writeInt(tick)
    // 可读取的按键包长度
    o.writeInt(cmd.size())
    for (c in cmd) {
        val enc = CompressOutputStream.getGzipOutputStream("c", false)
        enc.writeBytes(c.bytes)
        o.flushEncodeData(enc)
    }
    return o.createPacket(PacketType.PACKET_TICK)
}