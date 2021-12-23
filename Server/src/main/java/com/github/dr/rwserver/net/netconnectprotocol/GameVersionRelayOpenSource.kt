/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.netconnectprotocol

import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.data.global.Cache
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.data.global.RelayOpenSource
import com.github.dr.rwserver.io.GameInputStream
import com.github.dr.rwserver.io.GameOutputStream
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import com.github.dr.rwserver.net.game.ConnectionAgreement
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.PacketType
import com.github.dr.rwserver.util.Time.nanos
import com.github.dr.rwserver.util.alone.annotations.MainProtocolImplementation
import com.github.dr.rwserver.util.log.Log.debug
import com.github.dr.rwserver.util.log.Log.error
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.stream.IntStream

/**
 * @author Dr
 */
@MainProtocolImplementation
open class GameVersionRelayOpenSource(connectionAgreement: ConnectionAgreement?) : AbstractGameVersion(connectionAgreement!!) {
    private var cachePacket: Packet? = null

    private var relayOpenSource: RelayOpenSource? = null

    protected var site = 0

    override var name = "NOT NAME"
        protected set

    private var registerPlayerId: String? = null

    protected var uuid = UUID.randomUUID().toString()

    override fun getVersionNet(connectionAgreement: ConnectionAgreement): AbstractNetConnect {
        return GameVersionRelayOpenSource(connectionAgreement)
    }

    override fun setCache(packet: Packet) {
        cachePacket = packet
    }

    override val version: String
        get() = "RELAY Open Source"

    override fun sendRelayServerInfo() {
        val cPacket: Packet? = Cache.packetCache["sendRelayServerInfo"]
        if (IsUtil.notIsBlank(cPacket)) {
            sendPacket(cPacket!!)
            return
        }
        try {
            val o = GameOutputStream()
            o.writeByte(0)
            o.writeInt(1)
            o.writeInt(151)
            o.writeBoolean(false)
            val packetCache = o.createPacket(163)
            Cache.packetCache.put("sendRelayServerInfo",packetCache)
            sendPacket(packetCache)
        } catch (e: Exception) {
            error(e)
        }
    }

    @Throws(IOException::class)
    override fun relayDirectInspection() {
        sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.hi", Data.SERVER_CORE_VERSION))
    }

