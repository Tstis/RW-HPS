/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.data.plugin

import cn.rwhps.server.data.plugin.PluginEventManage.Companion.add
import cn.rwhps.server.func.Cons
import cn.rwhps.server.plugin.Plugin
import cn.rwhps.server.plugin.PluginsLoad.Companion.addPluginClass
import cn.rwhps.server.plugin.PluginsLoad.Companion.resultPluginData
import cn.rwhps.server.plugin.PluginsLoad.PluginLoadData
import cn.rwhps.server.plugin.event.AbstractEvent
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.alone.annotations.DidNotFinish
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.game.CommandHandler
import cn.rwhps.server.util.log.Log.error
import java.io.IOException

object PluginManage {
    private val pluginEventManage = PluginEventManage()
    private var pluginData: Seq<PluginLoadData>? = null
    @JvmStatic
    val loadSize: Int
        get() = pluginData!!.size()

    @JvmStatic
    fun run(cons: Cons<PluginLoadData?>) {
        pluginData!!.each { t: PluginLoadData? -> cons[t] }
    }

    @JvmStatic
    fun init(fileUtil: FileUtil) {
        pluginData = resultPluginData(fileUtil)
    }

    @JvmStatic
    fun addPluginClass(name: String,author: String,description: String, version: String, main: Plugin,mkdir: Boolean , skip: Boolean = false) {
        addPluginClass(name,author,description,version,main,mkdir,skip,pluginData!!)
    }

    /** 最先执行 可以进行Plugin的数据读取  -1  */
    @JvmStatic
    fun runOnEnable() {
        pluginData!!.each { e: PluginLoadData -> e.main.onEnable() }
    }

    /** 注册要在服务器端使用的任何命令，例如从控制台 */
    @JvmStatic
    fun runRegisterCoreCommands(handler: CommandHandler) {
        pluginData!!.each { e: PluginLoadData -> e.main.registerCoreCommands(handler) }
    }
    /** 注册要在服务器端使用的任何命令，例如从控制台-Server */
    @JvmStatic
    fun runRegisterServerCommands(handler: CommandHandler) {
        pluginData!!.each { e: PluginLoadData -> e.main.registerServerCommands(handler) }
    }
    /** 注册要在服务器端使用的任何命令，例如从控制台-Relay */
    @JvmStatic
    fun runRegisterRelayCommands(handler: CommandHandler) {
        pluginData!!.each { e: PluginLoadData -> e.main.registerRelayCommands(handler) }
    }

    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 */
    @JvmStatic
    fun runRegisterClientCommands(handler: CommandHandler) {
        pluginData!!.each { e: PluginLoadData -> e.main.registerClientCommands(handler) }
    }

    /** 注册事件 -4  */
    @JvmStatic
    fun runRegisterEvents() {
        pluginData!!.each { e: PluginLoadData ->
            val abstractEvent: AbstractEvent? = e.main.registerEvents()
            abstractEvent?.let { add(it)}
        }
    }

    /** 创建所有插件并注册命令后调用 -5  */
    @JvmStatic
    fun runInit() {
        pluginData!!.each { e: PluginLoadData -> e.main.init() }
    }

    /** Server退出时执行 可以进行Plugin的数据保存  -6  */
    @JvmStatic
    fun runOnDisable() {
        pluginData!!.each { e: PluginLoadData ->
            //e.main.pluginData.save()
            e.main.onDisable()
        }
    }

    @JvmStatic
    @DidNotFinish
    fun removePlugin(name: String) {
        pluginData!!.each({ e: PluginLoadData -> e.name.equals(name, ignoreCase = true) }) { p: PluginLoadData ->
            pluginData!!.remove(p)
            //p.main.onUnLoad();
            try {
                //p.main.classLoader!!.close()
            } catch (e: IOException) {
                error("卸载失败 : " + p.name)
            }
        }
        //TODO 完全卸载Plugin
    }
}