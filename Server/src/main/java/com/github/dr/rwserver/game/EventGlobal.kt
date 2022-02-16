/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.game

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.plugin.event.AbstractGlobalEvent
import com.github.dr.rwserver.util.Time
import com.github.dr.rwserver.util.log.Log

class EventGlobal : AbstractGlobalEvent {
    override fun registerServerLoadEvent() {
        Data.core.admin.addChatFilter { player: Player, message: String? ->
            if (player.muteTime > Time.millis()) {
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
}