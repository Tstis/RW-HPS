/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.encryption

import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.IsUtil.notIsBlank
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

//Java
/**
 * @author RW-HPS/Dr
 */
object Md5 {
    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    @JvmStatic
    fun md5(input: String?): String? {
        if (input == null) {
            return null
        }
        try {
            val resultByteArray = MessageDigest.getInstance("MD5").digest(input.toByteArray(StandardCharsets.UTF_8))
            return byteArrayToHex(resultByteArray)
        } catch (e: NoSuchAlgorithmException) {
            //Log.error
        }
        return null
    }

    @JvmStatic
    fun md5Formant(str: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5").digest(str.toByteArray(StandardCharsets.UTF_8))
            val sb = StringBuilder(digest.size * 2)
            for (b2 in digest) {
                val b3: Int = b2.toInt() and 0xFF
                if (b3 < 16) {
                    sb.append('0')
                }
                sb.append(Integer.toHexString(b3))
            }
            sb.toString()
        } catch (e2: NoSuchAlgorithmException) {
            throw RuntimeException("MD5 should be supported", e2)
        }
    }

    @JvmStatic
    fun md5(list: Seq<File>): Seq<String> {
        val result = Seq<String>()
        list.each { e: File ->
            val md5 = md5(e)
            if (notIsBlank(md5)) {
                result.add(md5)
            }
        }
        return result
    }

    @JvmStatic
    fun md5(file: File): String? {
        try {
            if (!file.isFile) {
                return null
            }
            val `in` = FileInputStream(file)
            val result = md5(`in`)
            `in`.close()
            return result
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun md5(inputStream: InputStream): String? {
        try {
            val messageDigest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                messageDigest.update(buffer, 0, read)
            }
            inputStream.close()
            return byteArrayToHex(messageDigest.digest())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    private fun byteArrayToHex(byteArray: ByteArray): String {
        val resultCharArray = CharArray(byteArray.size * 2)
        var index = 0
        for (b in byteArray) {
            resultCharArray[index++] = HEX_DIGITS[b.toInt().ushr(4) and 0xFF]
            resultCharArray[index++] = HEX_DIGITS[b.toInt() and 0xF]
        }
        return String(resultCharArray)
    }
}