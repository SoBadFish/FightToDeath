package org.sobadfish.fighttodeath.player.team;

import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.fighttodeath.event.PlayerChoseTeamEvent;
import org.sobadfish.fighttodeath.event.TeamDefeatEvent;
import org.sobadfish.fighttodeath.event.TeamVictoryEvent;
import org.sobadfish.fighttodeath.manager.TotalManager;
import org.sobadfish.fighttodeath.player.PlayerInfo;
import org.sobadfish.fighttodeath.player.team.config.TeamInfoConfig;
import org.sobadfish.fighttodeath.room.GameRoom;
import org.sobadfish.fighttodeath.room.config.WorldInfoConfig;

import java.util.ArrayList;

/**
 * @author SoBadFish
 * 2022/1/2
 */

public class TeamInfo {

    //队伍基本配置
    private TeamInfoConfig teamConfig;

    //淘汰
    private boolean stop;

    private boolean close;

    private final GameRoom room;

    private int spawnTeleportLocation = 0;

    //队伍的玩家
    private ArrayList<PlayerInfo> teamPlayers = new ArrayList<>();

    private ArrayList<PlayerInfo> defeatPlayers = new ArrayList<>();

    private ArrayList<PlayerInfo> victoryPlayers = new ArrayList<>();


    public TeamInfo(GameRoom room,TeamInfoConfig teamConfig){
        this.teamConfig = teamConfig;
        this.room = room;
    }


    public boolean isClose() {
        return close;
    }


    public boolean isLoading() {
        return !stop;
    }

    public void setClose(boolean close) {
        this.close = close;
    }


    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public TeamInfoConfig getTeamConfig() {
        return teamConfig;
    }



    public ArrayList<PlayerInfo> getVictoryPlayers() {
        return victoryPlayers;
    }

    public ArrayList<PlayerInfo> getDefeatPlayers() {
        return defeatPlayers;
    }

    public void setTeamConfig(TeamInfoConfig teamConfig) {
        this.teamConfig = teamConfig;
    }

    public void setVictoryPlayers(ArrayList<PlayerInfo> victoryPlayers) {
        this.victoryPlayers = victoryPlayers;
    }

    public void setDefeatPlayers(ArrayList<PlayerInfo> defeatPlayers) {
        this.defeatPlayers = defeatPlayers;
    }

    public void setTeamPlayers(ArrayList<PlayerInfo> teamPlayers) {
        this.teamPlayers = teamPlayers;
    }

    public void sendMessage(String msg){
        teamPlayers.forEach(playerInfo ->
                playerInfo.sendMessage(msg));
    }

    public void sendFaceMessage(String msg){
        teamPlayers.forEach(playerInfo ->
                playerInfo.sendForceMessage(msg));
    }


    public Position getSpawnLocation(){
        if(spawnTeleportLocation >= teamConfig.getSpawnPosition().size()){
           spawnTeleportLocation = 0;
        }
        Position pos = WorldInfoConfig.getPositionByString(teamConfig.getSpawnPosition().get(spawnTeleportLocation));
        spawnTeleportLocation++;
        return pos;

    }



    public ArrayList<PlayerInfo> getTeamPlayers() {
        teamPlayers.removeIf((p)->p.disable);
        return teamPlayers;
    }



    public ArrayList<PlayerInfo> getInRoomPlayer(){
        ArrayList<PlayerInfo> playerInfos = new ArrayList<>();
        for(PlayerInfo playerInfo: getTeamPlayers()){
            if(playerInfo.isInRoom()){
                playerInfos.add(playerInfo);
            }
        }
        return playerInfos;
    }

    public ArrayList<PlayerInfo> getLivePlayer(){
        ArrayList<PlayerInfo> playerInfos = new ArrayList<>();
        for(PlayerInfo playerInfo: getTeamPlayers()){
            if(playerInfo.isLive()){
                playerInfos.add(playerInfo);
            }
        }
        return playerInfos;
    }


    public void echoVictory(){
        //TODO 当队伍胜利
        TeamVictoryEvent event = new TeamVictoryEvent(this,room, TotalManager.getPlugin());
        Server.getInstance().getPluginManager().callEvent(event);

    }

    public void echoDefeat(){
        //TODO 当队伍失败
        TeamDefeatEvent event = new TeamDefeatEvent(this,room,TotalManager.getPlugin());
        Server.getInstance().getPluginManager().callEvent(event);

    }

    public void sendActionBar(String msg){
        teamPlayers.forEach(playerInfo ->
                playerInfo.sendActionBar(msg));
    }

    public void sendTip(String msg){
        teamPlayers.forEach(playerInfo ->
                playerInfo.sendTip(msg));
    }

    public void sendTitle(String msg,int time){
        teamPlayers.forEach(playerInfo ->
                playerInfo.sendTitle(msg,time));
    }

    public void sendTitle(String msg){
        teamPlayers.forEach(playerInfo ->
                playerInfo.sendTitle(msg));
    }

    public void sendSubTitle(String msg){
        teamPlayers.forEach(playerInfo ->
                playerInfo.sendSubTitle(msg));
    }

    public void addSound(Sound sound){
        teamPlayers.forEach(playerInfo ->
                playerInfo.addSound(sound));
    }

    public void onUpdate(){
        if(close){
            return;
        }
        if(stop){
            close = true;
            return;
        }
        int d = 0;
        for(PlayerInfo info: getTeamPlayers()){
            if(info.getPlayerType() == PlayerInfo.PlayerType.WATCH || info.getPlayerType() == PlayerInfo.PlayerType.LEAVE){
                d++;
            }
        }


        if(d == getTeamPlayers().size()){
            //被淘汰了
            room.sendMessage("&r团灭 > "+this+"&c已被淘汰!");
            defeatPlayers.addAll(getTeamPlayers());
            echoDefeat();
            stop = true;
        }

    }
   

    public boolean join(PlayerInfo info){
        PlayerChoseTeamEvent event = new PlayerChoseTeamEvent(info,this,room,TotalManager.getPlugin());
        Server.getInstance().getPluginManager().callEvent(event);
        if(teamPlayers.contains(info)){
            return false;
        }
        if(info.getTeamInfo() != null){
            info.getTeamInfo().quit(info);
        }
        info.setTeamInfo(this);
        teamPlayers.add(info);
        return true;
    }

    public void mjoin(PlayerInfo info){
        TeamInfo teamInfo = info.getTeamInfo();
        if(teamInfo != null){
            teamInfo.getTeamPlayers().remove(info);
        }
        info.setTeamInfo(this);
        teamPlayers.add(info);
    }

    public void quit(PlayerInfo info){
        info.setTeamInfo(null);
        teamPlayers.remove(info);
    }

    public double getAllHealth(){
        double dh = 0;
        for(PlayerInfo info: getLivePlayer()){
            dh += info.getPlayer().getHealth();
        }
        return dh;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof TeamInfo){
            return ((TeamInfo) obj).teamConfig.equals(teamConfig);
        }
        return false;
    }

    public void close(){
        close = true;
    }

    @Override
    public String toString() {
        return TextFormat.colorize('&',getTeamConfig().getNameColor()+getTeamConfig().getName());
    }
}
