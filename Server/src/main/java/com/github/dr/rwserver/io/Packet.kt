/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.io

import com.github.dr.rwserver.util.ExtractUtil

/**
 * @author Dr
 */
class Packet(@JvmField val type: Int, @JvmField val bytes: ByteArray) {

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