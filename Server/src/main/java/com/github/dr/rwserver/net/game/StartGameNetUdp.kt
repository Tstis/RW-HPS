/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.util.log.Log.error
import com.github.dr.rwserver.util.threads.ThreadFactoryName.nameThreadFactory
import net.udp.ReliableServerSocket.ReliableClientSocket
import java.io.DataInputStream
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class StartGameNetUdp(
    private val startNet: StartNet
) {
    private val group = ThreadPoolExecutor(4, Int.MAX_VALUE, 1L, TimeUnit.MINUTES, LinkedBlockingQueue(), nameThreadFactory("UDP-"))
    private val timeoutDetection: TimeoutDetection = TimeoutDetection(5, startNet)


    @Throws(IOException::class)
    protected fun close() {
        group.shutdownNow()
    }

    @Throws(Exception::class)
    internal fun run(socket: ReliableClientSocket) {
        val typeConnect: TypeConnect = NetStaticData.protocolData.typeConnect
        val sockAds = socket.remoteSocketAddress
        var type = startNet.OVER_MAP[sockAds.toString()]
        if (type == null) {
            type = typeConnect.getTypeConnect(ConnectionAgreement(socket, startNet))
            startNet.OVER_MAP.put(sockAds.toString(), type)
        }
        val typeFinal = type
        group.execute {
            while (!socket.isClosed) {
                try {
                    val `in` = DataInputStream(socket.inputStream)
                    val size = `in`.readInt()
                    val typeInt = `in`.readInt()
                    val bytes = ByteArray(size)
                    var bytesRead = 0
                    while (bytesRead < size) {
                        val readIn = `in`.read(bytes, bytesRead, size - bytesRead)
                        if (readIn == -1) {
                            break
                        }
                        bytesRead += readIn
                    }
                    typeFinal.typeConnect(Packet(typeInt, bytes))
                } catch (e: Exception) {
                    error("UDP READ", e)
                    typeFinal.abstractNetConnect.disconnect()
                    startNet.OVER_MAP.remove(sockAds.toString())
                    return@execute
                }
            }
            typeFinal.abstractNetConnect.disconnect()
            startNet.OVER_MAP.remove(sockAds.toString())
        }
    }

}