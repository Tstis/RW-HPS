/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

import com.github.dr.rwserver.struct.Seq

/**
 * @author Dr
 */
object Convert {
    @JvmStatic
    fun <T> castList(obj: Any?, clazz: Class<T>): List<T>? {
        val result: MutableList<T> = ArrayList()
        if (obj is List<*>) {
            for (o in obj) {
                result.add(clazz.cast(o))
            }
            return result
        }
        return null
    }

    @JvmStatic
    fun <T> castSeq(obj: Any?, clazz: Class<T>): Seq<T>? {
        val result = Seq<T>()
        if (obj is Seq<*>) {
            for (o in obj) {
                result.add(clazz.cast(o))
            }
            return result
        }
        return null
    }
}