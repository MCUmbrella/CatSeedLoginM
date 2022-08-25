package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.task.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

import java.util.regex.Pattern;

import static cc.baka9.catseedlogin.bukkit.Config.Settings.*;

public class Listeners implements Listener {

    private boolean playerIsNotMinecraftPlayer(Player p){
        return !p.getClass().getName().matches("org\\.bukkit\\.craftbukkit.*?\\.entity\\.CraftPlayer");
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
        if (playerIsNotMinecraftPlayer(event.getPlayer())) return;
        if (LoginPlayerHelper.isLoggedIn(event.getPlayer().getName())) return;
        String input = event.getMessage().toLowerCase();
        for (Pattern regex : commandWhitelist)
            if (regex.matcher(input).find()) return;
        event.setCancelled(true);

    }

    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event){
        if (!Cache.isLoaded) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "服务器还在初始化..");
            return;
        }
        String name = event.getName();
        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) return;
        if (!lp.getName().equals(name)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "游戏名字母大小写不匹配,请使用游戏名" + lp.getName() + "重新尝试登录");
            return;
        }
        if (LoginPlayerHelper.isLoggedIn(name))
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "玩家 " + lp.getName() + " 已经在线了!");
        int count = 0;
        String hostAddress = event.getAddress().getHostAddress();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String ip = p.getAddress().getAddress().getHostAddress();
            if (ip.equals(hostAddress)) count++;
            if (count >= maxOnlinePerIP) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "太多相同ip的账号同时在线!");
                return;
            }
        }


    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event){
        if (playerIsNotMinecraftPlayer(event.getPlayer())) return;
        if (LoginPlayerHelper.isLoggedIn(event.getPlayer().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if (playerIsNotMinecraftPlayer(event.getPlayer())) return;
        if (LoginPlayerHelper.isLoggedIn(event.getPlayer().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event){
        if (LoginPlayerHelper.isLoggedIn(event.getPlayer().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if (!(event.getWhoClicked() instanceof Player) || LoginPlayerHelper.isLoggedIn(event.getWhoClicked().getName()))
            return;
        event.setCancelled(true);
    }

    //登录之前不能攻击
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player)) return;
        if (playerIsNotMinecraftPlayer((Player) event.getDamager())) return;
        if (LoginPlayerHelper.isLoggedIn(event.getDamager().getName())) return;
        event.setCancelled(true);
    }

    //登录之前不会受到伤害
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if (noDamageBeforeLogin) {
            Entity entity = event.getEntity();
            if (entity instanceof Player && !playerIsNotMinecraftPlayer((Player) entity))
                if (!LoginPlayerHelper.isLoggedIn(entity.getName()))
                    event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event){
        if (noMoveBeforeLogin && event.getTo().equals(spawnLocation)) return;
        if (playerIsNotMinecraftPlayer(event.getPlayer())) return;
        if (LoginPlayerHelper.isLoggedIn(event.getPlayer().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if (playerIsNotMinecraftPlayer(event.getPlayer())) return;
        if (LoginPlayerHelper.isLoggedIn(event.getPlayer().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event){
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (playerIsNotMinecraftPlayer(player)) return;
        if (LoginPlayerHelper.isLoggedIn(player.getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if (noMoveBeforeLogin)
        {
            Player player = event.getPlayer();
            if (playerIsNotMinecraftPlayer(player)) return;
            if (LoginPlayerHelper.isLoggedIn(player.getName())) return;
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getY() - to.getY() >= 0.0D) return;
            player.teleport(spawnLocation);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if (LoginPlayerHelper.isLoggedIn(player.getName())) {
            if (!player.isDead() || saveDeadPlayerLogoutLocation)
                Config.setOfflineLocation(player);
            Bukkit.getScheduler().runTaskLater(CatSeedLogin.instance, () -> LoginPlayerHelper.remove(player.getName()), rejoinInterval * 20);
        }
        Task.getTaskAutoKick().playerJoinTime.remove(player.getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        Cache.refresh(p.getName());
        if (noMoveBeforeLogin) p.teleport(spawnLocation);
    }

    //id只能下划线字母数字
    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event){ //TODO: i18n
        String name = event.getName();
        if (forceStandardPlayerName)
            if (!name.matches("^\\w+$"))
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        "请使用由数字,字母和下划线组成的游戏名,才能进入游戏");

        if (name.length() < minPlayerNameLength)
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "你的游戏名太短了,至少需要 " + minPlayerNameLength + " 个字符的长度");

        if (name.length() > maxPlayerNameLength)
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "你的游戏名太长了,最长只能到达 " + maxPlayerNameLength + " 个字符的长度");
    }
}
