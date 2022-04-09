/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.handler.rudp

import cn.rwhps.server.core.thread.Threads
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.net.code.rudp.PacketDecoder
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.util.log.Log
import net.udp.ReliableServerSocket
import net.udp.ReliableSocketInputStream
import okhttp3.internal.wait
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap

/**
 * RUDP
 * @property udpSocket ReliableServerSocket
 * @property rudpDataList ConcurrentHashMap<String, RudpData>
 * @constructor
 * @author Dr
 */
internal class StartGameNetUdp(
    private val udpSocket: ReliableServerSocket
) {
    private val rudpDataList = ConcurrentHashMap<String, RudpData>()

    @Throws(Exception::class)
    internal fun run(socket: PackagingSocket) {
        val sockAds = socket.remoteSocketAddressString
        val type = socket.type

        if (type == null) {
            socket.type = NetStaticData.RwHps.typeConnect.getTypeConnect(ConnectionAgreement(socket))
        }

        rudpDataList[sockAds] = RudpData(socket,socket.inputStream)
    }

    /**
     * wrap it up
     * @property socket PackagingSocket
     * @property input ReliableSocketInputStream
     * @property decoder PacketDecoder
     * @constructor
     * @author Dr
     */
    internal class RudpData(val socket: PackagingSocket, val input: ReliableSocketInputStream) {
        val decoder = PacketDecoder(socket)
    }

    init {
        Threads.newThreadCoreNet {
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
                            // Attempt to read Stream data
                            val length = it.input.readsDataLength

                            /*
                             * If data is read
                             * then write it to the decoder and the decoder will try to decode it
                             */
                            if (length > 0) {
                                it.decoder.decode(it.input.readsData,length)
                            }
                        } catch (e : SocketException) {
                            Log.debug("RUDP Close")
                            it.socket.type!!.abstractNetConnect.disconnect()
                            rudpDataList.remove(it.socket.remoteSocketAddressString)
                        }
                    }
                    net.udp.Data.waitData.wait()
                }
            }

        }
    }
}