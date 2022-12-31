package org.sobadfish.fighttodeath.room.event.defaults;

import cn.nukkit.Server;
import cn.nukkit.command.ConsoleCommandSender;

import org.sobadfish.fighttodeath.player.PlayerInfo;
import org.sobadfish.fighttodeath.room.GameRoom;
import org.sobadfish.fighttodeath.room.config.GameRoomEventConfig;
import org.sobadfish.fighttodeath.room.event.IGameRoomEvent;

/**
 * @author Sobadfish
 */
public class CommandEvent extends IGameRoomEvent {

    public CommandEvent(GameRoomEventConfig.GameRoomEventItem item) {
        super(item);
    }

    @Override
    public void onStart(GameRoom room) {
        for(PlayerInfo info: room.getLivePlayers()){
            Server.getInstance().getCommandMap().dispatch(new ConsoleCommandSender(),getEventItem().value.toString().replace("@p","'"+info.getName()+"'"));
        }
    }
}
