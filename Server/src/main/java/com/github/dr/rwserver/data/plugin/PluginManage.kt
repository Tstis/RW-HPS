/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.data.plugin

import com.github.dr.rwserver.data.plugin.PluginEventManage.Companion.add
import com.github.dr.rwserver.func.Cons
import com.github.dr.rwserver.plugin.PluginsLoad.Companion.resultPluginData
import com.github.dr.rwserver.plugin.PluginsLoad.PluginLoadData
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.alone.annotations.DidNotFinish
import com.github.dr.rwserver.util.file.FileUtil
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.log.Log.error
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

    /** 最先执行 可以进行Plugin的数据读取  -1  */
    @JvmStatic
    fun runOnEnable() {
        pluginData!!.each { e: PluginLoadData -> e.main.onEnable() }
    }

    /** 注册要在服务器端使用的任何命令，例如从控制台 -2  */
    @JvmStatic
    fun runRegisterServerCommands(handler: CommandHandler) {
        pluginData!!.each { e: PluginLoadData -> e.main.registerServerCommands(handler) }
    }

    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 -3  */
    @JvmStatic
    fun runRegisterClientCommands(handler: CommandHandler) {
        pluginData!!.each { e: PluginLoadData -> e.main.registerClientCommands(handler) }
    }

    /** 注册事件 -4  */
    @JvmStatic
    fun runRegisterEvents() {
        pluginData!!.each { e: PluginLoadData ->
            val abstractEvent = e.main.registerEvents()
            if (IsUtil.notIsBlank(abstractEvent)) {
                add(abstractEvent!!)
            }
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
            e.main.pluginData.save()
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