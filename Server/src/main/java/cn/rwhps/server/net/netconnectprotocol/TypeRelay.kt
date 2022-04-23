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
import cn.rwhps.server.net.core.DataPermissionStatus.RelayStatus
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
        if (relayCheck(packet)) {
            return
        }

        val permissionStatus = con.permissionStatus

        // CPU branch prediction
        if (packet.type == PacketType.PACKET_FORWARD_CLIENT_TO.type) {
            if (permissionStatus == RelayStatus.HostPermission) {
                con.addRelaySend(packet)
            }
        } else {
            when (packet.type) {
                PacketType.HEART_BEAT.type -> {
                    con.addGroup(packet)
                    con.getPingData(packet)
                }
                PacketType.PACKET_FORWARD_CLIENT_TO_REPEATED.type -> {
                }
                PacketType.ACCEPT_START_GAME.type -> {
                    con.relay!!.isStartGame = true
                    con.sendResultPing(packet)
                }
                PacketType.DISCONNECT.type -> con.disconnect()
                PacketType.SERVER_DEBUG_RECEIVE.type -> con.debug(packet)
                else -> con.sendResultPing(packet)

        }

        }
    }

    protected fun relayCheck(packet: Packet): Boolean {
        con.lastReceivedTime()

        val permissionStatus = con.permissionStatus

        if (permissionStatus.ordinal < RelayStatus.PlayerPermission.ordinal) {
            // Initial Connection
            if (permissionStatus == RelayStatus.InitialConnection) {
                if (packet.type == PacketType.PREREGISTER_INFO_RECEIVE.type) {
                    // Wait Certified
                    con.permissionStatus = RelayStatus.WaitCertified

                    con.setCachePacket(packet)
                    con.sendRelayServerInfo()
                    con.sendVerifyClientValidity()
                } else {
                    con.disconnect()
                }
                return true
            }

            if (permissionStatus == RelayStatus.WaitCertified) {
                if (packet.type == 152) {
                    if (con.receiveVerifyClientValidity(packet)) {
                        // Certified End
                        con.permissionStatus = RelayStatus.CertifiedEnd
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
                        con.sendVerifyClientValidity()
                    }
                } else {
                    con.disconnect()
                }
                return true
            }

            if (permissionStatus == RelayStatus.CertifiedEnd) {
                if (packet.type == PacketType.RELAY_118_117_REC.type) {
                    con.sendRelayServerTypeReply(packet)
                }
                return true
            }

            // 你肯定没验证
            con.disconnect()
        }
        return false
    }

    override val version: String
        get() = "RELAY 1.1.0"
}