/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.encryption

/**
 * @author Dr.
 * @Data 2020/6/25 9:28
 */
object Game {
    // 1.14
    @JvmStatic
	fun connectKey(paramInt: Int): String {
        return buildString {
            append("c:${paramInt}")
            append("m:${paramInt * 87 + 24}")
            append("0:${paramInt * 44000}")
            append("1:${paramInt}")
            append("2:${paramInt * 13000}")
            append("3:${paramInt + 28000}")
            append("4:${paramInt * 75000}")
            append("5:${paramInt + 160000}")
            append("6:${paramInt * 850000}")
            append("t1:${paramInt * 44000}")
            append("d:${paramInt * 5}")
        }
    }

    // 1.15
    @JvmStatic
    fun connectKeyNew(paramInt: Int): String {
        return buildString {
            append("c:${paramInt}")
            append("m:${paramInt * 87 + 24}")
            append("0:${paramInt * 44000}")
            append("1:${paramInt}")
            append("2:${paramInt * 13000}")
            append("3:${paramInt + 28000}")
            append("4:${paramInt * 75000}")
            append("5:${paramInt + 160000}")
            append("6:${paramInt * 850000}")
            append("t1:${paramInt * 4000.0 * 11.0}")
            append("d:${paramInt * 5}")
        }
    }
}