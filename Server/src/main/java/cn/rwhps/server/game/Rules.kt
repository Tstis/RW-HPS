/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package cn.rwhps.server.game

import cn.rwhps.server.core.thread.Threads
import cn.rwhps.server.core.thread.Threads.newThreadService2
import cn.rwhps.server.core.thread.TimeTaskData
import cn.rwhps.server.custom.CustomEvent
import cn.rwhps.server.data.base.BaseConfig
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.data.player.PlayerManage
import cn.rwhps.server.game.GameMaps.MapData
import cn.rwhps.server.io.packet.GameCommandPacket
import cn.rwhps.server.io.packet.GameSavePacket
import cn.rwhps.server.struct.OrderedMap
import cn.rwhps.server.util.IsUtil.notIsBlank
import cn.rwhps.server.util.encryption.Base64.decodeString
import cn.rwhps.server.util.encryption.Base64.isBase64
import cn.rwhps.server.util.encryption.Sha.sha256Array
import cn.rwhps.server.util.file.FileUtil.Companion.getFolder
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.Log.clog
import cn.rwhps.server.util.log.Log.debug
import cn.rwhps.server.util.log.Log.error
import cn.rwhps.server.util.zip.zip.ZipDecoder
import java.io.File
import java.math.BigInteger
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author RW-HPS/Dr
 */
class Rules(private var config: BaseConfig) {
    /** 是否已启动游戏  */
    @JvmField
    var isStartGame = false

    /** 倍数  */
    var income: Float

    /** 初始钱  */
    var credits = 0

    /** 最大玩家  */
    var maxPlayer: Int
        private set

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



    /** 重连暂停  */
    @Volatile
    var gamePaused = false

    /** 重连缓存 GameSave  */
    @Volatile
    var gameSaveCache: GameSavePacket? = null
    var gameSaveWaitObject = Object()

    /** PlayerManage  */
    var playerManage: PlayerManage
        private set

    /** AFK  */
    var isAfk: Boolean = true
    /** Mpa Lock  */
    var mapLock: Boolean = false

    /** AD  */
    @JvmField
    val serverUpID = ""

    /* */
    var lockTeam = false
    val mapsData = OrderedMap<String, MapData>(8)

    val tickGame = AtomicInteger(10)

    init {
        try {
            checkMaps()
            clog(Data.i18NBundle.getinput("server.load.maps"))
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

        isAfk = Data.core.settings.getData("Rules.IsAfk") { isAfk }
        mapLock = Data.core.settings.getData("Rules.MapLock") { mapLock }

        Threads.addSavePool {
            Data.core.settings.setData("Rules.IsAfk",isAfk)
            Data.core.settings.setData("Rules.MapLock",mapLock)
        }
    }

    fun re() {
        this.config = Data.config

        gameCommandCache.clear()
        playerManage.cleanPlayerAllData()
        // 重置Tick
        tickGame.set(10)

        income = config.DefIncome
        val maxPlayer = config.MaxPlayer
        this.maxPlayer = maxPlayer
        playerManage = PlayerManage(maxPlayer)

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