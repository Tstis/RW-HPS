# RW-HPS - Event API

# Events - Server (只会在使用`NetType.ServerProtocol`等衍生协议上生效)
| Name                          | Desc           |
|:------------------------------|:---------------|
| PlayerReJoinEvent             | 玩家重连服务器时       |
| PlayerConnectEvent            | 玩家连接时          |
| PlayerChatEvent               | 玩家发言时          |
| PlayerConnectPasswdCheckEvent | 玩家加入密码验证       |
| PlayerJoinEvent               | 有新玩家加入         |
| PlayerLeaveEvent              | 玩家离开服务器        |
| GameStartEvent                | 服务器开始游戏        |
| GameOverEvent                 | 服务器结束游戏        |
| PlayerBanEvent                | 玩家被禁止加入        |
| PlayerIpBanEvent              | 玩家被禁止加入IP      |
| PlayerUnbanEvent              | 玩家被解除禁止加入      |
| PlayerIpUnbanEvent            | 玩家被解除禁止加入IP    |
| PlayerJoinNameEvent           | 玩家加入时的名字过滤     |
| PlayerOperationUnitEvent      | 玩家操作单位事件       |

# Events - Global (全局生效)
| Name                          | Desc       |
|:------------------------------|:-----------|
| ServerLoadEvent               | Server加载完成 |
| NewConnectEvent             | 新的连接       |
| NewCloseEvent            | 连接断开       |

# 如何使用
## 新版
**事件名称或许与上文的Name不同,请参阅  `AbstractEvent`/`AbstractGlobalEvent`**  
**新版只是在旧版的基础上加了register且支持异步**  
```java
public class Event implements AbstractEvent {
    @Override
    public void registerPlayerJoinEvent(Player player) {
        player.sendSystemMessage("[玩家加入] 你好!! 这是RW-HPS新的Event的实现");
    }
}

public class GlobalEvent implements AbstractGlobalEvent {
    @Override
    public void registerServerLoadEvent() {
        Log.clog("[服务器加载完毕] Example Plugin加载完了");
    }
}
```

## 旧版 (不推荐使用) - 未来版本将被切为内部方法

```java
Events.on(EventGlobalType.ServerLoadEvent.class, event -> {
    Log.clog("[服务器加载完毕] Example Plugin加载完了");
});

Events.on(EventType.PlayerJoinEvent.class, event -> {
    event.getPlayer().sendSystemMessage("[玩家加入] Plugin测试 这是进入的时间 "+ Time.getUtcMilliFormat(1));
});
```
