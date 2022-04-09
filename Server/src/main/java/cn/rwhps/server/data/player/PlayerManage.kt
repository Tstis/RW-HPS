/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package cn.rwhps.server.data.player

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionServer
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.I18NBundle
import cn.rwhps.server.util.IsUtil
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

    fun addPlayer(con: GameVersionServer, uuid: String, name: String, i18NBundle: I18NBundle = Data.i18NBundle): Player {
        val player = Player(con, uuid, name, i18NBundle)
        if (Data.config.OneAdmin) {
            if (playerGroup.size() == 0) {
                player.isAdmin = true
            }
        }

        if (Data.core.admin.isAdmin(player)) {
            val data = Data.core.admin.playerAdminData.get(player.uuid)
            player.isAdmin = data.admin
            player.superAdmin = data.superAdmin
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
            if (player1 != null && player1.controlThePlayer) {
                if (player1.controlThePlayer) {
                    int3 = int3 or 1 shl i
                }
            }
        }
        sharedControlPlayer = int3
    }

    fun cleanPlayerAllData() {
        playerGroup.each { it.kickPlayer("CleanAllPlayer")}
        playerAll.each { it.kickPlayer("CleanAllPlayer")}

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