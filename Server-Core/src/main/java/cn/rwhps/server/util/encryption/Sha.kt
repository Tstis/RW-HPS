/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.encryption

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Sha {
    @JvmStatic
    fun sha256(strText: String): String {
        return toSha(strText, "SHA-256")
    }

    @JvmStatic
    fun sha512(strText: String): String {
        return toSha(strText, "SHA-512")
    }

    @JvmStatic
    fun sha256Array(strText: String): ByteArray {
        return toShaArray(strText, "SHA-256")
    }

    @JvmStatic
    fun sha512Array(strText: String): ByteArray {
        return toShaArray(strText, "SHA-512")
    }

    private fun toShaArray(strText: String, strType: String): ByteArray {
        // 是否是有效字符串
        try {
            val messageDigest = MessageDigest.getInstance(strType)
            messageDigest.update(strText.toByteArray())
            return messageDigest.digest()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }

    private fun toSha(strText: String, strType: String): String {
        // 返回值
        var strResult = ""

        val byteBuffer = toShaArray(strText, strType)
        val strHexString = StringBuffer()
        for (b in byteBuffer) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) {
                strHexString.append('0')
            }
            strHexString.append(hex)
            strResult = strHexString.toString()
        }
        return strResult
    }
}