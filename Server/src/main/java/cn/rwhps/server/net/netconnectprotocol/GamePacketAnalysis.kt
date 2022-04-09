/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol

import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.IsUtil
import java.io.IOException

object GamePacketAnalysis {
    @Throws(IOException::class)
    fun getPacketUnitName(bytes: ByteArray): Seq<String> {
        val result = Seq<String>()
        GameInputStream(bytes).use { stream ->
            stream.readString()
            stream.skip(8)
            stream.readString()
            stream.skip(34)
            if (stream.readBoolean()) {
                stream.getDecodeStream(false).use { unit ->
                    unit.skip(4)
                    for (i in 0 until unit.readInt()) {
                        unit.readString()
                        unit.readInt()
                        unit.readBoolean()
                        val cacheA = unit.isReadString()
                        val cache = if (IsUtil.isBlank(cacheA)) "Default" else cacheA
                        if (!result.contains(cache)) {
                            result.add(cache)
                        }
                        unit.skip(16)
                    }
                }
            }
        }
        return result
    }
}