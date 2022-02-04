/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.command

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.Relay
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.util.ExtractUtil.ipToLong
import com.github.dr.rwserver.util.IsUtil.isBlank
import com.github.dr.rwserver.util.game.CommandHandler

class RelayCommands(handler: CommandHandler) {
    private fun registerRelayCommand(handler: CommandHandler) {
        handler.register("players", "serverCommands.players") { _: Array<String>?, log: StrCons ->
            //if (Relay.serverRelayIpData.size() < 0) {
            //    log["No players are currently in the server."]
            //} else {
                log[Relay.relayAllIP]
            //}
        }
        handler.register("banrelay", "<id>", "serverCommands.banrelay") { arg: Array<String>, log: StrCons ->
            val relay = Relay.getRelay(arg[0])
            if (isBlank(relay)) {
                log["NOT RELAY ROOM"]
            } else {
                relay!!.groupNet.disconnect()
                relay.sendMsg("You are banned by the administrator, please do not occupy public resources")
                val ip = relay.admin!!.ip
                Data.core.admin.bannedIP24.add(ipToLong(ip))
                relay.admin!!.disconnect()
                log["OK!  $ip The *.*.*.0 segment is disabled"]
            }

        }

        handler.register("unbanrelay", "<ip>", "serverCommands.unBanrelay") { arg: Array<String>, log: StrCons ->
            val ip = arg[0]
            Data.core.admin.bannedIP24.remove(ipToLong(ip))
            log["OK!  $ip The *.*.*.0 segment is unDisabled"]
        }
    
    
    //own
        handler.register("banrelayip", "<ip>", "serverCommands.banrelay") { arg: Array<String>, log: StrCons ->
            val ip = arg[0]
            Data.core.admin.bannedIP24.add(ipToLong(ip))
            //bannedIP24.disconnect(ipToLong(ip))
            log["OK!  $ip The *.*.*.0 segment is disabled"]
        }
    
        handler.register("kickrelay", "<id>", "serverCommands.banrelay") { arg: Array<String>, log: StrCons ->
            val relay = Relay.getRelay(arg[0])
            relay!!.groupNet.disconnect()
            relay.admin!!.disconnect()
            log["OK!  The room segment is kicked"]
        }
        
        handler.register("kickrelayip", "<ip>", "serverCommands.banrelay") { arg: Array<String>, log: StrCons ->
            val ip = arg[0]
            Data.core.admin.bannedIP24.add(ipToLong(ip))
            Thread.sleep(500)
            Data.core.admin.bannedIP24.remove(ipToLong(ip))
            log["OK!  $ip The *.*.*.0 segment is kicked"]
        }
    }
        
    companion object {
        private val localeUtil = Data.localeUtil
    }

    init {
        registerRelayCommand(handler)
    }
}
