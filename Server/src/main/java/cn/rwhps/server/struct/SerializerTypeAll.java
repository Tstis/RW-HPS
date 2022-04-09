/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.struct;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 序列化/反序列化使用的读取与写入
 * 用在DefaultSerializers.java中
 * @author Dr
 */
public class SerializerTypeAll {
    public interface TypeSerializer<T> {
        /**
         * 序列化写入
         * @param param1DataOutput 输出流
         * @param param1T 输入的数据
         * @throws IOException Error
         */
        void write(DataOutput param1DataOutput, T param1T) throws IOException;

        /**
         * 反序列化读取
         * @param param1DataInput 输入流
         * @return 反序列化后的数据
         * @throws IOException Error
         */
        T read(DataInput param1DataInput) throws IOException;
    }

    public interface TypeWriter<T> {
        /**
         * 序列化写入
         * @param param1DataOutput 输出流
         * @param param1T 输入的数据
         * @throws IOException Error
         */
        void write(DataOutput param1DataOutput, T param1T) throws IOException;
    }
    public interface TypeReader<T> {
        /**
         * 反序列化读取
         * @param param1DataInput 输入流
         * @return 反序列化后的数据
         * @throws IOException Error
         */
        T read(DataInput param1DataInput) throws IOException;
    }
}
