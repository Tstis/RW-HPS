/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.command.relay

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.net.netconnectprotocol.internal.relay.fromRelayJumpsToAnotherServer
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
import cn.rwhps.server.util.game.CommandHandler

internal class RelayClientCommands(handler: CommandHandler) {
    private val localeUtil = Data.i18NBundle

    private fun isAdmin(con: GameVersionRelay, sendMsg: Boolean = true): Boolean {
        if (con.relay?.admin === con) {
            return true
        }
        if (sendMsg) {
            con.getRelayT4(localeUtil.getinput("err.noAdmin"))
        }
        return false
    }

    init {
        handler.register("help", "clientCommands.help") { _: Array<String>?, con: GameVersionRelay ->
            val str = StringBuilder(16)
            for (command in handler.commandList) {
                if (command.description.startsWith("#")) {
                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
                        .append(command.paramText).append(" - ").append(command.description.substring(1))
                } else {
                    if ("HIDE" == command.description) {
                        continue
                    }
                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
                        .append(command.paramText).append(" - ").append(localeUtil.getinput(command.description))
                        .append(Data.LINE_SEPARATOR)
                }
            }
            sendMsg(con,str.toString())
        }

        handler.register("jump","<ip/id>", "# jump Server") { args: Array<String>, con: GameVersionRelay ->
            if (!isAdmin(con,false)) {
                con.sendCustomPacket(fromRelayJumpsToAnotherServer(args[0]))
            } else {
                sendMsg(con,"You Is ADMIN !")
            }
        }
    }

    private fun sendMsg(con: GameVersionRelay, msg: String) {
        con.sendCustomPacket(NetStaticData.RwHps.abstractNetPacket.getSystemMessagePacket(msg))

    }
}