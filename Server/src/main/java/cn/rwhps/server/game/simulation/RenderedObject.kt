/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package cn.rwhps.server.game.simulation

/**
 * 渲染游戏对象
 */
abstract class RenderedObject : GameObject() {
    var x = 0f
    var y = 0f
    var height = 0.0f
    var drawOrder = 0

    class RenderedObjectComparator : Comparator<Any?> {
        override fun compare(object1: Any?, object2: Any?): Int {
            return if (object1 !is RenderedObject || object2 !is RenderedObject) {
                0
            } else {
                object1.drawOrder - object2.drawOrder
            }
        }
    }
}