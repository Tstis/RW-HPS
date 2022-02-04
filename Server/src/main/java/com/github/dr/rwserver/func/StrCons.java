/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.func;

import java.text.MessageFormat;

/**
 * @author Dr
 */
public interface StrCons {
    /**
     * Log 专用
     * @param str String
     */
    void get(String str);

    /**
     * Log 转换
     * @param t String
     * @param obj Object...
     */
    default void get(String t,Object... obj) {
        get(new MessageFormat(t).format(obj));
    }
}
