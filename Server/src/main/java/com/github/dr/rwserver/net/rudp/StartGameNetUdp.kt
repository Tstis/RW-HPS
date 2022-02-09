/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.net.rudp

import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.net.code.rudp.PacketDecoder
import com.github.dr.rwserver.net.core.ConnectionAgreement
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.threads.ThreadFactoryName.nameThreadFactory
import net.udp.ReliableServerSocket
import net.udp.ReliableSocketInputStream
import okhttp3.internal.wait
import java.io.IOException
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * RUDP
 * @property udpSocket ReliableServerSocket
 * @property rudpDataList ConcurrentHashMap<String, RudpData>
 * @property group ThreadPoolExecutor
 * @constructor
 * @author Dr
 */
internal class StartGameNetUdp(
    private val udpSocket: ReliableServerSocket
) {
    val rudpDataList = ConcurrentHashMap<String, RudpData>()

    private val work = ThreadPoolExecutor(1, 1, 1L, TimeUnit.MINUTES, LinkedBlockingQueue(), nameThreadFactory("UDP-"))
    private val group = ThreadPoolExecutor(4, 8, 1L, TimeUnit.MINUTES, LinkedBlockingQueue(), nameThreadFactory("UDP-"))

    @Throws(IOException::class)
    protected fun close() {
        work.shutdownNow()
        group.shutdownNow()
    }

    @Throws(Exception::class)
    internal fun run(socket: PackagingSocket) {
        val sockAds = socket.remoteSocketAddressString
        val type = socket.type

        if (type == null) {
            socket.type = NetStaticData.protocolData.typeConnect.getTypeConnect(ConnectionAgreement(socket))
        }

        rudpDataList[sockAds] = RudpData(socket,socket.inputStream)
    }

    /**
     * 包装一下
     * @property socket PackagingSocket
     * @property input ReliableSocketInputStream
     * @property decoder PacketDecoder
     * @property lastReadPacket Long
     * @constructor
     * @author Dr
     */
    internal class RudpData(val socket: PackagingSocket, val input: ReliableSocketInputStream) {
        val decoder = PacketDecoder(socket)
    }

    init {
        work.execute {
            while (!udpSocket.isClosed) {
                synchronized (net.udp.Data.waitData) {
                    rudpDataList.values.forEach {
                        /*
                         * 如果这个Socket断开 或者 这个Socket已经超过checkTimeoutDetection内的时间
                         * 那么 就判定这个Socket已经断开了 服务器主动执行断开 释放资源
                         */
                        if (it.socket.isClosed || TimeoutDetection.checkTimeoutDetection(it.socket.type!!.abstractNetConnect)) {
                            Log.debug("RUDP Close")
                            it.socket.type!!.abstractNetConnect.disconnect()
                            rudpDataList.remove(it.socket.remoteSocketAddressString)
                            return@forEach
                        }

                        try {
                            // 尝试读取Stream数据
                            val length = it.input.readsDataLength

                            /*
                             * 如果读取到了数据
                             * 那么把它写入解码器 由解码器尝试解码
                             */
                            if (length > 0) {
                                it.decoder.decode(it.input.readsData,length,group)
                            }
                        } catch (e : SocketException) {
                            Log.debug("RUDP Close")
                            it.socket.type!!.abstractNetConnect.disconnect()
                            rudpDataList.remove(it.socket.remoteSocketAddressString)
                        }
                    }
                    net.udp.Data.waitData.wait();
                }
            }

        }
    }
}