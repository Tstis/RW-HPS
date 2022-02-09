/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.game;

import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.core.thread.TimeTaskData;
import com.github.dr.rwserver.custom.CustomEvent;
import com.github.dr.rwserver.data.base.BaseConfig;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.data.player.PlayerManage;
import com.github.dr.rwserver.io.packet.GameCommandPacket;
import com.github.dr.rwserver.io.packet.GameSavePacket;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.encryption.Base64;
import com.github.dr.rwserver.util.encryption.Sha;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.zip.ZipDecoder;

import java.io.File;
import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Dr
 */
public class Rules {
    /** 是否已启动游戏 */
    public boolean isStartGame = false;
    /** 倍数 */
    public float income;
    /** 初始钱 */
    public int credits = 0;
    /** 最大玩家 */
    public int maxPlayer;
    /** 地图数据 */
    public final GameMaps maps = new GameMaps();
    /** nukes */
    public boolean noNukes = false;
    /** 初始单位 */
    public int initUnit = 1;
    /** 迷雾 */
    public int mist = 2;
    /** 共享控制 */
    public boolean sharedControl = false;
    /** 密码 */
    public final String passwd;
    /** 按键包缓存 */
    public final LinkedBlockingQueue<GameCommandPacket> gameCommandCache = new LinkedBlockingQueue<>();
    /** AFK */
    public boolean isAfk = true;
    /** 重连暂停 */
    public volatile boolean gamePaused = false;
    /** 重连缓存 GameSave */
    public volatile GameSavePacket gameSaveCache = null;
    /** PlayerManage */
    public final PlayerManage playerManage;
    /** Mpa Lock */
    public boolean mapLock = false;

    /** AD */
    public final String serverUpID = "";

    /* */
    public boolean lockTeam = false;

    public final OrderedMap<String,GameMaps.MapData> mapsData = new OrderedMap<>(8);

    public Rules(BaseConfig config) {
        passwd = IsUtil.notIsBlank(Data.config.getPasswd()) ? new BigInteger(1, Sha.sha256Array(Data.config.getPasswd())).toString(16).toUpperCase(Locale.ROOT) : "";

        try {
            checkMaps();
            Log.clog(Data.localeUtil.getinput("server.load.maps"));
        } catch (Exception exp) {
            Log.debug("Read Error",exp);
        }

        NetStaticData.relay.setMod(config.getSingleUserRelayMod());

        autoLoadOrUpdate(config);

        int maxPlayer = config.getMaxPlayer();
        this.maxPlayer = maxPlayer;
        this.playerManage = new PlayerManage(maxPlayer);
        income = Data.config.getDefIncome();
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public void init() {
        new CustomEvent();
    }

    public void re() {
        gameCommandCache.clear();
        playerManage.cleanPlayerAllData();
        income = Data.config.getDefIncome();
        initUnit = 1;
        mist = 2;
        sharedControl = false;

        gameSaveCache = null;
        gamePaused = false;
    }

    public void checkMaps() {
        Seq<File> list = FileUtil.getFolder(Data.Plugin_Maps_Path).getFileListNotNullSizeSort();
        list.each(e -> {
            final String original = Base64.isBase64(e.getName()) ? Base64.decodeString(e.getName()) : e.getName();
            final String postpone = original.substring(original.lastIndexOf("."));
            final String name = original.substring(0, original.length()-postpone.length());
            switch (postpone) {
                case ".tmx":
                    try {
                        mapsData.put(name,new GameMaps.MapData(GameMaps.MapType.customMap, GameMaps.MapFileType.file, name));
                    } catch (Exception exception) {
                        Log.error("read tmx Maps",exception);
                    }
                    break;
                case ".save":
                    try {
                        mapsData.put(name,new GameMaps.MapData(GameMaps.MapType.savedGames, GameMaps.MapFileType.file, name));
                    } catch (Exception exception) {
                        Log.error("read save Maps",exception);
                    }
                    break;
                case ".zip":
                    try {
                        Seq<String> zipTmx = new ZipDecoder(e).getTheFileNameOfTheSpecifiedSuffixInTheZip("tmx");
                        zipTmx.each(zipMapName -> mapsData.put(zipMapName,new GameMaps.MapData(GameMaps.MapType.customMap, GameMaps.MapFileType.zip , zipMapName, original)));
                        Seq<String> zipSave = new ZipDecoder(e).getTheFileNameOfTheSpecifiedSuffixInTheZip("save");
                        zipSave.each(zipSaveName -> mapsData.put(zipSaveName,new GameMaps.MapData(GameMaps.MapType.savedGames, GameMaps.MapFileType.zip , zipSaveName, original)));

                    } catch (Exception exception) {
                        Log.error("ZIP READ",exception);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    private void autoLoadOrUpdate(BaseConfig config) {
        if (config.getAutoReLoadMap()) {
            TimeTaskData.AutoReLoadMapTask = Threads.newThreadService2(() -> {
                if (IsUtil.notIsBlank(Data.game) && !Data.game.isStartGame) {
                    Data.game.mapsData.clear();
                    Data.game.checkMaps();
                }
            },0,1, TimeUnit.MINUTES);
        }
    }
}
