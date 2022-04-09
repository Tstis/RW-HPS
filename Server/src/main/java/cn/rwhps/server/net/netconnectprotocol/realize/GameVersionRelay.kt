/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol.realize

import cn.rwhps.server.data.global.Cache
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.data.global.Relay
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.server.AbstractNetConnect
import cn.rwhps.server.net.core.server.AbstractNetConnectRelay
import cn.rwhps.server.net.netconnectprotocol.internal.relay.relayServerTypeInternal
import cn.rwhps.server.net.netconnectprotocol.internal.relay.relayServerTypeReplyInternal
import cn.rwhps.server.util.IsUtil
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.RandomUtil.getRandomIetterString
import cn.rwhps.server.util.StringFilteringUtil.cutting
import cn.rwhps.server.util.alone.annotations.MainProtocolImplementation
import cn.rwhps.server.util.encryption.Sha
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.Log.debug
import cn.rwhps.server.util.log.Log.error
import java.io.IOException
import java.math.BigInteger
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer
import java.util.stream.IntStream

/**
 * Many thanks to them for providing cloud computing for the project
 * This is essential to complete the RW-HPS Relay test
 * @Thanks : [SimpFun Cloud](https://vps.tiexiu.xyz)
 * @Thanks : [Github 1dNDN](https://github.com/1dNDN)
 *
 * This test was done on :
 * Relay-CN (V. 3.2.0)
 * 2021.11.05 16:30
 */

/**
 * Relay protocol implementation
 * Direct forwarding consumes more bandwidth and the same effect as using VPN forwarding
 *
 * @property relay Relay example
 * @property relayPlayerQQ NO USE
 * @property site RELAY's internal location
 * @property connectUUID UUID of this connection
 * @property cachePacket Cached Package
 * @property betaGameVersion Is it a beta version
 * @property netConnectAuthenticate Connection validity verification
 * @property name player's name
 * @property registerPlayerId UUID-Hash code after player registration
 * @property version Protocol version
 * @constructor
 *
 * @author Dr
 */
@MainProtocolImplementation
open class GameVersionRelay(connectionAgreement: ConnectionAgreement) : AbstractNetConnect(connectionAgreement), AbstractNetConnectRelay {
    override var relay: Relay? = null
        protected set

    override var relayPlayerQQ: String? = ""
        protected set

    protected var site = 0

    private val connectUUID = UUID.randomUUID().toString()

    private var cachePacket: Packet? = null
    private var betaGameVersion = false

    private var netConnectAuthenticate: NetConnectAuthenticate? = null

    var name = "NOT NAME"
        protected set

    private var registerPlayerId: String? = null

    private var relaySelect: ((String) -> Unit)? = null

    override fun setCachePacket(packet: Packet) {
        cachePacket = packet
    }

    override fun setlastSentPacket(packet: Packet) {
    }


