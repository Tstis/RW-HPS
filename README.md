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
Depending on the startup protocol, the server command will change accordingly    
Please use help in the console to see more  

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
- [AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.html)
```
Copyright (C) 2020-2021 RW-HPS Team and contributors.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
- [EULA](https://github.com/RW-HPS/RW-HPS/blob/master/Server/src/main/resources/eula/Engilsh.md)
```
Copyright © 2021 RW-HPS.Team <RW-HPS@der.kim>

Permission is granted to each person to copy and distribute a verbatim copy of this license document and to make changes to it while it complies with the CC BY-NC-SA 4.0 agreement, provided that its copyright information is retained with the original author.

Please be sure to carefully read and understand all rights and restrictions set forth in the General User Agreement. You are required to carefully read and decide whether to accept or not accept the terms of this Agreement before using it. The Software and its related copies, related program code or related resources may not be downloaded, installed or used on any of your terminals unless or until you accept the terms of this Agreement.

By downloading or using the Software, its related copies, related program code or related resources, you agree to be bound by the terms of this Agreement. If you do not agree to the terms of this Agreement, you shall immediately remove the Software, the Affiliated Resources and their associated source code.

The rights in the Software are licensed, not sold.

This Agreement and the GNU Affero General Public License (i.e., the AGPL Agreement) together constitute the agreement between the Software and you, and any conflict between this Agreement and the AGPL Agreement shall be governed by this Agreement. You must agree to and abide by both this Agreement and the AGPL Agreement, or you shall immediately uninstall and remove the Software, the Auxiliary Resources, and their associated source code.
```