    override fun sendRelayServerCheck() {
        val cPacket: Packet? = Cache.packetCache["sendRelayServerCheck"]
        if (IsUtil.notIsBlank(cPacket)) {
            sendPacket(cPacket!!)
            return
        }
        try {
            val o = GameOutputStream()
            // 理论上是随机数？
            o.writeInt(1)
            o.writeInt(1) //可能和-AX一样
            o.writeBoolean(false)
            o.writeBoolean(false)
            o.writeString("RW-HPS")
            o.writeString("RW-HPS")
            o.writeInt(1) //随机？
            o.writeBoolean(false)

            val cachePacket = o.createPacket(151)
            Cache.packetCache.put("sendRelayServerCheck",cachePacket)

            sendPacket(cachePacket)/*->152（可做验证）*/
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun sendRelayServerType(msg: String) {
        try {
            val o = GameOutputStream()
            // 理论上是随机数？
            o.writeByte(1)
            o.writeInt(5) //可能和-AX一样
            o.writeString(msg)
            sendPacket(o.createPacket(117)) //->118
            inputPassword = true
        } catch (e: Exception) {
            error(e)
        }
    }

    @Throws(IOException::class)
    override fun receiveChat(p: Packet) {
        GameInputStream(p).use { inStream ->
            val message: String = inStream.readString()

            /*
            if (relayOpenSource!!.admin.name == pName && relayOpenSource!!.admin == this) {
                if (message.startsWith(".")) {
                    Data.RELAY_COMMAND.handleMessage(message, this)
                }
            }*/

            if (!message.contains("self_"))
            //&& relayOpenSource!!.relayData != null)
            {
                addRelayAccept(p)
            }
        }
    }

    override fun sendRelayServerTypeReply(packet: Packet) {
        try {
            GameInputStream(packet).use { inStream ->
                inStream.skip(5)
                idCustom(inStream.readString().trim { it <= ' ' })
            }
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun sendRelayServerId() {
        try {
            inputPassword = false
            if (relayOpenSource == null) {
                relayOpenSource = NetStaticData.relayOpenSource
            }
            if (relayOpenSource!!.admin != null) {
                relayOpenSource!!.removeAbstractNetConnect(site)
            }
            relayOpenSource!!.admin = this
            val o = GameOutputStream()
            o.writeByte(1)
            o.writeBoolean(true)
            o.writeBoolean(true)
            o.writeBoolean(true)
            o.writeString(Data.core.serverConnectUuid)
            o.writeBoolean(relayOpenSource!!.isMod) //MOD
            o.writeBoolean(false)
            o.writeBoolean(true)
			o.writeString("{{ ЧИСТЫЙ БАЛАНС 1 >>> z" + relayOpenSource!!.id + " }}")
            o.writeBoolean(false)
            sendPacket(o.createPacket(170))

            // Server AD [RW-HPS!]
            //sendPacket(NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket("Thank you for your use, this server is RW-HPS, you can get it in Github", "RW-HPS-AD", 5))

//            sendPacket(
//                NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket(
//                    Data.localeUtil.getinput(
//                        "relayOpenSource.server.admin.connect",
//                        relayOpenSource!!.id
//                    ), "СЕРВЕР", 10
//                )
//            )
            sendPacket(
                NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket(
                    Data.localeUtil.getinput(
                        "relayOpenSource",
                        relayOpenSource!!.id
                    ), "СЕРВЕР", -1
                )
            )
            //ping();
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun getRelayT4(msg: String) {
        try {
            val o = GameOutputStream()
            // 理论上是随机数？
            //o.writeString("RW-HPS RelayOpenSource TEST?");
            o.writeString(msg)
            o.writeByte(0)
            sendPacket(o.createPacket(140))
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun getPingData(packet: Packet) {
        try {
            GameInputStream(packet).use { inStream ->
                val out = GameOutputStream()
                //out.writeBytes(inStream.readNBytes(8))
                out.transferToFixedLength(inStream,8)
                out.writeByte(1)
                out.writeByte(60)
                sendPacket(out.createPacket(109))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun addGroup(packet: Packet) {
        relayOpenSource!!.groupNet.broadcast(packet, null)
    }

    override fun addGroupPing(packet: Packet) {
        try {
            relayOpenSource!!.groupNet.broadcast(packet, null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun addRelayConnect() {
        try {
            inputPassword = false
            if (relayOpenSource == null) {
                relayOpenSource = NetStaticData.relayOpenSource
            }
            relayOpenSource!!.setAddSite()
            relayOpenSource!!.setAbstractNetConnect(this)
            site = relayOpenSource!!.site
            val o = GameOutputStream()
            o.writeByte(0)
            o.writeInt(site)
            o.writeString(uuid)
            o.writeBoolean(false)
            relayOpenSource!!.admin.sendPacket(o.createPacket(172))
            val o1 = GameOutputStream()
            o1.writeInt(site)
            o1.writeInt(cachePacket!!.bytes.size + 8)
            o1.writeInt(cachePacket!!.bytes.size)
            o1.writeInt(160)
            o1.writeBytes(cachePacket!!.bytes)
            relayOpenSource!!.admin.sendPacket(o1.createPacket(174))
            connectionAgreement.add(relayOpenSource!!.groupNet)
            sendPacket(
                NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket(
                    Data.localeUtil.getinput(
                        "relayOpenSource",
                        relayOpenSource!!.id
                    ), "СЕРВЕР", -1
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            //cachePacket = null;
        }
    }

    override fun relayRegisterConnection(packet: Packet) {
        try {
            GameInputStream(packet).use { stream ->
                stream.readString()
                stream.skip(12)
                stream.readString()
                stream.isReadString()
                stream.readString()
                if (IsUtil.isBlank(registerPlayerId)) {
                    registerPlayerId = stream.readString()
                }
            }
        } catch (e: Exception) {
        }
        addRelayAccept(packet)
    }

    override fun addReRelayConnect() {
        try {
            val o = GameOutputStream()
            o.writeByte(0)
            o.writeInt(site)
            o.writeString(uuid)
            o.writeBoolean(true)
            o.writeString(registerPlayerId!!)
            relayOpenSource!!.admin.sendPacket(o.createPacket(172))
            val o1 = GameOutputStream()
            o1.writeInt(site)
            o1.writeInt(cachePacket!!.bytes.size + 8)
            o1.writeInt(cachePacket!!.bytes.size)
            o1.writeInt(160)
            o1.writeBytes(cachePacket!!.bytes)
            relayOpenSource!!.admin.sendPacket(o1.createPacket(174))
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            //cachePacket = null;
        }
    }

    override fun addRelaySend(packet: Packet) {
        try {
            GameInputStream(packet).use { inStream ->
                val target = inStream.readInt()
                val type = inStream.readInt()
                if (IntStream.of(
                        PacketType.PACKET_DISCONNECT,
                        PacketType.PACKET_HEART_BEAT
                    ).anyMatch { i: Int -> i == type }) {
                    return
                }
                inStream.skip(4)
                val bytes = inStream.readAllBytes()
                val abstractNetConnect = relayOpenSource!!.getAbstractNetConnect(target)
                if (PacketType.PACKET_KICK == type) {
                    val gameOutputStream = GameOutputStream()
                    // Shield Tencent group number
                    gameOutputStream.writeString(GameInputStream(bytes).readString().replace("[0-9]".toRegex(), ""))
                    abstractNetConnect!!.sendPacket(gameOutputStream.createPacket(type))
                    relayPlayerDisconnect()
                    return
                }
                abstractNetConnect?.sendPacket(Packet(type, bytes))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (nullPointerException: NullPointerException) {
        }
    }

    override fun addRelayAccept(packet: Packet) {
        try {
            val o = GameOutputStream()
            o.writeInt(site)
            o.writeInt(packet.bytes.size + 8)
            o.writeInt(packet.bytes.size)
            o.writeInt(packet.type)
            o.writeBytes(packet.bytes)
            relayOpenSource!!.admin.sendPacket(o.createPacket(174))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun addRelayAccept1(packet: Packet) {
        try {
            sendPacket(packet)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun relayPlayerDisconnect() {
        try {
            val out = GameOutputStream()
            out.writeByte(0)
            out.writeInt(site)
            relayOpenSource!!.admin.sendPacket(out.createPacket(173))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun disconnect() {
        if (super.isDis) {
            return
        }
        super.isDis = true
        if (relayOpenSource != null) {
            relayOpenSource!!.setRemoveSize()
            if (this !== relayOpenSource!!.admin) {
                relayOpenSource!!.removeAbstractNetConnect(site)
                try {
                    relayOpenSource!!.updateMinSize()
                    addRelayAccept(NetStaticData.protocolData.abstractNetPacket.getExitPacket())
                } catch (e: IOException) {
                    error("[Relay disconnect] Send Exited", e)
                }
            } else {
                //relay.groupNet.disconnect();
                    if (relayOpenSource!!.size > 0) {
                        // Move Room Admin
                        adminMoveNew()
                } else {
                    // Close Room
                    relayOpenSource!!.groupNet.disconnect()
                }
            }
            // Log.clog(String.valueOf(relay.getSize()));
            if (relayOpenSource!!.size <= 0 && !relayOpenSource!!.closeRoom) {
                if (!Data.config.SingleUserRelay) {
                    relayOpenSource!!.closeRoom = true
                }
                debug("[Relay] Gameover")
                relayOpenSource!!.re()
            }
            super.close(relayOpenSource!!.groupNet)
        } else {
            super.close(null)
        }
    }

    private fun idCustom(inId: String) {
        var id = inId
        if (id.isEmpty()) {
            sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.server.no", "-0"))
            return
        }
        if ("Z".equals(id[0].toString(), ignoreCase = true)) {
            id = id.substring(1)
		} else if ("X".equals(id[0].toString(), ignoreCase = true)) {
            id = id.substring(1)
            if (id.length > 5 || id.length < 3) {
                sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.id.re"))
                return
            }
            if ("M".equals(id[0].toString(), ignoreCase = true)) {
                id = id.substring(1)
                if (RelayOpenSource.getRelay(id) == null) {
                    newRelayId(id, true)
                } else {
                    sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.id.re"))
                }
            } else {
                if (id.length > 4) {
                    sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.id.re"))
                    return
                }
                if (RelayOpenSource.getRelay(id) == null) {
                    newRelayId(id, false)
                } else {
                    sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.id.re"))
                }
            }
            return
        }
        if (IsUtil.notIsBlank(id)) {
            if ("new".equals(id, ignoreCase = true) || "новая".equals(id, ignoreCase = true)) {
                newRelayId(false)
            } else if ("mod".equals(id, ignoreCase = true) || "mods".equals(id, ignoreCase = true) || "мод".equals(id, ignoreCase = true) || "моды".equals(id, ignoreCase = true)) {
                newRelayId(true)
            } else {
                try {
                    relayOpenSource = RelayOpenSource.getRelay(id)
                    if (relayOpenSource != null) {
                        addRelayConnect()
                        relayOpenSource!!.setAddSize()
                    } else {
                        sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.server.no", id))
                    }
                } catch (e: Exception) {
                    sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.server.no", id))
                }
            }
        } else {
            sendRelayServerType(Data.localeUtil.getinput("relayOpenSource.server.no", id))
        }
    }

    private fun adminMoveNew() {
        relayOpenSource!!.updateMinSize()
        relayOpenSource!!.getAbstractNetConnect(relayOpenSource!!.minSize).sendRelayServerId()
        relayOpenSource!!.abstractNetConnectIntMap.values()
            .forEach(Consumer { obj: AbstractNetConnect -> obj.addReRelayConnect() })
    }

    private fun newRelayId(mod: Boolean) {
        newRelayId(null, mod)
    }

    private fun newRelayId(id: String?, mod: Boolean) {
        relayOpenSource = if (IsUtil.isBlank(id)) {
            RelayOpenSource(nanos())
        } else {
            RelayOpenSource(nanos(), id)
        }
        relayOpenSource!!.isMod = mod
        sendRelayServerId()
        relayOpenSource!!.setAddSize()
    }

    override val player: Player
        get() = Player(null,"","",Data.localeUtil)
}
