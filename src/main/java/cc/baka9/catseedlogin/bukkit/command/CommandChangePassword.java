package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.Crypt;
import cc.baka9.catseedlogin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static cc.baka9.catseedlogin.bukkit.I18nManager.translate;

public class CommandChangePassword implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args){
        if (args.length != 3 || !(sender instanceof Player)) {
            return false;
        }
        String name = sender.getName();
        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) {
            sender.sendMessage(translate("not-registered"));
            return true;
        }
        if (!LoginPlayerHelper.isLoggedIn(name)) {
            sender.sendMessage(translate("not-logged-in"));
            return true;
        }
        if (!Objects.equals(Crypt.encrypt(name, args[0]), lp.getPassword().trim())) {
            sender.sendMessage(translate("chpw-incorrect-old-password"));
            return true;

        }
        if (!args[1].equals(args[2])) {
            sender.sendMessage(translate("chpw-new-password-not-same"));
            return true;
        }
        if (!Util.passwordIsDifficulty(args[1])) {
            sender.sendMessage(translate("password-too-weak"));
            return true;
        }
        if (!Cache.isLoaded) {
            return true;
        }
        sender.sendMessage(translate("please-wait"));
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                lp.setPassword(args[1]);
                lp.crypt();
                CatSeedLogin.sql.edit(lp);
                LoginPlayerHelper.remove(lp);

                Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> {
                    Player player = Bukkit.getPlayer(((Player) sender).getUniqueId());
                    if (player != null && player.isOnline()) {
                        player.sendMessage(translate("chpw-success"));
                        Config.setOfflineLocation(player);
                        if (Config.Settings.CanTpSpawnLocation) {
                            player.teleport(Config.Settings.SpawnLocation);
                            if (CatSeedLogin.loadProtocolLib) {
                                LoginPlayerHelper.sendBlankInventoryPacket(player);
                            }
                        }

                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(translate("internal-error"));
            }
        });
        return true;
    }
}
