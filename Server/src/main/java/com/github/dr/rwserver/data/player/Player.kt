/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.data.player

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.net.game.ConnectServer
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionServer
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.LocaleUtil
import org.jetbrains.annotations.Nls
import java.util.*

/**
 *
 * @author Dr
 */
class Player(
    @JvmField var con: GameVersionServer?,
    /** Player connection UUID  */
    @JvmField val uuid: String,
    /** Player name  */
    @JvmField val name: String,
    /**   */
    @JvmField val localeUtil: LocaleUtil
) {
    /** is Admin  */
	@JvmField
	var isAdmin = false
    /** Team number  */
	@JvmField
	var team = 0
    /** List position  */
	@JvmField
	var site = 0
    /** */
    private val credits = Data.game.credits
    /** Shared control  */
	@JvmField
	var sharedControl = false
    /** (Markers)  */
	@JvmField
	var start = false
    /** Whether the player is dead  */
	@JvmField
	var dead = false
    /** Last move time  */
    @JvmField
	@Volatile
    var lastMoveTime: Long = 0
    /** Mute expiration time */
    @JvmField
	var muteTime: Long = 0
    /** Kick expiration time */
    @JvmField
	var kickTime: Long = 0
    @JvmField
	var timeTemp: Long = 0
    /** Ping */
    @JvmField
	var ping = 50
    @JvmField
	var lastMessageTime: Long = 0
    @JvmField
	var lastSentMessage = ""
    @JvmField
	var noSay = false
    var watch = false

    private var connectServer: ConnectServer? = null

    fun sendSystemMessage(@Nls text: String) {
        con!!.sendSystemMessage(text)
    }

    fun sendMessage(player: Player, @Nls text: String) {
        con!!.sendChatMessage(text, player.name, player.team)
    }

    fun kickPlayer(@Nls text: String) {
        con!!.sendKick(text)
    }

    /**
     * The player’s data on the local server is transferred to the new server
     * At this time, the local server only forwards the player data and has nothing to do with the local player.
     * The player will not exist in [Data.game.playerManage.playerGroup] and [Data.game.playerManage.playerAll]
     * Player ⇄ LocalServer ⇄ NewServer
     * @param ip
     * @param port
     */
    fun playerJumpsToAnotherServer(ip: String, port: Int) {
        if (!IsUtil.isDomainName(ip)) {
            throw RuntimeException("Error Domain")
        }
        connectServer = ConnectServer(ip,port,con!!)
    }

    /**
     * Disconnect this transit connection
     * Switch the player to the local server
     */
    fun playerJumpsToAnotherServerClose() {
        connectServer!!.close()
        con!!.isConnectServer = false
        con!!.connectServer = null
        Data.game.playerManage.playerGroup.add(this)
        Data.game.playerManage.playerAll.add(this)
    }

    fun clear() {
        con = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return  if (other == null || javaClass != other.javaClass) {
                    false
                } else if (other is Player) {
                    uuid == other.uuid
                } else {
                    uuid == other.toString()
                }
    }

    override fun hashCode(): Int {
        return Objects.hash(uuid)
    }
}