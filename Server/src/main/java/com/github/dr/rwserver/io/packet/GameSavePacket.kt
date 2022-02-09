/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.io.packet

import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.util.ExtractUtil

class GameSavePacket(val packet: Packet) {
    fun convertGameSaveDataPacket(): Packet {
        return NetStaticData.protocolData.abstractNetPacket.convertGameSaveDataPacket(packet)
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