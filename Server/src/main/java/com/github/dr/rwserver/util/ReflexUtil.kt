/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

object ReflexUtil {
    @JvmStatic
    @Throws(Exception::class)
    fun modifyVariablesFinalStatic(type: Class<*>, name: String?, data: Any?) {
        // 反射获取字段, name成员变量
        val nameField = type.getDeclaredField(name)
        // 由于name成员变量是private, 所以需要进行访问权限设定
        nameField.isAccessible = true
        // 使用反射进行赋值
        nameField[type.getDeclaredConstructor().newInstance()] = data
    }
}