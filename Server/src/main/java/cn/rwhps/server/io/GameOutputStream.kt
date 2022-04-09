/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.io

import cn.rwhps.server.io.output.CompressOutputStream
import cn.rwhps.server.io.output.DisableSyncByteArrayOutputStream
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.util.IsUtil
import java.io.DataOutputStream
import java.io.IOException

/**
 * @author Dr
 */
open class GameOutputStream @JvmOverloads constructor(private var buffer: DisableSyncByteArrayOutputStream = DisableSyncByteArrayOutputStream()) {

    private var stream: DataOutputStream = DataOutputStream(buffer)

    fun createPacket(type: Int): Packet {
        try {
            stream.use { buffer.use {
                stream.flush()
                buffer.flush()
                return Packet(type, buffer.toByteArray())
            }}
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun getPacketBytes(): ByteArray {
        try {
            stream.use { buffer.use {
                stream.flush()
                buffer.flush()
                return buffer.toByteArray()
            }}
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun getByteArray(): ByteArray {
        stream.flush()
        buffer.flush()
        return buffer.toByteArray()
    }

    fun size(): Int {
        return this.buffer.size()
    }

    @Throws(IOException::class)
    fun writeByte(value: Int) {
        stream.writeByte(value)
    }

    @Throws(IOException::class)
    fun writeBytes(value: ByteArray) {
        buffer.writeBytes(value)
    }

    @Throws(IOException::class)
    fun writeBoolean(value: Boolean) {
        stream.writeBoolean(value)
    }

    @Throws(IOException::class)
    fun writeInt(value: Int) {
        stream.writeInt(value)
    }

    @Throws(IOException::class)
    fun writeShort(value: Short) {
        stream.writeShort(value.toInt())
    }

    @Throws(IOException::class)
    fun writeFloat(value: Int) {
        stream.writeFloat(value.toFloat())
    }
    @Throws(IOException::class)
    fun writeFloat(value: Float) {
        stream.writeFloat(value)
    }

    @Throws(IOException::class)
    fun writeLong(value: Long) {
        stream.writeLong(value)
    }

    @Throws(IOException::class)
    fun writeString(value: String) {
        stream.writeUTF(value)
    }

    @Throws(IOException::class)
    fun writeIsString(value: String?) {
        if (IsUtil.isBlank(value)) {
            writeBoolean(false)
        } else {
            writeBoolean(true)
            writeString(value!!)
        }
    }

    @Throws(IOException::class)
    fun writeIsString(gameInputStream: GameInputStream) {
        if (gameInputStream.readBoolean()) {
            writeBoolean(true)
            writeString(gameInputStream.readString())
        } else {
            writeBoolean(false)
        }
    }

    /*
    @Throws(IOException::class)
    fun writeGameObject(value: GameObject) {
        if (value == null) {
            stream.writeLong(-1L)
        } else {
            stream.writeLong(value.id)
        }
    }

    @Throws(IOException::class)
    fun writeUnit(value: Unit) {
        writeGameObject(value)
    }

    @Throws(IOException::class)
    fun writeOrderableUnit(value: OrderableUnit) {
        writeGameObject(value)
    }
    */

    @Throws(IOException::class)
    fun writeEnum(value: Enum<*>?) {
        if (value == null) {
            stream.writeInt(-1)
        } else {
            stream.writeInt(value.ordinal)
        }
    }
    
    @Throws(IOException::class)
    fun transferTo(inputStream: GameInputStream) {
        inputStream.transferTo(buffer)
    }

    @Throws(IOException::class)
    fun transferToFixedLength(inputStream: GameInputStream, length: Int) {
        inputStream.transferToFixedLength(buffer,length)
    }

    @Throws(IOException::class)
    fun flushEncodeData(enc: CompressOutputStream) {
        val bytes = enc.getByteArray()
        writeString(enc.head)
        writeInt(bytes.size)
        writeBytes(bytes)
    }

    @Throws(IOException::class)
    fun flushMapData(mapSize: Int, bytes: ByteArray) {
        writeInt(mapSize)
        stream.write(bytes)
    }

    fun reset() {
        this.buffer.reset()
    }
}