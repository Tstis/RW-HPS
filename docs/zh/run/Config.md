> 服务器默认启动命令  
>> Server模式为**start**  
>> Relay模式为**startrelayopen**  
"DefStartCommand": "start"  

> Log记录等级  
> OFF FATAL ERROR WARN INFO DEBUG TRACE ALL  
"Log": "ALL"  

> 服务器的名字 在上列表的PlayerName显示  
"ServerName": "RW-HPS"  
> 服务器的地图名 自定义在列表的MapsName显示 **留空为显示MapsName**  
"Subtitle": ""  

> 服务器运行的端口  
"Port": 5123  

> 服务器的密码  
"Passwd": ""  

> 是否启用UDP  
"UDPSupport": false  

> 玩家进入时发送的消息  
"EnterAd": ""  

> 游戏开始时发送的消息  
"StartAd": ""  

> 房间已满时KICK的消息  
"MaxPlayerAd": ""  

> 房间已开始游戏时KICK的消息  
"StartPlayerAd": ""  

> 最大玩家  
"MaxPlayer": 100  

> 房间启动需要的最小玩家  
"StartMinPlayerSize": 0  

> 最大发言长度  
"MaxMessageLen": 40  

> 最大单位数量  
"MaxUnit": 2000  

> 默认倍率  
"DefIncome": 1.0  

> 是否启用第一个进入为Admin  
"OneAdmin": true  

> 无用  
"IpCheckMultiLanguageSupport": false  

> 是否启用重连-BETA  
"ReConnect": false  

> 无用  
"WinOrLose": false  
"WinOrLoseTime": 30000

> RELAY模式下 是否启用单房间模式  
"SingleUserRelay": false  
> 单房间模式下是否启用Mod  
"SingleUserRelayMod": false

> Server模式下是否读取Mod  
"OneReadUnitList": false

> 无用  
"GameOverUpList": false
"PasswdCheckApi": false

> 是否自动重读取maps下地图  
"AutoReLoadMap": false

> 服务器进程的PID  
"RunPid": 2757