/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.command;

import cn.rwhps.server.data.global.Data;
import cn.rwhps.server.data.global.NetStaticData;
import cn.rwhps.server.data.global.Relay;
import cn.rwhps.server.func.StrCons;
import cn.rwhps.server.net.core.server.AbstractNetConnect;
import cn.rwhps.server.util.IpUtil;
import cn.rwhps.server.util.IsUtil;
import cn.rwhps.server.util.file.FileUtil;
import cn.rwhps.server.util.game.CommandHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author RW-HPS/Dr
 */
public class LogCommands {
	public LogCommands(CommandHandler handler) {
        handler.<AbstractNetConnect>register("updata", "", (arg, con) -> {

        });

        handler.<AbstractNetConnect>register("getlog", "", (arg, con) -> {
            con.sendDebug(FileUtil.getFolder(Data.Plugin_Log_Path).toFile("Log.txt").readFileStringData());
        });

        handler.<AbstractNetConnect>register("runserver","<command>","", (arg, con) -> {
            final StringBuilder str = new StringBuilder(8);
            Data.SERVER_COMMAND.handleMessage(arg[0], (StrCons) (e) -> str.append(e).append("\n"));
            con.sendDebug(str.toString());
        });

        handler.<AbstractNetConnect>register("getspecifiedrelayip","<id>","", (arg, con) -> {
            String result;
            Relay relay = Relay.getRelay(arg[0]);
            if (IsUtil.isBlank(relay)) {
                result = "NOT RELAY ROOM";
            } else {
                result = relay.getAllIP();
            }
            con.sendDebug(result);
        });

        handler.<AbstractNetConnect>register("getallrelayip","", (arg, con) -> {
            con.sendDebug(Relay.getRelayAllIP());
        });

        handler.<AbstractNetConnect>register("getspecifiedrelayqq","<id>", (arg, con) -> {
            String result;
            Relay relay = Relay.getRelay(arg[0]);
            if (IsUtil.isBlank(relay)) {
                result = "NOT RELAY ROOM";
            } else {
                result = relay.getAdmin().getRelayPlayerQQ();
            }
            con.sendDebug(result);
        });

        handler.<AbstractNetConnect>register("sendspecifiedrelaymsg","<id> <msg...>", (arg, con) -> {
            String result;
            Relay relay = Relay.getRelay(arg[0]);
            if (IsUtil.isBlank(relay)) {
                result = "NOT RELAY ROOM";
            } else {
                relay.sendMsg(arg[1]);
                result = "OK";
            }
            con.sendDebug(result);
        });

        handler.<AbstractNetConnect>register("sendallrelaymsg","<msg...>","", (arg, con) -> {
            Relay.sendAllMsg(arg[0]);
            con.sendDebug("OK");
        });

        handler.<AbstractNetConnect>register("getsize","", (arg, con) -> {
            AtomicInteger size = new AtomicInteger();
            NetStaticData.startNet.each(e -> size.addAndGet(e.getConnectSize()));
            con.sendDebug(String.valueOf(size.get()));
            //Relay.getAllSize()
        });

        handler.<AbstractNetConnect>register("killrelay","<id>","", (arg, con) -> {
            Relay relay= Relay.getRelay(arg[0]);
            relay.groupNet.disconnect();
            relay.sendMsg("您的房间被管理员强制关闭 请勿占用公共资源");
            relay.getAdmin().disconnect();
            con.sendDebug("OK");
        });

        handler.<AbstractNetConnect>register("banrelay","<id>","", (arg, con) -> {
            Relay relay= Relay.getRelay(arg[0]);
            relay.groupNet.disconnect();
            relay.sendMsg("您被管理员Ban 请勿占用公共资源");
            String ip = relay.getAdmin().getIp();
            Data.core.admin.bannedIP24.add(IpUtil.longToIp(ip));
            relay.getAdmin().disconnect();
            con.sendDebug("OK " + ip);
        });
/*
        handler.<AbstractNetConnect>register("systeminfo","", (arg, con) -> {
            final StringBuilder str = new StringBuilder(16);
            str.append("JavaHeap  ").append(FormatUtil.formatBytes(Data.core.getJavaHeap())).append("\n");
            str.append("JavaTotalMemory  ").append(FormatUtil.formatBytes(Data.core.getJavaTotalMemory())).append("\n");
            str.append("JavaFreeMemory  ").append(FormatUtil.formatBytes(Data.core.getJavaFreeMemory())).append("\n");
            str.append("JavaVendor  ").append(Data.core.getJavaVendor()).append("\n");
            str.append("JavaVersion  ").append(Data.core.getJavaVersion()).append("\n");
            str.append("OsName  ").append(Data.core.getOsName()).append("\n");
            SystemInfo systemInfo = new SystemInfo();
            CentralProcessor computerSystem = systemInfo.getHardware().getProcessor();
            str.append("Physical CPU Package(s)  ").append(computerSystem.getPhysicalPackageCount()).append("\n");
            str.append("Physical CPU core(s)  ").append(computerSystem.getPhysicalProcessorCount()).append("\n");
            str.append("Logical CPU(s)  ").append(computerSystem.getLogicalProcessorCount()).append("\n");
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            str.append("Memory  ").append(FormatUtil.formatBytes(memory.getAvailable())).append("/").append(FormatUtil.formatBytes(memory.getTotal())).append("\n");
            VirtualMemory vm = memory.getVirtualMemory();
            str.append("Swap used  ").append(FormatUtil.formatBytes(vm.getSwapUsed())).append("/").append(FormatUtil.formatBytes(vm.getSwapTotal())).append("\n");

            //Log.error(str.toString());
            con.senddebug(str.toString());
        });

 */
    }
}