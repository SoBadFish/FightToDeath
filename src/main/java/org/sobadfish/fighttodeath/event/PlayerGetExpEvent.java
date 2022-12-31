package org.sobadfish.fighttodeath.event;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

/**
 * 玩家获取经验的事件
 * */
public class PlayerGetExpEvent extends Event  {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlers() {
        return HANDLERS;
    }

    private final String playerName;

    private final int exp;

    private final int newExp;

    private final String cause;

    public PlayerGetExpEvent(String playerName, int exp, int newExp, String cause){
        this.playerName = playerName;
        this.exp = exp;
        this.newExp = newExp;
        this.cause = cause;
    }

    public String getCause() {
        return cause;
    }

    public int getExp() {
        return exp;
    }

    public int getNewExp() {
        return newExp;
    }

    public String getPlayerName() {
        return playerName;
    }



}
