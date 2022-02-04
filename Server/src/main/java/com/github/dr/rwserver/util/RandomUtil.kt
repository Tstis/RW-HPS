/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

import java.util.*

object RandomUtil {
    private const val ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LETTERCHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val NUMBERCHAR = "123456789"

    /**
     * 产生len长度的随机字符串
     * @param len
     * @return
     */
    @JvmStatic
    fun generateStr(len: Int): String {
        val sb = StringBuffer()
        val random = Random()
        for (i in 0 until len) {
            sb.append(ALLCHAR[random.nextInt(ALLCHAR.length)])
        }
        return sb.toString()
    }

    /**
     * 产生len长度的随机数字
     * @param len
     * @return
     */
    @JvmStatic
    fun generateInt(len: Int): Int {
        val sb = StringBuffer()
        val random = Random()
        for (i in 0 until len) {
            sb.append(NUMBERCHAR[random.nextInt(NUMBERCHAR.length)])
        }
        return sb.toString().toInt()
    }

    /**
     * 产生Str+len长度的随机数字
     * @param len
     * @return
     */
    @JvmStatic
    fun generateStrInt(str: String?, len: Int): String {
        val sb = StringBuffer(str)
        val random = Random()
        for (i in 0 until len) {
            sb.append(NUMBERCHAR[random.nextInt(NUMBERCHAR.length)])
        }
        return sb.toString()
    }

    /**
     * 返回一个定长的随机纯字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    @JvmStatic
    fun generateMixStr(length: Int): String {
        val sb = StringBuffer()
        val random = Random()
        for (i in 0 until length) {
            sb.append(LETTERCHAR[random.nextInt(LETTERCHAR.length)])
        }
        return sb.toString()
    }

    /**
     * 返回一个定长的随机纯大写字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    @JvmStatic
    fun generateLowerStr(length: Int): String {
        return generateMixStr(length).lowercase(Locale.getDefault())
    }

    /**
     * 返回一个定长的随机纯小写字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    @JvmStatic
    fun generateUpperStr(length: Int): String {
        return generateMixStr(length).uppercase(Locale.getDefault())
    }
}