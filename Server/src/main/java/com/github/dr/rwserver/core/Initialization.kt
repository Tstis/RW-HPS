/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.core

import com.github.dr.rwserver.Main
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.plugin.PluginData
import com.github.dr.rwserver.net.HttpRequestOkHttp
import com.github.dr.rwserver.util.LocaleUtil
import com.github.dr.rwserver.util.log.Log
import java.util.*

/**
 * @author Dr
 */
class Initialization {
    private fun initMaps() {
        Data.MapsMap.put("Beachlanding(2p)[byhxyy]", "Beach landing (2p) [by hxyy]@[p2]")
        Data.MapsMap.put("BigIsland(2p)", "Big Island (2p)@[p2]")
        Data.MapsMap.put("DireStraight(2p)[byuber]", "Dire_Straight (2p) [by uber]@[p2]")
        Data.MapsMap.put("FireBridge(2p)[byuber]", "Fire Bridge (2p) [by uber]@[p2]")
        Data.MapsMap.put("Hills(2p)[ByTstis&KPSS]", "Hills_(2p)_[By Tstis & KPSS]@[p2]")
        Data.MapsMap.put("IceIsland(2p)", "Ice Island (2p)@[p2]")
        Data.MapsMap.put("Lake(2p)", "Lake (2p)@[p2]")
        Data.MapsMap.put("SmallIsland(2p)", "Small_Island (2p)@[p2]")
        Data.MapsMap.put("Twocoldsides(2p)", "Two_cold_sides (2p)@[p2]")
        Data.MapsMap.put("Hercules(2vs1p)[byuber]", "Hercules_(2vs1p) [by_uber]@[p3]")
        Data.MapsMap.put("KingoftheMiddle(3p)", "King of the Middle (3p)@[p3]")
        Data.MapsMap.put("Depthcharges(4p)[byhxyy]", "Depth charges (4p) [by hxyy]@[p4]")
        Data.MapsMap.put("Desert(4p)", "Desert (4p)@[p4]")
        Data.MapsMap.put("IceLake(4p)[byhxyy]", "Ice Lake (4p) [by hxyy]@[p4]")
        Data.MapsMap.put("Islandfreeze(4p)[byhxyy]", "Island freeze (4p) [by hxyy]@[p4]")
        Data.MapsMap.put("Islands(4p)", "Islands (4p)@[p4]")
        Data.MapsMap.put("LavaMaze(4p)", "Lava Maze (4p)@[p4]")
        Data.MapsMap.put("LavaVortex(4p)", "Lava Vortex (4p)@[p4]")
        Data.MapsMap.put("MagmaIsland(4p)", "Magma Island (4p)@[p4]")
        Data.MapsMap.put("Manipulation(4p)[ByTstis]", "Manipulation_(4p)_[By Tstis]@[p4]")
        Data.MapsMap.put("Nuclearwar(4p)[byhxyy]", "Nuclear war (4p) [by hxyy]@[p4]")
        Data.MapsMap.put("Crossing(6p)", "Crossing (6p)@[p6]")
        Data.MapsMap.put("ShoretoShore(6p)", "Shore to Shore (6p)@[p6]")
        Data.MapsMap.put("ValleyPass(6p)", "Valley Pass (6p)@[p6]")
        Data.MapsMap.put("BridgesOverLava(8p)", "Bridges Over Lava (8p)@[p8]")
        Data.MapsMap.put("Coastline(8p)[byhxyy]", "Coastline (8p) [by hxyy]@[p8]")
        Data.MapsMap.put("HugeSubdivide(8p)", "Huge Subdivide (8p)@[p8]")
        Data.MapsMap.put("Interlocked(8p)", "Interlocked (8p)@[p8]")
        Data.MapsMap.put("InterlockedLarge(8p)", "Interlocked Large (8p)@[p8]")
        Data.MapsMap.put("IsleRing(8p)", "Isle Ring (8p)@[p8]")
        Data.MapsMap.put("LargeIceOutcrop(8p)", "Large Ice Outcrop (8p)@[p8]")
        Data.MapsMap.put("LavaBiogrid(8p)", "Lava Bio-grid(8p)@[p8]")
        Data.MapsMap.put("LavaDivide(8p)", "Lava Divide(8p)@[p8]")
        Data.MapsMap.put("ManyIslands(8p)", "Many Islands (8p)@[p8]")
        Data.MapsMap.put("RandomIslands(8p)", "Random Islands (8p)@[p8]")
        Data.MapsMap.put("Tornadoeye(8p)[byhxyy]", "Tornado eye (8p) [by hxyy]@[p8]")
        Data.MapsMap.put("TwoSides(8p)", "Two Sides (8p)@[p8]")
        Data.MapsMap.put("Volcano(8p)", "Volcano (8p)@[p8]")
        Data.MapsMap.put("VolcanoCrater(8p)", "Volcano Crater(8p)@[p8]")
        Data.MapsMap.put("TwoSidesRemake(10p)", "Two Sides Remake (10p)@[z;p10]")
        Data.MapsMap.put("ValleyArena(10p)[byuber]", "Valley Arena (10p) [by_uber]@[z;p10]")
        Data.MapsMap.put("ManyIslandsLarge(10p)", "Many Islands Large (10p)@[z;p10]")
        Data.MapsMap.put("CrossingLarge(10p)", "Crossing Large (10p)@[z;p10]")
        Data.MapsMap.put("Kingdoms(10p)[byVulkan]", "Kingdoms (10p) [by Vulkan]@[z;p10]")
        Data.MapsMap.put("LargeLavaDivide(10p)", "Large Lava Divide (10p)@[z;p10]")
        Data.MapsMap.put("EnclosedIsland(10p)", "Enclosed Island (10p)@[z;p10]")
        Data.MapsMap.put("TwoLargeIslands(10p)", "Two_Large_Islands_(10p)@[z;p10]")
        Data.MapsMap.put("Wetlands(10p)", "Wetlands (10p)@[z;p10]")
    }

