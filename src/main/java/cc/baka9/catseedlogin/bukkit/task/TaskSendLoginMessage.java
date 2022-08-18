package cc.baka9.catseedlogin.bukkit.task;

import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static cc.baka9.catseedlogin.bukkit.I18nManager.translate;


public class TaskSendLoginMessage extends Task {

    @Override
    public void run(){
        if (!Cache.isLoaded) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!LoginPlayerHelper.isLogin(player.getName())) {
                if (!LoginPlayerHelper.isRegister(player.getName())) {
                    player.sendMessage(translate("please-register"));
                    continue;
                }
                player.sendMessage(translate("please-login"));
            }
        }
    }
}
