package org.sobadfish.fighttodeath.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import org.sobadfish.fighttodeath.manager.TotalManager;
import org.sobadfish.fighttodeath.player.PlayerInfo;


/**
 * 玩家在游戏里的全体消息
 *
 * @author SoBadFish
 * 2022/1/15
 */
public class GameSpeakCommand extends Command {

    public GameSpeakCommand(String name) {
        super(name);
    }


    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(commandSender instanceof Player){
            PlayerInfo info = TotalManager.getRoomManager().getPlayerInfo((Player) commandSender);
            if(info == null){
                new PlayerInfo((Player)commandSender).sendForceMessage("&c你不在游戏房间内!");
                return false;
            }else{
                if(strings.length > 0){
                    info.getGameRoom().sendFaceMessage("&l&7(全体消息)&r "+info+"&r >> "+strings[0]);

                }else{
                    info.sendForceMessage("&c指令:/"+TotalManager.COMMAND_MESSAGE_NAME+" <你要说的内容> 全体消息");
                }
            }

        }
        return true;
    }
}
