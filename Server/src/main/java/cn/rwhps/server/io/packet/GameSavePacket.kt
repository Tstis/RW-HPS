/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.io.packet

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.util.ExtractUtil

class GameSavePacket(val packet: Packet) {
    fun convertGameSaveDataPacket(): Packet {
        return NetStaticData.RwHps.abstractNetPacket.convertGameSaveDataPacket(packet)
    }

    /**
     * 检测 诱骗的GameSave包是否对得住 (防止获取到的是旧的)
     * @return Boolean
     */
    fun checkTick(): Boolean {
        GameInputStream(packet).use { stream ->
            stream.readByte()
            val tick = stream.readInt()
            val tickGame = Data.game.tickGame.get()
            return ((tickGame - 50) < tick && tick < (tickGame + 50))
        }
    }

    /**
     * Return detailed Packet data
     * @return Packet String
     */
    override fun toString(): String {
        return  """
                GameSavePacket {
                    Bytes=${packet.bytes.contentToString()}
                    BytesHex=${ExtractUtil.bytesToHex(packet.bytes)}
                    Type=${packet.type}
                }
                """.trimIndent()
    }
}