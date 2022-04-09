/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util

import cn.rwhps.server.util.log.Log
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * IP 工具类
 * @author Dr
 */
object IpUtil {
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

    @JvmStatic
    fun ipToLong24(strIp: String): String {
        return if (strIp == "0") {
            "0"
        } else {
            when (validIPAddressAll(strIp)) {
                "IPv4" -> ipToLong(strIp)
                "IPv6" -> ip2ToLongs(strIp)
                else -> {
                    Log.error("NETWORK_IP_ERROR",strIp)
                    ""
                }
            }
        }
    }

    @JvmStatic
    fun long24ToIp(strLong: String): String {
        return if (strLong.contains("&")) {
            longs2Ip(strLong)
        } else {
            longToIP(strLong.toLong())
        }
    }

    /**
     * ip地址转成long型数字
     * 将IP地址转化成整数的方法如下：
     * 1、通过String的split方法按.分隔得到4个长度的数组
     * 2、通过左移位操作（<<）给每一段的数字加权，第一段的权为2的24次方，第二段的权为2的16次方，第三段的权为2的8次方，最后一段的权为1
     *
     * 忽略最后一位 直接取段
     * @param strIp
     * @return
     */
    private fun ipToLong(strIp: String): String {
        if (strIp == "0") {
            return strIp
        }
        val ip = strIp.split(".").toTypedArray()
        return ((ip[0].toLong() shl 24) + (ip[1].toLong() shl 16) + (ip[2].toLong() shl 8)).toString()
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
    private fun longToIP(longIp: Long): String {
        val sb = StringBuffer("")
        // 直接右移24位
        sb.append((longIp ushr 24).toString()).append(".")
        // 将高8位置0，然后右移16位
        sb.append((longIp and 0x00FFFFFF ushr 16).toString()).append(".")
        // 将高16位置0，然后右移8位
        sb.append((longIp and 0x0000FFFF ushr 8).toString()).append(".")
        // 将高24位置0
        sb.append((longIp and 0x000000FF).toString())
        return sb.toString()
    }

    /**
     * 将 IPv6 地址转为 long 数组，只支持冒分十六进制表示法
     * 忽略最后一位 直接取段
     */
    private fun ip2ToLongs(ipv6String: String): String {
        val ipSlices = ipv6String.split(":").toTypedArray()
        require(ipSlices.size == 8) {
            "$ipv6String is not an ipv6 address."
        }
        val ipv6 = LongArray(2)
        for (i in 0..6) {
            val slice = ipSlices[i]
            // 以 16 进制解析
            val num = slice.toLong(16)
            // 每组 16 位
            val right = num shl 16 * (8 - i - 1)
            // 每个 long 保存四组，i >> 2 = i / 4 ，i对4取余，其实就是前4个在数组0下标位置，后面4个在下标1位置。
            ipv6[i shr 2] = ipv6[i shr 2] or right
        }
        return "${ipv6[0]}&${ipv6[1]}"
    }

    /**
     * 将 long 数组转为冒分十六进制表示法的 IPv6 地址
     */
    private fun longs2Ip(ipv6String: String): String {
        val numbers = ipv6String.split("&")
        val sb = StringBuilder()
        for (numSlice0 in numbers) {
            var numSlice = numSlice0.toLong()
            val ip = arrayOfNulls<String>(4)
            for (j in 0..3) {
                // 取最后 16 位
                val current = numSlice and 0xFFFF
                ip[3 - j] = current.toString(16)
                // 右移 16 位，即去除掉已经处理过的 16 位
                numSlice = numSlice shr 16
            }
            for (v6 in ip) {
                sb.append(v6).append(":")
            }
        }
        // 去掉最后的 :
        return sb.substring(0, sb.length - 1)
    }

    /**
     * 判断所有的IP地址
     * @param IP
     * @return
     */
    private fun validIPAddressAll(IP: String): String {
        if (!IP.contains(".") && !IP.contains(":")) {
            return "Neither"
        }
        //如果是IPV4
        if (IP.contains(".")) {
            if (IP.endsWith(".")) {
                return "Neither"
            }
            val arr = IP.split("\\.".toRegex()).toTypedArray()
            if (arr.size != 4) {
                return "Neither"
            }
            for (i in 0..3) {
                if (arr[i].isEmpty() || arr[i].length > 3) {
                    return "Neither"
                }
                for (j in 0 until arr[i].length) {
                    if (arr[i][j] in ('0'..'9')) {
                        continue
                    }
                    return "Neither"
                }
                if (Integer.valueOf(arr[i]) > 255 || arr[i].length >= 2 && arr[i].startsWith("0")) {
                    return "Neither"
                }
            }
            return "IPv4"
        } //如果是IPV4

        //如果是IPV6
        if (IP.contains(":")) {
            if (IP.endsWith(":") && !IP.endsWith("::")) {
                return "Neither"
            }
            //如果包含多个“::”，一个IPv6地址中只能出现一个“::”
            if (IP.indexOf("::") != -1 && IP.indexOf("::", IP.indexOf("::") + 2) != -1) {
                return "Neither"
            }

            //如果含有一个“::”
            if (IP.contains("::")) {
                val arr = IP.split(":".toRegex()).toTypedArray()
                if (arr.size > 7 || arr.isEmpty()) { //"1::"是最短的字符串
                    return "Neither"
                }
                for (i in arr.indices) {
                    if (arr[i] == "") {
                        continue
                    }
                    if (arr[i].length > 4) {
                        return "Neither"
                    }
                    for (j in 0 until arr[i].length) {
                        if (arr[i][j] in '0'..'9' || arr[i][j] in 'A'..'F'
                            || arr[i][j] in 'a'..'f'
                        ) {
                            continue
                        }
                        return "Neither"
                    }
                }
                return "IPv6"
            }

            //如果不含有“::”
            if (!IP.contains("::")) {
                val arr = IP.split(":".toRegex()).toTypedArray()
                if (arr.size != 8) {
                    return "Neither"
                }
                for (i in arr.indices) {
                    if (arr[i].length > 4) {
                        return "Neither"
                    }
                    for (j in 0 until arr[i].length) {
                        if (arr[i][j] in '0'..'9' || arr[i][j] in 'A'..'F'
                            || arr[i][j] in 'a'..'f'
                        ) {
                            continue
                        }
                        return "Neither"
                    }
                }
                return "IPv6"
            }
        } //如果是IPV6
        return "Neither"
    }
}