    override val version: String
        get() = "RELAY"

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
        GameInputStream(cachePacket!!).use { inStream ->
            inStream.readString()
            val packetVersion = inStream.readInt()
            if (inStream.readInt() >= 157) {
                betaGameVersion = true
            }
            if (packetVersion >= 1) {
                inStream.skip(4)
            }
            var queryString: String? = null
            if (packetVersion >= 2) {
                queryString = inStream.isReadString()
            }
            if (packetVersion >= 3) {
                name = inStream.readString()
                //info(name)
                //this.playerAdminName = StringFilteringUtil.replaceChinese(this.playerAdminName,"?");
            }
            if (IsUtil.isBlank(queryString) || "RELAYCN".equals(queryString, ignoreCase = true)) {
                sendRelayServerType(Data.i18NBundle.getinput("relay.hi", Data.SERVER_CORE_VERSION))
            } else {
                idCustom(queryString!!.substring(1))
            }
        }
    }

    @Throws(IOException::class)
    override fun relayDirectInspection(relay: Relay) {
        try {
            GameInputStream(cachePacket!!).use { inStream ->
                inStream.readString()
                val packetVersion = inStream.readInt()
                if (inStream.readInt() >= 157) {
                    betaGameVersion = true
                }
                if (packetVersion >= 1) {
                    inStream.skip(4)
                }
                if (packetVersion >= 2) {
                    inStream.isReadString()
                }
                if (packetVersion >= 3) {
                    name = inStream.readString()
                    //this.playerAdminName = StringFilteringUtil.replaceChinese(this.playerAdminName,"?");
                }
                this.relay = relay
                if (this.relay != null) {
                    addRelayConnect()
                    this.relay!!.setAddSize()
                } else {
                    //sendRelayServerType(Data.localeUtil.getinput("relay.server.no",queryString));
                }
            }
        } catch (e: Exception) {
            //sendRelayServerType(Data.localeUtil.getinput("relay.server.no",queryString));
        }
    }

    override fun sendRelayServerCheck() {
        netConnectAuthenticate = NetConnectAuthenticate()
        val netConnectAuthenticate: NetConnectAuthenticate = netConnectAuthenticate!!
        try {
            val o = GameOutputStream()
            // 返回相同
            o.writeInt(netConnectAuthenticate.resultInt)
            o.writeInt(netConnectAuthenticate.authenticateType)
            o.writeBoolean(false)
            o.writeBoolean(false)
            o.writeString(netConnectAuthenticate.outcome)
            o.writeString(netConnectAuthenticate.fixedInitial)
            o.writeInt(netConnectAuthenticate.maximumNumberOfCalculations) //随机？
            o.writeBoolean(false)

            sendPacket(o.createPacket(151))
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun receiveRelayServerCheck(packet: Packet): Boolean {
        try {
            GameInputStream(packet).use { inStream ->
                if (netConnectAuthenticate != null) {
                    if (netConnectAuthenticate!!.check(inStream.readInt(),inStream.readInt(),inStream.readString().toInt())) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            error(e)
        }
        return false
    }

    override fun sendRelayServerType(msg: String, run: ((String) -> Unit)?) {
        try {
            sendPacket(relayServerTypeInternal(msg))

            relaySelect = run

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
            if (relay!!.admin.name == pName && relay!!.admin == this) {
                if (message.startsWith(".")) {
                    Data.RELAY_COMMAND.handleMessage(message, this)
                }
            }*/

            if (!message.contains("self_"))
                //&& relay!!.relayData != null)
                {
                sendResultPing(p)
            }
        }
    }

    override fun sendRelayServerTypeReply(packet: Packet) {
        try {
            inputPassword = false

            val id = relayServerTypeReplyInternal(packet)
            if (relaySelect == null) {
                idCustom(id)
            } else {
                relaySelect!!(id)
            }
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun sendRelayServerId() {
        try {
            inputPassword = false
            if (relay == null) {
                //Log.debug("sendRelayServerId","Relay == null");
                relay = NetStaticData.relay
            }
            if (relay!!.admin != null) {
                // Log.debug("sendRelayServerId","Admin != null");
                relay!!.removeAbstractNetConnect(site)
            }
            //Log.debug("sendRelayServerId","Set Admin");
            relay!!.admin = this
            //Log.debug(this == relay.getAdmin());
            val o = GameOutputStream()
            o.writeByte(1)
            o.writeBoolean(true)
            o.writeBoolean(true)
            o.writeBoolean(true)
            o.writeString(relay!!.serverUuid)
            o.writeBoolean(relay!!.isMod) //MOD
            // List OPEN
            o.writeBoolean(false)
            o.writeBoolean(true)
            o.writeString("{{ s.vangers.tk >>> i" + relay!!.id + " }}")
            //
            o.writeBoolean(false)
            sendPacket(o.createPacket(170)) //+108+140
            //getRelayT4(Data.localeUtil.getinput("relay.server.admin.connect",relay.getId()));
            //sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay.server.admin.connect", relay!!.id), "CEPBEP", -1))
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay", relay!!.id), "CEPBEP", -1))
            //ping();

            //debug(name)
            if (name.equals("SERVER", ignoreCase = true) || name.equals("RELAY", ignoreCase = true)) {
                relay!!.groupNet.disconnect() // Close Room
                disconnect() // Close Connect & Reset Room
            }
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun getRelayT4(msg: String) {
        try {
            val o = GameOutputStream()
            // 理论上是随机数？
            //o.writeString("RW-HPS Relay TEST?");
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
        relay!!.groupNet.broadcast(packet, null)
    }

    override fun addGroupPing(packet: Packet) {
        try {
            relay!!.groupNet.broadcast(packet, null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun addRelayConnect() {
        try {
            inputPassword = false
            if (relay == null) {
                relay = NetStaticData.relay
            }
            relay!!.setAddSite()
            relay!!.setAbstractNetConnect(this)
            site = relay!!.getSite()
            val o = GameOutputStream()
            o.writeByte(0)
            o.writeInt(site)
            //o.writeBoolean(true);
            o.writeString(connectUUID)
            o.writeBoolean(false)
            //o.writeIsString(Cache.relayAdminCache.getCache(name+ip))
            relay!!.admin!!.sendPacket(o.createPacket(172))
            val o1 = GameOutputStream()
            o1.writeInt(site)
            o1.writeInt(cachePacket!!.bytes.size + 8)
            o1.writeInt(cachePacket!!.bytes.size)
            o1.writeInt(160)
            o1.writeBytes(cachePacket!!.bytes)
            relay!!.admin!!.sendPacket(o1.createPacket(174))
            connectionAgreement.add(relay!!.groupNet)
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay", relay!!.id), "CEPBEP", -1))
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
        } catch (_: Exception) {
        }
        sendResultPing(packet)
    }

    override fun addReRelayConnect() {
        try {
            val o = GameOutputStream()
            o.writeByte(0)
            o.writeInt(site)
            //o.writeBoolean(true);
            o.writeString(connectUUID)
            o.writeBoolean(true)
            o.writeString(registerPlayerId!!)
            relay!!.admin!!.sendPacket(o.createPacket(172))
            val o1 = GameOutputStream()
            o1.writeInt(site)
            o1.writeInt(cachePacket!!.bytes.size + 8)
            o1.writeInt(cachePacket!!.bytes.size)
            o1.writeInt(160)
            o1.writeBytes(cachePacket!!.bytes)
            relay!!.admin!!.sendPacket(o1.createPacket(174))
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
                val abstractNetConnect = relay!!.getAbstractNetConnect(target)
                if (PacketType.PACKET_KICK == type) {
                    val gameOutputStream = GameOutputStream()
                    gameOutputStream.writeString(GameInputStream(bytes).readString().replace("[0-9]".toRegex(), ""))
                    abstractNetConnect?.sendPacket(gameOutputStream.createPacket(type))
                    relayPlayerDisconnect()
                    return
                }
                abstractNetConnect?.sendPacket(Packet(type, bytes))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (_: NullPointerException) {
        }
    }

    override fun sendResultPing(packet: Packet) {
        try {
            val o = GameOutputStream()
            o.writeInt(site)
            o.writeInt(packet.bytes.size + 8)
            o.writeInt(packet.bytes.size)
            o.writeInt(packet.type)
            o.writeBytes(packet.bytes)
            relay!!.admin!!.sendPacket(o.createPacket(174))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun sendCustomPacket(packet: Packet) {
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
            relay!!.admin!!.sendPacket(out.createPacket(173))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun multicastAnalysis(packet: Packet) {
        // Protocol not supported
    }

    override fun disconnect() {
        if (super.isDis) {
            return
        }
        super.isDis = true
        if (relay != null) {
            relay!!.setRemoveSize()
            if (this !== relay!!.admin) {
                relay!!.removeAbstractNetConnect(site)
                try {
                    relay!!.updateMinSize()
                    sendResultPing(NetStaticData.RwHps.abstractNetPacket.getExitPacket())
                } catch (e: IOException) {
                    error("[Relay disconnect] Send Exited", e)
                }
            } else {
                Relay.serverRelayIpData.remove(ip)
                //relay.groupNet.disconnect();
                if (relay!!.isStartGame) {
                    if (relay!!.getSize() > 0) {
                        // Move Room Admin
                        adminMoveNew()
                        //Cache.relayAdminCache.addCache(name+ip,BigInteger(1, sha256Array(uuid+Data.core.serverConnectUuid)).toString(16).uppercase(Locale.ROOT))
                    }
                } else {
                    // Close Room
                    relay!!.groupNet.disconnect()
                }
            }
            // Log.clog(String.valueOf(relay.getSize()));
            if (relay!!.getSize() <= 0 && !relay!!.closeRoom) {
                relay!!.closeRoom = true
                debug("[Relay] Gameover")
                relay!!.re()
            }
            super.close(relay!!.groupNet)
        } else {
            super.close(null)
        }
    }

    private fun idCustom(inId: String) {
        var id = inId
        if (id.isEmpty()) {
            sendRelayServerType(Data.i18NBundle.getinput("relay.server.no", "空"))
            return
        }

        if ("i".equals(id[0].toString(), ignoreCase = true) || "і".equals(id[0].toString(), ignoreCase = true)) {
            id = id.substring(1)
        } else if ("C".equals(id[0].toString(), ignoreCase = true)) {
            id = id.substring(1)
            val customID = splitNumbers(id)
            if (customID.length > 6 || customID.length < 4) {
                sendRelayServerType(Data.i18NBundle.getinput("relay.id.re"))
                return
            }
            if ("M".equals(id[0].toString(), ignoreCase = true)) {
                if (Relay.getRelay(customID) == null) {
                    newRelayId(customID, true)
                } else {
                    sendRelayServerType(Data.i18NBundle.getinput("relay.id.re"))
                }
            } else {
                if (Relay.getRelay(customID) == null) {
                    newRelayId(customID, false)
                } else {
                    sendRelayServerType(Data.i18NBundle.getinput("relay.id.re"))
                }
            }
            return
        }

        if (IsUtil.notIsBlank(id)) {
            if ("new".equals(id, ignoreCase = true)) {
            if ("new".equals(id, ignoreCase = true) || "нова".equals(id, ignoreCase = true) || "н".equals(id, ignoreCase = true)) {
                newRelayId(false)
            } else if ("mod".equals(id, ignoreCase = true) || "mods".equals(id, ignoreCase = true)) {
            } else if ("mod".equals(id, ignoreCase = true) || "mods".equals(id, ignoreCase = true) || "мод".equals(id, ignoreCase = true) || "моди".equals(id, ignoreCase = true) || "модифікація".equals(id, ignoreCase = true) || "модифікації".equals(id, ignoreCase = true) || "м".equals(id, ignoreCase = true)) {
                newRelayId(true)
            } else {
                try {
                    relay = Relay.getRelay(id)
                    if (relay != null) {
                        addRelayConnect()
                        relay!!.setAddSize()
                    } else {
                        sendRelayServerType(Data.i18NBundle.getinput("relay.server.no", id))
                    }
                } catch (e: Exception) {
                    Log.debug(e)
                    sendRelayServerType(Data.i18NBundle.getinput("relay.server.no", id))
                }
            }
        } else {
            sendRelayServerType(Data.i18NBundle.getinput("relay.server.no", id))
        }
    }

    private fun splitNumbers(str: String): String {
        var result = ""
        var readYes = false
        str.forEach {
            if(it.code in 48..57){
                readYes = true
                result += it
            } else {
                if (readYes) {
                    return result
                }
            }
        }
        return result
    }

    private fun adminMoveNew() {
        relay!!.updateMinSize()
        relay!!.getAbstractNetConnect(relay!!.minSize)!!.sendRelayServerId()
        relay!!.abstractNetConnectIntMap.values()
            .forEach(Consumer { obj: GameVersionRelay -> obj.addReRelayConnect() })
    }


    private fun newRelayId(mod: Boolean) {
        newRelayId(null, mod)
    }

    private fun newRelayId(id: String?, mod: Boolean) {
        relay = if (IsUtil.isBlank(id)) {
            Relay.getNoUpRelay()
        } else {
            Relay.getNoUpRelay(id!!)
        }
        relay!!.isMod = mod
        sendRelayServerId()
        relay!!.setAddSize()
    }


    class NetConnectAuthenticate {
        private val rand = ThreadLocalRandom.current()

        val resultInt = rand.nextInt()
        //val authenticateType = rand.nextInt(0,7)
        /* 并不是很想让假人简便的破解 */
        val authenticateType = 5

        val initInt_1 = 0
        val initInt_2 = 0

        val outcome: String
        val fixedInitial: String = getRandomIetterString(4)
        val off: Int = rand.nextInt(0, 10)
        val maximumNumberOfCalculations: Int = rand.nextInt(0, 10000000)

        init {
            outcome = cutting(BigInteger(1, Sha.sha256Array(fixedInitial + "" + off)).toString(16).uppercase(), 14)
        }

        fun check(resultInt: Int,authenticateType: Int,off: Int): Boolean {
            if (this.resultInt != resultInt || this.authenticateType != authenticateType || this.off != off) {
                return false
            }
            return true
        }
    }
}