/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.thread.TimeTaskData;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.net.StartNet;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

/**
 * @author Dr
 */
public class NetServer {

    static String userId;

    public static void closeServer() {
        if (Data.game != null) {
            NetStaticData.startNet.each(StartNet::stop);
            //Threads.newThreadCoreNet();
            TimeTaskData.stopGameWinOrLoseCheckTask();


            Data.game.playerManage.playerGroup.clear();
            Data.game.playerManage.playerAll.clear();
            Data.game = null;
            Log.clog("Server closed");
        }
    }

    public static void reLoadServer() {
        TimeTaskData.stopGameWinOrLoseCheckTask();
        if (Data.vote!= null) {
            Data.vote.stopVote();
        }
        Call.killAllPlayer();
        Data.game.re();
        Data.game.isStartGame = false;

        synchronized (net.udp.Data.waitData) {
            net.udp.Data.waitData.notify();
        }

        FileUtil fileUtil = FileUtil.getFolder(Data.Plugin_Log_Path).toFile("Log.txt");
        fileUtil.writeFile(Log.getLogCache(), fileUtil.getFile().length() <= 1024 * 1024);

        Log.clog("Server Gameover completed");
    }
}
