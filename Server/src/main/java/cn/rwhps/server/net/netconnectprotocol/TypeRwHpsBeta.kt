/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.io.packet.GameSavePacket
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionServer
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionServerBeta
import cn.rwhps.server.util.ExtractUtil
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.ReflectionUtils
import cn.rwhps.server.util.Time.concurrentSecond
import cn.rwhps.server.util.log.Log

class TypeRwHpsBeta : TypeRwHps {
    constructor(con: GameVersionServer) : super(con)

    constructor(con: Class<out GameVersionServerBeta>) : super(con)

    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
        return TypeRwHpsBeta(ReflectionUtils.accessibleConstructor(conClass!!, ConnectionAgreement::class.java).newInstance(connectionAgreement))
    }

    @Throws(Exception::class)
    override fun typeConnect(packet: Packet) {
        con.lastReceivedTime()

        if (packet.type == PacketType.PACKET_ADD_GAMECOMMAND) {
            con.receiveCommand(packet)
            con.player.lastMoveTime = concurrentSecond()
        } else {
            when (packet.type) {
                PacketType.PACKET_PREREGISTER_CONNECTION -> con.registerConnection(packet)
                PacketType.PACKET_PLAYER_INFO -> if (!con.getPlayerInfo(packet)) {
                    con.disconnect()
                }
                PacketType.PACKET_HEART_BEAT_RESPONSE -> {
                    val player = con.player
                    player.ping = (System.currentTimeMillis() - player.timeTemp).toInt() shr 1
                }
                PacketType.PACKET_ADD_CHAT -> con.receiveChat(packet)
                PacketType.PACKET_DISCONNECT -> con.disconnect()
                PacketType.PACKET_ACCEPT_START_GAME -> con.player.start = true
                PacketType.PACKET_SERVER_DEBUG -> con.debug(packet)
                // 竞争 谁先到就用谁
                PacketType.PACKET_SYNC -> {
                    val gameSavePacket = GameSavePacket(packet)
                    if (gameSavePacket.checkTick()) {
                        Data.game.gameSaveCache = gameSavePacket
                        synchronized(Data.game.gameSaveWaitObject) {
                            Data.game.gameSaveWaitObject.notifyAll()
                        }
                    }
                }

                0 -> {
                    // 忽略空包
                }

                else -> {
                    Log.warn("[Unknown Package]", """
                            Type : ${packet.type} Length : ${packet.bytes.size}
                            Hex : ${ExtractUtil.bytesToHex(packet.bytes)}
                        """.trimIndent())
                }
            }
        }
    }

    override val version: String
        get() = "${Data.SERVER_CORE_VERSION}: 1.1.0"
}