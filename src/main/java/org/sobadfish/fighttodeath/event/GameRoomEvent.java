package org.sobadfish.fighttodeath.event;

import cn.nukkit.event.HandlerList;
import cn.nukkit.event.plugin.PluginEvent;
import cn.nukkit.plugin.Plugin;
import org.sobadfish.fighttodeath.room.GameRoom;

/**
 * 房间主事件
 * 其他的房间事件都继承这个事件
 * @author SoBadFish
 * 2022/1/15
 */
public class GameRoomEvent extends PluginEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlers() {
        return HANDLERS;
    }

    private final GameRoom room;

    public GameRoomEvent(GameRoom room,
                         Plugin plugin) {
        super(plugin);
        this.room = room;
    }

    public GameRoom getRoom() {
        return room;
    }
}
