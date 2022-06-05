/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.replay

import cn.rwhps.server.game.replay.block.PointF
import cn.rwhps.server.game.replay.block.Waypoint
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.log.Log.debug

class PlayerDataAnalysis(inStream: GameInputStream) {
    val playerIndex: Int
    val waypoint:Waypoint?

    val boolean_1: Boolean
    val isCancel: Boolean

    // TODO
    val string_1: String

    val doNo: A

    val pointF: PointF?

    val boolean_2: Boolean

    /** 批量执行的单位数量 */
    val batchCount: Int
    val batchCountList = Seq<Long>()

    val byte_1: Byte

    val ping: PointF?

    // TODO
    val string_2: String

    val boolean_3: Boolean

    val controlCheckSum: Short

    val withOrWithoutUnitData: Boolean

    // change Step Rate
    var changeStepRate: Float = 0F
    var float_1: Float = 0F
    var systemCommand: Int = 0



    init {
        with (inStream) {
            playerIndex = readInt()
            debug("Player index",playerIndex)

            waypoint = if (readBoolean()) Waypoint(inStream) else null

            boolean_1 = readBoolean()
            isCancel = readBoolean()
            debug("Is player cancel a command",isCancel)


            string_1 = readInt().toString()

            doNo = readEnum(A::class.java) as A
            debug("[可能] 是操控单位的范围",doNo.name)


            pointF = if (readBoolean()) PointF(readFloat(),readFloat()) else null
            debug("Whether to control the unit move", if (pointF == null) "false" else "true X: ${pointF.x} Y: ${pointF.y}")

            boolean_2 = readBoolean()

            batchCount = readInt()
            for (index in 0..batchCount) {
                batchCountList.add(readLong())
            }

            byte_1 = if (readBoolean()) readByte().toByte() else -1

            ping = if (readBoolean()) PointF(readFloat(),readFloat()) else null
            debug("Whether to show Ping on the map", if (ping == null) "false" else "true X: ${ping.x} Y: ${ping.y}")


            readLong() // False TODO

            string_2 = readString()

            boolean_3 = readBoolean()

            controlCheckSum = readShort()
            debug("controlCheckSum",controlCheckSum)

            withOrWithoutUnitData = readBoolean()
            debug("withOrWithoutUnitData",withOrWithoutUnitData)

            if (withOrWithoutUnitData) {
                readByte() // skip
                changeStepRate = readFloat()
                float_1 = readFloat()
                systemCommand = readInt()

                val moveUnitsCount = readInt()

                for (index in 0..moveUnitsCount) {
                    // skip  By.Dr
                }
            }
        }
    }

    enum class A {
        outOfRange, onlyInRange, returnFire, holdFire, guardArea, aggressive, mixed
    }

    companion object {
        fun handlingTickPackages(packet: Packet) {
            GameInputStream(packet).use {
                val frameGame = it.readInt()/10
                val readCount = it.readInt()
                for (index in 0..readCount) {
                    // Name: c
                    // Gzip: false
                    PlayerDataAnalysis(it.getDecodeStream(false))
                }

            }
        }
    }
}