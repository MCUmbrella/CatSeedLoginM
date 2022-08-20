package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.EmailCode;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.util.MailUtil;
import cc.baka9.catseedlogin.util.CommonUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static vip.floatationdevice.msu.I18nUtil.translate;

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
        if (!Config.EmailVerify.enabled) {
            sender.sendMessage(translate("repw-disabled"));
            return true;
        }
        //command forget
        if (args[0].equalsIgnoreCase("forget")) {
            if (lp.getEmail() == null)
                sender.sendMessage(translate("repw-email-not-set"));
            else {
                Optional<EmailCode> optionalEmailCode = EmailCode.getByName(name, EmailCode.Type.ResetPassword);
                if (optionalEmailCode.isPresent())
                    sender.sendMessage(translate("repw-email-already-sent").replace("{email}", optionalEmailCode.get().getEmail()));
                else {
                    //20分钟有效期的验证码
                    EmailCode emailCode = EmailCode.create(name, lp.getEmail(), 1000 * 60 * 20, EmailCode.Type.ResetPassword);
                    sender.sendMessage(translate("repw-sending-email").replace("{email}", lp.getEmail()));
                    CatSeedLogin.instance.runTaskAsync(() -> {
                        try {
                            MailUtil.sendMail(emailCode.getEmail(), translate("repw-email-subject"),
                                    translate("repw-email-content")
                                            .replace("{code}", emailCode.getCode())
                                            .replace("{name}", name)
                                            .replace("{time}", String.valueOf((emailCode.getDurability() / (1000 * 60))))
                            );
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
            if (lp.getEmail() == null)
                sender.sendMessage(translate("repw-email-not-set"));
            else {
                Optional<EmailCode> optionalEmailCode = EmailCode.getByName(name, EmailCode.Type.ResetPassword);
                if (optionalEmailCode.isPresent()) {
                    EmailCode emailCode = optionalEmailCode.get();
                    String code = args[1], pwd = args[2];

                    if (emailCode.getCode().equals(code)) {
                        if (!CommonUtil.isStrongPassword(pwd)) {
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
                                        if (Config.Settings.noMoveBeforeLogin) {
//                                            PlayerTeleport.teleport(p, Config.Settings.SpawnLocation);
                                            p.teleport(Config.Settings.spawnLocation);
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
                    sender.sendMessage(translate("repw-no-pending-request"));
                }
            }
            return true;
        }
        return true;
    }
}
