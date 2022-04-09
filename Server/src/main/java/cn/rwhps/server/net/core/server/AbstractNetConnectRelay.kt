/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.core.server

import cn.rwhps.server.data.global.Relay
import cn.rwhps.server.io.packet.Packet
import java.io.IOException

/**
 * Only provide interface but not implement
 * As the interface of game CoreNet, it provides various version support for GameServer
 * @author Dr
 * @date 2021/7/31/ 14:14
 */
interface AbstractNetConnectRelay {
    /**
     * 获取Relay的协议 无则为null
     * @return Relay
     */
    val relay: Relay?

    val relayPlayerQQ: String?

    fun setCachePacket(packet: Packet)
    fun setlastSentPacket(packet: Packet)

    /**
     * 服务器握手
     */
    fun sendRelayServerInfo()

    /**
     * 检验ID直达
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun relayDirectInspection()

    fun relayRegisterConnection(packet: Packet)

    /**
     * ID直达
     * @param relay Relay
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun relayDirectInspection(relay: Relay)

    /**
     * 检测Relay的校验是否正确-发送
     */
    fun sendRelayServerCheck()

    /**
     * 检测Relay的校验是否正确-校验
     */
    fun receiveRelayServerCheck(packet: Packet): Boolean

    /**
     * Server类型
     * @param msg RelayID
     */
    fun sendRelayServerType(msg: String, run: ((String) -> Unit)? = null)

    /**
     * 类型回复
     */
    fun sendRelayServerTypeReply(packet: Packet)

    /**
     * 服务器Admin?
     */
    fun sendRelayServerId()

    /**
     * Accept language pack
     * @param p Packet
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun receiveChat(p: Packet)

    /**
     * 发送消息
     */
    fun getRelayT4(msg: String)
    //fun sendRelayPlayerInfo()
    //fun sendRelayPlayerConnectPacket(packet: Packet)
    //fun getRelayUnitData(packet: Packet)

    /**
     * 返回ping包
     * @param packet packet
     */
    fun getPingData(packet: Packet)

    /**
     * 群发包
     * @param packet Packet
     */
    fun addGroup(packet: Packet)

    /**
     * 群发ping包
     * @param packet Packet
     */
    fun addGroupPing(packet: Packet)

    /**
     * 连接Relay服务器
     */
    fun addRelayConnect()

    /**
     * 连接Relay服务器
     */
    fun addReRelayConnect()

    /**
     * 解析包
     * @param packet Packet
     */
    fun addRelaySend(packet: Packet)
    fun sendResultPing(packet: Packet)
    fun sendCustomPacket(packet: Packet)
    fun relayPlayerDisconnect()
    fun multicastAnalysis(packet: Packet)
}