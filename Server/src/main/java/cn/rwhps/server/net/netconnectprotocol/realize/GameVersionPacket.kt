/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol.realize

import cn.rwhps.server.data.global.Cache
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.player.Player
import cn.rwhps.server.game.GameMaps
import cn.rwhps.server.game.GameUnitType
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.output.CompressOutputStream
import cn.rwhps.server.io.packet.GameCommandPacket
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.AbstractNetPacket
import cn.rwhps.server.net.netconnectprotocol.internal.server.chatMessagePacketInternal
import cn.rwhps.server.net.netconnectprotocol.internal.server.gameTickCommandPacketInternal
import cn.rwhps.server.net.netconnectprotocol.internal.server.gameTickCommandsPacketInternal
import cn.rwhps.server.net.netconnectprotocol.internal.server.gameTickPacketInternal
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.IsUtil
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.alone.annotations.MainProtocolImplementation
import cn.rwhps.server.util.encryption.Game
import cn.rwhps.server.util.encryption.Sha
import cn.rwhps.server.util.log.Log.error
import java.io.IOException
import java.math.BigInteger

/**
 * @author Dr
 */
@MainProtocolImplementation
open class GameVersionPacket : AbstractNetPacket {
    @Throws(IOException::class)
    override fun getSystemMessagePacket(msg: String): Packet =
        chatMessagePacketInternal(msg, "SERVER", 5)

    @Throws(IOException::class)
    override fun getChatMessagePacket(msg: String, sendBy: String, team: Int): Packet =
        chatMessagePacketInternal(msg, sendBy, team)

    @Throws(IOException::class)
    override fun getPingPacket(player: Player): Packet {
        player.timeTemp = System.currentTimeMillis()
        val o = GameOutputStream()
        o.writeLong(1000L)
        o.writeByte(0)
        return o.createPacket(PacketType.PACKET_HEART_BEAT)
    }

    @Throws(IOException::class)
    override fun getTickPacket(tick: Int): Packet =
        gameTickPacketInternal(tick)

    @Throws(IOException::class)
    override fun getGameTickCommandPacket(tick: Int, cmd: GameCommandPacket): Packet =
        gameTickCommandPacketInternal(tick,cmd)

    @Throws(IOException::class)
    override fun getGameTickCommandsPacket(tick: Int, cmd: Seq<GameCommandPacket>): Packet =
        gameTickCommandsPacketInternal(tick,cmd)

    @Throws(IOException::class)
    override fun getTeamDataPacket(): CompressOutputStream {
        Data.game.playerManage.updateControlIdentifier()

        val enc = CompressOutputStream.getGzipOutputStream("teams", true)
        Data.game.playerManage.runPlayerArrayDataRunnable { player: Player? ->
            try {
                if (player == null) {
                    enc.writeBoolean(false)
                } else {
                    enc.writeBoolean(true)
                    enc.writeInt(0)
                    writePlayer(player, enc)
                }
            } catch (e: Exception) {
                error("[ALL/Player] Get Server Team Info", e)
            }
        }
        return enc
    }

    @Throws(IOException::class)
    override fun convertGameSaveDataPacket(packet: Packet): Packet {
        GameInputStream(packet).use { stream ->
            val o = GameOutputStream()
            o.writeByte(stream.readByte())
            o.writeInt(stream.readInt())
            o.writeInt(stream.readInt())
            o.writeFloat(stream.readFloat())
            o.writeFloat(stream.readFloat())
            o.writeBoolean(false)
            o.writeBoolean(false)
            stream.readBoolean()
            stream.readBoolean()
            stream.readString()
            val bytes = stream.readStreamBytes()
            o.writeString("gameSave")
            o.flushMapData(bytes.size, bytes)
            return o.createPacket(PacketType.PACKET_SYNC)
        }
    }

    // 0->本地 1->自定义 2->保存的游戏
    @Throws(IOException::class)
    override fun getStartGamePacket(): Packet {
        val o = GameOutputStream()
        o.writeByte(0)
        // 0->本地 1->自定义 2->保存的游戏
        o.writeInt(Data.game.maps.mapType.ordinal)
        if (Data.game.maps.mapType == GameMaps.MapType.defaultMap) {
            o.writeString("maps/skirmish/" + Data.game.maps.mapPlayer + Data.game.maps.mapName + ".tmx")
        } else {
            o.flushMapData(Data.game.maps.mapData!!.mapSize, Data.game.maps.mapData!!.bytesMap!!)
            o.writeString("SAVE:" + Data.game.maps.mapName + ".tmx")
        }
        o.writeBoolean(false)
        return o.createPacket(PacketType.PACKET_START_GAME)
    }

    @Throws(IOException::class)
    override fun getPacketMapName(bytes: ByteArray): String {
        GameInputStream(bytes).use { stream ->
            stream.readString()
            stream.readInt()
            stream.readInt()
            return stream.readString()
        }
    }

    override fun getDeceiveGameSave(): Packet {
        val cPacket: Packet? = Cache.packetCache["getDeceiveGameSave"]
        if (IsUtil.notIsBlank(cPacket)) {
            return cPacket!!
        }

        val o = GameOutputStream()
        o.writeByte(0)
        o.writeInt(0)
        o.writeInt(0)
        o.writeFloat(0)
        o.writeFloat(0)
        o.writeBoolean(true)
        o.writeBoolean(false)
        val gzipEncoder = CompressOutputStream.getGzipOutputStream("gameSave", false)
        gzipEncoder.writeString("This is RW-HPS [Deceive Get GameSave]!")
        o.flushEncodeData(gzipEncoder)

        val cachePacket = o.createPacket(PacketType.PACKET_SYNC)
        Cache.packetCache.put("getDeceiveGameSave",cachePacket)

        return cachePacket
    }

