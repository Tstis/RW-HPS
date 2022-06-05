/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.math

/**
 * @author RW-HPS/Dr
 */
object Mathf {
    @JvmField
    val random = Rand()

    /** 获取2的次方.  */
    @JvmStatic
    fun nextPowerOfTwo(valueIn: Int): Int {
        var value = valueIn
        if (value == 0) {
            return 1
        }
        value--
        value = value or (value shr 1)
        value = value or (value shr 2)
        value = value or (value shr 4)
        value = value or (value shr 8)
        value = value or (value shr 16)
        return value + 1
    }

    /** 返回介于0（含）和指定值（含）之间的随机数  */
    @JvmStatic
    fun random(range: Int): Int {
        return random.nextInt(range + 1)
    }

    /** 返回开始（包括）和结束（包括）之间的随机数  */
    @JvmStatic
    fun random(start: Int, end: Int): Int {
        return start + random.nextInt(end - start + 1)
    }
}