package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.command.*;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.database.MySQL;
import cc.baka9.catseedlogin.bukkit.database.SQL;
import cc.baka9.catseedlogin.bukkit.database.SQLite;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.task.Task;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static cc.baka9.catseedlogin.bukkit.I18nManager.translate;

public class CatSeedLogin extends JavaPlugin {

    public static CatSeedLogin instance;
    public static BukkitScheduler scheduler = Bukkit.getScheduler();
    public static SQL sql;
    public static boolean loadProtocolLib = false;

    @Override
    public void onEnable(){
        getLogger().info("CatSeedLogin version " + getDescription().getVersion());
        getLogger().info("Modified by MCUmbrella");
        instance = this;
        //Config
        try {
            Config.load();
            saveResource("settings.example.yml", true);
        } catch (Exception e) {
            getServer().getLogger().severe("Failed to load config file!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        sql = Config.MySQL.Enable ? new MySQL(this) : new SQLite(this);
        try {
            sql.init();
            Cache.refreshAll();
        } catch (Exception e) {
            getLogger().severe(translate("database-error"));
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //Listeners
        getServer().getPluginManager().registerEvents(new Listeners(), this);

        //ProtocolLibListeners
        try {
            Class.forName("com.comphenix.protocol.ProtocolLib");
            ProtocolLibListeners.enable();
            loadProtocolLib = true;
        } catch (ClassNotFoundException e) {
            getLogger().warning(translate("no-protocolLib"));
        }

        // bc
        if (Config.BungeeCord.Enable) {
            Communication.socketServerStartAsync();
        }

        //Commands
        getServer().getPluginCommand("login").setExecutor(new CommandLogin());
        getServer().getPluginCommand("login").setTabCompleter((commandSender, command, s, args)
                -> args.length == 1 ? Collections.singletonList("密码") : new ArrayList<>(0));

        getServer().getPluginCommand("register").setExecutor(new CommandRegister());
        getServer().getPluginCommand("register").setTabCompleter((commandSender, command, s, args)
                -> args.length == 1 ? Collections.singletonList("密码 重复密码") : new ArrayList<>(0));

        getServer().getPluginCommand("changepassword").setExecutor(new CommandChangePassword());
        getServer().getPluginCommand("changepassword").setTabCompleter((commandSender, command, s, args)
                -> args.length == 1 ? Collections.singletonList("旧密码 新密码 重复新密码") : new ArrayList<>(0));

        PluginCommand bindemail = getServer().getPluginCommand("bindemail");
        bindemail.setExecutor(new CommandBindEmail());
        bindemail.setTabCompleter((commandSender, command, s, args) -> {
            if (args.length == 1) {
                return Arrays.asList("set 需要绑定的邮箱", "verify 邮箱验证码");
            }
            if (args.length == 2) {
                if (args[0].equals("set")) {
                    return Collections.singletonList("需要绑定的邮箱");
                }
                if (args[0].equals("verify")) {
                    return Collections.singletonList("邮箱获取的验证码");
                }
            }
            return Collections.emptyList();
        });
        PluginCommand resetpassword = getServer().getPluginCommand("resetpassword");
        resetpassword.setExecutor(new CommandResetPassword());
        resetpassword.setTabCompleter((commandSender, command, s, args) -> {
            if (args.length == 1) {
                return Arrays.asList("forget", "re 验证码 新密码");
            }
            if (args[0].equals("re")) {
                if (args.length == 2) {
                    return Collections.singletonList("验证码 新密码");
                }
                if (args.length == 3) {
                    return Collections.singletonList("新密码");
                }
            }
            return Collections.emptyList();
        });
        PluginCommand catseedlogin = getServer().getPluginCommand("catseedlogin");
        catseedlogin.setExecutor(new CommandCatSeedLogin());

        //Task
        Task.runAll();

    }


    @Override
    public void onDisable(){
        Task.cancelAll();
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (!LoginPlayerHelper.isLogin(p.getName())) return;
            if (!p.isDead() || Config.Settings.DeathStateQuitRecordLocation) {
                Config.setOfflineLocation(p);
            }

        });
        try {
            sql.getConnection().close();
        } catch (Exception e) {
            getLogger().warning(translate("database-error"));
            e.printStackTrace();
        }
        super.onDisable();
    }

    public void runTaskAsync(Runnable runnable){
        scheduler.runTaskAsynchronously(this, runnable);
    }


}
