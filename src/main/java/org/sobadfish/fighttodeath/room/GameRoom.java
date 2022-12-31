package org.sobadfish.fighttodeath.room;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.potion.Effect;
import de.theamychan.scoreboard.network.Scoreboard;
import org.sobadfish.fighttodeath.event.GameCloseEvent;
import org.sobadfish.fighttodeath.event.GameRoomStartEvent;
import org.sobadfish.fighttodeath.event.PlayerJoinRoomEvent;
import org.sobadfish.fighttodeath.event.PlayerQuitRoomEvent;
import org.sobadfish.fighttodeath.item.button.FollowItem;
import org.sobadfish.fighttodeath.item.button.RoomQuitItem;
import org.sobadfish.fighttodeath.item.button.TeamChoseItem;
import org.sobadfish.fighttodeath.manager.RandomJoinManager;
import org.sobadfish.fighttodeath.manager.RoomManager;
import org.sobadfish.fighttodeath.manager.TotalManager;
import org.sobadfish.fighttodeath.manager.WorldResetManager;
import org.sobadfish.fighttodeath.player.PlayerInfo;
import org.sobadfish.fighttodeath.player.team.TeamInfo;
import org.sobadfish.fighttodeath.player.team.config.TeamInfoConfig;
import org.sobadfish.fighttodeath.room.config.GameRoomConfig;
import org.sobadfish.fighttodeath.room.config.ItemConfig;
import org.sobadfish.fighttodeath.room.event.EventControl;
import org.sobadfish.fighttodeath.room.floattext.FloatTextInfo;
import org.sobadfish.fighttodeath.room.floattext.FloatTextInfoConfig;
import org.sobadfish.fighttodeath.room.world.WorldInfo;
import org.sobadfish.fighttodeath.tools.Utils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 小游戏房间信息
 * @author Sobadfish
 * @date 2022/9/9
 */
public class GameRoom {

    public GameRoomConfig roomConfig;


    private boolean isInit = true;

    private boolean isMax;

    private boolean teamAll;


    private final ArrayList<FloatTextInfo> floatTextInfos = new ArrayList<>();

    //房间内的玩家
    private final CopyOnWriteArrayList<PlayerInfo> playerInfos = new CopyOnWriteArrayList<>();

    private final LinkedHashMap<PlayerInfo, Scoreboard> scoreboards = new LinkedHashMap<>();

    private boolean hasStart;

    public int loadTime = -1;

    private GameType type;

    private final ArrayList<TeamInfo> teamInfos = new ArrayList<>();

    /**
     * 地图配置
     * */
    public WorldInfo worldInfo;

    public boolean close;

    /**
     * 事件控制器
     * */
    private final EventControl eventControl;

    private GameRoom(GameRoomConfig roomConfig){
        this.roomConfig = roomConfig;
        this.worldInfo = new WorldInfo(this,roomConfig.worldInfo);

        type = GameType.WAIT;
        for(TeamInfoConfig config: getRoomConfig().getTeamConfigs()){
            teamInfos.add(new TeamInfo(this,config));
        }

        //启动事件
        eventControl = new EventControl(this,roomConfig.eventConfig);
        eventControl.initAll(this);
    }

    public ArrayList<FloatTextInfo> getFloatTextInfos() {
        return floatTextInfos;
    }

    public LinkedHashMap<PlayerInfo, Scoreboard> getScoreboards() {
        return scoreboards;
    }

    public CopyOnWriteArrayList<PlayerInfo> getPlayerInfos() {
        return playerInfos;
    }

    public GameRoomConfig getRoomConfig() {
        return roomConfig;
    }

    public ItemConfig getRandomItemConfig(Block block){
        if(roomConfig.items.containsKey(block.getId()+"")){
            return roomConfig.items.get(block.getId()+"");
        }
        return null;
    }

    /**
     * 获取事件控制器
     *
     * */
    public EventControl getEventControl() {
        return eventControl;
    }


    public GameType getType() {
        return type;
    }

    public enum GameType{
        /**
         * WAIT: 等待 START: 开始 END: 结束 CLOSE: 关闭
         * */
        WAIT,START,END,CLOSE
    }

    public PlayerInfo getPlayerInfo(EntityHuman player){
        if(playerInfos.contains(new PlayerInfo(player))){
            return playerInfos.get(playerInfos.indexOf(new PlayerInfo(player)));
        }
        return null;
    }

