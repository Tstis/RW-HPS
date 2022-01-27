/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.net.game.StartNet;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.util.concurrent.TimeUnit;

/**
 * @author Dr
 */
public class NetServer {

    static String userId;

    public static void closeServer() {
        if (Data.game != null) {
            NetStaticData.startNet.each(StartNet::stop);
            Threads.removeScheduledFutureData("GamePing");
            Threads.removeScheduledFutureData("GameTeam");
            Data.game.playerManage.playerGroup.clear();
            Data.game.playerManage.playerAll.clear();
            Data.game = null;
            Log.clog("Server closed");
        }
    }

    public static void reLoadServer() {
        Threads.removeScheduledFutureData("GamePing");
        Threads.removeScheduledFutureData("GameWinOrLoseCheck");
        Threads.removeScheduledFutureData("Gameover");
        Call.killAllPlayer();
        Data.game.playerManage.playerGroup.clear();
        Data.game.playerManage.playerAll.clear();
        Data.game.re();
        Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS,"GamePing");
        Data.game.isStartGame = false;
        FileUtil fileUtil = FileUtil.getFolder(Data.Plugin_Log_Path).toFile("Log.txt");
        fileUtil.writeFile(Log.getLogCache(), fileUtil.getFile().length() <= 1024 * 1024);

        Log.clog("Server Gameover completed");
    }
}
