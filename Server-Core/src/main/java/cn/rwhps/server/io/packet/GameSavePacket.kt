/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.io.packet

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.util.ExtractUtil
import cn.rwhps.server.util.log.Log

class GameSavePacket(val packet: Packet) {
    fun convertGameSaveDataPacket(): Packet {
        return NetStaticData.RwHps.abstractNetPacket.convertGameSaveDataPacket(packet)
    }

    /**
     * 检测 诱骗的GameSave包是否对得住 (防止获取到的是旧的)
     * @return Boolean
     */
    fun checkTick(): Boolean {
        GameInputStream(packet).use { stream ->
            stream.readByte()
            val tick = stream.readInt()
            val tickGame = Data.game.tickGame.get()
            return ((tickGame - 50) < tick && tick < (tickGame + 50))
        }
    }

    /**
     * 解析 GameSave包
     */
    fun analyze() {
        GameInputStream(packet).use { stream ->
            stream.readByte()
            Log.clog("Tick : {0}",stream.readInt())
            Log.clog("{0}",stream.readInt())
            Log.clog("{0}",stream.readFloat())
            Log.clog("{0}",stream.readFloat())
            Log.clog("{0}",stream.readBoolean())
            Log.clog("{0}",stream.readBoolean())

            stream.getDecodeStream(false).use { gameSave ->
                Log.clog(gameSave.readString())
                gameSave.readInt()
                Log.clog("GameSave Version: {0}", gameSave.readInt())
                gameSave.readBoolean()
                // GameSave Version > 23
                gameSave.getDecodeStream(true).use { saveCompression ->
                    // GameSave Version > 54
                    saveCompression.getDecodeStream(false).use { customUnitsBlock ->
                        // 我 不 知 道
                        Log.clog("customUnitsBlock SKIP !  -> {0}",customUnitsBlock.getSize())
                    }
                    // GameSave Version > 56
                    saveCompression.getDecodeStream(false).use { gameSetup ->
                        if (gameSetup.readBoolean()) {
                            gameSetup.readByte()

                            // version
                            gameSetup.readByte()
                            Log.clog("fogMode : {0}",gameSetup.readInt())
                            Log.clog("startingCredits : {0}",gameSetup.readInt())
                            Log.clog("revealedMap : {0}",gameSetup.readBoolean())
                            Log.clog("aiDifficulty : {0}",gameSetup.readInt())
                            Log.clog("startingUnits : {0}",gameSetup.readInt())
                            Log.clog("incomeMultiplier : {0}",gameSetup.readFloat())
                            Log.clog("noNukes : {0}",gameSetup.readBoolean())
                            Log.clog("? Boolean : {0}",gameSetup.readBoolean())
                            Log.clog("sharedControl : {0}",gameSetup.readBoolean())
                            // Version 1
                            Log.clog("? Boolean : {0}",gameSetup.readBoolean())
                            // Version 2
                            Log.clog("? Boolean : {0}",gameSetup.readBoolean())
                            // Version 3
                            Log.clog("allowSpectators : {0}",gameSetup.readBoolean())
                            Log.clog("lockedRoom : {0}",gameSetup.readBoolean())

                            Log.clog("MaxUnit ? : {0}",gameSetup.readInt())
                            Log.clog("MaxUnit ? : {0}",gameSetup.readInt())
                        }
                    }

                    Log.clog("MapName ? : {0}",saveCompression.readString())

                    // GameSave Version > 72
                    if (saveCompression.readBoolean()) {
                        val mapBytes = saveCompression.readStreamBytes()
                        Log.clog("Reading remote map stream : Size: {0}",mapBytes.size)
                    }

                    /**
                    if (u.bF.z && !u.bF.A && z3 && u.bF.aK != null && !z4) {
                        u.cS = "";
                        u.cT = u.bF.aK;
                    }
                    bjVar.a(bk.load_map);

                    // z3 always = true ?
                    if (z3) {
                        u.a(true, true, s.normalSave);
                        if (l.aj()) {
                        u.dc = true;
                        }
                    } else {
                        u.a(true, s.normalSave);
                    }
                     */

                    // not finished continue ....
                    Log.clog("saveCompression SKIP !  -> {0}",saveCompression.getSize())
                }

                // GameSave Version > 19
                if (gameSave.readShort() != "12345".toShort()) {
                    Log.clog("Mark wasn't read for: End of Save")

                }
                gameSave.readString()

                Log.clog("gameSave SKIP !  -> {0}",gameSave.getSize())

            }
        }
    }

    /**
     * Return detailed Packet data
     * @return Packet String
     */
    override fun toString(): String {
        return  """
                GameSavePacket {
                    Bytes=${packet.bytes.contentToString()}
                    BytesHex=${ExtractUtil.bytesToHex(packet.bytes)}
                    Type=${packet.type}
                }
                """.trimIndent()
    }
}