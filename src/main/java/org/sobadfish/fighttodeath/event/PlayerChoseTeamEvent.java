package org.sobadfish.fighttodeath.event;

import cn.nukkit.event.Cancellable;
import cn.nukkit.plugin.Plugin;
import org.sobadfish.fighttodeath.player.PlayerInfo;
import org.sobadfish.fighttodeath.player.team.TeamInfo;
import org.sobadfish.fighttodeath.room.GameRoom;


/**
 * 玩家选择队伍事件
 * @author SoBadFish
 * 2022/1/15
 */
public class PlayerChoseTeamEvent extends PlayerRoomInfoEvent implements Cancellable {


    private final TeamInfo teamInfo;

    public PlayerChoseTeamEvent(PlayerInfo playerInfo, TeamInfo teamInfo, GameRoom room, Plugin plugin) {
        super(playerInfo,room, plugin);
        this.teamInfo = teamInfo;
    }



    public TeamInfo getTeamInfo() {
        return teamInfo;
    }
}
