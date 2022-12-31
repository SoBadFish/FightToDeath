package org.sobadfish.fighttodeath.manager.data;


import org.sobadfish.fighttodeath.entity.GameFloatText;
import org.sobadfish.fighttodeath.manager.BaseDataWriterGetterManager;
import org.sobadfish.fighttodeath.top.TopItem;
import org.sobadfish.fighttodeath.top.TopItemInfo;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerTopManager extends BaseDataWriterGetterManager<TopItem> {


    public List<TopItemInfo> topItemInfos = new CopyOnWriteArrayList<>();


    public File file;

    public PlayerTopManager(List<TopItem> topItems,File file){
        super(topItems,file);
    }


    /**
     * 排行榜初始化
     * */
    public void init(){
        for(TopItem topItem: dataList){
            GameFloatText floatText = GameFloatText.showFloatText(topItem.name,topItem.getPosition(),"");
            if(floatText == null){
                continue;
            }
            topItemInfos.add(new TopItemInfo(topItem,floatText));
        }

    }

    /**
     * 判断是否存在该名称的排行榜
     * @param top 排行榜名称
     * @return 是否存在排行榜
     * */
    public boolean hasTop(String top){
        for(TopItem topItem: dataList){
            if(topItem.name.equals(top)){
                return true;
            }
        }
        return false;
    }

    /**
     * 移除排行榜
     * @param topItem 排行榜数据
     * */
    public void removeTopItem(TopItem topItem){
        dataList.remove(topItem);
        TopItemInfo topItemInfo = new TopItemInfo(topItem,null);
        if(topItemInfos.contains(topItemInfo)){
            topItemInfo = topItemInfos.get(topItemInfos.indexOf(topItemInfo));
            GameFloatText floatText = topItemInfo.floatText;
            if(floatText != null){
                floatText.toClose();
            }
        }
        topItemInfos.remove(topItemInfo);
    }

    /**
     * 添加排行榜
     * @param topItem 排行榜数据
     * */
    public void addTopItem(TopItem topItem){

        GameFloatText floatText = GameFloatText.showFloatText(topItem.name,topItem.getPosition(),"");
        if(floatText == null){
            return;
        }
        topItemInfos.add(new TopItemInfo(topItem,floatText));
        dataList.add(topItem);
    }

    public static PlayerTopManager asFile(File file){
        return (PlayerTopManager) BaseDataWriterGetterManager.asFile(file,"top.json",TopItem[].class,PlayerTopManager.class);
    }


    /**
     * 根据名称获取排行榜
     * @param name 排行榜名称
     * @return 排行榜数据
     * */
    public TopItem getTop(String name) {
        for(TopItem topItem: dataList){
            if(topItem.name.equalsIgnoreCase(name)){
                return topItem;
            }
        }
        return null;
    }
}
