/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.plugin.beta.adserver

import com.github.dr.rwserver.core.Call
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.game.EventType
import com.github.dr.rwserver.game.GameMaps
import com.github.dr.rwserver.io.input.GameInputStream
import com.github.dr.rwserver.io.output.GameOutputStream
import com.github.dr.rwserver.io.packet.Packet
import com.github.dr.rwserver.net.core.ConnectionAgreement
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionServer
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.PacketType
import com.github.dr.rwserver.util.game.Events
import java.io.IOException

class AdSpecializationNet(connectionAgreement: ConnectionAgreement) : GameVersionServer(connectionAgreement) {
    /* 拒绝用户发送任何消息 */
    override fun receiveChat(p: Packet) {
    }

    @Throws(IOException::class)
    override fun sendServerInfo(utilData: Boolean) {
        val o = GameOutputStream()
        o.writeString(Data.SERVER_ID)
        o.writeInt(supportedVersion)
        /* 强制以地图名为AD */
        o.writeInt(GameMaps.MapType.customMap.ordinal)
        o.writeString(Data.config.Subtitle)

        o.writeInt(Data.game.credits)
        o.writeInt(Data.game.mist)
        o.writeBoolean(true)
        o.writeInt(1)
        o.writeByte(7)
        o.writeBoolean(false)
        /* 谁都不是Admin */
        o.writeBoolean(false)
        o.writeInt(0)
        o.writeInt(0)
        o.writeInt(1)
        o.writeFloat(0)
        /* NO Nukes */
        o.writeBoolean(true)
        o.writeBoolean(false)
        // 不验证单位元数据
        o.writeBoolean(false)

        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(true)
        o.writeBoolean(false)
        sendPacket(o.createPacket(PacketType.PACKET_SERVER_INFO))
    }

    override fun getPlayerInfo(p: Packet): Boolean {
        try {
            GameInputStream(p).use { stream ->
                stream.readString()
                stream.skip(12)
                var name = stream.readString()
                val passwd = stream.isReadString()
                stream.readString()
                val uuid = stream.readString()

                val playerConnectPasswdCheck = EventType.PlayerConnectPasswdCheckEvent(this, passwd)
                Events.fire(playerConnectPasswdCheck)
                if (playerConnectPasswdCheck.result) {
                    return true
                }
                if (IsUtil.notIsBlank(playerConnectPasswdCheck.name)) {
                    name = playerConnectPasswdCheck.name
                }

                Events.fire(EventType.PlayerJoinUuidandNameEvent(uuid, name))

                val playerJoinName = EventType.PlayerJoinNameEvent(name)
                Events.fire(playerJoinName)
                if (IsUtil.notIsBlank(playerJoinName.resultName)) {
                    name = playerJoinName.resultName
                }

                inputPassword = false

                if (Data.game.playerManage.playerGroup.size() >= Data.game.maxPlayer) {
                    if (IsUtil.isBlank(Data.config.MaxPlayerAd)) {
                        sendKick("服务器没有位置 # The server has no free location")
                    } else {
                        sendKick(Data.config.MaxPlayerAd)
                    }
                    return false
                }
                val localeUtil = Data.localeUtilMap["CN"]
                player = Data.game.playerManage.addPlayer(this, uuid, name, localeUtil)

                connectionAgreement.add(NetStaticData.groupNet)
                Call.sendTeamData()
                sendServerInfo(true)
                Events.fire(EventType.PlayerJoinEvent(player))
                if (IsUtil.notIsBlank(Data.config.EnterAd)) {
                    sendSystemMessage(Data.config.EnterAd)
                }
                return true
            }
        } finally {
        }
    }
}