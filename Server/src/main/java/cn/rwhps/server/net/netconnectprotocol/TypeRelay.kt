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
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.net.core.server.AbstractNetConnect
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.ReflectionUtils

open class TypeRelay : TypeConnect {
    val con: GameVersionRelay
    var conClass: Class<out GameVersionRelay>? = null

    override val abstractNetConnect: AbstractNetConnect
        get() = con

    constructor(con: GameVersionRelay) {
        this.con = con
    }
    constructor(con: Class<out GameVersionRelay>) {
        // 不会被使用
        this.con = GameVersionRelay(ConnectionAgreement())
        // 给实例化使用
        conClass = con
    }

    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
         return TypeRelay(ReflectionUtils.accessibleConstructor(conClass!!, ConnectionAgreement::class.java).newInstance(connectionAgreement))
    }

    @Throws(Exception::class)
    override fun typeConnect(packet: Packet) {
        con.lastReceivedTime()

        // CPU branch prediction
        if (packet.type == 175) {
            con.addRelaySend(packet)
        } else {
            when (packet.type) {
                PacketType.PACKET_HEART_BEAT -> {
                    con.addGroup(packet)
                    con.getPingData(packet)
                }

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

                118 -> con.sendRelayServerTypeReply(packet)
                176 -> {
                }
                112 -> {
                    con.relay!!.isStartGame = true
                    con.sendResultPing(packet)
                }
                PacketType.PACKET_DISCONNECT -> con.disconnect()
                PacketType.PACKET_SERVER_DEBUG -> con.debug(packet)
                else -> con.sendResultPing(packet)

        }

        }
    }

    override val version: String
        get() = "RELAY"
}