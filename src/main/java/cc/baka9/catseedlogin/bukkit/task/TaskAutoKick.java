package cc.baka9.catseedlogin.bukkit.task;

import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static vip.floatationdevice.msu.I18nUtil.translate;

public class TaskAutoKick extends Task {
    public Map<String, Long> playerJoinTime = new HashMap<>();

    @Override
    public void run(){
        if (!Cache.isLoaded || Config.Settings.loginTimeout < 1) return;
        long loginTimeoutMs = Config.Settings.loginTimeout * 1000L;
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerName = player.getName();
            if (!LoginPlayerHelper.isLoggedIn(playerName)) {
                if (playerJoinTime.containsKey(playerName)) {
                    if (now - playerJoinTime.get(playerName) > loginTimeoutMs) {
                        player.kickPlayer(translate("login-timed-out").replace("{time}", Config.Settings.loginTimeout + ""));
                    }
                } else {
                    playerJoinTime.put(playerName, now);
                }
            } else {
                playerJoinTime.remove(playerName);
            }
        }
    }
}
