/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.game.checksum

class CheckSumMain {
    val unitPos = CheckSumData("Unit Pos")
    val unitDir = CheckSumData("Unit Dir", false)
    val unitHp = CheckSumData("Unit Hp")
    val unitId = CheckSumData("Unit Id")
    val waypoints = CheckSumData("Waypoints")
    val waypointsPos = CheckSumData("Waypoints Pos")
    val teamCredits = CheckSumData("Team Credits")
    val unitPaths = CheckSumData("UnitPaths")
    val unitCount = CheckSumData("Unit Count")
    val teamInfo = CheckSumData("Team Info", false)
    val team1Credits = CheckSumData("Team 1 Credits", false)
    val team2Credits = CheckSumData("Team 2 Credits", false)
    val team3Credits = CheckSumData("Team 3 Credits", false)
    val commandCenter2 = CheckSumData("Command center2", false)
    val commandCenter3 = CheckSumData("Command center3", false)
}