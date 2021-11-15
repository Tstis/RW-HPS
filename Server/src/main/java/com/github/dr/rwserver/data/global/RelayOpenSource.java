/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.data.global;

import com.github.dr.rwserver.math.Rand;
import com.github.dr.rwserver.net.GroupNet;
import com.github.dr.rwserver.net.core.server.AbstractNetConnect;
import com.github.dr.rwserver.struct.IntMap;
import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.log.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR;

public class RelayOpenSource {
    private static final IntMap<RelayOpenSource> serverRelayData = new IntMap<>();
    /** */
    public final GroupNet groupNet;
    public final IntMap<AbstractNetConnect> abstractNetConnectIntMap = new IntMap<>();
    private final String serverUuid = UUID.randomUUID().toString();
    private AbstractNetConnect admin = null;
    private final AtomicInteger site = new AtomicInteger(0);
    private final AtomicInteger size = new AtomicInteger();
    private boolean isStartGame = false;
    private boolean closeRoom = false;
    private final String id;
    private boolean mod = false;
    private int minSize = 1;
    private static final Rand rand = new Rand();

    public RelayOpenSource(long a) {
        String stringId;
        while (true) {
            int intIdNumber = rand.random(100,199);
            String strId = "CLR" + Integer.toString(intIdNumber);
            if (!serverRelayData.containsKey(strId)) {
                serverRelayData.put(strId,this);
                stringId = strId;
                Log.debug(strId);
                break;
            }
        }
        this.id = stringId;
        groupNet = new GroupNet(Time.nanos());
    }

    public RelayOpenSource(long a, String id) {
        serverRelayData.put(Integer.parseInt(id),this);
        this.id = id;
        groupNet = new GroupNet(Time.nanos());
    }

    public RelayOpenSource(String id) {
        this.id = id;
        groupNet = NetStaticData.groupNet;
    }

    public void re() {
        abstractNetConnectIntMap.clear();
        site.set(0);
        setAdmin(null);
        isStartGame = false;
        if (IsUtil.isNumeric(id)) {
            serverRelayData.remove(Integer.parseInt(id));
        }
    }

    public AbstractNetConnect getAbstractNetConnect(int site) {
        return abstractNetConnectIntMap.get(site);
    }

    public AbstractNetConnect setAbstractNetConnect(AbstractNetConnect abstractNetConnect) {
        return abstractNetConnectIntMap.put(site.get(),abstractNetConnect);
    }

    public String getAllIP() {
        final StringBuilder str = new StringBuilder(10);
        str.append(LINE_SEPARATOR)
                .append(admin.getName())
                .append(" / ")
                .append("IP: ").append(admin.getIp())
                .append(" / ")
                .append("Protocol: ").append(admin.getConnectionAgreement())
                .append(" / ")
                .append("Admin: true");
        abstractNetConnectIntMap.values().forEach(e -> str.append(LINE_SEPARATOR)
                .append(e.getName())
                .append(" / ")
                .append("IP: ").append(e.getIp())
                .append(" / ")
                .append("Protocol: ").append(e.getConnectionAgreement())
                .append(" / ")
                .append("Admin: false"));
        return str.toString();
    }

    public void sendMsg(String msg) {
        try {

            admin.sendPacket(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(msg));
            groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(msg),null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void removeAbstractNetConnect(int site) {
        abstractNetConnectIntMap.remove(site);
    }

    public AbstractNetConnect getAdmin() {
        return admin;
    }

    public void setAdmin(AbstractNetConnect admin) {
        this.admin = admin;
    }

    public int getActiveConnectionSize() {
        return abstractNetConnectIntMap.size;
    }

    public int getSite() {
        return site.get();
    }

    public void setAddSite() {
        site.incrementAndGet();
    }

    public void setRemoveSite() {
        site.decrementAndGet();
    }

    public int getSize() {
        return size.get();
    }

    public void setAddSize() {
        size.incrementAndGet();
    }

    public void setRemoveSize() {
        size.decrementAndGet();
    }


    public boolean isStartGame() {
        return isStartGame;
    }

    public void setStartGame(boolean startGame) {
        if (isStartGame) {
            return;
        }
        isStartGame = startGame;
    }

    public AbstractNetConnect getRandAdmin() {
        return abstractNetConnectIntMap.values().toArray().random();
    }

    public String getId() {
        return id;
    }

    public boolean isMod() {
        return mod;
    }

    public void setMod(boolean mod) {
        this.mod = mod;
    }

    public int getMinSize() {
        return minSize;
    }

    public void updateMinSize() {
        try {
            minSize = Arrays.stream(abstractNetConnectIntMap.keys().toArray().toArray()).min().getAsInt();
        } catch (Exception e) {}
    }
    //return NetStaticData.groupNet

    public static RelayOpenSource getRelay(String id) {
        return serverRelayData.get(Integer.parseInt(id));
    }

    public final String getServerUuid() {
        return serverUuid;
    }

    public static String getRelayAllIP() {
        final StringBuilder str = new StringBuilder(10);
        serverRelayData.values().forEach(e -> str.append(LINE_SEPARATOR)
                .append(e.id)
                .append(e.getAllIP()));
        return str.toString();
    }

    public static void sendAllMsg(String msg) {
        serverRelayData.values().forEach(e -> e.sendMsg(msg));
    }

    public static int getAllSize() {
        final AtomicInteger size = new AtomicInteger();
        serverRelayData.values().forEach(e -> size.getAndAdd(e.getSize()));
        return size.get();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelayOpenSource relay = (RelayOpenSource) o;
        return id.equals(relay.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean getCloseRoom() {
        return closeRoom;
    }

    public void setCloseRoom(boolean closeRoom) {
        this.closeRoom = closeRoom;
    }
}
