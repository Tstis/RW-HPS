/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.plugin.center

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.json.Json
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.net.HttpRequestOkHttp
import com.github.dr.rwserver.plugin.GetVersion
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.file.FileUtil.Companion.getFolder
import com.github.dr.rwserver.util.game.CommandHandler

class PluginCenter {
    private val PluginCommand = CommandHandler("")
    private var pluginCenterData: PluginCenterData

    fun command(str: String?, log: StrCons) {
        val response = PluginCommand.handleMessage(str, log)
        if (response.type != CommandHandler.ResponseType.valid) {
            val text: String = when (response.type) {
                        CommandHandler.ResponseType.manyArguments -> {
                            "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText
                        }
                        CommandHandler.ResponseType.fewArguments -> {
                            "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText
                        }
                        else -> {
                            "Unknown command. Check plugin help"
                        }
                    }
            log[text]
        }
    }

    private fun register() {
        PluginCommand.register("help", "") { _: Array<String?>?, log: StrCons ->
            //log.get("plugin updata  更新插件列表");
            log["plugin updatalist  更新插件列表"]
            log["plugin install PluginID  安装指定id的插件"]
        }
        PluginCommand.register("updatelist", "") { _: Array<String?>?, log: StrCons ->
            pluginCenterData = PluginCenterData(url + "PluginData")
            log["更新插件列表完成"]
        }
        PluginCommand.register("list", "") { _: Array<String?>?, log: StrCons ->
            log[pluginCenterData.pluginData]
        }
        PluginCommand.register("install", "<PluginID>", "") { arg: Array<String>, log: StrCons ->
            val json = pluginCenterData.getJson(arg[0].toInt())
            if (!GetVersion(Data.SERVER_CORE_VERSION).getIfVersion(json.getData("supportedVersions"))) {
                log["Plugin版本不兼容 Plugin名字为: {0}", json.getData("name")]
            } else {
                HttpRequestOkHttp.downUrl(
                    url + json.getData("name") + ".jar",
                    getFolder(Data.Plugin_Plugins_Path).toFile(json.getData("name") + ".jar").file
                )
                log["安装完成 请重启服务器"]
            }
        }
    }

    private class PluginCenterData(url: String) {
         val pluginCenterData: Seq<Json>

        val pluginData: String
            get() {
                val stringBuilder = StringBuilder()
                var json: Json
                for (i in 0 until pluginCenterData.size()) {
                    json = pluginCenterData[i]
                    stringBuilder.append("ID: ").append(i).append("  ")
                        .append("Name: ").append(json.getData("name")).append("  ")
                        .append("Description: ").append(json.getData("description")).append(Data.LINE_SEPARATOR)
                }
                return stringBuilder.toString()
            }

        fun getJson(i: Int): Json {
            return pluginCenterData[i]
        }

        init {
            pluginCenterData = Json(HttpRequestOkHttp.doGet(url)).getArraySeqData()
        }
    }

    companion object {
        val pluginCenter = PluginCenter()
        private const val url = "https://plugin.data.der.kim/"
    }

    init {
        pluginCenterData = PluginCenterData(url + "PluginData")
        register()
    }
}