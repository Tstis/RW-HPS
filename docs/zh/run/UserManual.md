# RW-HPS - UserManual

RW-HPS 用户手册  
本文面向对开发不熟悉而希望使用 RW-HPS 的用户。如果你要开发，请先阅读 [开发文档](../plugin/README.md)

## 使用纯控制台版本 启动 RW-HPS
### 安装
* [直接运行](Run.md)
* [使用Docker容器](../../../docker/README.md)

### 了解运行环境
第一次运行会初始化运行环境。下表说明了各个文件夹的用途。

Config配置解释 : [Config.json](Config.md)  

|          文件夹名称           | 用途               |
|:------------------------:|:-----------------|
|      `data/plugins`      | 存放插件             |
|       `data/maps`        | 存放地图             |
|       `data/mods`        | 存放Rwmod          |
|       `data/cache`       | 存放缓存，一般不需要在意它们   |
|       `data/libs`        | 存放依赖，一般不需要在意它们   |
|        `data/log`        | 存放Log，一般不需要在意它们  |
| [Config.json](Config.md) | 存放配置，可以打开并修改配置   |
|      `Settings.bin`      | 存放内部配置，一般不需要在意它们 |

### 下载和安装插件
刚刚装好的 RW-HPS 是没有任何自定义功能的。功能将由插件提供。

### 使用Mod
把mod扔进 `data/mods` 就好了 !

## 解决问题

如果遇到使用问题或想提建议，可以在  
[issues](https://github.com/RW-HPS/RW-HPS/issues)  
[Tencent QQ群](https://qm.qq.com/cgi-bin/qm/qr?k=qhJ6ekYF9pD9jO6j8H2rZw8ePAVypoU0&jump_from=webapi)  
<del>[Telegram组](https://t.me/RW_HPS) </del>  
[DisCord组](https://discord.gg/VwwxJhVG64)

进行发表和讨论