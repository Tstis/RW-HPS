/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util.log.exp

import cn.rwhps.server.util.log.ErrorCode

/**
 * @author Dr
 */
class RwGamaException {
    open class KickException(type: String) : Exception(ErrorCode.valueOf(type).error)

    class KickStartException(type: String) : KickException(type)

    class KickPullException(type: String) : KickException(type)

    class PasswdException(type: String) : KickException(type)
}