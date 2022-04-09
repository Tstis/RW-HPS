/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol.`null`

import cn.rwhps.server.data.player.Player
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.output.CompressOutputStream
import cn.rwhps.server.io.packet.GameCommandPacket
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.AbstractNetPacket
import cn.rwhps.server.struct.Seq

class NullNetPacket : AbstractNetPacket {
    override fun getSystemMessagePacket(msg: String): Packet {
        TODO("Not yet implemented")
    }

    override fun getChatMessagePacket(msg: String, sendBy: String, team: Int): Packet {
        TODO("Not yet implemented")
    }

    override fun getPingPacket(player: Player): Packet {
        TODO("Not yet implemented")
    }

    override fun getTickPacket(tick: Int): Packet {
        TODO("Not yet implemented")
    }

    override fun getGameTickCommandPacket(tick: Int, cmd: GameCommandPacket): Packet {
        TODO("Not yet implemented")
    }

    override fun getGameTickCommandsPacket(tick: Int, cmd: Seq<GameCommandPacket>): Packet {
        TODO("Not yet implemented")
    }

    override fun getTeamDataPacket(): CompressOutputStream {
        TODO("Not yet implemented")
    }

    override fun convertGameSaveDataPacket(packet: Packet): Packet {
        TODO("Not yet implemented")
    }

    override fun getStartGamePacket(): Packet {
        TODO("Not yet implemented")
    }

    override fun getPacketMapName(bytes: ByteArray): String {
        TODO("Not yet implemented")
    }

    override fun getDeceiveGameSave(): Packet {
        TODO("Not yet implemented")
    }

    override fun gameSummonPacket(index: Int, unit: String, x: Float, y: Float): GameCommandPacket {
        TODO("Not yet implemented")
    }

    override fun getExitPacket(): Packet {
        TODO("Not yet implemented")
    }

    override fun writePlayer(player: Player, stream: GameOutputStream) {
        TODO("Not yet implemented")
    }

    override fun getPlayerConnectPacket(): Packet {
        TODO("Not yet implemented")
    }

    override fun getPlayerRegisterPacket(name: String, uuid: String, passwd: String?, key: Int): Packet {
        TODO("Not yet implemented")
    }
}