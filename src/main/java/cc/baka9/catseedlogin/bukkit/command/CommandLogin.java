package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.CryptUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

import static vip.floatationdevice.msu.I18nUtil.translate;

public class CommandLogin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args){
        if (args.length == 0 || !(sender instanceof Player)) return false;
        Player player = (Player) sender;
        String name = player.getName();
        if (LoginPlayerHelper.isLoggedIn(name)) {
            sender.sendMessage(translate("login-already"));
            return true;
        }
        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) {
            sender.sendMessage(translate("not-registered"));
            return true;
        }
        if (Objects.equals(CryptUtil.encrypt(name, args[0]), lp.getPassword().trim())) {
            LoginPlayerHelper.add(lp);
            CatSeedPlayerLoginEvent loginEvent = new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.SUCCESS);
            Bukkit.getServer().getPluginManager().callEvent(loginEvent);
            sender.sendMessage(translate("login-success"));
            player.updateInventory();
            LoginPlayerHelper.recordCurrentIP(player, lp);
            if (Config.Settings.backAfterLogin && Config.Settings.noMoveBeforeLogin)
                Config.getOfflineLocation(player).ifPresent(player::teleport);
        } else {
            sender.sendMessage(translate("login-wrong-password"));
            CatSeedPlayerLoginEvent loginEvent = new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.FAIL);
            Bukkit.getServer().getPluginManager().callEvent(loginEvent);
            if (Config.EmailVerify.enabled)
                sender.sendMessage(translate("login-wrong-password-can-repw"));
        }
        return true;
    }
}