    private fun loadIpBin() {
        if (!Data.config.IpCheckMultiLanguageSupport) {
            return
        }
        /*
		try {
			Data.ip2Location = new IP2Location();
			Data.ip2Location.Open(FileUtil.getFolder(Data.Plugin_Data_Path).toFile("IP.bin").getPath(), true);
		} catch (IOException e) {
			Log.error("IP-LOAD ERR",e);
		}*/
    }

    private fun loadLang() {
        Data.localeUtilMap.put("CN", LocaleUtil(Objects.requireNonNull(Main::class.java.getResourceAsStream("/bundles/GA_zh_CN.properties"))))
        Data.localeUtilMap.put("HK", LocaleUtil(Objects.requireNonNull(Main::class.java.getResourceAsStream("/bundles/GA_zh_HK.properties"))))
        Data.localeUtilMap.put("RU", LocaleUtil(Objects.requireNonNull(Main::class.java.getResourceAsStream("/bundles/GA_ru_RU.properties"))))
        Data.localeUtilMap.put("EN", LocaleUtil(Objects.requireNonNull(Main::class.java.getResourceAsStream("/bundles/GA_en_US.properties"))))

        // Default use EN
        Data.localeUtil = Data.localeUtilMap["EN"]
    }

    internal class ExitHandler : Thread("Exit Handler") {
        override fun run() {
            Data.core.save()
            println("Exit Save Ok")
        }
    }

    companion object {
        fun startInit(pluginData: PluginData) {
            initServerLanguage(pluginData)
        }

        /**
         * The country is determined according to the server's export ip when it is first started
         * Choose the language environment according to the country
         */
        private fun initServerLanguage(pluginData: PluginData) {
            val serverCountry = pluginData.getData<String>("serverCountry") {
                val country = HttpRequestOkHttp.doGet("https://api.data.der.kim/IP/getCountry")
                if (country.contains("香港")) {
                    "HK"
                } else if (country.contains("中国")) {
                    "CN"
                } else if (country.contains("俄罗斯")) {
                    "RU"
                } else {
                    "EN"
                }
            }

            Data.localeUtil = Data.localeUtilMap[serverCountry]

            Log.clog(Data.localeUtil.getinput("server.language"))
        }
    }

    init {
        //update();
        loadLang()
        initMaps()

        //downPlugin();

        //loadIpBin();
        Runtime.getRuntime().addShutdownHook(ExitHandler())
    }
}