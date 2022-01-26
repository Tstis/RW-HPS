[Chinese version / 中文介绍](README-CN.md)

**Welcome to provide better translation**

![](https://img.shields.io/github/stars/RW-HPS/RW-HPS.svg)
![](https://github.com/RW-HPS/RW-HPS/actions/workflows/gradle.yml/badge.svg?branch=master)
![](https://jitpack.io/v/RW-HPS/RW-HPS.svg)
![](https://app.fossa.com/api/projects/git%2Bgithub.com%2FRW-HPS%2FRW-HPS.svg?type=shield)

# RW-HPS Project
RW-HPS is a Rusted Warfare game server, used to quickly set up high-performance game servers on servers running Java11

## Features
### High performance
Ditch the <Java -Blocking IO / BIO> development approach used by the game itself and use <Java -Not-Blocking IO / NIO> to increase throughput and reduce latency
### Plugin framework
Despite most of the things you can do in a normal game, you may also do a lot of other things on this server because there's a plugin framework which allows you to extend the functionality of this server. Furthermore, you can even implement your own actions!

## Actions List

<details>
  <summary>Actions List</summary>  

**Message**
- Team Chat
- All Chat
- Map location

**Game**
- Unit movement
- Game reconnection
- Custom map
- Load Save Game
- Load Mods?

**Ex**
- Loading plugins
- Player jump server
- Map generation unit
- RELAY Server

**Other**
- BanUUID
- BanIP
- Mute Player

</details>

### Actions that won't be supported
- Anything related to real money

## Needs Help?
- Documentation: [docs](docs/en/README.md)  
- Update log: [release](https://github.com/RW-HPS/RW-HPS/releases)
- Development milestones: [milestones](https://github.com/RW-HPS/RW-HPS/milestones)
- Discussion:
  > The developemenet team is actively answering questions on Github Discussions, please feel free to share you ideas about this project.  
  > Email contact: dr@der.kim      
  > Tencent QQ Group: [901913920](https://qm.qq.com/cgi-bin/qm/qr?k=qhJ6ekYF9pD9jO6j8H2rZw8ePAVypoU0&jump_from=webapi)  
  > <del>Telegram Group: [RW-HPS](https://t.me/RW_HPS) </del>  
  > Discord: [RW-HPS](https://discord.gg/VwwxJhVG64)  
- Mirrors:
  [Github](https://github.com/RW-HPS/RW-HPS) ; [Gitee](https://gitee.com/derdct/RW-HPS)

## Recommended Configuration

| Configuration 		| CPU                     | RAM 	| System 			 | Disk 	  | Java Version |
|:--- 		|:------------------------|:---     |:-----------|:--------|:---       |
| Recommended 	| ARMv7 Processor rev 5 + | 128MB      | Any        | 64M HDD | Java 11   |
| Minimum 	| Any                     | 64M      | Any        | 16M HDD | Java 11   |

## Server Command List
<details>
  <summary>Server Command List</summary>  

| Command 					 | Parameter 																						 | Information 									 |
|:--- 					 |:--- 																						 |:--- 									 |
| help 		              |                                                  										 | Get help 		 |
| start                  |                                                  										 | Turn on the server 						 |
| say 		      | &lt;TEXT&gt;                                                  										 | Send messages in the name of Server 				 |
| giveadmin                | &lt;PlayerSerialNumber&gt; 																 | Transfer Admin       		         |
| restart 			 | 																							 | Restart server 				 |
| gameover 				 |  	 | Restart The Game               				 |
| clearbanip          		 |                                                  										 | Clean up the banned IP               	 |
| admin          		 |&lt;add/remove&gt; &lt;PlayerSite&gt;                                                  										 | Set up admin               			 |
| clearbanuuid          		 |                               	   											 | Clear banned uuid               			 |
| clearbanall          		 |                               	   											 | Empty ban               			 |
| ban          		 | &lt;PlayerSerialNumber&gt;                                 	   											 | Ban someone               			 |
| mute          		 |  &lt;PlayerSerialNumber&gt;  &lt;Time/s&gt;                             	   											 | Forbid sb from speaking               			 |
| kick          		 |  &lt;PlayerSerialNumber&gt;  &lt;Time/s&gt;                             	   											 | Kick               			 |
| isafk          		 |  &lt;off/on&gt;                             	   											 | Whether to enable AFK               			 |
| plugins          		 |                               	   											 | View the list of plugins               			 |
| players          		 |                               	   											 | View player list               			 |
| kill          		 | &lt;PlayerSerialNumber&gt;                             	   											 | Kill the player               			 |
| clearmuteall          		 |                               	   											 | Unmute all               			 |
| maps          		 |                               	   											 | View Custom Map               			 |
| reloadmaps          		 |                               	   											 | Reload map               			 |
| stop          		 |                               	   											 | Stop the server               			 |
Sorry, maybe more commands have not been added because the document has no time to update  
Please use help in the console to see more  
</details>


## Game Command List
<details>
  <summary>Game Command List</summary>  

| Command 			| Parameter 												 | Information 										 |
|:---           |:--- 												 |:--- 										 |
| help      |   | Get help 									 |
There are many commands not shown here. I suggest you test them yourself  
Please use .help in the game to see more  
</details>

## Sponsorship
RW-HPS is a open source project released under AGPL V3. However, the amount of work required to maintain and develop new features for the project is not sustainable without your generous dedications.
Please note that donations are completely voluntary, thus sponsors do not have additional privileges and all the features are available without sponsorship.

We receive donations through the following channels：
+ [Ko-Fi](https://ko-fi.com/derdct)
+ [CN - 爱发电](https://afdian.net/@derdct)

## Contribution
All kinds of contributions are welcomed.
If you hold an interest in helping us implementing RW-HPS on JS, iOS or Native platforms, please email us .
If you meet any problem or have any questions, feel free to file an issue. Our goal is to make RW-HPS easy to use `RW-HPS@der.kim`

## Special Thanks
> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) is an IDE that maximizes the productivity of developers in all aspects, and is suitable for JVM platform languages。

[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=rw-hps)

## Statements
### For educational purposes only
- RW-HPS is a free and open source project and is only for educational purposes. You shall not use this project for any illegal purposes. The author or the developement team of this project will not be held responsible for any damages.

## License
```
Copyright (C) 2020-2021 RW-HPS Team and contributors.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License v3.0 as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License v3.0
along with this program.  If not, see <https://www.gnu.org/licenses/agpl-3.0>.
```
