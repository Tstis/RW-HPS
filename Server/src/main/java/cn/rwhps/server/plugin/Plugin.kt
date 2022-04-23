/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.plugin

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.plugin.event.AbstractEvent
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.game.CommandHandler

/**
 * @author RW-HPS/Dr
 */
abstract class Plugin {
    lateinit var pluginDataFileUtil: FileUtil
        interface set

    /**
     * 提供语言注入
     * @param lang String
     * @param k String
     * @param v String
     * @return 是否成功
     */
    fun loadLang(lang: String,k: String, v: String): Boolean {
        return Data.i18NBundleMap[lang]?.addLang(k,v) == true
    }

    /** 最先执行 可以进行Plugin的数据读取  -1  */
    open fun onEnable() {}

    /**
     * 注册要在服务器端使用的Core命令，例如从控制台
     * 这里注册的命令无论启动什么协议 都会存在
     */
    open fun registerCoreCommands(handler: CommandHandler) {}
    /**
     * 注册要在服务器端使用的Server命令，例如从控制台-Server
     * 这里注册的命令只有启动Server协议 才会存在
     */
    open fun registerServerCommands(handler: CommandHandler) {}
    /**
     * 注册要在服务器端使用的Relay命令，例如从控制台0Relay
     * 这里注册的命令只有启动Relay协议 才会存在
     */
    open fun registerRelayCommands(handler: CommandHandler) {}

    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 -3  */
    open fun registerClientCommands(handler: CommandHandler) {}

    /**
     * 注册事件
     * @return AbstractEvent实例
     */
    open fun registerEvents(): AbstractEvent? {
        return null
    }

    /** 创建所有插件并注册命令后调用 -5  */
    open fun init() {}


    /** [注意 将会强制继承] Server退出时执行 可以进行Plugin的数据保存 -6  */
    open fun onDisable() {}

    /** [强制继承] Server卸载这个插件的时候执行 需要Plugin配合关闭 */
    //abstract fun onUnLoad()
}