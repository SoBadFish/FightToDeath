package org.sobadfish.fighttodeath.thread;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import cn.nukkit.scheduler.AsyncTask;
import org.sobadfish.fighttodeath.entity.GameFloatText;
import org.sobadfish.fighttodeath.manager.*;
import org.sobadfish.fighttodeath.room.GameRoom;
import org.sobadfish.fighttodeath.room.config.GameRoomConfig;
import org.sobadfish.fighttodeath.room.config.WorldInfoConfig;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Sobadfish
 */
public class PluginMasterRunnable extends ThreadManager.AbstractBedWarRunnable {

    private long loadTime = 0;

    //浮空字一秒更新一次会跳
    private int update = 0;


    @Override
    public GameRoom getRoom() {
        return null;
    }

    @Override
    public String getThreadName() {
        String color = "&a";
        if(isClose){
            color = "&7";
        }
        StringBuilder s = new StringBuilder(color + "插件主进程  浮空字 &7(" +
                FloatTextManager.floatTextList.size() + ") &a" + loadTime + " ms\n");
        for(GameFloatText floatText:FloatTextManager.floatTextList){
            s.append("&r   - ").append(floatText.name).append(" &7pos=(")
                    .append(floatText.getFloorX()).append(":")
                    .append(floatText.getFloorY()).append(":")
                    .append(floatText.getFloorZ()).append(":")
                    .append(floatText.getLevel().getFolderName()).append(")\n");
        }

        return s.toString();
    }

    @Override
    public void run() {
        long t1 = System.currentTimeMillis();
        update ++;
        try {
            if (isClose) {
                ThreadManager.cancel(this);
            }

            if (TotalManager.isDisabled()) {
                isClose = true;
                return;
            }
            for (Player player : new ArrayList<>(Server.getInstance().getOnlinePlayers().values())) {
                for (GameFloatText floatText : new CopyOnWriteArrayList<>(FloatTextManager.floatTextList)) {
                    if (floatText == null) {
                        continue;
                    }

                    if (floatText.isFinalClose) {
                        FloatTextManager.removeFloatText(floatText);
                        continue;
                    }
                    if (floatText.player.contains(player)) {
                        if (!player.getLevel().getFolderName().equalsIgnoreCase(floatText.getPosition().getLevel().getFolderName()) || !player.isOnline()) {
                            if (!floatText.closed) {
                                RemoveEntityPacket rp = new RemoveEntityPacket();
                                rp.eid = floatText.getId();
                                player.dataPacket(rp);
                            }
                            floatText.player.remove(player);
                        }
                    }
                    if (player.getLevel().getFolderName().equalsIgnoreCase(floatText.getPosition().getLevel().getFolderName())) {
                        if(!floatText.player.contains(player)){
                            floatText.player.add(player);
                        }

                    }
                    if(update > 5){
                        floatText.disPlayers();
                        update = 0;
                    }

                }

            }
            Server.getInstance().getScheduler().scheduleAsyncTask(TotalManager.getPlugin(), new AsyncTask() {
                @Override
                public void onRun() {
                    List<GameRoomConfig> bufferQueue = new ArrayList<>();
                    try {
                        for(Map.Entry<GameRoomConfig,String> map: WorldResetManager.RESET_QUEUE.entrySet()){
                            if (WorldInfoConfig.toPathWorld(map.getKey().getName(), map.getValue())) {
                                TotalManager.sendMessageToConsole("&a" + map.getKey().getName() + " 地图已还原");
                            }
                            Server.getInstance().loadLevel(map.getValue());
                            TotalManager.sendMessageToConsole("&r释放房间 " + map.getKey().getName());
                            TotalManager.sendMessageToConsole("&r房间 " + map.getKey().getName() + " 已回收");
                            bufferQueue.add(map.getKey());
                        }
                        //TODO 从列表中移除
                        for(GameRoomConfig config: bufferQueue){
                            TotalManager.getRoomManager().getRooms().remove(config.getName());
                            RoomManager.LOCK_GAME.remove(config);
                            WorldResetManager.RESET_QUEUE.remove(config);
                        }
                    } catch (Exception e) {
                        TotalManager.sendMessageToConsole("&c释放房间出现了一个小问题，导致无法正常释放,已将这个房间暂时锁定");
                    }
                }
            });


        }catch (Exception e){
            e.printStackTrace();
        }
        loadTime = System.currentTimeMillis() - t1;
    }


}
