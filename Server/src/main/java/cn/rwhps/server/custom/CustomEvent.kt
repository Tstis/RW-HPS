/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package cn.rwhps.server.custom

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.json.Json
import cn.rwhps.server.game.event.EventType.PlayerConnectPasswdCheckEvent
import cn.rwhps.server.net.HttpRequestOkHttp
import cn.rwhps.server.util.IsUtil.isBlank
import cn.rwhps.server.util.encryption.Md5.md5
import cn.rwhps.server.util.game.Events
import cn.rwhps.server.util.log.Log.debug
import java.io.IOException
import java.util.*

/**
 * @author RW-HPS/Dr
 */
class CustomEvent {
    init {
        if (Data.config.PasswdCheckApi) {
            //if (false) {
            Events.remove(PlayerConnectPasswdCheckEvent::class.java)
            Events.on(PlayerConnectPasswdCheckEvent::class.java) { e: PlayerConnectPasswdCheckEvent ->
                if (isBlank(e.passwd)) {
                    try {
                        e.abstractNetConnect.sendErrorPasswd()
                    } catch (ioException: IOException) {
                    } finally {
                        e.result = true
                    }
                    return@on
                }
                //final String passwd = Base64.encode(new BigInteger(e.passwd,16).toString());
                val passwd = e.passwd.uppercase(Locale.ROOT)
                val result = HttpRequestOkHttp.doPost("https://rw.tiexiu.xyz/api.php", "action=userkey&Data=" + passwd + "&key=" + md5("userkey$passwd"))
                val json = Json(result)
                val code = json.getData("Status")
                debug("Check Passwd POST", result)
                if ("0" != code) {
                    try {
                        e.abstractNetConnect.sendErrorPasswd()
                    } catch (ioException: IOException) {
                    } finally {
                        e.result = true
                    }
                    return@on
                }
                e.name = json.getData("qq")
            }
        }
    }
}