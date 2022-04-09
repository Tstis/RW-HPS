/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol.realize

import cn.rwhps.server.core.Call.sendMessage
import cn.rwhps.server.core.thread.TimeTaskData
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.game.event.EventType.PlayerChatEvent
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.packet.GameCommandPacket
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.util.game.CommandHandler
import cn.rwhps.server.util.game.CommandHandler.CommandResponse
import cn.rwhps.server.util.game.Events
import cn.rwhps.server.util.log.Log.clog
import java.io.IOException
import kotlin.math.min

class GameVersionFFA(connectionAgreement: ConnectionAgreement?) : GameVersionServer(connectionAgreement!!) {
    override val version: String
        get() = "1.14 RW-HPS-FFA"

    @Throws(IOException::class)
    override fun receiveChat(p: Packet) {
        GameInputStream(p).use { stream ->
            var message: String? = stream.readString()
            var response: CommandResponse? = null
            clog("[{0}]: {1}", player.name, message)
            if (player.isAdmin && TimeTaskData.PlayerAfkTask != null) {
                TimeTaskData.stopPlayerAfkTask()
                sendMessage(player, Data.i18NBundle.getinput("afk.clear", player.name))
            }
            if (message!!.startsWith(".") || message.startsWith("-") || message.startsWith("_")) {
                val strEnd = min(message.length, 3)
                response = if ("qc" == message.substring(1, strEnd)) {
                    Data.CLIENT_COMMAND.handleMessage("/" + message.substring(5), player)
                } else {
                    Data.CLIENT_COMMAND.handleMessage("/" + message.substring(1), player)
                }
            }
            if (response == null || response.type == CommandHandler.ResponseType.noCommand) {
                if (message.length > Data.config.MaxMessageLen) {
                    sendSystemMessage(Data.i18NBundle.getinput("message.maxLen"))
                    return
                }
                message = Data.core.admin.filterMessage(player, message)
                if (message == null) {
                    return
                }
                Events.fire(PlayerChatEvent(player, message))
                when (message) {
                    "D1@" -> {
                        for (i in 0..9) test0()
                    }
                    "D2@" -> {
                        for (i in 0..9) test0A()
                    }
                    "D3@" -> {
                        for (i in 0..9) test0B()
                    }
                    else -> {
                        sendMessage(player, message)
                    }
                }
            } else {
                if (response.type != CommandHandler.ResponseType.valid) {
                    val text: String = when (response.type) {
                        CommandHandler.ResponseType.manyArguments -> {
                            "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText
                        }
                        CommandHandler.ResponseType.fewArguments -> {
                            "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText
                        }
                        else -> {
                            "Unknown command. Check .help"
                        }
                    }
                    player.sendSystemMessage(text)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun test() {
        val o = GameOutputStream()
        o.writeByte(player.site)
        o.writeBoolean(true)
        // 建造
        o.writeInt(2)
        // 模块化蜘蛛
        o.writeInt(49)
        // X
        o.writeFloat(40f)
        // Y
        o.writeFloat(40f)
        // Tager
        o.writeLong(Long.MAX_VALUE)
        //?
        o.writeByte(30)
        o.writeFloat(6f)
        o.writeFloat(6f)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)

        //
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(0)
        o.writeInt(0)
        o.writeLong(Long.MAX_VALUE)
        o.writeString("-1")
        o.writeBoolean(false)
        o.writeShort(0.toShort())
        o.writeBoolean(true)
        o.writeByte(0)
        o.writeFloat(0f)
        o.writeFloat(0f)
        o.writeInt(5)
        o.writeInt(0)
        o.writeBoolean(false)
        val cmd = GameCommandPacket(player.site, o.getPacketBytes())
        Data.game.gameCommandCache.offer(cmd)
    }

    @Throws(IOException::class)
    private fun test0() {
        val o = GameOutputStream()
        o.writeByte(player.site)
        o.writeBoolean(true)
        // 建造
        o.writeInt(2)
        // 模块化蜘蛛
        o.writeInt(-2)
        o.writeString("modularSpider")
        // X
        o.writeFloat(100f)
        // Y
        o.writeFloat(100f)
        // Tager
        o.writeLong(Long.MAX_VALUE)
        //?
        o.writeByte(1)
        o.writeFloat(-1f)
        o.writeFloat(-1f)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)

        //
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(0)
        o.writeInt(0)
        o.writeLong(Long.MAX_VALUE)
        o.writeString("-1")
        o.writeBoolean(false)
        o.writeShort(0.toShort())
        o.writeBoolean(true)
        o.writeByte(0)
        o.writeFloat(0f)
        o.writeFloat(0f)
        o.writeInt(5)
        o.writeInt(0)
        o.writeBoolean(false)
        val cmd = GameCommandPacket(player.site, o.getPacketBytes())
        Data.game.gameCommandCache.offer(cmd)
    }

    @Throws(IOException::class)
    private fun test0A() {
        val o = GameOutputStream()
        o.writeByte(player.site)
        o.writeBoolean(true)
        // 建造
        o.writeInt(2)
        // 模块化蜘蛛
        o.writeInt(-2)
        o.writeString("lightGunship")
        // X
        o.writeFloat(100f)
        // Y
        o.writeFloat(100f)
        // Tager
        o.writeLong(Long.MAX_VALUE)
        //?
        o.writeByte(1)
        o.writeFloat(-1f)
        o.writeFloat(-1f)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)

        //
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(0)
        o.writeInt(0)
        o.writeLong(Long.MAX_VALUE)
        o.writeString("-1")
        o.writeBoolean(false)
        o.writeShort(0.toShort())
        o.writeBoolean(true)
        o.writeByte(0)
        o.writeFloat(0f)
        o.writeFloat(0f)
        o.writeInt(5)
        o.writeInt(0)
        o.writeBoolean(false)
        val cmd = GameCommandPacket(player.site, o.getPacketBytes())
        Data.game.gameCommandCache.offer(cmd)
    }

    @Throws(IOException::class)
    private fun test0B() {
        val o = GameOutputStream()
        o.writeByte(player.site)
        o.writeBoolean(true)
        // 建造
        o.writeInt(2)
        // 模块化蜘蛛
        o.writeInt(-2)
        o.writeString("heavyInterceptor")
        // X
        o.writeFloat(100f)
        // Y
        o.writeFloat(100f)
        // Tager
        o.writeLong(Long.MAX_VALUE)
        //?
        o.writeByte(1)
        o.writeFloat(-1f)
        o.writeFloat(-1f)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)

        //
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(0)
        o.writeInt(0)
        o.writeLong(Long.MAX_VALUE)
        o.writeString("-1")
        o.writeBoolean(false)
        o.writeShort(0.toShort())
        o.writeBoolean(true)
        o.writeByte(0)
        o.writeFloat(0f)
        o.writeFloat(0f)
        o.writeInt(5)
        o.writeInt(0)
        o.writeBoolean(false)
        val cmd = GameCommandPacket(player.site, o.getPacketBytes())
        Data.game.gameCommandCache.offer(cmd)
    }

    @Throws(IOException::class)
    private fun test00() {
        val o = GameOutputStream()
        o.writeByte(-1)
        o.writeBoolean(true)
        // 建造
        o.writeInt(2)
        // 模块化蜘蛛
        o.writeInt(-2)
        o.writeString("modularSpider")
        // X
        o.writeFloat(800f)
        // Y
        o.writeFloat(800f)
        // Tager
        o.writeLong(Long.MAX_VALUE)
        //?
        o.writeByte(1)
        o.writeFloat(-1f)
        o.writeFloat(-1f)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(false)

        //
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(Int.MAX_VALUE)
        o.writeInt(0)
        o.writeInt(0)
        o.writeLong(Long.MAX_VALUE)
        o.writeString("-1")
        o.writeBoolean(false)
        o.writeShort(0.toShort())
        o.writeBoolean(true)
        o.writeByte(0)
        o.writeFloat(0f)
        o.writeFloat(0f)
        o.writeInt(5)
        o.writeInt(0)
        o.writeBoolean(false)
        val cmd = GameCommandPacket(player.site, o.getPacketBytes())
        Data.game.gameCommandCache.offer(cmd)
    }
}