/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.io.packet

import cn.rwhps.server.util.ExtractUtil
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.log.Log

/**
 * @author RW-HPS/Dr
 */
class Packet(type0: Int, @JvmField val bytes: ByteArray) {
    val type = PacketType.from(type0)

    init {
        if (type == PacketType.NOT_RESOLVED) {
            Log.fatal("ERROR , $type0")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is PacketType) {
            return other.name == type.name && other.typeInt == type.typeInt
        }
        return false
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    /**
     * Return detailed Packet data
     * @return Packet String
     */
    override fun toString(): String {
        return  """
                Packet{
                    Bytes=${bytes.contentToString()}
                    BytesHex=${ExtractUtil.bytesToHex(bytes)}
                    type=${type}
                }
                """.trimIndent()
    }
}