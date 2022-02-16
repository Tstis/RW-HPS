/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.plugin.event

import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionServer

interface AbstractEvent {
    /**
     * 玩家加入 [同步-Synchronization]
     * @param player Player
     */
    fun registerPlayerJoinEvent(player: Player) {}

    /**
     * 玩家重连 [同步-Synchronization]
     * @param player Player
     */
    fun registerPlayerReJoinEvent(player: Player) {}

    /**
     * 玩家连接密码验证 [同步-Synchronization]
     * @param abstractNetConnect 游戏实现协议
     * @param passwd 密码SHA256的16进
     * @return String[0]=密码是否正确(Boolean) String[1]=你可以给他设置一个名字
     */
    fun registerPlayerConnectPasswdCheckEvent(abstractNetConnect: GameVersionServer, passwd: String): Array<String> {
        return arrayOf("false", "")
    }

    /**
     * 玩家连接时 [异步-ASync]
     * @param player Player
     */
    fun registerPlayerConnectEvent(player: Player) {}

    /**
     * 玩家离开时 [异步-ASync]
     * @param player Player
     */
    fun registerPlayerLeaveEvent(player: Player) {}

    /**
     * 玩家发言时 [异步-ASync]
     * @param player
     * @param message
     */
    fun registerPlayerChatEvent(player: Player, message: String) {}

    /** 开始游戏 [异步-ASync]  */
    fun registerGameStartEvent() {}

    /** 结束游戏 [异步-ASync]  */
    fun registerGameOverEvent() {}

    /** 玩家被ban [异步-ASync]  */
    fun registerPlayerBanEvent(player: Player) {}

    /** 玩家被解除ban [异步-ASync]  */
    fun registerPlayerUnbanEvent(player: Player) {}

    /** 玩家被banIp [异步-ASync]  */
    fun registerPlayerIpBanEvent(player: Player) {}

    /** 玩家被解banIp [异步-ASync]  */
    fun registerPlayerIpUnbanEvent(ip: String) {}

    /** Use Java AbstractEventJava */
    abstract class AbstractEventJava : AbstractEvent
}