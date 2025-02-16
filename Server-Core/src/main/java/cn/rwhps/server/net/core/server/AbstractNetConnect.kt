/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.core.server

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.GroupNet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.game.ConnectServer
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.Time
import cn.rwhps.server.util.log.Log
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Realize basic network information packaging
 * The game protocol compulsory inheritance of this class
 * @author RW-HPS/Dr
 * @date 2021/12/16 08:55:26
 */
abstract class AbstractNetConnect(protected val connectionAgreement: ConnectionAgreement) {
    /** 连接转发标识 */
    var isConnectServer: Boolean = false

    var connectServer: ConnectServer? = null

    /**
     * Get connection IP
     * @return IP
     */
    val ip: String
        get() = connectionAgreement.ip

    /**
     * Get the local port used
     * @return Port
     */
    val port: Int
        get() = connectionAgreement.localPort

    /** 尝试数 */
    var numberOfRetries = 0


    /**
     * Set up try
     * //@param status
     * Get try status
     * @return Boolean
     */
    var tryBoolean: Boolean = false

    /**
     * Get whether you are entering a password
     * @return Boolean
     */
    var inputPassword: Boolean = false

    var isDis: Boolean = false

    /**
     * last time to Received Packet
     * @return Time
     */
    var lastReceivedTime: Long = Time.concurrentMillis()
        private set

    fun lastReceivedTime() {
        lastReceivedTime = Time.concurrentMillis()
    }

    /**
     * Get connection agreement
     * @return Protocol
     */
    val useConnectionAgreement: String
        get() = connectionAgreement.useAgreement

    /**
     * Protocol version
     * @return version number
     */
    abstract val version: String

    /**
     * Disconnect
     */
    abstract fun disconnect()

    /**
     * Send package
     * @param packet Data
     */
    fun sendPacket(packet: Packet) {
        try {
            connectionAgreement.send(packet)
        } catch (e: Exception) {
            disconnect()
            if (connectionAgreement.useAgreement != "UDP") {
                Log.error("[${connectionAgreement.useAgreement}] SendError - 本消息单独出现无妨 连续多次出现请debug", e)
            }
        }
    }

    /**
     * Debug Special development not open temporarily
     * @param packet Packet
     */
    fun debug(packet: Packet) {
        try {
            GameInputStream(packet).use { stream ->
                Data.LOG_COMMAND.handleMessage(URLDecoder.decode(stream.readString(), StandardCharsets.UTF_8.toString()), this)
            }
        } catch (_: IOException) {
        }
    }

    /**
     * Debug Special development not open temporarily
     * @param str String
     */
    fun sendDebug(str: String) {
        try {
            val o = GameOutputStream()
            o.writeString(str)
            sendPacket(o.createPacket(PacketType.SERVER_DEBUG))
        } catch (_: Exception) {
        }
    }

    protected fun close(groupNet: GroupNet?) {
        try {
            connectionAgreement.close(groupNet)
        } catch (e: Exception) {
            Log.error("Close Connect", e)
        }
    }
}