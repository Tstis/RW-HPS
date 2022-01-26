/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
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
        if (Data.game.isStartGame) {
            stream.writeByte(player.site)
            stream.writeInt(player.ping)
            stream.writeBoolean(Data.game.sharedControl)
            stream.writeBoolean(player.sharedControl)
            return
        }
        stream.writeByte(player.site)
        stream.writeInt(Data.game.credits)
        stream.writeInt(player.team)
        stream.writeBoolean(true)
        stream.writeString(player.name)
        stream.writeBoolean(false)

        /* -1 N/A ; -2 -  ; -99 HOST */stream.writeInt(player.ping)
        stream.writeLong(System.currentTimeMillis())
        /* MS */stream.writeBoolean(false)
        stream.writeInt(0)
        stream.writeInt(player.site)
        stream.writeByte(0)
        /* 共享控制 */stream.writeBoolean(Data.game.sharedControl)
        /* 是否掉线 */stream.writeBoolean(player.sharedControl)
        /* 是否投降 */stream.writeBoolean(false)
        stream.writeBoolean(false)
        stream.writeInt(-9999)
        stream.writeBoolean(false)
        // 延迟后显示 （HOST)
        stream.writeInt(if (player.isAdmin) 1 else 0)

        // Ai Difficulty Override
        stream.writeInt(1)
        // Player Color
        stream.writeInt(5)
        // ?
        stream.writeInt(0)
        // ?
        stream.writeInt(0)
        // ? Not > 0
        stream.writeInt(0)
    }
}