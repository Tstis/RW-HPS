<<<<<<< HEAD
/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.data.global

import com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR
import com.github.dr.rwserver.math.Rand
import com.github.dr.rwserver.net.GroupNet
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionRelay
import com.github.dr.rwserver.struct.IntMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil.isNumeric
import com.github.dr.rwserver.util.log.Log.debug
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class Relay {
    /**  */
    @JvmField
    val groupNet: GroupNet
    val abstractNetConnectIntMap = IntMap<GameVersionRelay>()
    val serverUuid = UUID.randomUUID().toString()

    //private final String serverUuid = Data.core.serverConnectUuid;
    var admin: GameVersionRelay? = null
    var closeRoom = false
    val id: String
    var isMod = false
    var minSize = 1
        private set
    var isStartGame: Boolean = false
        set(value) {
            if (value) {
                return
            }
            field = value
        }

    private val site = AtomicInteger(0)
    private val size = AtomicInteger()

    constructor(a: Long) {
        val stringId: String
        while (true) {
            val intId = rand.random(1000, 100000)
            if (!serverRelayData.containsKey(intId)) {
                serverRelayData.put(intId, this)
                stringId = intId.toString()
                debug(intId)
                break
            }
        }
        id = stringId
        groupNet = GroupNet()
    }

    constructor(a: Long, id: String) {
        serverRelayData.put(id.toInt(), this)
        this.id = id
        groupNet = GroupNet()
    }

    constructor(i: String?, up: Boolean, playerName: Array<String>, isMod: Boolean, betaGameVersion: Boolean) {
        val stringId: String
        while (true) {
            val intId = rand.random(1000, 100000)
            if (!serverRelayData.containsKey(intId)) {
                serverRelayData.put(intId, this)
                stringId = intId.toString()
                debug(intId)
                break
            }
        }
        id = stringId
        groupNet = GroupNet()
        this.isMod = isMod
    }

    constructor(a: Long, id: String, up: Boolean, playerName: Array<String>, isMod: Boolean, betaGameVersion: Boolean) {
        serverRelayData.put(id.toInt(), this)
        this.id = id
        groupNet = GroupNet()
        this.isMod = isMod
    }

    constructor(id: String) {
        this.id = id
        groupNet = NetStaticData.groupNet
    }

    val allIP: String
        get() {
            val str = StringBuilder(10)
            str.append(LINE_SEPARATOR)
                .append(admin!!.name)
                .append(" / ")
                .append("IP: ").append(admin!!.ip)
                .append(" / ")
                .append("Protocol: ").append(admin!!.useConnectionAgreement)
                .append(" / ")
                .append("Admin: true")
            abstractNetConnectIntMap.values().forEach(Consumer { e: GameVersionRelay ->
                str.append(LINE_SEPARATOR)
                    .append(e.name)
                    .append(" / ")
                    .append("IP: ").append(e.ip)
                    .append(" / ")
                    .append("Protocol: ").append(e.useConnectionAgreement)
                    .append(" / ")
                    .append("Admin: false")
            })
            return str.toString()
        }

    fun re() {
        abstractNetConnectIntMap.clear()
        site.set(0)
        admin = null
        isStartGame = false
        if (isNumeric(id)) {
            serverRelayData.remove(id.toInt())
        }
    }

    fun getAbstractNetConnect(site: Int): GameVersionRelay? {
        return abstractNetConnectIntMap[site]
    }

    fun setAbstractNetConnect(abstractNetConnect: GameVersionRelay): GameVersionRelay? {
        return abstractNetConnectIntMap.put(site.get(), abstractNetConnect)
    }

    fun sendMsg(msg: String?) {
        try {
            admin!!.sendPacket(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(msg!!))
            groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(msg), null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun removeAbstractNetConnect(site: Int) {
        abstractNetConnectIntMap.remove(site)
    }

    fun getActiveConnectionSize(): Int {
        return abstractNetConnectIntMap.size
    }

    fun getSite(): Int {
        return site.get()
    }

    fun setAddSite() {
        site.incrementAndGet()
    }

    fun setRemoveSite() {
        site.decrementAndGet()
    }

    fun getSize(): Int {
        return size.get()
    }

    fun setAddSize() {
        size.incrementAndGet()
    }

    fun setRemoveSize() {
        size.decrementAndGet()
    }

    fun getRandAdmin(): GameVersionRelay? {
        return abstractNetConnectIntMap.values().toArray().random()
    }

    fun updateMinSize() {
        try {
            minSize = Arrays.stream(abstractNetConnectIntMap.keys().toArray().toArray()).min().asInt
        } catch (_: Exception) {
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val relay = o as Relay
        return id == relay.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    companion object {
        val serverRelayIpData = Seq<String>()

        private val serverRelayData = IntMap<Relay>()
        private val rand = Rand()

        @JvmStatic
        val relayAllIP: String
            get() {
                val str = StringBuilder(10)
                serverRelayData.values().forEach(Consumer { e: Relay ->
                    str.append(LINE_SEPARATOR)
                        .append(e.id)
                        .append(e.allIP)
                })
                return str.toString()
            }

        val allSize: Int
            get() {
                val size = AtomicInteger()
                serverRelayData.values().forEach(Consumer { e: Relay -> size.getAndAdd(e.getSize()) })
                return size.get()
            }

        @JvmStatic
        fun getRelay(id: String): Relay? {
            return serverRelayData[id.toInt()]
        }

        @JvmStatic
        fun sendAllMsg(msg: String?) {
            serverRelayData.values().forEach(Consumer { e: Relay -> e.sendMsg(msg) })
        }
    }
=======
/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.data.global

import com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR
import com.github.dr.rwserver.math.Rand
import com.github.dr.rwserver.net.GroupNet
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionRelay
import com.github.dr.rwserver.struct.IntMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil.isNumeric
import com.github.dr.rwserver.util.log.Log.debug
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class Relay {
    /**  */
    @JvmField
    val groupNet: GroupNet
    val abstractNetConnectIntMap = IntMap<GameVersionRelay>()
    val serverUuid = UUID.randomUUID().toString()

    //private final String serverUuid = Data.core.serverConnectUuid;
    var admin: GameVersionRelay? = null
    var closeRoom = false
    val id: String
    var isMod = false
    var minSize = 1
        private set
    var isStartGame: Boolean = false
        set(value) {
            if (value) {
                return
            }
            field = value
        }

    private val site = AtomicInteger(0)
    private val size = AtomicInteger()

    constructor(a: Long) {
        val stringId: String
        while (true) {
            val intId = rand.random(1000, 100000)
            if (!serverRelayData.containsKey(intId)) {
                serverRelayData.put(intId, this)
                stringId = intId.toString()
                debug(intId)
                break
            }
        }
        id = stringId
        groupNet = GroupNet()
    }

    constructor(a: Long, id: String) {
        serverRelayData.put(id.toInt(), this)
        this.id = id
        groupNet = GroupNet()
    }

    constructor(i: String?, up: Boolean, playerName: Array<String>, isMod: Boolean, betaGameVersion: Boolean) {
        val stringId: String
        while (true) {
            val intId = rand.random(1000, 100000)
            if (!serverRelayData.containsKey(intId)) {
                serverRelayData.put(intId, this)
                stringId = intId.toString()
                debug(intId)
                break
            }
        }
        id = stringId
        groupNet = GroupNet()
        this.isMod = isMod
    }

    constructor(a: Long, id: String, up: Boolean, playerName: Array<String>, isMod: Boolean, betaGameVersion: Boolean) {
        serverRelayData.put(id.toInt(), this)
        this.id = id
        groupNet = GroupNet()
        this.isMod = isMod
    }

    constructor(id: String) {
        this.id = id
        groupNet = NetStaticData.groupNet
    }

    val allIP: String
        get() {
            val str = StringBuilder(10)
            str.append(LINE_SEPARATOR)
                .append(admin!!.name)
                .append(" / ")
                .append("IP: ").append(admin!!.ip)
                .append(" / ")
                .append("Protocol: ").append(admin!!.useConnectionAgreement)
                .append(" / ")
                .append("Admin: true")
            abstractNetConnectIntMap.values().forEach(Consumer { e: GameVersionRelay ->
                str.append(LINE_SEPARATOR)
                    .append(e.name)
                    .append(" / ")
                    .append("IP: ").append(e.ip)
                    .append(" / ")
                    .append("Protocol: ").append(e.useConnectionAgreement)
                    .append(" / ")
                    .append("Admin: false")
            })
            return str.toString()
        }

    fun re() {
        abstractNetConnectIntMap.clear()
        site.set(0)
        admin = null
        isStartGame = false
        if (isNumeric(id)) {
            serverRelayData.remove(id.toInt())
        }
    }

    fun getAbstractNetConnect(site: Int): GameVersionRelay? {
        return abstractNetConnectIntMap[site]
    }

    fun setAbstractNetConnect(abstractNetConnect: GameVersionRelay): GameVersionRelay? {
        return abstractNetConnectIntMap.put(site.get(), abstractNetConnect)
    }

    fun sendMsg(msg: String?) {
        try {
            admin!!.sendPacket(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(msg!!))
            groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(msg), null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun removeAbstractNetConnect(site: Int) {
        abstractNetConnectIntMap.remove(site)
    }

    fun getActiveConnectionSize(): Int {
        return abstractNetConnectIntMap.size
    }

    fun getSite(): Int {
        return site.get()
    }

    fun setAddSite() {
        site.incrementAndGet()
    }

    fun setRemoveSite() {
        site.decrementAndGet()
    }

    fun getSize(): Int {
        return size.get()
    }

    fun setAddSize() {
        size.incrementAndGet()
    }

    fun setRemoveSize() {
        size.decrementAndGet()
    }

    fun getRandAdmin(): GameVersionRelay? {
        return abstractNetConnectIntMap.values().toArray().random()
    }

    fun updateMinSize() {
        try {
            minSize = Arrays.stream(abstractNetConnectIntMap.keys().toArray().toArray()).min().asInt
        } catch (_: Exception) {
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val relay = o as Relay
        return id == relay.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    companion object {
        val serverRelayIpData = Seq<String>()

        private val serverRelayData = IntMap<Relay>()
        private val rand = Rand()

        @JvmStatic
        val relayAllIP: String
            get() {
                val str = StringBuilder(10)
                serverRelayData.values().forEach(Consumer { e: Relay ->
                    str.append(LINE_SEPARATOR)
                        .append(e.id)
                        .append(e.allIP)
                })
                return str.toString()
            }

        val allSize: Int
            get() {
                val size = AtomicInteger()
                serverRelayData.values().forEach(Consumer { e: Relay -> size.getAndAdd(e.getSize()) })
                return size.get()
            }

        @JvmStatic
        fun getRelay(id: String): Relay? {
            return serverRelayData[id.toInt()]
        }

        @JvmStatic
        fun sendAllMsg(msg: String?) {
            serverRelayData.values().forEach(Consumer { e: Relay -> e.sendMsg(msg) })
        }
    }
>>>>>>> ab468a8efac1b6b73535dee9461c3323bd13d144
}