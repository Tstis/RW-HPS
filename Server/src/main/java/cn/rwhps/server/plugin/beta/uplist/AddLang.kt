/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.plugin.beta.uplist

import cn.rwhps.server.plugin.Plugin

/**
 * 为服务器注入多语言
 * @author RW-HPS/Dr
 */
class AddLang(val plugin: Plugin) {
    init {
        help()
    }
    
    private fun help() {
        loadCN("uplist.help",
        """
        
        [uplist add] 服务器上传到列表 显示配置文件端口
        [uplist add (port)] 服务器上传到列表 服务器运行配置文件端口 显示自定义端口
        [uplist update] 立刻更新列表服务器信息
        [uplist remove] 取消服务器上传列表
        [uplist help] 获取帮助
        """.trimIndent())
        loadEN("uplist.help",
        """
        
        [uplist add] Server upload to list Show profile port
        [uplist add (port)] Server upload to list Server running profile port Display custom port
        [uplist update] Update list server information immediately
        [uplist remove] Cancel server upload list
        [uplist help] Get Help
        """.trimIndent())
        loadHK("uplist.help",
        """
        
        [uplist add] 服务器上传到列表 显示配置文件端口
        [uplist add (port)] 服务器上传到列表 服务器运行配置文件端口 显示自定义端口
        [uplist update] 立刻更新列表服务器信息
        [uplist remove] 取消服务器上传列表
        [uplist help] 获取帮助
        """.trimIndent())
        loadRU("uplist.help",
        """
        
        [uplist add] Загрузка сервера в список Показать порт профиля
        [uplist add (port)] Загрузка сервера в список Порт запущенного профиля сервера Показать пользовательские порты
        [uplist update] Немедленное обновление информации сервера списка
        [uplist remove] Отмена загрузки сервера в список
        [uplist help] Получить помощь
        """.trimIndent())
    }
    
    private fun loadCN(k: String, v: String) {
        plugin.loadLang("CN",k,v)
    }
    private fun loadEN(k: String, v: String) {
        plugin.loadLang("EN",k,v)
    }
    private fun loadHK(k: String, v: String) {
        plugin.loadLang("HK",k,v)
    }
    private fun loadRU(k: String, v: String) {
        plugin.loadLang("RU",k,v)
    }
}