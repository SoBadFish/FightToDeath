package org.sobadfish.fighttodeath;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.fighttodeath.command.GameAdminCommand;
import org.sobadfish.fighttodeath.command.GameCommand;
import org.sobadfish.fighttodeath.command.GameSpeakCommand;
import org.sobadfish.fighttodeath.manager.TotalManager;

/**

 * @author Sobadfish
 * 13:07
 */
public class FightToDeathMain extends PluginBase {



    @Override
    public void onEnable() {

        //字符生成地址 http://www.network-science.de/ascii/
        //Font: small
        this.getLogger().info(TextFormat.colorize('&',"&e ___ _      _   _  _____    ___           _   _    "));
        this.getLogger().info(TextFormat.colorize('&',"&e| __(_)__ _| |_| ||_   _|__|   \\ ___ __ _| |_| |_  "));
        this.getLogger().info(TextFormat.colorize('&',"&e| _|| / _` | ' \\  _|| |/ _ \\ |) / -_) _` |  _| ' \\ "));
        this.getLogger().info(TextFormat.colorize('&',"&e|_| |_\\__, |_||_\\__||_|\\___/___/\\___\\__,_|\\__|_||_|"));
        this.getLogger().info(TextFormat.colorize('&',"&e      |___/                                        "));
        this.getLogger().info(TextFormat.colorize('&',"&e正在加载"+TotalManager.GAME_NAME+" 插件 本版本为&av"+this.getDescription().getVersion()));
        this.getLogger().info(TextFormat.colorize('&',"&a插件加载完成，祝您使用愉快"));

        TotalManager.init(this);
        this.getServer().getCommandMap().register(TotalManager.GAME_NAME,new GameAdminCommand(TotalManager.COMMAND_ADMIN_NAME));
        this.getServer().getCommandMap().register(TotalManager.GAME_NAME,new GameCommand(TotalManager.COMMAND_NAME));
        this.getServer().getCommandMap().register(TotalManager.GAME_NAME,new GameSpeakCommand(TotalManager.COMMAND_MESSAGE_NAME));

        this.getLogger().info(TextFormat.colorize('&',"&a插件加载完成，祝您使用愉快"));

    }

    @Override
    public void onDisable() {
       TotalManager.onDisable();
    }


//    public enum UiType{
//        /**
//         * auto: 自动
//         *
//         * packet: GUI界面
//         *
//         * ui: 箱子界面
//         * */
//        AUTO,PACKET,UI
//    }
}
