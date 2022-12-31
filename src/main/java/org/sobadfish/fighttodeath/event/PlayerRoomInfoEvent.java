package org.sobadfish.fighttodeath.event;

import cn.nukkit.plugin.Plugin;
import org.sobadfish.fighttodeath.player.PlayerInfo;
import org.sobadfish.fighttodeath.room.GameRoom;


/**
 * 玩家在房间内的事件主类。关于玩家在房间的操作都继承这个事件
 *
 * @author SoBadFish
 * 2022/1/15
 */
public class PlayerRoomInfoEvent extends GameRoomEvent{

    private final PlayerInfo playerInfo;

    public PlayerRoomInfoEvent(PlayerInfo playerInfo, GameRoom room, Plugin plugin) {
        super(room, plugin);
        this.playerInfo = playerInfo;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }
}
