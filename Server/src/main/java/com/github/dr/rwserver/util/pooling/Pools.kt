/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.pooling

object Pools {
    /**
     * Returns a new or existing pool for the specified type, stored in a Class to [Pool] map. Note that the max size is ignored for some reason.
     * if this is not the first time this pool has been requested.
     */
    operator fun <T> get(type: Class<T>?, supplier: () -> T, max: Int): Pool<T> {
        return object : Pool<T>(4, max) {
                override fun newObject(): T {
                    return supplier()
                }
            }
    }

    /**
     * Returns a new or existing pool for the specified type, stored in a Class to [Pool] map. The max size of the pool used
     * is 5000.
     */
    operator fun <T> get(type: Class<T>?, supplier: () -> T): Pool<T> {
        return get(type, supplier, 5000)
    }
}