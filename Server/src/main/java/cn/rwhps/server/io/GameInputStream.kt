/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.io

import cn.rwhps.server.io.input.DisableSyncByteArrayInputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.util.io.CompressInputStream
import java.io.Closeable
import java.io.DataInputStream
import java.io.IOException
import java.io.OutputStream

/**
 * @author Dr
 */
class GameInputStream : Closeable {
    private val buffer: DisableSyncByteArrayInputStream
    private val stream: DataInputStream

    internal constructor(buffer: DisableSyncByteArrayInputStream) {
        this.buffer = buffer
        this.stream = DataInputStream(buffer)
    }
    
    constructor(packet: Packet) {
        this.buffer = DisableSyncByteArrayInputStream(packet.bytes)
        this.stream = DataInputStream(buffer)
    }

    constructor(bytes: ByteArray) {
        this.buffer = DisableSyncByteArrayInputStream(bytes)
        this.stream = DataInputStream(buffer)
    }
    /**
     * Read a Byte (1 byte)
     * @return Byte
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readByte(): Int {
        return stream.readByte().toInt()
    }

    /**
     * Read a Boolean (1 byte)
     * @return Boolean
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readBoolean(): Boolean {
        return stream.readBoolean()
    }

    /**
     * Read an Int (4 byte)
     * @return Int
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readInt(): Int {
        return stream.readInt()
    }

    /**
     * Read a Short (2 byte)
     * @return Short
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readShort(): Short {
        return stream.readShort()
    }

    /**
     * Read a Float (4 byte)
     * @return Float
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readFloat(): Float {
        return stream.readFloat()
    }

    /**
     * Read a Long (8 byte)
     * @return Long
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readLong(): Long {
        return stream.readLong()
    }

    /**
     * Read a String
     * Length 2 byte
     * Data X byte
     * @return String
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readString(): String {
        return stream.readUTF()
    }

    /**
     * Judge and read a String
     * Boolean (1 bytes)
     *    - True :
     *        - Length 2 byte
     *        - Data X byte
     *    -False
     *        - ""
     * @return String
     * @throws IOException
     */
    @Throws(IOException::class)
    fun isReadString(): String {
        return if (readBoolean()) readString() else ""
    }

    /**
     * Skip the specified length
     * @param skip length
     * @throws IOException
     */
    @Throws(IOException::class)
    fun skip(skip: Long) {
        this.buffer.skip(skip)
    }

    /**
     * Read bytes of specified length
     * @param readBytesLength length
     * @return ByteArray
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readNBytes(readBytesLength: Int): ByteArray {
        return this.buffer.readNBytes0(readBytesLength)
    }

    /**
     * Read all the remaining bytes
     * @return ByteArray
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readAllBytes(): ByteArray {
        return this.buffer.readAllBytes0()
    }

    @Throws(IOException::class)
    fun readStreamBytes(): ByteArray {
        return buffer.readNBytes0(readInt())
    }

    @Throws(IOException::class)
    fun readStreamBytesNew(): ByteArray {
        readInt() // Relay type
        return readStreamBytes()
    }

    @Throws(IOException::class)
    fun readEnum(clazz: Class<*>): Enum<*>? {
        return clazz.enumConstants[readInt()] as Enum<*>
    }

    /**
     * Write this stream from the current position to the new stream
     * @param out OutputStream
     * @throws IOException
     */
    @Throws(IOException::class)
    fun transferTo(out: OutputStream) {
        this.buffer.transferTo0(out)
    }

    /**
     * Write this stream from the current position to the specified length to the new stream
     * @param out OutputStream
     * @throws IOException
     */
    @Throws(IOException::class)
    fun transferToFixedLength(out: OutputStream, length: Int) {
        this.buffer.transferToFixedLength(out,length)
    }

    @Throws(IOException::class)
    fun getDecodeStream(bl: Boolean): GameInputStream {
        readString()
        val bytes = readStreamBytes()
        return CompressInputStream.getGzipInputStream(bl, bytes)
    }

    @Throws(IOException::class)
    fun getStream(): GameInputStream {
        val bytes = readStreamBytesNew()
        return CompressInputStream.getGzipInputStream(false, bytes)
    }

    @Throws(IOException::class)
    fun getDecodeBytes(): ByteArray {
        readString()
        return readStreamBytes()
    }

    override fun toString(): String {
        return "GameInputStream{" +
                "buffer=" + buffer +
                ", stream=" + stream +
                '}'
    }

    @Throws(IOException::class)
    override fun close() {
        buffer.close()
        stream.close()
    }
}