    public void sendMessageOnWatch(String msg) {
        ArrayList<PlayerInfo> watchPlayer = new ArrayList<>();
        for(PlayerInfo info: playerInfos){
            if(info.isWatch()){
                watchPlayer.add(info);
            }
        }
        watchPlayer.forEach(dp -> dp.sendMessage(msg));
    }

    public void joinWatch(PlayerInfo info,boolean isTeleport){
        //TODO 欢迎加入观察者大家庭
        if(!playerInfos.contains(info)){


            info.init();
            info.setGameRoom(this);
            if(info.getPlayer() instanceof Player) {
                TotalManager.getRoomManager().playerJoin.put(info.getPlayer().getName(),getRoomConfig().name);
            }
            playerInfos.add(info);

        }
        if(info.getPlayer() instanceof Player) {
            ((Player)info.getPlayer()).setGamemode(3);
        }

        info.setMoveSpeed(2);
        info.setPlayerType(PlayerInfo.PlayerType.WATCH);
        info.getPlayer().getInventory().setItem(RoomQuitItem.getIndex(),RoomQuitItem.get());
        info.getPlayer().getInventory().setItem(FollowItem.getIndex(), FollowItem.get());
        info.getPlayer().getInventory().setHeldItemSlot(0);
        sendMessage("&7"+info+"&7 成为了旁观者 （"+getWatchPlayers().size()+"）");
        info.sendMessage("&e你可以等待游戏结束 也可以手动退出游戏房间");
        if(isTeleport) {
            Position position = getTeamInfos().get(0).getSpawnLocation();
            position.add(0, 64, 0);
            position.level = getWorldInfo().getConfig().getGameWorld();
            info.getPlayer().teleport(position);
        }
    }

    public void joinWatch(PlayerInfo info) {
       joinWatch(info,true);

    }

    public static GameRoom enableRoom(GameRoomConfig roomConfig){

        if(roomConfig.getWorldInfo().getGameWorld() == null){
            return null;
        }
        if(WorldResetManager.RESET_QUEUE.containsKey(roomConfig)){
            return null;
        }
        return new GameRoom(roomConfig);
    }

    public JoinType joinPlayerInfo(PlayerInfo info,boolean sendMessage){
        if(WorldResetManager.RESET_QUEUE.containsKey(roomConfig)){
            return JoinType.NO_JOIN;
        }
        if(info.getGameRoom() == null){
            if(info.getPlayer() instanceof Player) {
                if(!((Player) info.getPlayer()).isOnline()){
                    return JoinType.NO_ONLINE;
                }
            }

            if(getType() != GameType.WAIT){
                if(getType() == GameType.END || getType() == GameType.CLOSE){
                    return JoinType.NO_JOIN;
                }
                return JoinType.CAN_WATCH;
            }
            if(getWorldInfo().getConfig().getGameWorld() == null || getWorldInfo().getConfig().getGameWorld().getSafeSpawn() == null){
                return JoinType.NO_LEVEL;
            }

            PlayerJoinRoomEvent event = new PlayerJoinRoomEvent(info,this,TotalManager.getPlugin());
            event.setSend(sendMessage);
            Server.getInstance().getPluginManager().callEvent(event);
            if(event.isCancelled()){
                return JoinType.NO_JOIN;
            }
            info.sendForceTitle("",1);
            info.sendForceSubTitle("");
            sendMessage(info+"&e加入了游戏 &7("+(playerInfos.size()+1)+"/"+getRoomConfig().getMaxPlayerSize()+")");
            info.init();
            if(roomConfig.teamConfigs.size() > 1) {
                info.getPlayer().getInventory().setItem(TeamChoseItem.getIndex(), TeamChoseItem.get());

            }
            info.getPlayer().getInventory().setItem(RoomQuitItem.getIndex(),RoomQuitItem.get());
            info.setPlayerType(PlayerInfo.PlayerType.WAIT);
            info.setGameRoom(this);
            if(info.getPlayer() instanceof Player) {
                TotalManager.getRoomManager().playerJoin.put(info.getPlayer().getName(),getRoomConfig().name);
            }
            playerInfos.add(info);
            info.getPlayer().teleport(getWorldInfo().getConfig().getWaitPosition());
            if(info.getPlayer() instanceof Player) {
                ((Player)info.getPlayer()).setGamemode(2);
            }
            if(isInit){
                isInit = false;
            }

        }else {
            if(info.getGameRoom().getType() != GameType.END && info.getGameRoom() == this){
                return JoinType.NO_JOIN;
            }else{
                info.getGameRoom().quitPlayerInfo(info,true);
                return JoinType.CAN_WATCH;
            }
        }
        return JoinType.CAN_JOIN;

    }

