/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.func;

/**
 * Boolf 轮询中if判定 Seq专用
 * @author Dr
 */
public interface BooleanIf<T>{
    boolean get(T t);

    default BooleanIf<T> and(BooleanIf<T> pred){
        return t -> get(t) && pred.get(t);
    }

    default BooleanIf<T> or(BooleanIf<T> pred){
        return t -> get(t) || pred.get(t);
    }
}
