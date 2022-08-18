package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.EmailCode;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.Mail;
import cc.baka9.catseedlogin.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static cc.baka9.catseedlogin.bukkit.I18nManager.translate;

public class CommandResetPassword implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args){
        if (args.length == 0 || !(sender instanceof Player)) return false;
        Player player = (Player) sender;
        String name = player.getName();
        LoginPlayer lp = Cache.getIgnoreCase(name);

        if (lp == null) {
            sender.sendMessage(translate("not-registered"));
            return true;
        }
        if (!Config.EmailVerify.Enable) {
            sender.sendMessage(translate("repw-disabled"));
            return true;
        }
        //command forget
        if (args[0].equalsIgnoreCase("forget")) {
            if (lp.getEmail() == null) {
                sender.sendMessage(translate("repw-email-not-set"));
            } else {
                Optional<EmailCode> optionalEmailCode = EmailCode.getByName(name, EmailCode.Type.ResetPassword);
                if (optionalEmailCode.isPresent()) {
                    sender.sendMessage(translate("repw-email-already-sent").replace("{email}", optionalEmailCode.get().getEmail()));
                } else {
                    //20分钟有效期的验证码
                    EmailCode emailCode = EmailCode.create(name, lp.getEmail(), 1000 * 60 * 20, EmailCode.Type.ResetPassword);
                    sender.sendMessage(translate("repw-sending-email").replace("{email}", lp.getEmail()));
                    CatSeedLogin.instance.runTaskAsync(() -> {
                        try { //TODO: i18n for the email content
                            Mail.sendMail(emailCode.getEmail(), "重置密码",
                                    "你的验证码是 <strong>" + emailCode.getCode() + "</strong>" +
                                            "<br/>在服务器中使用帐号 " + name + " 输入指令<strong>/resetpassword re " + emailCode.getCode() + " 新密码</strong> 来重置新密码" +
                                            "<br/>此验证码有效期为 " + (emailCode.getDurability() / (1000 * 60)) + "分钟");
                            Bukkit.getScheduler().runTask(CatSeedLogin.instance, () ->
                                    sender.sendMessage(translate("repw-email-sent").replace("{email}", emailCode.getEmail())));
                        } catch (Exception e) {
                            Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> sender.sendMessage(translate("internal-error")));
                            e.printStackTrace();
                        }
                    });
                }
            }
            return true;
        }
        //command re
        if (args[0].equalsIgnoreCase("re") && args.length > 2) {
            if (lp.getEmail() == null) {
                sender.sendMessage(translate("repw-email-not-set"));
            } else {
                Optional<EmailCode> optionalEmailCode = EmailCode.getByName(name, EmailCode.Type.ResetPassword);
                if (optionalEmailCode.isPresent()) {
                    EmailCode emailCode = optionalEmailCode.get();
                    String code = args[1], pwd = args[2];

                    if (emailCode.getCode().equals(code)) {
                        if (!Util.passwordIsDifficulty(pwd)) {
                            sender.sendMessage(translate("password-too-weak"));
                            return true;
                        }
                        sender.sendMessage(translate("please-wait"));
                        CatSeedLogin.instance.runTaskAsync(() -> {
                            lp.setPassword(pwd);
                            lp.crypt();
                            try {
                                CatSeedLogin.sql.edit(lp);
                                LoginPlayerHelper.remove(lp);
                                EmailCode.removeByName(name, EmailCode.Type.ResetPassword);
                                Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> {
                                    Player p = Bukkit.getPlayer(lp.getName());
                                    if (p != null && p.isOnline()) {
                                        if (Config.Settings.CanTpSpawnLocation) {
//                                            PlayerTeleport.teleport(p, Config.Settings.SpawnLocation);
                                            p.teleport(Config.Settings.SpawnLocation);
                                        }
                                        p.sendMessage(translate("repw-success"));
                                        if (CatSeedLogin.loadProtocolLib) {
                                            LoginPlayerHelper.sendBlankInventoryPacket(player);
                                        }
                                    }

                                });
                            } catch (Exception e) {
                                Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> sender.sendMessage(translate("database-error")));
                                e.printStackTrace();
                            }


                        });
                    } else {
                        sender.sendMessage(translate("repw-invalid-code"));
                    }

                } else {
                    sender.sendMessage(translate("internal-error"));
                }
            }
            return true;
        }
        return true;
    }
}
