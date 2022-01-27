> Server default start command
>> Server mode is **start**
>>> Relay mode is **startrelay** :   
>>> Direct forwarding consumes more bandwidth and has the same effect as forwarding with VPN  
>>> Relay mode is **startrelaytest** :  
>>> Use multicast to reduce bandwidth consumption  
"DefStartCommand": "start"

> Log logging level  
> OFF FATAL ERROR WARN INFO DEBUG TRACE ALL  
"Log": "ALL"

> Server name The PlayerName in the above list is displayed  
"ServerName": "RW-HPS"  
> Server's map name Customize the MapsName display in the list **leave blank to show MapsName**  
"Subtitle": ""

> Port on which the server is running  
"Port": 5123

> Password of the server  
"Passwd": ""

> whether UDP is enabled  
"UDPSupport": false

> Message sent when player enters  
"EnterAd": ""

> Message sent when the game starts  
"StartAd": ""

> KICK message when the room is full  
"MaxPlayerAd": ""

> KICK message when the room has started playing  
"StartPlayerAd": ""

> Maximum players  
"MaxPlayer": 100

> Minimum players needed to start the room  
"StartMinPlayerSize": 0

> Maximum message length  
"MaxMessageLen": 40

> Maximum number of units  
"MaxUnit": 2000

> default multiplier  
"DefIncome": 1.0

> whether to enable the first entry as Admin  
"OneAdmin": true

> **Not implemented/not recommended**  
"IpCheckMultiLanguageSupport": false

> whether to enable reconnect-BETA  
"ReConnect": false

> **Not implemented/not recommended**  
"WinOrLose": false  
"WinOrLoseTime": 30000

> Whether to enable single-room mode in RELAY mode  
"SingleUserRelay": false  
> Whether to enable Mod in single-room mode  
"SingleUserRelayMod": false

> Whether to read mods in Server mode  
"OneReadUnitList": false

> **Not implemented/not recommended**  
"GameOverUpList": false
"PasswdCheckApi": false

> Whether to automatically reload maps under maps  
"AutoReLoadMap": false

> PID of the server process  
"RunPid": 2757