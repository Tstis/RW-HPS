/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.util.log.Log.warn
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.util.ReferenceCountUtil

/**
 * Parse game packets
 * @author Dr
 */
/**
 *    1 2 3 4  5  6  7  8  ...
 *   +-+-+-+-+-+-+-+-+---------------+
 *   |0|0|0|0| 0| 0| 0| 0| Data|
 *   +-+-+-+-+-+-+-+-+---------------+
 *   |  Type |Data length| Data
 *   +---------------+---------------+
 */
internal class PacketDecoder : ByteToMessageDecoder() {
    companion object {
        /** Packet header data length */
        private const val HEADER_SIZE = 8
        /** Maximum accepted single package size */
        private const val MAX_CONTENT_LENGTH = 52428800
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, bufferIn: ByteBuf?, out: MutableList<Any>) {
        if (bufferIn == null) {
            return
        }

        val readableBytes = bufferIn.readableBytes()
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
            warn("Package size exceeds maximum")
            ReferenceCountUtil.release(bufferIn)
            /*
            NetStaticData.blackList.addBlackList(ip)
            debug("Add BlackList", ip)
             */
            ctx.close()
            return
        }
        val readerIndex = bufferIn.readerIndex()
        val contentLength = bufferIn.readInt()
        val type = bufferIn.readInt()
        /*
         * Insufficient data length, reset the identification bit and read again
         */
        if (bufferIn.readableBytes() < contentLength) {
            bufferIn.readerIndex(readerIndex)
            return
        }
        val b = ByteArray(contentLength)
        bufferIn.readBytes(b)
        out.add(Packet(type, b))
    }
}