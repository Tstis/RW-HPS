/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.code.rudp

import com.github.dr.rwserver.io.input.ClearableAndReusableDisableSyncByteArrayInputStream
import com.github.dr.rwserver.io.packet.Packet
import com.github.dr.rwserver.net.rudp.PackagingSocket
import com.github.dr.rwserver.util.log.Log
import java.io.EOFException
import java.io.IOException
import java.util.concurrent.ThreadPoolExecutor

internal class PacketDecoder(private val socket: PackagingSocket) {
    companion object {
        /** Packet header data length */
        private const val HEADER_SIZE = 8
        /** Maximum accepted single package size */
        private const val MAX_CONTENT_LENGTH = 52428800
    }

    private val inputStream = ClearableAndReusableDisableSyncByteArrayInputStream()

    @Throws(Exception::class)
    fun decode(bytes: ByteArray, length: Int,group: ThreadPoolExecutor) {
        inputStream.addBytes(bytes,length)

        val readableBytes = inputStream.count()
        if (readableBytes < HEADER_SIZE) {
            return
        }

        /*
        val addSock = ctx.channel().remoteAddress().toString()
        val ip = addSock.substring(1, addSock.indexOf(':'))
        if (NetStaticData.blackList.containsBlackList(ip)) {
            error("Black")
            ReferenceCountUtil.release(bufferIn)
            ctx.close()
        }
        */

        /*
         * Someone may be sending a lot of packets to the server to take up broadband
         * Close the connection directly by default
         *
         * Maximum accepted single package size = 50 MB
         */
        if (readableBytes > MAX_CONTENT_LENGTH) {
            Log.warn("Package size exceeds maximum")
            inputStream.close()
            /*
            NetStaticData.blackList.addBlackList(ip)
            debug("Add BlackList", ip)
             */
            socket.close()
            return
        }

        inputStream.mark()

        val contentLength = readInt()
        val type = readInt()

        /*
         * Insufficient data length, reset the identification bit and read again
         */
        if (readableBytes < contentLength) {
            inputStream.reset()
            return
        }

        val packet = Packet(type,inputStream.readNBytes(contentLength))

        inputStream.removeOldRead()

        group.execute { this.socket.type!!.typeConnect(packet) }
    }

    @Throws(IOException::class)
    private fun readInt(): Int {
        val ch1: Int = inputStream.read()
        val ch2: Int = inputStream.read()
        val ch3: Int = inputStream.read()
        val ch4: Int = inputStream.read()
        if (ch1 or ch2 or ch3 or ch4 < 0) throw EOFException()
        return (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + (ch4 shl 0)
    }
}