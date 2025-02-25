package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerRegisterEvent;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static cc.baka9.catseedlogin.bukkit.Config.Settings.*;
import static vip.floatationdevice.msu.I18nUtil.translate;

public class CommandRegister implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args){
        if (args.length != 2) return false;
        Player player = (Player) sender;
        String name = sender.getName();
        if (LoginPlayerHelper.isLoggedIn(name)) {
            sender.sendMessage(translate("reg-already-registered"));
            return true;
        }
        if (LoginPlayerHelper.isRegistered(name)) {
            sender.sendMessage(translate("reg-player-name-taken"));
            return true;
        }
        if (!args[0].equals(args[1])) {
            sender.sendMessage(translate("reg-password-not-same"));
            return true;
        }
        if (forceStrongPassword && !CommonUtil.isStrongPassword(args[0])) {
            sender.sendMessage(translate("password-too-weak").replace("{maxPwdLen}", String.valueOf(maxPasswordLength)));
            return true;
        }
        if (!Cache.isLoaded) {
            return true;
        }
        sender.sendMessage(translate("please-wait"));
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                String currentIp = player.getAddress().getAddress().getHostAddress();
                List<LoginPlayer> LoginPlayerListlikeByIp = CatSeedLogin.sql.getLikeByIp(currentIp);
                if (LoginPlayerListlikeByIp.size() >= maxRegPerIP) {
                    sender.sendMessage(translate("reg-accounts-limit-exceeded")
                            .replace("{count}", String.valueOf(LoginPlayerListlikeByIp.size()))
                            .replace("{accounts}", String.join(", ", LoginPlayerListlikeByIp.stream().map(LoginPlayer::getName).toArray(String[]::new))));
                } else {
                    LoginPlayer lp = new LoginPlayer(name, args[0]);
                    lp.crypt();
                    CatSeedLogin.sql.add(lp);
                    LoginPlayerHelper.add(lp);
                    Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> {
                        CatSeedPlayerRegisterEvent event = new CatSeedPlayerRegisterEvent(Bukkit.getPlayer(sender.getName()));
                        Bukkit.getServer().getPluginManager().callEvent(event);
                    });
                    sender.sendMessage(translate("reg-success"));
                    player.updateInventory();
                    LoginPlayerHelper.recordCurrentIP(player, lp);
                }


            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(translate("internal-error"));
            }
        });
        return true;

    }
}
