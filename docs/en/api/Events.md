# RW-HPS - Event API

# Events

| Name                          | Desc                                    |
|:------------------------------|:----------------------------------------|
| ServerLoadEvent               | Server loading completed                |
| PlayerReJoinEvent             | When a player reconnects to the server  |
| PlayerConnectEvent            | When player connects                    |
| PlayerChatEvent               | When a player speaks                    |
| PlayerConnectPasswdCheckEvent | Player joins with password verification |
| PlayerJoinEvent               | A new player joins the server           |
| PlayerLeaveEvent              | Player leaves the server                |
| GameStartEvent                | The server starts the game              |
| GameOverEvent                 | The server ends the game                |
| PlayerBanEvent                | Player was banned from joining          |
| PlayerIpBanEvent              | Player banned from IP                   |
| PlayerUnbanEvent              | Player is unbanned                      |
| PlayerIpUnbanEvent            | PlayerUnbaned from IP                   |
| PlayerJoinNameEvent           | Player joined with name filter          |
| PlayerOperationUnitEvent      | Player operation unit event             |
| PlayerJoinUuidandNameEvent    | Player joined UUID/Name                 |

# How to use
## New Version
**Event name may be different from Name above, see AbstractEvent**  
**The new version just adds register to the old version and supports asynchronous **
```java
public class Event implements AbstractEvent {
    @Override
    public void registerServerLoadEvent() {
        Log.clog("Example Plugin finished loading");
    }

    @Override
    public void registerPlayerJoinEvent(Player player) {
        player.sendSystemMessage("Hello!!! This is the new Event implementation for RW-HPS");
    }
}
````

## old version
``` java
Events.on(EventType.ServerLoadEvent.class, event -> {
    Log.clog("Example Plugin is finished loading");
});

Events.on(EventType.PlayerJoinEvent.class, event -> {
    event.getPlayer().sendSystemMessage("Plugin test This is the time of entry" + Time.getUtcMilliFormat(1));
});
```