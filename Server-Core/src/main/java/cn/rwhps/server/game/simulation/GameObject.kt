/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation

import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.log.Log
import java.util.concurrent.ConcurrentLinkedQueue

abstract class GameObject() : SyncedObject {

    var deleted = false
    val id = getNextUnitId()

    init {
        gameObjectList.add(this)
        fastGameObjectList.add(this)
    }

    open fun remove() {
        gameObjectList.remove(this)
        fastGameObjectList.remove(this)
        this.deleted = true
    }

    fun getFromId(id: Long): GameObject? {
        if (id == -1L) {
            return null
        }
        val it: Iterator<GameObject> = fastGameObjectList.iterator()

        while (it.hasNext()) {
            val `object`: GameObject = it.next()
            if (`object`.id == id) {
                return `object`
            }
        }
        Log.error("ReportDesync","getFromId:$id was not found")
        return null
    }

    /*
    fun getUnitFromId(id: Long): Unit? {
        return getFromId(id) as Unit?
    }

    fun getOrderableUnitFromId(id: Long): OrderableUnit? {
        return getFromId(id) as OrderableUnit?
    }
*/
    fun getGameObjectList(): ConcurrentLinkedQueue<GameObject> {
        return gameObjectList
    }
    
    companion object {
        val gameObjectList = ConcurrentLinkedQueue<GameObject>()
        val fastGameObjectList = Seq<GameObject>()
        private var unitId = 0L


        fun getNextUnitId(): Long {
            return unitId++
        }
    }
}