/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.net.netconnectprotocol.realize

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.io.output.GameOutputStream
import java.io.IOException

/**
 * @author Dr
 */
class GameVersionPacketBeta : GameVersionPacket() {
    @Throws(IOException::class)
    override fun writePlayer(player: Player, stream: GameOutputStream) {
        with (stream) {
            if (Data.game.isStartGame) {
                writeByte(player.site)
                writeInt(player.ping)
                writeBoolean(Data.game.sharedControl)
                writeBoolean(player.sharedControl)
                return
            }
            writeByte(player.site)
            writeInt(Data.game.credits)
            writeInt(player.team)
            writeBoolean(true)
            writeString(player.name)
            writeBoolean(false)

            /* -1 N/A ; -2 -  ; -99 HOST */
            writeInt(player.ping)
            writeLong(System.currentTimeMillis())
            /* MS */
            writeBoolean(false)
            writeInt(0)
            writeInt(player.site)
            writeByte(0)
            /* 共享控制 */
            writeBoolean(Data.game.sharedControl)
            /* 是否掉线 */
            writeBoolean(player.sharedControl)
            /* 是否投降 */
            writeBoolean(false)
            writeBoolean(false)
            writeInt(-9999)
            writeBoolean(false)
            // 延迟后显示 （HOST)
            writeInt(if (player.isAdmin) 1 else 0)

            // Ai Difficulty Override
            writeInt(1)
            // Player Color
            writeInt(5)
            // ?
            writeInt(0)
            // ?
            writeInt(0)
            // ? Not > 0
            writeInt(0)
        }
    }
}