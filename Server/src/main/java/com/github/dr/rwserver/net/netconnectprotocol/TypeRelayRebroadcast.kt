<<<<<<< HEAD
/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.netconnectprotocol

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.net.game.ConnectionAgreement
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionRelayRebroadcast
import com.github.dr.rwserver.util.PacketType
import java.util.stream.IntStream

class TypeRelayRebroadcast(val con: GameVersionRelayRebroadcast) : TypeConnect(con) {
    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
        return TypeRelayRebroadcast(GameVersionRelayRebroadcast(connectionAgreement))
    }

    @Throws(Exception::class)
    override fun typeConnect(packet: Packet) {
        con.lastReceivedTime()

        // CPU branch prediction
        if (IntStream.of(175, 176).noneMatch { i: Int -> i == packet.type }) {
            con.setlastSentPacket(packet)
        }
        if (packet.type == 175) {
            con.addRelaySend(packet)
        } else if (packet.type == 176) {
            con.multicastAnalysis(packet)
        } else {
            when {
                IntStream.of(120,141 , 115).anyMatch { i: Int -> i == packet.type } -> {
                    // 快速抛弃
                }
                packet.type == PacketType.PACKET_HEART_BEAT -> {
                    con.getPingData(packet)
                    //con.addGroup(packet)
                }
                IntStream.of(
                    PacketType.PACKET_SYNCCHECKSUM_STATUS,
                    PacketType.PACKET_HEART_BEAT_RESPONSE,
                    PacketType.PACKET_ADD_GAMECOMMAND
                ).anyMatch { i: Int -> i == packet.type } -> {
                    con.sendResultPing(packet)
                }
                else -> {
                    when (packet.type) {
                        PacketType.PACKET_PREREGISTER_CONNECTION -> {
                            con.setCachePacket(packet)
                            con.sendRelayServerInfo()
                            con.sendRelayServerCheck()
                        }
                        152 -> {
                            if (con.receiveRelayServerCheck(packet)) {
                                if (!Data.config.SingleUserRelay) {
                                    con.relayDirectInspection()
                                } else {
                                    NetStaticData.relay.setAddSize()
                                    if (NetStaticData.relay.admin == null) {
                                        con.sendRelayServerId()
                                    } else {
                                        con.addRelayConnect()
                                    }
                                }
                            } else {
                                con.disconnect()
                            }
                        }
                        //141 -> con.receiveChat(packet)
                        140 -> con.receiveChat(packet)
                        118 -> con.sendRelayServerTypeReply(packet)
                        110 -> con.relayRegisterConnection(packet)
                        112 -> {
                            con.relay!!.isStartGame = true
                            con.sendResultPing(packet)
                        }
                        //PacketType.PACKET_ADD_CHAT -> con.addRelayAccept(packet)
                        PacketType.PACKET_DISCONNECT -> con.disconnect()
                        PacketType.PACKET_SERVER_DEBUG -> con.debug(packet)
                        else ->
                            //Log.clog(packet.type.toString());
                            con.sendResultPing(packet)
                    }
                }
            }
        }

    }

    override val version: String
        get() = "2.0.0"
=======
/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.netconnectprotocol

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.net.game.ConnectionAgreement
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionRelayRebroadcast
import com.github.dr.rwserver.util.PacketType
import java.util.stream.IntStream

class TypeRelayRebroadcast(val con: GameVersionRelayRebroadcast) : TypeConnect(con) {
    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
        return TypeRelayRebroadcast(GameVersionRelayRebroadcast(connectionAgreement))
    }

    @Throws(Exception::class)
    override fun typeConnect(packet: Packet) {
        con.lastReceivedTime()

        // CPU branch prediction
        if (IntStream.of(175, 176).noneMatch { i: Int -> i == packet.type }) {
            con.setlastSentPacket(packet)
        }
        if (packet.type == 175) {
            con.addRelaySend(packet)
        } else if (packet.type == 176) {
            con.multicastAnalysis(packet)
        } else {
            when {
                IntStream.of(120,141 , 115).anyMatch { i: Int -> i == packet.type } -> {
                    // 快速抛弃
                }
                packet.type == PacketType.PACKET_HEART_BEAT -> {
                    con.getPingData(packet)
                    //con.addGroup(packet)
                }
                IntStream.of(
                    PacketType.PACKET_SYNCCHECKSUM_STATUS,
                    PacketType.PACKET_HEART_BEAT_RESPONSE,
                    PacketType.PACKET_ADD_GAMECOMMAND
                ).anyMatch { i: Int -> i == packet.type } -> {
                    con.sendResultPing(packet)
                }
                else -> {
                    when (packet.type) {
                        PacketType.PACKET_PREREGISTER_CONNECTION -> {
                            con.setCachePacket(packet)
                            con.sendRelayServerInfo()
                            con.sendRelayServerCheck()
                        }
                        152 -> {
                            if (con.receiveRelayServerCheck(packet)) {
                                if (!Data.config.SingleUserRelay) {
                                    con.relayDirectInspection()
                                } else {
                                    NetStaticData.relay.setAddSize()
                                    if (NetStaticData.relay.admin == null) {
                                        con.sendRelayServerId()
                                    } else {
                                        con.addRelayConnect()
                                    }
                                }
                            } else {
                                con.disconnect()
                            }
                        }
                        //141 -> con.receiveChat(packet)
                        140 -> con.receiveChat(packet)
                        118 -> con.sendRelayServerTypeReply(packet)
                        110 -> con.relayRegisterConnection(packet)
                        112 -> {
                            con.relay!!.isStartGame = true
                            con.sendResultPing(packet)
                        }
                        //PacketType.PACKET_ADD_CHAT -> con.addRelayAccept(packet)
                        PacketType.PACKET_DISCONNECT -> con.disconnect()
                        PacketType.PACKET_SERVER_DEBUG -> con.debug(packet)
                        else ->
                            //Log.clog(packet.type.toString());
                            con.sendResultPing(packet)
                    }
                }
            }
        }

    }

    override val version: String
        get() = "2.0.0"
>>>>>>> ab468a8efac1b6b73535dee9461c3323bd13d144
}