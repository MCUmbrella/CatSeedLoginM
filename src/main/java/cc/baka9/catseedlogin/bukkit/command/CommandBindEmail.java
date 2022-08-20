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

import static vip.floatationdevice.msu.I18nUtil.translate;

public class CommandBindEmail implements CommandExecutor {
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
        if (!LoginPlayerHelper.isLoggedIn(name)) {
            sender.sendMessage(translate("not-logged-in"));
            return true;
        }
        if (!Config.EmailVerify.enabled) {
            sender.sendMessage(translate("bind-disabled"));
            return true;
        }

        // command set email
        if (args[0].equalsIgnoreCase("set") && args.length > 1) {
            if (lp.getEmail() != null && Util.checkMail(lp.getEmail()))
                sender.sendMessage(translate("bind-already"));
            else {
                String mail = args[1];
                Optional<EmailCode> bindEmailOptional = EmailCode.getByName(name, EmailCode.Type.Bind);
                if (bindEmailOptional.isPresent() && bindEmailOptional.get().getEmail().equals(mail))
                    sender.sendMessage(translate("bind-email-already-sent").replace("{email}", mail));
                else if (Util.checkMail(mail)) {
                    //创建有效期为20分钟的验证码
                    EmailCode bindEmail = EmailCode.create(name, mail, 1000 * 60 * 20, EmailCode.Type.Bind);
                    sender.sendMessage(translate("bind-sending-email").replace("{email}", mail));
                    CatSeedLogin.instance.runTaskAsync(() -> {
                        try {
                            Mail.sendMail(mail, translate("bind-email-subject"),
                                    translate("bind-email-content")
                                            .replace("{code}", bindEmail.getCode())
                                            .replace("{name}", name)
                                            .replace("{time}", String.valueOf((bindEmail.getDurability() / (1000 * 60))))
                            );
                            Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> sender.sendMessage(translate("bind-email-sent").replace("{email}", mail)));
                        } catch (Exception e) {
                            Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> sender.sendMessage(translate("internal-error")));
                            e.printStackTrace();
                        }
                    });
                } else
                    sender.sendMessage(translate("bind-invalid-email-format"));
            }
            return true;
        }

        // command verify code
        if (args[0].equalsIgnoreCase("verify") && args.length > 1) {
            if (lp.getEmail() != null && Util.checkMail(lp.getEmail())) {
                sender.sendMessage(translate("bind-already"));
            } else {
                Optional<EmailCode> emailOptional = EmailCode.getByName(name, EmailCode.Type.Bind);
                if (emailOptional.isPresent()) {
                    EmailCode bindEmail = emailOptional.get();
                    String code = args[1];
                    if (bindEmail.getCode().equals(code)) {
                        sender.sendMessage(translate("please-wait"));
                        CatSeedLogin.instance.runTaskAsync(() -> {
                            try {
                                lp.setEmail(bindEmail.getEmail());
                                CatSeedLogin.sql.edit(lp);
                                Bukkit.getScheduler().runTask(CatSeedLogin.instance, () -> {
                                    Player syncPlayer = Bukkit.getPlayer(((Player) sender).getUniqueId());
                                    if (syncPlayer != null && syncPlayer.isOnline()) {
                                        syncPlayer.sendMessage(translate("bind-success").replace("{email}", bindEmail.getEmail()));
                                        EmailCode.removeByName(name, EmailCode.Type.Bind);
                                    }
                                });
                            } catch (Exception e) {
                                sender.sendMessage(translate("internal-error"));
                                e.printStackTrace();
                            }
                        });
                    } else {
                        sender.sendMessage(translate("bind-invalid-code"));
                    }
                } else {
                    sender.sendMessage(translate("bind-no-pending-request"));
                }
            }
            return true;
        }
        return true;
    }
}
