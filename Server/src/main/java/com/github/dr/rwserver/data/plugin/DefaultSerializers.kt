/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.data.plugin

import com.github.dr.rwserver.net.Administration.PlayerAdminInfo
import com.github.dr.rwserver.net.Administration.PlayerInfo
import com.github.dr.rwserver.struct.ObjectMap
import com.github.dr.rwserver.struct.OrderedMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.struct.SerializerTypeAll.TypeSerializer
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * @author Dr
 */
internal object DefaultSerializers {
    @JvmStatic
    fun register() {
        registrationBasis()
        registerMap()
        registerCustomClass()
    }

    private fun registrationBasis() {
        AbstractPluginData.setSerializer(String::class.java, object : TypeSerializer<String> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, objectData: String?) {
                stream.writeUTF(objectData ?: "")
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): String {
                return stream.readUTF()
            }
        })
        AbstractPluginData.setSerializer(Int::class.java, object : TypeSerializer<Int> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, objectData: Int) {
                stream.writeInt(objectData)
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): Int {
                return stream.readInt()
            }
        })
        AbstractPluginData.setSerializer(Float::class.java, object : TypeSerializer<Float> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, objectData: Float) {
                stream.writeFloat(objectData)
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): Float {
                return stream.readFloat()
            }
        })
        AbstractPluginData.setSerializer(Long::class.java, object : TypeSerializer<Long> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, objectData: Long) {
                stream.writeLong(objectData)
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): Long {
                return stream.readLong()
            }
        })
        AbstractPluginData.setSerializer(Boolean::class.java, object : TypeSerializer<Boolean> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, objectData: Boolean) {
                stream.writeBoolean(objectData)
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): Boolean {
                return stream.readBoolean()
            }
        })
    }

    private fun registerMap() {
        AbstractPluginData.setSerializer(Seq::class.java, object : TypeSerializer<Seq<*>> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, objectData: Seq<*>) {
                stream.writeInt(objectData.size())
                if (objectData.size() != 0) {
                    val ser = AbstractPluginData.getSerializer(objectData[0].javaClass)
                        ?: throw IllegalArgumentException(objectData[0].javaClass.toString() + " does not have a serializer registered!")
                    stream.writeUTF(objectData[0].javaClass.name)
                    for (element in objectData) {
                        ser.write(stream, element)
                    }
                }
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): Seq<*>? {
                 return try {
                    val size = stream.readInt()
                    val arr = Seq<Any>(size)
                    if (size == 0) {
                        return arr
                    }
                    val type = stream.readUTF()
                    val ser = AbstractPluginData.getSerializer(lookup(type))
                     requireNotNull(ser) {
                         "$type does not have a serializer registered!"
                     }

                     for (i in 0 until size) {
                        arr.add(ser.read(stream))
                    }
                    arr
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    null
                }
            }
        })
        AbstractPluginData.setSerializer(ObjectMap::class.java, object : TypeSerializer<ObjectMap<*, *>> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, map: ObjectMap<*, *>) {
                stream.writeInt(map.size)
                if (map.size == 0) {
                    return
                }
                val entry = map.entries().next()
                val keySer = AbstractPluginData.getSerializer(entry.key.javaClass)
                val valSer = AbstractPluginData.getSerializer(entry.value.javaClass)
                requireNotNull(keySer) { entry.key.javaClass.toString() + " does not have a serializer registered!" }
                requireNotNull(valSer) { entry.value.javaClass.toString() + " does not have a serializer registered!" }
                stream.writeUTF(entry.key.javaClass.name)
                stream.writeUTF(entry.value.javaClass.name)
                for (e in map.entries()) {
                    val en = e as ObjectMap.Entry<*, *>
                    keySer.write(stream, en.key)
                    valSer.write(stream, en.value)
                }
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): ObjectMap<*, *>? {
                return try {
                    val size = stream.readInt()
                    val map = ObjectMap<Any?, Any?>()
                    if (size == 0) {
                        return map
                    }
                    val keySerName = stream.readUTF()
                    val valSerName = stream.readUTF()
                    val keySer = AbstractPluginData.getSerializer(lookup(keySerName))
                    val valSer = AbstractPluginData.getSerializer(lookup(valSerName))
                    requireNotNull(keySer) { "$keySerName does not have a serializer registered!" }
                    requireNotNull(valSer) { "$valSerName does not have a serializer registered!" }
                    for (i in 0 until size) {
                        val key = keySer.read(stream)
                        val `val` = valSer.read(stream)
                        map.put(key, `val`)
                    }
                    map
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    null
                }
            }
        })
        AbstractPluginData.setSerializer(OrderedMap::class.java, object : TypeSerializer<OrderedMap<*, *>> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, map: OrderedMap<*, *>) {
                stream.writeInt(map.size)
                if (map.size == 0) {
                    return
                }
                val entry = map.entries().next()
                val keySer = AbstractPluginData.getSerializer(entry.key.javaClass)
                val valSer = AbstractPluginData.getSerializer(entry.value.javaClass)
                requireNotNull(keySer) { entry.key.javaClass.toString() + " does not have a serializer registered!" }
                requireNotNull(valSer) { entry.value.javaClass.toString() + " does not have a serializer registered!" }
                stream.writeUTF(entry.key.javaClass.name)
                stream.writeUTF(entry.value.javaClass.name)
                for (e in map.entries()) {
                    val en = e as ObjectMap.Entry<*, *>
                    keySer.write(stream, en.key)
                    valSer.write(stream, en.value)
                }
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): OrderedMap<*, *>? {
                return try {
                    val size = stream.readInt()
                    val map = OrderedMap<Any?, Any?>()
                    if (size == 0) {
                        return map
                    }
                    val keySerName = stream.readUTF()
                    val valSerName = stream.readUTF()
                    val keySer = AbstractPluginData.getSerializer(lookup(keySerName))
                    val valSer = AbstractPluginData.getSerializer(lookup(valSerName))
                    requireNotNull(keySer) { "$keySerName does not have a serializer registered!" }
                    requireNotNull(valSer) { "$valSerName does not have a serializer registered!" }
                    for (i in 0 until size) {
                        val key = keySer.read(stream)
                        val `val` = valSer.read(stream)
                        map.put(key, `val`)
                    }
                    map
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    null
                }
            }
        })
    }

    private fun registerCustomClass() {
        AbstractPluginData.setSerializer(PlayerInfo::class.java, object : TypeSerializer<PlayerInfo> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, objectData: PlayerInfo) {
                AbstractPluginData.getSerializer(String::class.java)!!.write(stream, objectData.uuid)
                stream.writeLong(objectData.timesKicked)
                stream.writeLong(objectData.timesJoined)
                stream.writeLong(objectData.timeMute)
                stream.writeBoolean(objectData.admin)
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): PlayerInfo {
                val objectData = PlayerInfo(AbstractPluginData.getSerializer(String::class.java)!!.read(stream) as String)
                objectData.timesKicked = stream.readLong()
                objectData.timesJoined = stream.readLong()
                objectData.timeMute = stream.readLong()
                objectData.admin = stream.readBoolean()
                return objectData
            }
        })

        AbstractPluginData.setSerializer(PlayerAdminInfo::class.java, object : TypeSerializer<PlayerAdminInfo> {
            @Throws(IOException::class)
            override fun write(stream: DataOutput, objectData: PlayerAdminInfo) {
                stream.writeUTF(objectData.uuid)
                stream.writeBoolean(objectData.admin)
                stream.writeBoolean(objectData.superAdmin)
            }

            @Throws(IOException::class)
            override fun read(stream: DataInput): PlayerAdminInfo {
                return PlayerAdminInfo(stream.readUTF(), stream.readBoolean(), stream.readBoolean())
            }
        })
    }

    @Throws(ClassNotFoundException::class)
    private fun lookup(name: String): Class<*> {
        return Class.forName(name)
    }
}