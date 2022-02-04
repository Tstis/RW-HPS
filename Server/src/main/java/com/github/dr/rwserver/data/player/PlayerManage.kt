/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.data.player

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.net.netconnectprotocol.realize.GameVersionServer
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.LocaleUtil
import java.util.*

class PlayerManage(private val maxPlayerSize: Int) {
    /** 混战分配  */
    var amTeam = false

    /** 共享控制  */
    var sharedControlPlayer = 0

    /** Online players   */
    @JvmField
    val playerGroup = Seq<Player>(16)

    /** ALL players  */
    @JvmField
    val playerAll = Seq<Player>(16)

    /** 队伍数据  */
    private val playerData = arrayOfNulls<Player?>(maxPlayerSize)

    fun addPlayer(con: GameVersionServer, uuid: String, name: String, localeUtil: LocaleUtil = Data.localeUtil): Player {
        val player = Player(con, uuid, name, localeUtil)
        if (Data.config.OneAdmin) {
            if (playerGroup.size() == 0) {
                player.isAdmin = true
            }
        } else {
            if (Data.core.admin.playerData.contains(player.uuid)) {
                player.isAdmin = true
            }
        }
        autoPlayerTeam(player)
        playerGroup.add(player)
        playerAll.add(player)
        return player
    }

    fun removePlayerArray(player: Player) {
        removePlayerArray(player.site)
    }
    fun removePlayerArray(size: Int) {
        playerData[size] = null
    }
    fun setPlayerArray(size: Int, player: Player?) {
        playerData[size] = player
    }
    fun getPlayerArray(size: Int): Player? {
        return playerData[size]
    }


    fun runPlayerArrayDataRunnable(run: (Player?) -> Unit) {
        for (player in playerData) {
            run(player)
        }
    }

    fun updateControlIdentifier() {
        var int3 = 0
        for (i in 0 until maxPlayerSize) {
            val player1 = playerData[i]
            if (player1 != null) {
                if (player1.sharedControl || Data.game.sharedControl) {
                    int3 = int3 or 1 shl i
                }
            }
        }
        sharedControlPlayer = int3
    }

    fun cleanPlayerAllData() {
        Arrays.fill(playerData, null)
        playerAll.clear()
        playerGroup.clear()
    }




    /* TEAM  */

    private fun autoPlayerTeam(player: Player) {
        if (amTeam) {
            for (i in 0 until maxPlayerSize) {
                if (playerData[i] == null) {
                    playerData[i] = player
                    player.site = i
                    player.team = i
                    return
                }
            }
        } else {
            for (i in 0 until maxPlayerSize) {
                if (playerData[i] == null) {
                    playerData[i] = player
                    player.site = i
                    player.team = if (IsUtil.isTwoTimes(i + 1)) 1 else 0
                    return
                }
            }
        }
    }

    fun amYesPlayerTeam() {
        for (i in 0 until maxPlayerSize) {
            if (playerData[i] != null) {
                playerData[i]!!.team = i
            }
        }
    }

    fun amNoPlayerTeam() {
        for (i in 0 until maxPlayerSize) {
            if (playerData[i] != null) {
                playerData[i]!!.team = if (IsUtil.isTwoTimes(i + 1)) 1 else 0
            }
        }
    }
}