    override fun gameSummonPacket(index: Int, unit: String, x: Float, y: Float): GameCommandPacket {
        Data.game.playerManage.updateControlIdentifier()

        val outStream = GameOutputStream()
        outStream.writeByte(index)
        outStream.writeBoolean(true)
        // 建造
        outStream.writeInt(GameUnitType.GameActions.BUILD.ordinal)


        var unitID = -2
        if (IsUtil.notIsNumeric(unit)) {
            GameUnitType.GameUnits.values().forEach {
                if (it.name.equals(unit,ignoreCase = true)) {
                    unitID = it.ordinal
                    return@forEach
                }
            }
        } else {
            unitID = unit.toInt()
        }

        outStream.writeInt(unitID)

        if (unitID == -2) {
            outStream.writeString(unit)
        }


        // X
        outStream.writeFloat(x)
        // Y
        outStream.writeFloat(y)
        // Tager
        outStream.writeLong(-1L)
        //?
        outStream.writeByte(42)
        outStream.writeFloat(1)
        outStream.writeFloat(1)
        outStream.writeBoolean(false)
        outStream.writeBoolean(false)
        outStream.writeBoolean(false)
        outStream.writeBoolean(false)
        outStream.writeBoolean(false)
        outStream.writeBoolean(false)

        //
        outStream.writeInt(-1)
        outStream.writeInt(-1)

        outStream.writeBoolean(false)
        outStream.writeBoolean(false)

        outStream.writeInt(0)

        outStream.writeBoolean(false)
        outStream.writeBoolean(false)


        outStream.writeLong(-1)
        outStream.writeString("-1")
        outStream.writeBoolean(false)
        outStream.writeShort(Data.game.playerManage.sharedControlPlayer.toShort())
        // System action
        outStream.writeBoolean(true)
        outStream.writeByte(0)
        outStream.writeFloat(0f)
        outStream.writeFloat(0f)
        //action type
        outStream.writeInt(5)
        outStream.writeInt(0)
        outStream.writeBoolean(false)
        return GameCommandPacket(index, outStream.getPacketBytes())
    }

    @Throws(IOException::class)
    override fun getExitPacket(): Packet {
        val cPacket: Packet? = Cache.packetCache["getExitPacket"]
        if (IsUtil.notIsBlank(cPacket)) {
            return cPacket!!
        }

        val o = GameOutputStream()
        o.writeString("exited")

        val cachePacket = o.createPacket(PacketType.PACKET_DISCONNECT)
        Cache.packetCache.put("getExitPacket",cachePacket)

        return cachePacket
    }

    @Throws(IOException::class)
    override fun writePlayer(player: Player, stream: GameOutputStream) {
        if (Data.game.isStartGame) {
            stream.writeByte(player.site)
            stream.writeInt(player.ping)
            stream.writeBoolean(Data.game.sharedControl)
            stream.writeBoolean(player.controlThePlayer)
            return
        }
        stream.writeByte(player.site)
        // 并没有什么用
        stream.writeInt(player.credits)
        stream.writeInt(player.team)
        stream.writeBoolean(true)
        stream.writeString(player.name)
        stream.writeBoolean(false)

        /* -1 N/A  -2 -   -99 HOST */
        stream.writeInt(player.ping)
        stream.writeLong(System.currentTimeMillis())

        /* Is AI */
        stream.writeBoolean(false)
        /* AI Difficu */
        stream.writeInt(0)

        stream.writeInt(player.site)
        stream.writeByte(0)

        /* 共享控制 */
        stream.writeBoolean(Data.game.sharedControl)
        /* 是否掉线 */
        stream.writeBoolean(player.sharedControl)

        /* 是否投降 */
        stream.writeBoolean(false)
        stream.writeBoolean(false)
        stream.writeInt(-9999)
        stream.writeBoolean(false)
        // 延迟后显示 （HOST) [房主]
        stream.writeInt(if (player.isAdmin) 1 else 0)
    }

    @Throws(IOException::class)
    override fun getPlayerConnectPacket(): Packet {
        val out = GameOutputStream()
        out.writeString("com.corrodinggames.rwhps.forward")
        out.writeInt(1)
        out.writeInt(151)
        out.writeInt(151)
        return out.createPacket(PacketType.PACKET_PREREGISTER_CONNECTION)
    }

    @Throws(IOException::class)
    override fun getPlayerRegisterPacket(name: String, uuid: String, passwd: String?, key: Int): Packet {
        val out = GameOutputStream()
        out.writeString("com.corrodinggames.rts")
        out.writeInt(4)
        out.writeInt(151)
        out.writeInt(151)
        out.writeString(name)

        if (IsUtil.isBlank(passwd)) {
            out.writeBoolean(false)
        } else {
            out.writeBoolean(true)
            out.writeString(BigInteger(1, Sha.sha256Array(passwd!!)).toString(16).uppercase())
        }

        out.writeString("com.corrodinggames.rts.java")
        out.writeString(uuid)
        out.writeInt(1198432602)
        out.writeString(Game.connectKey(key))
        return out.createPacket(PacketType.PACKET_PLAYER_INFO)
    }
}