    public WorldInfo getWorldInfo() {
        return worldInfo;
    }

    public ArrayList<TeamInfo> getTeamInfos() {
        return teamInfos;
    }

    public void setType(GameType type) {
        this.type = type;
    }

    /**
     * 根据名称
     * */
    private TeamInfo getTeamInfo(String name){
        for(PlayerInfo info : playerInfos){
            if(info.getTeamInfo() != null &&
                    info.getTeamInfo().getTeamConfig().getName().equalsIgnoreCase(name)){
                return info.getTeamInfo();
            }
        }
        return null;
    }

    public enum JoinType{
        //加入类型
        NO_ONLINE,NO_JOIN,NO_LEVEL,CAN_WATCH,CAN_JOIN
    }

    /**
     * 分配玩家
     * */
    private boolean allotOfAverage(){

        int t =  (int) Math.ceil(playerInfos.size() / (double)getRoomConfig().getTeamConfigs().size());
        PlayerInfo listener;
        LinkedList<PlayerInfo> noTeam = getNoTeamPlayers();
        // TODO 检测是否一个队伍里有太多的人 拆掉多余的人
        for (TeamInfo manager: teamInfos){
            if(manager.getTeamPlayers().size() > t){
                int size = manager.getTeamPlayers().size() - t;
                for(int i = 0;i < size;i++){
                    PlayerInfo info = manager.getTeamPlayers().remove(manager.getTeamPlayers().size()-1);
                    noTeam.add(info);
                }
            }
        }
        while(noTeam.size() > 0){
            for (TeamInfo manager: teamInfos){
                if(manager.getTeamPlayers().size() == 0
                        || (manager.getTeamPlayers().size() < t )){
                    if(noTeam.size() > 0) {
                        listener = noTeam.poll();
                        manager.mjoin(listener);
                    }
                }else{
                    if(manager.getTeamPlayers().size() > t){
                        int size =  manager.getTeamPlayers().size();
                        LinkedList<PlayerInfo> playerInfos = new LinkedList<>(manager.getTeamPlayers());
                        for(int i = 0;i <size - t;i++) {
                            noTeam.add(playerInfos.pollLast());
                        }
                    }
                }
            }
        }
        return true;
    }


    public LinkedList<PlayerInfo> getNoTeamPlayers(){
        LinkedList<PlayerInfo> noTeam = new LinkedList<>();
        for(PlayerInfo playerInfo: playerInfos){
            if(playerInfo.getTeamInfo() == null){
                noTeam.add(playerInfo);
            }
        }
        return noTeam;
    }

    /**
     * 还在游戏内的玩家
     * */
    public ArrayList<PlayerInfo> getInRoomPlayers(){
        ArrayList<PlayerInfo> t = new ArrayList<>();
        for(PlayerInfo playerInfo: playerInfos){
            if(playerInfo.isInRoom()){
                t.add(playerInfo);
            }
        }
        return t;
    }


    public ArrayList<TeamInfo> getLiveTeam(){
        ArrayList<TeamInfo> t = new ArrayList<>();
        for(TeamInfo teamInfo: teamInfos){
            if(teamInfo.isLoading()){
                t.add(teamInfo);
            }
        }
        return t;
    }


    public ArrayList<PlayerInfo> getIPlayerInfos() {
        ArrayList<PlayerInfo> p = new ArrayList<>();
        for(PlayerInfo info: playerInfos){
            if(info.getPlayer() instanceof Player){
                if(!info.isLeave()) {
                    p.add(info);
                }
            }
        }
        return p;
    }
    /**
     * 旁观者们
     * */
    public ArrayList<PlayerInfo> getWatchPlayers(){
        ArrayList<PlayerInfo> t = new ArrayList<>();
        for(PlayerInfo playerInfo: playerInfos){
            if(playerInfo.isWatch()){
                t.add(playerInfo);
            }
        }
        return t;
    }

    /**
     * 还在游戏内的存活玩家
     * */
    public ArrayList<PlayerInfo> getLivePlayers(){
        ArrayList<PlayerInfo> t = new ArrayList<>();
        for(PlayerInfo playerInfo: playerInfos){
            if(playerInfo.isLive()){
                t.add(playerInfo);
            }
        }
        return t;
    }



