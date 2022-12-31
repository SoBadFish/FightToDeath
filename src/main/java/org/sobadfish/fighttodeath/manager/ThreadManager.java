package org.sobadfish.fighttodeath.manager;


import org.sobadfish.fighttodeath.room.GameRoom;
import org.sobadfish.fighttodeath.thread.PluginMasterRunnable;
import org.sobadfish.fighttodeath.thread.RandomJoinRunnable;
import org.sobadfish.fighttodeath.thread.RoomLoadRunnable;
import org.sobadfish.fighttodeath.thread.TopRunnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程管理器
 * 不建议更改
 * @author SoBadFish
 * 2022/1/2
 */
public class ThreadManager {


    public static final List<AbstractBedWarRunnable> RUNNABLES = new CopyOnWriteArrayList<>();

    // 线程池核心线程数
    private final static Integer CORE_POOLSIZE = 5;


    private static final ScheduledThreadPoolExecutor SCHEDULED = new ScheduledThreadPoolExecutor(CORE_POOLSIZE,new ThreadPoolExecutor.AbortPolicy());


    public static void cancel(AbstractBedWarRunnable r) {
        RUNNABLES.remove(r);
        SCHEDULED.remove(r);
    }

    private static void schedule(AbstractBedWarRunnable r,int delay) {
        RUNNABLES.add(r);
        SCHEDULED.scheduleAtFixedRate(r,delay,1,TimeUnit.SECONDS);
    }

    private static void schedule(AbstractBedWarRunnable r) {
        schedule(r,0);
    }


    /**
     * 获取当前线程池线程数量
     */
    public static int getScheduledSize() {
        return SCHEDULED.getPoolSize();
    }

    /**
     * 获取当前活动的线程数量
     */
    public static int getScheduledActiveCount() {
        return SCHEDULED.getActiveCount();
    }




    public static String info() {
        StringBuilder builder = new StringBuilder();
        Map<String, List<AbstractBedWarRunnable>> map = getRunnables();
        for(Map.Entry<String, List<AbstractBedWarRunnable>> me : map.entrySet()){
            builder.append("&r").append(me.getKey()).append("\n").append(listToString(me.getValue()));
        }
        String s = builder.toString();
        if("".equalsIgnoreCase(s)){
            return "null";
        }
        return s;
    }

    private static String listToString(List<AbstractBedWarRunnable> runnables){
        StringBuilder s = new StringBuilder();
        for(AbstractBedWarRunnable runnable: runnables){
            s.append("  &r- ").append(runnable.getThreadName()).append("\n");
        }
        return s.toString();
    }

    private static Map<String,List<AbstractBedWarRunnable>> getRunnables(){
        LinkedHashMap<String, List<AbstractBedWarRunnable>> threadList = new LinkedHashMap<>();

        for(AbstractBedWarRunnable workerValue: RUNNABLES) {
            GameRoom room = workerValue.getRoom();
            if (room != null) {
                if (!threadList.containsKey(room.getRoomConfig().name)) {
                    threadList.put(room.getRoomConfig().name, new ArrayList<>());
                }
                List<AbstractBedWarRunnable> runnables = threadList.get(room.getRoomConfig().name);
                runnables.add(workerValue);
                threadList.put(room.getRoomConfig().name, runnables);
            } else {
                String name = "Unknown";
                if (!threadList.containsKey(name)) {
                    threadList.put(name, new ArrayList<>());
                }
                List<AbstractBedWarRunnable> runnables = threadList.get(name);
                runnables.add(workerValue);
                threadList.put(name, runnables);
            }
        }
        return threadList;
    }

    public static void init() {
        ThreadManager.schedule(new RunnableCheck());
        ThreadManager.schedule(new PluginMasterRunnable(),1);
        ThreadManager.schedule(new RoomLoadRunnable());
        ThreadManager.schedule(new TopRunnable());
        ThreadManager.schedule(new RandomJoinRunnable());

    }


    public abstract static class AbstractBedWarRunnable implements Runnable{

        public boolean isClose;

        abstract public GameRoom getRoom();

        abstract public String getThreadName();

        public boolean isClose() {
            return isClose;
        }
    }

    public static class RunnableCheck extends AbstractBedWarRunnable{
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
            return color+"线程检测";
        }

        @Override
        public void run() {
            if(isClose){
                return;
            }
            if(TotalManager.isDisabled()){
                isClose = true;
                return;
            }
            for (AbstractBedWarRunnable runnable : RUNNABLES) {
                if (runnable.isClose) {
                    cancel(runnable);
                }
            }
        }
    }
}
