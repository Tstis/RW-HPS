/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.game

import com.github.dr.rwserver.core.Call
import com.github.dr.rwserver.core.NetServer
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.net.Administration.PlayerInfo
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionServer
import com.github.dr.rwserver.plugin.event.AbstractEvent
import com.github.dr.rwserver.util.Time.millis
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.log.Log.debug
import com.github.dr.rwserver.util.log.Log.error
import java.io.IOException

/**
 * @author Dr
 */
class Event : AbstractEvent {
    override fun registerServerLoadEvent() {
        Data.core.admin.addChatFilter { player: Player, message: String? ->
            if (player.muteTime > millis()) {
                return@addChatFilter null
            }
            message
        }
        Log.run {
            info("ServerConnectUuid", Data.core.serverConnectUuid)
            info("TOKEN", Data.core.serverToken)
            info("bannedIPs", Data.core.admin.bannedIPs)
            info("bannedUUIDs", Data.core.admin.bannedUUIDs)
        }
    }

    override fun registerPlayerJoinEvent(player: Player) {
        if (Data.core.admin.bannedUUIDs.contains(player.uuid)) {
            try {
                player.con!!.sendKick(player.localeUtil.getinput("kick.ban"))
            } catch (ioException: IOException) {
                error("[Player] Send Kick Player Error", ioException)
            }
            return
        }
        if (Data.core.admin.playerDataCache.containsKey(player.uuid)) {
            val info = Data.core.admin.playerDataCache[player.uuid]
            if (info.timesKicked > millis()) {
                try {
                    player.con!!.sendKick(player.localeUtil.getinput("kick.you.time"))
                } catch (ioException: IOException) {
                    error("[Player] Send Kick Player Error", ioException)
                }
            } else {
                player.muteTime = info.timeMute
            }
        }

       // ConnectServer("127.0.0.1",5124,player.con)
    }

    override fun registerPlayerConnectPasswdCheckEvent(abstractNetConnect: GameVersionServer, passwd: String): Array<String> {
        if ("" != Data.game.passwd) {
            if (passwd != Data.game.passwd) {
                try {
                    abstractNetConnect.sendErrorPasswd()
                } catch (ioException: IOException) {
                    debug("Event Passwd", ioException)
                }
                return arrayOf("true", "")
            }
        }
        return arrayOf("false", "")
    }

    override fun registerPlayerLeaveEvent(player: Player) {
        if (Data.config.OneAdmin && player.isAdmin && Data.game.playerManage.playerGroup.size() > 0) {
            try {
                val p = Data.game.playerManage.playerGroup[0]
                p.isAdmin = true
                Call.upDataGameData()
                player.isAdmin = false
                Call.sendSystemMessage("give.ok", p.name)
            } catch (ignored: IndexOutOfBoundsException) {
            }
        }
        Data.core.admin.playerDataCache.put(player.uuid, PlayerInfo(player.uuid, player.kickTime, player.muteTime))

        if (Data.game.isStartGame) {
            player.sharedControl = true
            Data.game.playerManage.updateControlIdentifier()
            Call.sendSystemMessage("player.dis", player.name)
            Call.sendTeamData()

        } else {
            Call.sendSystemMessage("player.disNoStart", player.name)
        }
    }

    override fun registerGameStartEvent() {
        Data.core.admin.playerDataCache.clear()
    }

    override fun registerGameOverEvent() {
        if (Data.game.maps.mapData != null) {
            Data.game.maps.mapData!!.clean()
        }
        NetServer.reLoadServer()
        System.gc()
    }

    override fun registerPlayerBanEvent(player: Player) {
        Data.core.admin.bannedUUIDs.add(player.uuid)
        Data.core.admin.bannedIPs.add(player.con!!.ip)
        try {
            player.con!!.sendKick(player.localeUtil.getinput("kick.ban"))
        } catch (ioException: IOException) {
            error("[Player] Send Kick Player Error", ioException)
        }
        Call.sendSystemMessage("ban.yes", player.name)
    }

    override fun registerPlayerIpBanEvent(player: Player) {
        Data.core.admin.bannedIPs.add(player.con!!.ip)
        try {
            player.con!!.sendKick("kick.ban")
        } catch (ioException: IOException) {
            error("[Player] Send Kick Player Error", ioException)
        }
        Call.sendSystemMessage("ban.yes", player.name)
    }
}