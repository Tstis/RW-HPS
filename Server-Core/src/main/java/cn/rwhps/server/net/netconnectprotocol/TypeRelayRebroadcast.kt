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
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionRelayRebroadcast
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.ReflectionUtils
import cn.rwhps.server.util.game.CommandHandler

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
                PacketType.PACKET_FORWARD_CLIENT_TO  -> {
                    con.addRelaySend(packet)
                    return
                }
                PacketType.PACKET_FORWARD_CLIENT_TO_REPEATED  -> {
                    con.multicastAnalysis(packet)
                    return
                }
                else -> {
                    con.setlastSentPacket(packet)

                    // Command?
                    if (packet.type == PacketType.CHAT) {
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
                    }
                    return
                }
            }
        }

        when (packet.type) {
            // 快速抛弃
            PacketType.START_GAME,
            PacketType.CHAT,
            PacketType.TEAM_LIST -> {
            }

            PacketType.HEART_BEAT -> {
                con.getPingData(packet)
                //con.addGroup(packet)
            }

            PacketType.SYNCCHECKSUM_STATUS,
            PacketType.HEART_BEAT_RESPONSE,
            PacketType.GAMECOMMAND_RECEIVE -> {
                con.sendResultPing(packet)
            }

            else -> {
                when (packet.type) {
                    //141 -> con.receiveChat(packet)
                    PacketType.CHAT_RECEIVE -> con.receiveChat(packet)
                    PacketType.REGISTER_PLAYER -> con.relayRegisterConnection(packet)
                    PacketType.ACCEPT_START_GAME -> {
                        con.relay!!.isStartGame = true
                        con.sendResultPing(packet)
                    }
                    //PacketType.PACKET_ADD_CHAT -> con.addRelayAccept(packet)
                    PacketType.DISCONNECT -> con.disconnect()
                    PacketType.SERVER_DEBUG_RECEIVE -> con.debug(packet)
                    else ->
                        //Log.clog(packet.toString());
                        con.sendResultPing(packet)
                }
            }
        }

    }

    override val version: String
        get() = "2.1.0"
}