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
import cn.rwhps.server.net.core.server.AbstractNetConnect
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionServerJump
import cn.rwhps.server.util.ExtractUtil
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.ReflectionUtils
import cn.rwhps.server.util.Time.concurrentSecond
import cn.rwhps.server.util.log.Log

open class TypeRwHpsJump : TypeConnect {
    val con: GameVersionServerJump
    var conClass: Class<out GameVersionServerJump>? = null

    override val abstractNetConnect: AbstractNetConnect
        get() = con

    constructor(con: GameVersionServerJump) {
        this.con = con
    }
    constructor(con: Class<out GameVersionServerJump>) {
        // will not be used ; just override the initial value to avoid refusing to compile
        this.con = ReflectionUtils.accessibleConstructor(con, ConnectionAgreement::class.java).newInstance(ConnectionAgreement())

        // use for instantiation
        conClass = con
    }

    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
        return TypeRwHpsJump(ReflectionUtils.accessibleConstructor(conClass!!, ConnectionAgreement::class.java).newInstance(connectionAgreement))
    }

    @Throws(Exception::class)
    override fun typeConnect(packet: Packet) {
        con.lastReceivedTime()

        //Log.debug(packet.type,ExtractUtil.bytesToHex(packet.bytes))
        if (packet.type == PacketType.GAMECOMMAND_RECEIVE) {
            con.receiveCommand(packet)
            con.player.lastMoveTime = concurrentSecond()
        } else {
            when (packet.type) {
                PacketType.PREREGISTER_INFO_RECEIVE -> {
                    con.sendRelayServerInfo()
                    con.registerConnection(packet)
                }
                PacketType.REGISTER_PLAYER -> if (!con.getPlayerInfo(packet)) {
                    con.disconnect()
                }
                PacketType.HEART_BEAT_RESPONSE -> {
                    val player = con.player
                    player.ping = (System.currentTimeMillis() - player.timeTemp).toInt() shr 1
                }
                PacketType.CHAT_RECEIVE -> con.receiveChat(packet)
                PacketType.DISCONNECT -> con.disconnect()
                PacketType.ACCEPT_START_GAME -> con.player.start = true
                PacketType.SERVER_DEBUG_RECEIVE -> con.debug(packet)
                // 竞争 谁先到就用谁
                PacketType.SYNC -> if (Data.game.gameSaveCache == null) {
                    val gameSavePacket = GameSavePacket(packet)
                    //gameSavePacket.analyze()
                    //Core.exit()

                    if (gameSavePacket.checkTick()) {
                        Data.game.gameSaveCache = gameSavePacket
                        synchronized(Data.game.gameSaveWaitObject) {
                            Data.game.gameSaveWaitObject.notifyAll()
                        }
                    }
                }

                PacketType.RELAY_118_117_RETURN -> con.sendRelayServerTypeReply(packet)

                PacketType.EMPTYP_ACKAGE -> {
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