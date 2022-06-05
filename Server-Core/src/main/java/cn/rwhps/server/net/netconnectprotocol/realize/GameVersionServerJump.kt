/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol.realize

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.packet.GameCommandPacket
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.netconnectprotocol.internal.relay.fromRelayJumpsToAnotherServer
import cn.rwhps.server.net.netconnectprotocol.internal.relay.relayServerInitInfo
import cn.rwhps.server.util.log.Log
import java.io.IOException

/**
 * 在Server协议进行扩展 加入更多可用参数
 *
 * 测试功能
 *
 * @author RW-HPS/Dr
 */
class GameVersionServerJump(connectionAgreement: ConnectionAgreement) : GameVersionServer(connectionAgreement) {
    fun sendRelayServerInfo() {
        try {
            sendPacket(relayServerInitInfo())
        } catch (e: Exception) {
            Log.error(e)
        }
    }

    fun jumpNewServer(ip: String) {
        try {
            sendPacket(fromRelayJumpsToAnotherServer(ip))
        } catch (e: Exception) {
            Log.error(e)
        }
    }

    @Throws(IOException::class)
    override fun receiveCommand(p: Packet) {
        //PlayerOperationUnitEvent
        try {
            GameInputStream(GameInputStream(p).getDecodeBytes()).use { inStream ->
                val outStream = GameOutputStream()
                outStream.writeByte(inStream.readByte())
                val boolean1 = inStream.readBoolean()
                outStream.writeBoolean(boolean1)
                if (boolean1) {
                    outStream.writeInt(inStream.readInt())
                    val int1 = inStream.readInt()
                    //Log.error(int1)
                    outStream.writeInt(int1)
                    if (int1 == -2) {
                        val nameUnit = inStream.readString()
                        //Log.error(nameUnit)
                        outStream.writeString(nameUnit)
                    }
                    outStream.transferToFixedLength(inStream,28)
                    outStream.writeIsString(inStream)
                }
                outStream.transferToFixedLength(inStream,10)
                val boolean3 = inStream.readBoolean()
                outStream.writeBoolean(boolean3)
                if (boolean3) {
                    outStream.transferToFixedLength(inStream,8)
                }
                outStream.writeBoolean(inStream.readBoolean())
                val int2 = inStream.readInt()
                outStream.writeInt(int2)
                for (i in 0 until int2) {
                    outStream.transferToFixedLength(inStream,8)
                }
                val boolean4 = inStream.readBoolean()
                outStream.writeBoolean(boolean4)
                if (boolean4) {
                    outStream.writeByte(inStream.readByte())
                }
                val boolean5 = inStream.readBoolean()
                outStream.writeBoolean(boolean5)
                if (boolean5) {
                    if (player.getData<String>("Summon") != null) {
                        gameSummon(player.getData<String>("Summon")!!,inStream.readFloat(),inStream.readFloat())
                        player.removeData("Summon")
                        return
                    } else {
                        outStream.transferToFixedLength(inStream,8)
                    }
                }
                outStream.transferToFixedLength(inStream,8)
                outStream.writeString(inStream.readString())
                //outStream.writeBoolean(inStream.readBoolean())
                outStream.writeByte(inStream.readByte())
                inStream.readShort()
                outStream.writeShort(Data.game.playerManage.sharedControlPlayer.toShort())
                outStream.transferTo(inStream)
                Data.game.gameCommandCache.offer(GameCommandPacket(player.site, outStream.getPacketBytes()))
            }
        } catch (e: Exception) {
            Log.error(e)
        } finally {
            //sync.unlock()
        }
    }
}