/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object Strings {
    val utf8: Charset = StandardCharsets.UTF_8

    /** Replaces all instances of `find` with `replace`.  */
    fun replace(builder: StringBuilder, find: String, replace: String): StringBuilder {
        val findLength = find.length
        val replaceLength = replace.length
        var index = 0
        while (true) {
            index = builder.indexOf(find, index)
            if (index == -1) {
                break
            }
            builder.replace(index, index + findLength, replace)
            index += replaceLength
        }
        return builder
    }

    /** Replaces all instances of `find` with `replace`.  */
    fun replace(builder: StringBuilder, find: Char, replace: String): StringBuilder {
        val replaceLength = replace.length
        var index = 0
        while (true) {
            while (true) {
                if (index == builder.length) {
                    return builder
                }
                if (builder[index] == find) {
                    break
                }
                index++
            }
            builder.replace(index, index + 1, replace)
            index += replaceLength
        }
    }
}