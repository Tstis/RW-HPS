/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.data.global.cache

import cn.rwhps.server.struct.ObjectMap
import cn.rwhps.server.util.IsUtil
import cn.rwhps.server.util.Time
import cn.rwhps.server.util.log.Log

class ExistenceCache {
    private val cache: ObjectMap<String, CacheData> = ObjectMap()

    fun addCache(key: String, value: String) {
        Log.error("add",key)
        cache.put(key, CacheData(value))
    }

    fun addCheckCache(key: String): Boolean {
        return cache.containsKey(key)
    }

    fun getCache(key: String): String? {
        val cacheData = cache[key]

        Log.error("getTest: $key")

        if (IsUtil.notIsBlank(cacheData)) {
            cache.remove(key)
            Log.error("get: $key",cacheData.data)
            return cacheData.data
        } else {
            return null
        }
    }

    private class CacheData(val data: String) {
        private val cleanTime = Time.concurrentSecond() + 300

        fun isClean(): Boolean {
            return cleanTime < Time.concurrentSecond()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (javaClass != other?.javaClass) {
                return false
            }

            if (data != (other as CacheData).data) {
                return false
            }

            return true
        }

        override fun hashCode(): Int {
            return data.hashCode()
        }

        override fun toString(): String {
            return data
        }
    }
}