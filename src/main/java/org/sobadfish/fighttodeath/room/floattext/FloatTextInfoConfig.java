package org.sobadfish.fighttodeath.room.floattext;

import java.util.LinkedHashMap;
import java.util.Map;

public class FloatTextInfoConfig {

    public String name;

    public String position;

    public String text;

    //
    public FloatTextInfoConfig(String name,String position,String text){
        this.name = name;
        this.position = position;
        this.text = text;
    }


    public static FloatTextInfoConfig build(Map map){
        String name = "";
        String pos = "";
        String text = "";
        if(map.containsKey("name")){
            name = map.get("name").toString();
        }
        if(map.containsKey("position")){
            pos = map.get("position").toString();
        }
        if(map.containsKey("text")){
            text = map.get("text").toString();
        }
        if(pos.equalsIgnoreCase("") || name.equalsIgnoreCase("")){
            return null;
        }
//        Position position = WorldInfoConfig.getPositionByString(pos);
        return new FloatTextInfoConfig(name,pos,text);
    }

    public Map<String,Object> toConfig(){
        Map<String,Object> conf = new LinkedHashMap<>();
        conf.put("name", name);
        conf.put("position", position);
        conf.put("text", text);
        return conf;
    }
}
