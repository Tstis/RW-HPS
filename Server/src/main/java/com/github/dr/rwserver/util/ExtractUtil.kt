/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

import com.github.dr.rwserver.data.global.Data
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


/**
 * @author Dr
 */
object ExtractUtil {
    val defCharset = Charset.defaultCharset()
    @JvmStatic
    fun stringDefToUtf8(string: String): String {
        // 用指定编码转换String为byte[]:
        return String(string.toByteArray(defCharset), Data.UTF_8)
    }

    @JvmStatic
    fun stringGbkToUtf8(string: String): String {
        // 用指定编码转换String为byte[]:
        return String(string.toByteArray(StandardCharsets.ISO_8859_1), Data.UTF_8)
    }

    /**
     * 合并byte数组
     */
    @JvmStatic
    fun unitByteArray(byte1: ByteArray, byte2: ByteArray): ByteArray {
        val unitByte = ByteArray(byte1.size + byte2.size)
        System.arraycopy(byte1, 0, unitByte, 0, byte1.size)
        System.arraycopy(byte2, 0, unitByte, byte1.size, byte2.size)
        return unitByte
    }

    @JvmStatic
	fun hexToByteArray(inHex: String): ByteArray {
        var inHexRemoveAir = inHex.replace(" ", "")
        var hexLength = inHexRemoveAir.length
        val result: ByteArray
        if (hexLength % 2 != 1) {
            //偶数
            result = ByteArray(hexLength / 2)
        } else {
            //奇数
            hexLength++
            result = ByteArray(hexLength / 2)
            inHexRemoveAir = "0$inHexRemoveAir"
        }
        var j = 0
        var i = 0
        while (i < hexLength) {
            result[j] = inHexRemoveAir.substring(i, i + 2).toInt(16).toByte()
            j++
            i += 2
        }
        return result
    }

    @JvmStatic
	fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuffer()
        for (aByte in bytes) {
            val hex = Integer.toHexString(aByte.toInt() and 0xFF)
            if (hex.length < 2) {
                sb.append(0)
            }
            sb.append(hex).append(" ")
        }
        return sb.toString()
    }

    /**
     * 获取本机内网ip
     * @return
     * @throws UnknownHostException
     */
    @JvmStatic
    @Throws(UnknownHostException::class)
    fun getPrivateIp(): String? {
        return InetAddress.getLocalHost().hostAddress
    }

    /**
     * ip地址转成long型数字
     * 将IP地址转化成整数的方法如下：
     * 1、通过String的split方法按.分隔得到4个长度的数组
     * 2、通过左移位操作（<<）给每一段的数字加权，第一段的权为2的24次方，第二段的权为2的16次方，第三段的权为2的8次方，最后一段的权为1
     * @param strIp
     * @return
     */
    @JvmStatic
    fun ipToLong(strIp: String): Long {
        if (strIp.equals("0")) {
            return 0
        }
        val ip = strIp.split(".").toTypedArray()
        return (ip[0].toLong() shl 24) + (ip[1].toLong() shl 16) + (ip[2].toLong() shl 8) + 0
    }

    /**
     * 将十进制整数形式转换成127.0.0.1形式的ip地址
     * 将整数形式的IP地址转化成字符串的方法如下：
     * 1、将整数值进行右移位操作（>>>），右移24位，右移时高位补0，得到的数字即为第一段IP。
     * 2、通过与操作符（&）将整数值的高8位设为0，再右移16位，得到的数字即为第二段IP。
     * 3、通过与操作符吧整数值的高16位设为0，再右移8位，得到的数字即为第三段IP。
     * 4、通过与操作符吧整数值的高24位设为0，得到的数字即为第四段IP。
     * @param longIp
     * @return
     */
    @JvmStatic
    fun longToIP(longIp: Long): String {
        val sb = StringBuffer("")
        // 直接右移24位
        sb.append((longIp ushr 24).toString())
        sb.append(".")
        // 将高8位置0，然后右移16位
        sb.append((longIp and 0x00FFFFFF ushr 16).toString())
        sb.append(".")
        // 将高16位置0，然后右移8位
        sb.append((longIp and 0x0000FFFF ushr 8).toString())
        sb.append(".")
        // 将高24位置0
        sb.append((longIp and 0x000000FF).toString())
        return sb.toString()
    }
}