/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.data.global

import cn.rwhps.server.command.ex.Vote
import cn.rwhps.server.core.Application
import cn.rwhps.server.data.base.BaseConfig
import cn.rwhps.server.game.Rules
import cn.rwhps.server.io.output.CompressOutputStream
import cn.rwhps.server.struct.ObjectMap
import cn.rwhps.server.util.I18NBundle
import cn.rwhps.server.util.file.LoadIni
import cn.rwhps.server.util.game.CommandHandler
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * @author Dr
 */
object Data {
    const val Plugin_Data_Path = "/data"
    const val Plugin_Save_Path = "/data/save"
    const val Plugin_Cache_Path = "/data/cache"
    const val Plugin_Lib_Path = "/data/lib"
    const val Plugin_Log_Path = "/data/log"
    const val Plugin_Maps_Path = "/data/maps"
    const val Plugin_Plugins_Path = "/data/plugins"
    const val Plugin_Mods_Path = "/data/mods"
    val UTF_8: Charset = StandardCharsets.UTF_8
    /*
	 * 插件默认变量
	 */
    /** 自定义包名  */
    const val SERVER_ID = "com.github.dr.rwserver"
    const val SERVER_CORE_VERSION = "5.4.2"
    //public static final int SERVER_VERSION2 = 1.13.6;
    /** 单位数据缓存  */
	@JvmField
	val utilData = CompressOutputStream.getGzipOutputStream("customUnits", false)

    /**  */
    const val SERVER_MAX_TRY = 3
    @JvmField
	val LINE_SEPARATOR: String = System.getProperty("line.separator")

    /** 服务端 客户端命令  */
	@JvmField
	val SERVER_COMMAND = CommandHandler("")
    @JvmField
	val LOG_COMMAND = CommandHandler("!")
    @JvmField
	val CLIENT_COMMAND = CommandHandler("/")
    @JvmField
    val RELAY_COMMAND = CommandHandler(".")

    /**  */
	@JvmField
	val MapsMap = ObjectMap<String, String>()

    @JvmField
	val core = Application()
    @JvmField
	val i18NBundleMap = ObjectMap<String, I18NBundle>(8)
    @JvmField
    val urlData: LoadIni

    lateinit var config: BaseConfig

    /**
     * 可控变量
     */
    lateinit var i18NBundle: I18NBundle
    lateinit var game: Rules

    @JvmField
    var vote: Vote? = null

    init {
        urlData = LoadIni(Data::class.java.getResourceAsStream("/URL.ini")!!)
    }
}