    /**
     * 仅阵亡玩家观看
     * */
    public void sendMessageOnDeath(String msg){
        ArrayList<PlayerInfo> deathPlayer = new ArrayList<>();
        for(PlayerInfo info: playerInfos){
            if(info.isDeath()){
                deathPlayer.add(info);
            }
        }
        deathPlayer.forEach(dp -> dp.sendMessage(msg));
    }


    public void sendTipMessage(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendTipMessage(msg);
        }
    }

    public void sendMessage(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendMessage(msg);
        }
    }

    public void sendFaceMessage(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendForceMessage(msg);
        }
    }
    public void sendTitle(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendTitle(msg);
        }
    }
    public void sendSubTitle(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendSubTitle(msg);
        }
    }
    public void sendTip(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendTip(msg);
        }
    }

    public void sendActionBar(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendActionBar(msg);
        }
    }

    public void addSound(Sound sound){
        for(PlayerInfo info: getPlayerInfos()){
            info.addSound(sound);
        }
    }

    /**
     * 全队BUFF
     * */
    public void addEffect(Effect effect){
        for(PlayerInfo info: getLivePlayers()){
            info.addEffect(effect);
        }
    }

    /**
     * 玩家离开游戏
     * */
    public boolean quitPlayerInfo(PlayerInfo info,boolean teleport){
        if(info != null) {
            info.isLeave = true;
            if (info.getPlayer() instanceof Player) {
                if (playerInfos.contains(info)) {
                    PlayerQuitRoomEvent event = new PlayerQuitRoomEvent(info, this,TotalManager.getPlugin());
                    Server.getInstance().getPluginManager().callEvent(event);
                    if(((Player) info.getPlayer()).isOnline()) {
                        if (teleport) {
                            info.getPlayer().teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                        }
                        info.getPlayer().removeAllEffects();
                        ((Player) info.getPlayer()).setExperience(0, 0);
                    }
                    info.cancel();
                    TotalManager.getRoomManager().playerJoin.remove(info.getPlayer().getName());
                } else {
                    TotalManager.getRoomManager().playerJoin.remove(info.getPlayer().getName());

                }
            } else {
                info.getPlayer().close();
                playerInfos.remove(info);

            }
        }
        if (getIPlayerInfos().size() == 0) {
            onDisable();
        }
        return true;
    }

    /** 房间被实例化后 */
    public void onUpdate(){
        if(close){
            return;
        }
        //TODO 当房间启动后
        if(getIPlayerInfos().size() == 0 && !isInit){
            onDisable();
            return;
        }
        switch (type){
            case WAIT:
                onWait();
                break;
            case START:
                eventControl.enable = true;
                worldInfo.isStart = true;
                try {
                    onStart();
                }catch (Exception e){
                    e.printStackTrace();
                    for(PlayerInfo playerInfo: new ArrayList<>(playerInfos)){
                        playerInfo.sendForceMessage("房间出现异常 请联系服主/管理员修复");
                    }
                    onDisable();
                    return;
                }

                break;
            case END:
                //TODO 房间结束
                onEnd();
                break;
            case CLOSE:
                onDisable();
                break;
            default:break;
        }

        //移除编外人员
        for(PlayerInfo info: getInRoomPlayers()){
            if(!TotalManager.getRoomManager().playerJoin.containsKey(info.getPlayer().getName())){
                playerInfos.remove(info);
            }
        }

    }

    private void onEnd() {
        if(loadTime == -1){
            loadTime = 10;
        }

        for(PlayerInfo playerInfo:getLivePlayers()){
            Utils.spawnFirework(playerInfo.getPosition());
        }

        if(loadTime == 0){
            type = GameType.CLOSE;

        }

    }

    /**
     * 执行这个可以将游戏直接结束
     * 传入胜利的队伍
     * */
    public void gameEnd(TeamInfo teamInfo,boolean more){
        if(!more){
            teamInfo.echoDefeat();
        }
        teamInfo.echoVictory();


        type = GameType.END;
        worldInfo.setClose(true);
        loadTime = 5;
    }

    private void onStart() {
        hasStart = true;
        eventControl.run();
        if(loadTime == -1 && teamAll){
            //TODO 房间首次重置
            for(FloatTextInfoConfig config: roomConfig.floatTextInfoConfigs){
                FloatTextInfo info = new FloatTextInfo(config).init(this);
                if(info != null){
                    floatTextInfos.add(info);
                }
            }
            //TODO 当房间开始

            for(PlayerInfo i : getPlayerInfos()){
                try {
                    i.spawn();
                }catch (Exception e){
                    i.sendForceMessage("&c出现未知原因影响导致无法正常传送 正在重新将你移动中");
                    try {
                        i.spawn();
                    }catch (Exception e1){
                        i.sendForceMessage("&c移动失败 请尝试重新进入游戏");
                        quitPlayerInfo(i,true);
                    }
                }
            }
            sendTitle("&c游戏开始");

            loadTime = getRoomConfig().time;
            worldInfo = new WorldInfo(this,getRoomConfig().worldInfo);
            GameRoomStartEvent event = new GameRoomStartEvent(this,TotalManager.getPlugin());
            Server.getInstance().getPluginManager().callEvent(event);

        }
        //TODO 可以在这里实现胜利的条件
        ////////////////////////// 示例算法 ///////////////////////////
        demoGameEnd();
        ////////////////////////// 示例算法 ///////////////////////////
    }

    /**
     * 游戏结束的条件示例代码
     * 可参考实现自己的逻辑
     * 这个方法的主要实现逻辑为 PVP，存活到最后为胜利条件
     * */
    private void demoGameEnd(){
        if(loadTime > 0) {
            //TODO 在房间倒计时内
            for (TeamInfo teamInfo : teamInfos) {
                teamInfo.onUpdate();
            }
            if(getRoomConfig().teamConfigs.size() > 1) {
                if (getLiveTeam().size() == 1) {
                    //当有多个队伍的时候 只剩余一个队伍时将这个队伍中所有的玩家都扔进 胜利的玩家列表。
                    TeamInfo teamInfo = getLiveTeam().get(0);
                    teamInfo.getVictoryPlayers().addAll(teamInfo.getTeamPlayers());
                    gameEnd(teamInfo,true);
                }
            }else{
                //当仅有一个队伍时，把最终存活的玩家放到胜利列表中
                TeamInfo teamInfo = getTeamInfos().get(0);
                ArrayList<PlayerInfo> pl = teamInfo.getLivePlayer();
                //判断是否为唯一幸存者
                if(pl.size() == 1){
                    teamInfo.getVictoryPlayers().add(pl.get(0));
                    gameEnd(teamInfo,false);
                }
            }
        } else{
            //TODO 在房间倒计时结束
            TeamInfo successInfo;
            if(getRoomConfig().teamConfigs.size() > 1) {
                //在这个判断条件下为多队伍状态
                //TODO 倒计时结束后 找到血量最高的队伍判胜
                ArrayList<TeamInfo> teamInfos = getLiveTeam();
                if (teamInfos.size() > 0) {
                    int pl = 0;
                    double dh = 0;
                    successInfo = teamInfos.get(0);
                    for (TeamInfo info : teamInfos) {
                        ArrayList<PlayerInfo> successInfos = info.getLivePlayer();
                        if (successInfos.size() > pl) {
                            pl = successInfos.size();
                            successInfo = info;
                            dh = info.getAllHealth();

                        }else if(successInfos.size() == pl && pl > 0){
                            double dh2 = info.getAllHealth();
                            if(dh2 > dh){
                                successInfo = info;
                                dh = dh2;
                            }
                        }
                    }
                    successInfo.getVictoryPlayers().addAll(successInfo.getTeamPlayers());
                    gameEnd(successInfo,true);
                }
            }else{
                //在这个判断条件下为单队伍状态
                //TODO 倒计时结束后 找到血量最高的玩家判胜，其余玩家均失败
                double h = 0;
                PlayerInfo successPlayerInfo = null;
                TeamInfo teamInfo = getTeamInfos().get(0);
                for(PlayerInfo info: teamInfo.getLivePlayer()){
                    if(info.player.getHealth() > h){
                        successPlayerInfo = info;
                        h = info.player.getHealth();
                    }
                }
                if(successPlayerInfo == null){
                    successPlayerInfo = teamInfo.getLivePlayer().get(0);
                }
                teamInfo.getVictoryPlayers().add(successPlayerInfo);
                for(PlayerInfo info: teamInfo.getLivePlayer()){
                    if(!info.equals(successPlayerInfo)){
                        teamInfo.getDefeatPlayers().add(info);
                    }
                }
                //游戏结束
                gameEnd(teamInfo,false);
            }


        }
    }


    private void onWait() {
        if(getPlayerInfos().size() >= getRoomConfig().minPlayerSize){
            if(loadTime == -1){
                loadTime = getRoomConfig().waitTime;
                sendMessage("&2到达最低人数限制&e "+loadTime+" &2秒后开始游戏");

            }
        }else {
            loadTime = -1;
        }
        if(getPlayerInfos().size() == getRoomConfig().getMaxPlayerSize()){
            if(!isMax){
                isMax = true;
                loadTime = getRoomConfig().getMaxWaitTime();
            }
        }
        if(loadTime >= 1) {
            sendTip("&e距离开始还剩 &a " + loadTime + " &e秒");
            if(loadTime <= 5){
                switch (loadTime){
                    case 5: sendTitle("&a5");break;
                    case 4: sendTitle("&e4");break;
                    case 3: sendTitle("&63");break;
                    case 2: sendTitle("&42");break;
                    case 1: sendTitle("&41");break;
                    default:
                        sendTitle("");break;

                }
                //音效
                addSound(Sound.RANDOM_CLICK);

            }
            if(loadTime == 1){
                type = GameType.START;
                loadTime = -1;
                if(allotOfAverage()){
                    teamAll = true;
                }


            }
        }else{
            sendTip("&a等待中");
        }
    }

    /**
     * 关闭房间
     * 已设计好算法，不建议修改
     * */
    public void onDisable(){
        if(close){
            return;
        }
        close = true;
        type = GameType.CLOSE;
        if(hasStart) {
            roomConfig.save();
            GameCloseEvent event = new GameCloseEvent(this, TotalManager.getPlugin());
            Server.getInstance().getPluginManager().callEvent(event);
            worldInfo.setClose(true);
            //房间结束后的执行逻辑
            if(getRoomConfig().isAutomaticNextRound){
                sendMessage("&7即将自动进行下一局");
                for(PlayerInfo playerInfo: getInRoomPlayers()){
                    RandomJoinManager.joinManager.nextJoin(playerInfo);
                }
            }
            //TODO 房间被关闭 释放一些资源
            for (PlayerInfo info : playerInfos) {
                info.clear();
                if (info.getPlayer() instanceof Player) {
                    quitPlayerInfo(info, true);
                }
            }

            //浮空字释放
            for(FloatTextInfo floatTextInfo: floatTextInfos){
                floatTextInfo.gameFloatText.toClose();
            }

            String level = worldInfo.getConfig().getLevel();
            Level level1 = getWorldInfo().getConfig().getGameWorld();
            for(Entity entity: new CopyOnWriteArrayList<>(level1.getEntities())){
                if(entity instanceof Player){
                    //这里出现的玩家就是没有清出地图的玩家
                    entity.teleport(Server.getInstance().getDefaultLevel().getSpawnLocation());
                    TotalManager.getRoomManager().playerJoin.remove(entity.getName());
                    ((Player) entity).setGamemode(0);
                    entity.removeAllEffects();
                    ((Player) entity).getInventory().clearAll();
                    ((Player) entity).getEnderChestInventory().clearAll();
                    ((Player) entity).getFoodData().reset();
                    continue;
                }
                if(entity != null && !entity.isClosed()){
                    entity.close();
                }

            }
            //卸载区块就炸...
//            level1.unloadChunks();
            worldInfo.setClose(true);
            worldInfo = null;
            WorldResetManager.RESET_QUEUE.put(getRoomConfig(),level);
        }else{
            worldInfo.setClose(true);
            worldInfo = null;
            TotalManager.getRoomManager().getRooms().remove(getRoomConfig().name);
            RoomManager.LOCK_GAME.remove(getRoomConfig());
        }

    }

    /**
     * 设置资源箱的物品
     * */
    public LinkedHashMap<Integer, Item> getRandomItem(int size, Block block){
        LinkedHashMap<Integer,Item> itemLinkedHashMap = new LinkedHashMap<>();
        if(worldInfo == null){
            return itemLinkedHashMap;
        }
        if(!worldInfo.clickChest.contains(block)){
            List<Item> list = getRoundItems(block);
            if(list.size() > 0) {
                for (int i = 0; i < size; i++) {
                    if (Utils.rand(0, 100) <= getRoomConfig().getRound()) {
                        itemLinkedHashMap.put(i, list.get(new Random().nextInt(list.size())));
                    }
                }
                worldInfo.clickChest.add(block);
            }
        }
        return itemLinkedHashMap;

    }

    public List<Item> getRoundItems(Block block){
        if(roomConfig.items.containsKey(block.getId()+"")){
            return roomConfig.items.get(block.getId()+"").items;
        }
        return new ArrayList<>();
    }



}
