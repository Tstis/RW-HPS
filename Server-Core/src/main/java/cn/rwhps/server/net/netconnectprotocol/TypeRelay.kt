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
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.DataPermissionStatus.RelayStatus
import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.net.core.server.AbstractNetConnect
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
import cn.rwhps.server.util.PacketType.*
import cn.rwhps.server.util.ReflectionUtils
import cn.rwhps.server.util.game.CommandHandler

open class TypeRelay : TypeConnect {
    val con: GameVersionRelay
    var conClass: Class<out GameVersionRelay>? = null

    override val abstractNetConnect: AbstractNetConnect
        get() = con

    constructor(con: GameVersionRelay) {
        this.con = con
    }
    constructor(con: Class<out GameVersionRelay>) {
        // will not be used ; just override the initial value to avoid refusing to compile
        this.con = ReflectionUtils.accessibleConstructor(con, ConnectionAgreement::class.java).newInstance(ConnectionAgreement())

        // use for instantiation
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
        if (permissionStatus == RelayStatus.HostPermission) {
            when (packet.type) {
                PACKET_FORWARD_CLIENT_TO  -> {
                    con.addRelaySend(packet)
                    return
                }
                CHAT -> {
                    GameInputStream(packet).use {
                        val message = it.readString()
                        it.skip(1)
                        if (it.isReadString() == con.name) {
                            if (message.startsWith(".")) {
                                val response = Data.RELAY_COMMAND.handleMessage(message, con)
                                if (response == null || response.type == CommandHandler.ResponseType.noCommand) {
                                } else if (response.type != CommandHandler.ResponseType.valid) {
                                    val text: String = when (response.type) {
                                        CommandHandler.ResponseType.manyArguments -> "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText
                                        CommandHandler.ResponseType.fewArguments -> "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText
                                        else -> return@use
                                    }
                                    con.sendPacket(NetStaticData.RwHps.abstractNetPacket.getSystemMessagePacket(text))
                                }
                            }
                        }
                    }
                    return
                }
                else -> {}
            }
        }

        when (packet.type) {
            HEART_BEAT -> {
                con.addGroup(packet)
                con.getPingData(packet)
            }
            PACKET_FORWARD_CLIENT_TO_REPEATED -> {
            }
            ACCEPT_START_GAME -> {
                con.relay!!.isStartGame = true
                con.sendResultPing(packet)
            }
            DISCONNECT -> con.disconnect()
            SERVER_DEBUG_RECEIVE -> con.debug(packet)
            else -> con.sendResultPing(packet)

        }
    }

    protected fun relayCheck(packet: Packet): Boolean {
        con.lastReceivedTime()

        if (packet.type == SERVER_DEBUG_RECEIVE) {
            con.permissionStatus = RelayStatus.Debug
        }

        val permissionStatus = con.permissionStatus

        if (permissionStatus.ordinal < RelayStatus.PlayerPermission.ordinal) {
            // Initial Connection
            if (permissionStatus == RelayStatus.InitialConnection) {
                if (packet.type == PREREGISTER_INFO_RECEIVE) {
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
                if (packet.type == RELAY_152_151_RETURN) {
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
                if (packet.type == RELAY_118_117_RETURN) {
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