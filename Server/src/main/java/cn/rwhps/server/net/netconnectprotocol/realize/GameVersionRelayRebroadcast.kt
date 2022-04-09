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
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.StringFilteringUtil
import cn.rwhps.server.util.log.Log.error
import java.io.IOException
import java.util.*
import java.util.stream.IntStream

/**
 * Many thanks to them for providing cloud computing for the project
 * This is essential to complete the RW-HPS Relay test
 * @Thanks : [SimpFun Cloud](https://vps.tiexiu.xyz)
 * @Thanks : [Github 1dNDN](https://github.com/1dNDN)
 *
 * This test was done on :
 * Relay-CN (V. 5.1.0)
 * 2022.1.26 20:00
 */

/**
 * Relay protocol implementation
 * Can realize multicast and save bandwidth
 *
 * @property lastSentPacket Last packet sent
 * @property version Protocol version
 * @constructor
 *
 * @author Dr
 */
class GameVersionRelayRebroadcast(connectionAgreement: ConnectionAgreement) : GameVersionRelay(connectionAgreement) {
    private lateinit var lastSentPacket: Packet

    override val version: String
        get() = "RELAY Rebroadcast"

    override fun setlastSentPacket(packet: Packet) {
        lastSentPacket = packet
    }

    override fun sendRelayServerId() {
        try {
            inputPassword = false
            if (relay == null) {
                relay = NetStaticData.relay
            }
            if (relay!!.admin != null) {
                relay!!.removeAbstractNetConnect(site)
            }
            relay!!.admin = this
            val o = GameOutputStream()
            o.writeByte(1)
            o.writeBoolean(true)
            o.writeBoolean(true)
            o.writeBoolean(true)
            o.writeString(Data.core.serverConnectUuid)
            o.writeBoolean(relay!!.isMod) //MOD
            // List OPEN
            o.writeBoolean(false)
            o.writeBoolean(true)
            o.writeString("{{RW-HPS }}.Room ID : " + relay!!.id)
            // Multicast
            o.writeBoolean(true)
            sendPacket(o.createPacket(170)) //+108+140
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay.server.admin.connect", relay!!.id), "ADMIN", 5))
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay", relay!!.id), "ADMIN", 5))
            //ping();

            val nnn = StringFilteringUtil.filterChines(name)

            // 人即像树，树枝越向往光明的天空，树根越伸向阴暗的地底
            /**
             * 禁止玩家使用 Server/Relay 做玩家名
             * 禁止玩家使用带 eess 做玩家名 (色情网站)
             */
            if (nnn.lowercase(Locale.getDefault()).contains("server") || nnn.lowercase(Locale.getDefault()).contains("relay") || nnn.lowercase(Locale.getDefault()).contains("eess")) {
                relay!!.groupNet.disconnect() // Close Room
                disconnect() // Close Connect & Reset Room
            }
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun addRelaySend(packet: Packet) {
        try {
            GameInputStream(packet).use { inStream ->
                val target = inStream.readInt()
                val type = inStream.readInt()
                //Log.clog(target+"   "+type);

                if (IntStream.of(PacketType.PACKET_DISCONNECT, PacketType.PACKET_HEART_BEAT).anyMatch { i: Int -> i == type }) {
                    return
                }

                inStream.skip(4) // Lenght
                val bytes = inStream.readAllBytes()
                val abstractNetConnect = relay!!.getAbstractNetConnect(target)



                if (abstractNetConnect != null) {
                    val sendPacketData = Packet(type, bytes)
                    lastSentPacket = sendPacketData
                    abstractNetConnect.sendPacket(sendPacketData)
                }

                when (type) {
                    PacketType.PACKET_KICK -> {
                        relayPlayerDisconnect()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (_: NullPointerException) {
        }
    }

    @Throws(IOException::class)
    override fun multicastAnalysis(packet: Packet) {
        try {
            GameInputStream(packet).use { stream ->
                val target = stream.readInt()
                val abstractNetConnect = relay!!.getAbstractNetConnect(target)
                abstractNetConnect?.sendPacket(lastSentPacket)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (_: NullPointerException) {
        }
    }
}
