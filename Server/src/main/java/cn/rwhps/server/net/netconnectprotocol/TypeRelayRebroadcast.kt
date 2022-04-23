/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol

import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.DataPermissionStatus.RelayStatus
import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionRelayRebroadcast
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.ReflectionUtils
import java.util.stream.IntStream

class TypeRelayRebroadcast : TypeRelay {
    constructor(con: GameVersionRelay) : super(con)

    constructor(con: Class<out GameVersionRelayRebroadcast>) : super(con)

    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
        return TypeRelayRebroadcast(ReflectionUtils.accessibleConstructor(conClass!!, ConnectionAgreement::class.java).newInstance(connectionAgreement))
    }

    @Throws(Exception::class)
    override fun typeConnect(packet: Packet) {
        if (relayCheck(packet)) {
            return
        }

        val permissionStatus = con.permissionStatus

        if (permissionStatus == RelayStatus.HostPermission) {
            when (packet.type) {
                PacketType.PACKET_FORWARD_CLIENT_TO.type  -> con.addRelaySend(packet)
                PacketType.PACKET_FORWARD_CLIENT_TO_REPEATED.type  -> con.multicastAnalysis(packet)
                else -> con.setlastSentPacket(packet)
            }
        }

        when {
            IntStream.of(
                PacketType.START_GAME.type,
                PacketType.CHAT.type ,
                PacketType.TEAM_LIST.type).anyMatch { i: Int -> i == packet.type } -> {
                // 快速抛弃
            }
            packet.type == PacketType.HEART_BEAT.type -> {
                con.getPingData(packet)
                //con.addGroup(packet)
            }
            IntStream.of(
                PacketType.SYNCCHECKSUM_STATUS.type,
                PacketType.HEART_BEAT_RESPONSE.type,
                PacketType.GAMECOMMAND_RECEIVE.type
            ).anyMatch { i: Int -> i == packet.type } -> {
                con.sendResultPing(packet)
            }
            else -> {
                when (packet.type) {
                    //141 -> con.receiveChat(packet)
                    PacketType.CHAT_RECEIVE.type -> con.receiveChat(packet)
                    PacketType.REGISTER_PLAYER.type -> con.relayRegisterConnection(packet)
                    PacketType.ACCEPT_START_GAME.type -> {
                        con.relay!!.isStartGame = true
                        con.sendResultPing(packet)
                    }
                    //PacketType.PACKET_ADD_CHAT -> con.addRelayAccept(packet)
                    PacketType.DISCONNECT.type -> con.disconnect()
                    PacketType.SERVER_DEBUG_RECEIVE.type -> con.debug(packet)
                    else ->
                        //Log.clog(packet.type.toString());
                        con.sendResultPing(packet)
                }
            }
        }

    }

    override val version: String
        get() = "2.1.0"
}