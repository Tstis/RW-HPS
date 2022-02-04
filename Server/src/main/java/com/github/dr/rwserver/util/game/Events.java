/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.game;

import com.github.dr.rwserver.func.Cons;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.Seq;

/**
 * @author Dr
 */
@SuppressWarnings("unchecked")
public class Events {
    private static final ObjectMap<Object, Seq<Cons<?>>> EVENTS = new ObjectMap<>();

    public static <T> void on(Class<T> type, com.github.dr.rwserver.func.Cons<T> listener) {
        EVENTS.get(type, Seq::new).add(listener);
    }

    public static void on(Object type, Runnable listener) {
        EVENTS.get(type, Seq::new).add(e -> listener.run());
    }

    public static <T> void remove(Class<T> type, com.github.dr.rwserver.func.Cons<T> listener) {
        EVENTS.get(type, Seq::new).remove(listener);
    }

    public static <T> void fire(T type) {
        fire(type.getClass(), type);
    }

    public static <T> void fire(Class<?> type1, T type) {
        if(EVENTS.get(type) != null) {
            EVENTS.get(type).each(e -> ((com.github.dr.rwserver.func.Cons<T>)e).get(type));
        }
        if(EVENTS.get(type1) != null) {
            EVENTS.get(type1).each(e -> ((com.github.dr.rwserver.func.Cons<T>)e).get(type));
        }
    }

}