/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.func;

/**
 * Cons2 轮循 Map专用
 * @author RW-HPS/Dr
 */
public interface Cons2<T, N>{
    /**
     * echo -> .get(k.v)
     * @param t k
     * @param n v
     */
    void get(T t, N n);

    /**
     * 多个cons2
     * @param cons cons
     * @return
     */
    default Cons2<T, N> with(Cons2<T, N> cons){
        return (t, n) -> {
            get(t, n);
            cons.get(t, n);
        };
    }
}
