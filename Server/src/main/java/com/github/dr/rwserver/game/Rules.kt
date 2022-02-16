/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.game

import com.github.dr.rwserver.core.thread.Threads.newThreadService2
import com.github.dr.rwserver.core.thread.TimeTaskData
import com.github.dr.rwserver.custom.CustomEvent
import com.github.dr.rwserver.data.base.BaseConfig
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.data.player.PlayerManage
import com.github.dr.rwserver.game.GameMaps.MapData
import com.github.dr.rwserver.io.packet.GameCommandPacket
import com.github.dr.rwserver.io.packet.GameSavePacket
import com.github.dr.rwserver.struct.OrderedMap
import com.github.dr.rwserver.util.IsUtil.notIsBlank
import com.github.dr.rwserver.util.encryption.Base64.decodeString
import com.github.dr.rwserver.util.encryption.Base64.isBase64
import com.github.dr.rwserver.util.encryption.Sha.sha256Array
import com.github.dr.rwserver.util.file.FileUtil.Companion.getFolder
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.log.Log.clog
import com.github.dr.rwserver.util.log.Log.debug
import com.github.dr.rwserver.util.log.Log.error
import com.github.dr.rwserver.util.zip.zip.ZipDecoder
import java.io.File
import java.math.BigInteger
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * @author Dr
 */
class Rules(config: BaseConfig) {
    /** 是否已启动游戏  */
    @JvmField
    var isStartGame = false

    /** 倍数  */
    var income: Float

    /** 初始钱  */
    var credits = 0

    /** 最大玩家  */
    @JvmField
    var maxPlayer: Int

    /** 地图数据  */
    val maps = GameMaps()

    /** nukes  */
    var noNukes = false

    /** 初始单位  */
    var initUnit = 1

    /** 迷雾  */
    var mist = 2

    /** 共享控制  */
    var sharedControl = false

    /** 密码  */
    @JvmField
    val passwd: String = if (notIsBlank(Data.config.Passwd)) BigInteger(1, sha256Array(Data.config.Passwd)).toString(16).uppercase() else ""

    /** 按键包缓存  */
    val gameCommandCache = LinkedBlockingQueue<GameCommandPacket>()

    /** AFK  */
    var isAfk = true

    /** 重连暂停  */
    @Volatile
    var gamePaused = false

    /** 重连缓存 GameSave  */
    @Volatile
    var gameSaveCache: GameSavePacket? = null

    /** PlayerManage  */
    @JvmField
    val playerManage: PlayerManage

    /** Mpa Lock  */
    var mapLock = false

    /** AD  */
    @JvmField
    val serverUpID = ""

    /* */
    var lockTeam = false
    val mapsData = OrderedMap<String, MapData>(8)

    init {
        try {
            checkMaps()
            clog(Data.localeUtil.getinput("server.load.maps"))
        } catch (exp: Exception) {
            debug("Read Error", exp)
        }
        NetStaticData.relay.isMod = config.SingleUserRelayMod
        autoLoadOrUpdate(config)
        val maxPlayer = config.MaxPlayer
        this.maxPlayer = maxPlayer
        playerManage = PlayerManage(maxPlayer)
        income = Data.config.DefIncome

        if (maxPlayer > 100) {
            Log.skipping("[WARN !]","The number of players is too large and the game cannot be played normally")
        }
    }

    fun init() {
        CustomEvent()
    }

    fun re() {
        gameCommandCache.clear()
        playerManage.cleanPlayerAllData()
        income = Data.config.DefIncome
        initUnit = 1
        mist = 2
        sharedControl = false
        gameSaveCache = null
        gamePaused = false
    }

    fun checkMaps() {
        val list = getFolder(Data.Plugin_Maps_Path).fileListNotNullSizeSort
        list.each { e: File ->
            val original = if (isBase64(e.name)) decodeString(e.name) else e.name
            val postpone = original.substring(original.lastIndexOf("."))
            val name = original.substring(0, original.length - postpone.length)
            when (postpone) {
                ".tmx" ->   try {
                                mapsData.put(name, MapData(GameMaps.MapType.customMap, GameMaps.MapFileType.file, name))
                            } catch (exception: Exception) {
                                error("read tmx Maps", exception)
                            }
                ".save" ->  try {
                                mapsData.put(name, MapData(GameMaps.MapType.savedGames, GameMaps.MapFileType.file, name))
                            } catch (exception: Exception) {
                                error("read save Maps", exception)
                            }
                ".zip" ->   try {
                                val zipTmx = ZipDecoder(e).getTheFileNameOfTheSpecifiedSuffixInTheZip("tmx")
                                zipTmx.each { zipMapName: String -> mapsData.put(zipMapName, MapData(GameMaps.MapType.customMap, GameMaps.MapFileType.zip, zipMapName, original)) }
                                val zipSave = ZipDecoder(e).getTheFileNameOfTheSpecifiedSuffixInTheZip("save")
                                zipSave.each { zipSaveName: String -> mapsData.put(zipSaveName, MapData(GameMaps.MapType.savedGames, GameMaps.MapFileType.zip, zipSaveName, original)) }
                            } catch (exception: Exception) {
                                error("ZIP READ", exception)
                            }
                else -> {}
            }
        }
    }

    private fun autoLoadOrUpdate(config: BaseConfig) {
        if (config.AutoReLoadMap) {
            TimeTaskData.AutoReLoadMapTask = newThreadService2({
                if (notIsBlank(Data.game) && !Data.game.isStartGame) {
                    Data.game.mapsData.clear()
                    Data.game.checkMaps()
                }
            }, 0, 1, TimeUnit.MINUTES)
        }
    }
}