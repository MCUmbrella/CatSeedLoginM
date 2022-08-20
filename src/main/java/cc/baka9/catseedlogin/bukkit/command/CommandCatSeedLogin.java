package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Communication;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.database.MySQL;
import cc.baka9.catseedlogin.bukkit.database.SQLite;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandCatSeedLogin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args){
        return reload(sender, args)
                || setPwd(sender, args)
                || delPlayer(sender, args)
                || setMaxOnlinePerIP(sender, args)
                || forceStandardPlayerName(sender, args)
                || setIdLength(sender, args)
                || noDamageBeforeLogin(sender, args)
                || setRejoinInterval(sender, args)
                || backAfterLogin(sender, args)
                || setSpawnLocation(sender, args)
                || commandWhitelistInfo(sender, args)
                || commandWhitelistAdd(sender, args)
                || commandWhitelistDel(sender, args)
                || noMoveBeforeLogin(sender, args)
                || loginTimeout(sender, args)
                || setIpRegCountLimit(sender, args)
                || saveDeadPlayerLogoutLocation(sender, args);
    }

    private boolean saveDeadPlayerLogoutLocation(CommandSender sender, String[] args){
        if (args.length > 0 && args[0].equalsIgnoreCase("saveDeadPlayerLogoutLocation")) {
            Config.Settings.saveDeadPlayerLogoutLocation = !Config.Settings.saveDeadPlayerLogoutLocation;
            Config.Settings.save();
            sender.sendMessage("§e死亡状态退出游戏记录退出位置" + (Config.Settings.saveDeadPlayerLogoutLocation ? "§a开启" : "§8关闭"));
            return true;
        }
        return false;
    }

    private boolean loginTimeout(CommandSender sender, String[] args){
        if (args.length > 1 && args[0].equalsIgnoreCase("setLoginTimeout")) {
            try {

                Config.Settings.loginTimeout = Integer.parseInt(args[1]);
                Config.Settings.save();
                sender.sendMessage(Config.Settings.loginTimeout > 0 ? "§e已设置未登录自动踢出累计时间为 §a" + Config.Settings.loginTimeout + "秒" : "§e已关闭未登录自动踢出");
            } catch (NumberFormatException e) {
                sender.sendMessage("§e秒数必须是一个数字");
            }
            return true;
        }
        return false;
    }

    private boolean noMoveBeforeLogin(CommandSender sender, String[] args){
        if (args.length > 0 && args[0].equalsIgnoreCase("noMoveBeforeLogin")) {
            Config.Settings.noMoveBeforeLogin = !Config.Settings.noMoveBeforeLogin;
            Config.Settings.save();
            sender.sendMessage("§e登录之前强制在登录地点 " + (Config.Settings.noMoveBeforeLogin ? "§a开启" : "§8关闭"));
            return true;
        }
        return false;
    }

    private boolean commandWhitelistDel(CommandSender sender, String[] args){
        if (args.length > 1 && args[0].equalsIgnoreCase("commandWhitelistDel")) {
            String[] cmd = new String[args.length - 1];
            System.arraycopy(args, 1, cmd, 0, cmd.length);
            String regex = String.join(" ", cmd);
            List<String> collect = Config.Settings.commandWhitelist.stream().map(Pattern::toString).collect(Collectors.toList());
            if (collect.contains(regex)) {
                collect.remove(regex);
                Config.Settings.commandWhitelist = collect.stream().map(Pattern::compile).collect(Collectors.toList());
                Config.Settings.save();
                sender.sendMessage("§e已删除登录前可执行指令 " + regex);
            } else {
                sender.sendMessage("§c不存在 " + regex);
            }
            return true;
        }
        return false;
    }

    private boolean commandWhitelistAdd(CommandSender sender, String[] args){
        if (args.length > 1 && args[0].equalsIgnoreCase("commandWhitelistAdd")) {
            String[] cmd = new String[args.length - 1];
            System.arraycopy(args, 1, cmd, 0, cmd.length);
            String regex = String.join(" ", cmd);
            Pattern pattern = Pattern.compile(regex);
            List<String> collect = Config.Settings.commandWhitelist.stream().map(Pattern::toString).collect(Collectors.toList());
            if (collect.contains(regex)) {
                sender.sendMessage("§c已经存在 " + regex);
            } else {
                Config.Settings.commandWhitelist.add(pattern);
                Config.Settings.save();
                sender.sendMessage("§e已添加登录前可执行指令 " + regex);
            }
            return true;
        }
        return false;
    }

    private boolean commandWhitelistInfo(CommandSender sender, String[] args){
        if (args.length > 0 && args[0].equalsIgnoreCase("commandWhitelistInfo")) {
            sender.sendMessage("§e登录前可执行指令: ");
            Config.Settings.commandWhitelist.forEach(cmdRegex -> sender.sendMessage(cmdRegex.toString()));
            return true;
        }
        return false;
    }

    private boolean setSpawnLocation(CommandSender sender, String[] args){
        if (args.length > 0 && args[0].equalsIgnoreCase("setSpawnLocation")) {
            if (sender instanceof Player) {
                Config.Settings.spawnLocation = ((Player) sender).getLocation();
                Config.Settings.save();
                sender.sendMessage("§e已设置玩家登录坐标为你站着的位置");
            } else {
                sender.sendMessage("§c不能在控制台使用这个指令");
            }
            return true;
        }
        return false;
    }

    private boolean backAfterLogin(CommandSender sender, String[] args){
        if (args.length > 0 && args[0].equalsIgnoreCase("backAfterLogin")) {
            Config.Settings.backAfterLogin = !Config.Settings.backAfterLogin;
            Config.Settings.save();
            sender.sendMessage("§e登录之后返回下线地点 " + (Config.Settings.backAfterLogin ? "§a开启" : "§8关闭"));
            return true;
        }
        return false;
    }

    private boolean setRejoinInterval(CommandSender sender, String[] args){
        if (args.length > 1 && args[0].equalsIgnoreCase("setRejoinInterval")) {
            try {
                Config.Settings.rejoinInterval = Long.parseLong(args[1]);
                Config.Settings.save();
                sender.sendMessage("§e离开服务器重新进入的间隔限制 " + Config.Settings.rejoinInterval + "tick");
            } catch (NumberFormatException e) {
                sender.sendMessage("§c请输入一个数字");
            }
            return true;
        }
        return false;
    }

    private boolean noDamageBeforeLogin(CommandSender sender, String[] args){
        if (args.length > 0 && args[0].equalsIgnoreCase("noDamageBeforeLogin")) {
            Config.Settings.noDamageBeforeLogin = !Config.Settings.noDamageBeforeLogin;
            Config.Settings.save();
            sender.sendMessage("§e登录之前不受到伤害 " + (Config.Settings.noDamageBeforeLogin ? "§a开启" : "§8关闭"));
            return true;
        }
        return false;
    }

    private boolean setIdLength(CommandSender sender, String[] args){
        if (args.length > 2 && args[0].equalsIgnoreCase("setPlayerNameLength")) {
            try {
                Config.Settings.minPlayerNameLength = Integer.parseInt(args[1]);
                Config.Settings.maxPlayerNameLength = Integer.parseInt(args[2]);
                Config.Settings.save();
                sender.sendMessage("§e游戏名最小和最大长度为 " + Config.Settings.minPlayerNameLength + " ~ " + Config.Settings.maxPlayerNameLength);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c请输入数字");
            }
            return true;
        }
        return false;
    }

    private boolean forceStandardPlayerName(CommandSender sender, String[] args){
        if (args.length > 0 && args[0].equalsIgnoreCase("forceStandardPlayerName")) {
            Config.Settings.forceStandardPlayerName = !Config.Settings.forceStandardPlayerName;
            Config.Settings.save();
            sender.sendMessage("§e限制中文游戏名 " + (Config.Settings.forceStandardPlayerName ? "§a开启" : "§8关闭"));
            return true;
        }
        return false;
    }

    private boolean setMaxOnlinePerIP(CommandSender sender, String[] args){
        if (args.length > 1 && args[0].equalsIgnoreCase("setMaxOnlinePerIP")) {
            try {
                Config.Settings.maxOnlinePerIP = Integer.parseInt(args[1]);
                Config.Settings.save();
                sender.sendMessage("§e相同ip登录限制数量为 " + Config.Settings.maxOnlinePerIP);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c请输入数字");
            }
            return true;
        }
        return false;
    }

    private boolean setIpRegCountLimit(CommandSender sender, String[] args){
        if (args.length > 1 && args[0].equalsIgnoreCase("setMaxRegPerIP")) {
            try {
                Config.Settings.maxRegPerIP = Integer.parseInt(args[1]);
                Config.Settings.save();
                sender.sendMessage("§e相同ip注册限制数量为 " + Config.Settings.maxRegPerIP);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c请输入数字");
            }
            return true;
        }
        return false;
    }

    private boolean delPlayer(CommandSender sender, String[] args){
        if (args.length > 1 && args[0].equalsIgnoreCase("delplayer")) {
            String name = args[1];
            LoginPlayer lp = Cache.getIgnoreCase(name);
            if (lp != null) {
                CatSeedLogin.instance.runTaskAsync(() -> {
                    try {
                        CatSeedLogin.sql.del(lp.getName());
                        LoginPlayerHelper.remove(lp);
                        sender.sendMessage("§e已删除账户 §a" + lp.getName());
                        Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> {
                            Player p = Bukkit.getPlayerExact(lp.getName());
                            if (p != null && p.isOnline()) {
                                p.kickPlayer("§c你的账户已被删除!");
                            }
                        });
                    } catch (Exception e) {
                        sender.sendMessage("§c数据库异常!");
                        e.printStackTrace();
                    }
                });
            } else {
                sender.sendMessage(String.format("§c账户 §a%s §c不存在", name));
            }
            return true;
        }
        return false;
    }

    private boolean setPwd(CommandSender sender, String[] args){
        if (args.length > 2 && args[0].equalsIgnoreCase("setpwd")) {

            String name = args[1], pwd = args[2];
            if (!CommonUtil.isStrongPassword(pwd)) {
                sender.sendMessage("§c密码必须是6~16位之间的数字和字母组成");
                return true;
            }
            sender.sendMessage("§e设置中..");
            CatSeedLogin.instance.runTaskAsync(() -> {
                LoginPlayer lp = Cache.getIgnoreCase(name);
                if (lp == null) {
                    lp = new LoginPlayer(name, pwd);
                    lp.crypt();
                    try {
                        CatSeedLogin.sql.add(lp);
                        sender.sendMessage("§a指定账户不存在,现已注册..");
                    } catch (Exception e) {
                        sender.sendMessage("§c数据库异常!");
                        e.printStackTrace();
                    }
                } else {
                    lp.setPassword(pwd);
                    lp.crypt();
                    try {
                        CatSeedLogin.sql.edit(lp);
                        LoginPlayerHelper.remove(lp);
                        sender.sendMessage(String.join(" ", "§a玩家", lp.getName(), "密码已设置"));
                        LoginPlayer finalLp = lp;
                        Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> {
                            Player p = Bukkit.getPlayer(finalLp.getName());
                            if (p != null && p.isOnline()) {
                                p.sendMessage("§c密码已被管理员重新设置,请重新登录");
                                if (Config.Settings.noMoveBeforeLogin) {
                                    p.teleport(Config.Settings.spawnLocation);
                                    if (CatSeedLogin.loadProtocolLib) {
                                        LoginPlayerHelper.sendBlankInventoryPacket(p);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        sender.sendMessage("§c数据库异常!");
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }
        return false;
    }

    private boolean reload(CommandSender sender, String[] args){
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            Config.reload();
            CatSeedLogin.sql = Config.MySQL.enabled ? new MySQL(CatSeedLogin.instance) : new SQLite(CatSeedLogin.instance);
            try {
                CatSeedLogin.sql.init();
                Cache.refreshAll();
            } catch (Exception e) {
                CatSeedLogin.instance.getLogger().warning("§c加载数据库时出错");
                e.printStackTrace();
            }
            Communication.socketServerStopAsync();
            if (Config.BungeeCord.enabled) {
                Communication.socketServerStartAsync();
            }
            sender.sendMessage("配置已重载!");
            return true;
        }
        return false;
    }
}
