package org.sobadfish.fighttodeath.manager;

import cn.nukkit.Player;
import org.sobadfish.fighttodeath.player.PlayerInfo;
import org.sobadfish.fighttodeath.room.GameRoom;


import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 房间匹配队列
 * @author Sobadfish
 * */
public class RandomJoinManager {



    public static RandomJoinManager joinManager;



    public static RandomJoinManager newInstance(){
        if(joinManager == null){
            joinManager = new RandomJoinManager();
        }
        return joinManager;
    }

    public List<IPlayerInfo> playerInfos = new CopyOnWriteArrayList<>();


    /**
     * 当玩家在房间内时，调用这个方法匹配房间
     * */
    public void nextJoin(PlayerInfo info){
        //TODO 匹配下一局 程序分配
        GameRoom gameRoom = info.getGameRoom();
        if(gameRoom != null){
            gameRoom.quitPlayerInfo(info,false);
        }
        join(info,null,true);
    }

    /**
     * 调用这个匹配房间
     * 这个是进入匹配队列
     * */
    public void join(PlayerInfo info, String name){
        join(info, name,false);
    }

    public void join(PlayerInfo info, String name,boolean isNext){
        if(info.getGameRoom() != null && info.getGameRoom().getType() != GameRoom.GameType.END){
            return;
        }
        IPlayerInfo iPlayerInfo = new IPlayerInfo();
        iPlayerInfo.playerInfo = info;
        iPlayerInfo.isNext = isNext;
        if(playerInfos.contains(iPlayerInfo)){
            info.sendForceMessage("&c取消匹配");
            playerInfos.remove(iPlayerInfo);
            return;
        }

        iPlayerInfo.name = name;
        iPlayerInfo.time = new Date();
        playerInfos.add(iPlayerInfo);


    }



    public static class IPlayerInfo{

        private PlayerInfo playerInfo;

        public String name;

        public Date time;

        public boolean cancel;

        public boolean isNext;

        public PlayerInfo getPlayerInfo() {
            if(playerInfo != null && playerInfo.getPlayer() instanceof Player && !playerInfo.getPlayer().closed){
                return playerInfo;
            }
            cancel = true;
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof IPlayerInfo){
                return ((IPlayerInfo) o).playerInfo.equals(playerInfo);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(playerInfo, name, time, cancel);
        }
    }

}
