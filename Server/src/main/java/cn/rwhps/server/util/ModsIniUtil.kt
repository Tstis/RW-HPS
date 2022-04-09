/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util

internal object ModsIniUtil {
    fun nameCheck(paramString: String) {
        if (paramString.isEmpty()) {
            throw RuntimeException("name cannot be empty")
        }
        if (paramString.contains(" ") ||
            paramString.contains("}") ||
            paramString.contains("$") ||
            paramString.contains(".") ||
            paramString.contains("{") ||
            paramString.contains("-") ||
            paramString.contains("+") ||
            paramString.contains(":") ||
            paramString.contains("(")
        ) {
            throw RuntimeException("invalid character in name")
        }
        if (Character.isDigit(paramString[0])) {
            throw RuntimeException("name cannot start with a digit")
        }
    }

    fun checkCharAt(paramString: String): Boolean {
        for (b in paramString.indices) {
            val c = paramString[b]
            if (!Character.isDigit(c) && c != '.') {
                if (c != '-' || b != 0) {
                    return false
                }
            }
        }
        return true
    }

    fun checkForInclusion(paramString: String): Boolean {
        if (paramString.contains("*")) {
            return true
        }
        if (paramString.contains("/")) {
            return true
        }
        if (paramString.contains("+")) {
            return true
        }
        if (paramString.contains("-")) {
            return true
        }
        if (paramString.contains("(")) {
            return true
        }
        if (paramString.contains(")")) {
            return true
        }
        return paramString.contains("^")
    }

    /**
     * 转换部分
     */


    @Strictfp
    fun doubleToString(paramDouble: Double): String {
        return if (paramDouble == paramDouble.toInt().toDouble()) {
            "" + paramDouble.toInt()
        } else {
            "" + paramDouble
        }
    }


    /**
     * 无法理解
     */
}