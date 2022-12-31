package org.sobadfish.fighttodeath.event;

import cn.nukkit.plugin.Plugin;
import org.sobadfish.fighttodeath.player.PlayerInfo;
import org.sobadfish.fighttodeath.room.GameRoom;


/**
 * 玩家在游戏中死亡的事件
 * @author SoBadFish
 * 2022/1/15
 */
public class PlayerGameDeathEvent extends PlayerRoomInfoEvent{

    public PlayerGameDeathEvent(PlayerInfo playerInfo, GameRoom room, Plugin plugin) {
        super(playerInfo,room, plugin);
    }
}
