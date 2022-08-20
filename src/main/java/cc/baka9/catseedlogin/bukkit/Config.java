package cc.baka9.catseedlogin.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import vip.floatationdevice.msu.I18nUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 加载/保存/重载 yml配置文件
 * config.yml 玩家退出服务器的位置
 * email.yml 邮箱找回密码
 * settings.yml 设置
 * sql.yml 数据库
 */
public class Config {
    private static CatSeedLogin plugin = CatSeedLogin.instance;
    private static Map<String, String> offlineLocations = new HashMap<>();

    /**
     * 数据库
     */
    public static class MySQL { //TODO
        public static boolean enabled;
        public static String host;
        public static String port;
        public static String database;
        public static String user;
        public static String password;

        public static void load(){
            FileConfiguration config = getConfig("sql.yml");
            MySQL.enabled = config.getBoolean("MySQL.Enable");
            MySQL.host = config.getString("MySQL.Host");
            MySQL.port = config.getString("MySQL.Port");
            MySQL.database = config.getString("MySQL.Database");
            MySQL.user = config.getString("MySQL.User");
            MySQL.password = config.getString("MySQL.Password");
        }
    }

    public static class BungeeCord { //TODO
        public static boolean enabled;
        public static String host;
        public static String port;
        public static String authKey;

        public static void load(){

            FileConfiguration config = getConfig("bungeecord.yml");
            BungeeCord.enabled = config.getBoolean("Enable");
            BungeeCord.host = config.getString("Host");
            BungeeCord.port = config.getString("Port");
            BungeeCord.authKey = config.getString("AuthKey");
        }
    }

    /**
     * 设置
     */
    public static class Settings {
        public static String language;
        public static int maxRegPerIP;
        public static int maxOnlinePerIP;
        public static Location spawnLocation;
        public static boolean forceStandardPlayerName;
        public static int maxPlayerNameLength;
        public static int minPlayerNameLength;
        public static boolean noDamageBeforeLogin;
        public static long rejoinInterval;
        public static boolean backAfterLogin;
        public static boolean noMoveBeforeLogin;
        public static List<Pattern> commandWhitelist = new ArrayList<>();
        public static long loginTimeout;
        // 死亡状态退出游戏是否记录退出位置 (玩家可以通过死亡时退出服务器然后重新进入，再复活，登录返回死亡地点)
        public static boolean saveDeadPlayerLogoutLocation;
        public static boolean forceStrongPassword; //TODO

        public static void load(){
            FileConfiguration config = getConfig("settings.yml");
            FileConfiguration resourceConfig = getResourceConfig("settings.yml");

            language = config.getString("language", "en_US");
            I18nUtil.setLanguage(CatSeedLogin.instance, language);
            CatSeedLogin.instance.getLogger().info("Language: " + I18nUtil.getLanguage() + " (" + language + ") by " + I18nUtil.getLanguageFileContributor());
            maxRegPerIP = config.getInt("maxRegPerIP", resourceConfig.getInt("maxRegPerIP"));
            maxOnlinePerIP = config.getInt("maxOnlinePerIP", resourceConfig.getInt("maxOnlinePerIP"));
            forceStandardPlayerName = config.getBoolean("forceStandardPlayerName", resourceConfig.getBoolean("forceStandardPlayerName"));
            minPlayerNameLength = config.getInt("minPlayerNameLength", resourceConfig.getInt("minPlayerNameLength"));
            maxPlayerNameLength = config.getInt("maxPlayerNameLength", resourceConfig.getInt("maxPlayerNameLength"));
            noDamageBeforeLogin = config.getBoolean("noDamageBeforeLogin", resourceConfig.getBoolean("noDamageBeforeLogin"));
            rejoinInterval = config.getLong("rejoinInterval", resourceConfig.getLong("rejoinInterval"));
            backAfterLogin = config.getBoolean("backAfterLogin", resourceConfig.getBoolean("backAfterLogin"));
            noMoveBeforeLogin = config.getBoolean("noMoveBeforeLogin", resourceConfig.getBoolean("noMoveBeforeLogin"));
            List<String> commandWhitelist = config.getStringList("commandWhitelist");
            if (commandWhitelist.size() == 0) {
                commandWhitelist = resourceConfig.getStringList("commandWhitelist");
            }
            Settings.commandWhitelist.clear();
            Settings.commandWhitelist.addAll(commandWhitelist.stream().map(Pattern::compile).collect(Collectors.toList()));
            loginTimeout = config.getLong("loginTimeout", 120L);
            spawnLocation = str2Location(config.getString("spawnLocation"));
            saveDeadPlayerLogoutLocation = config.getBoolean("saveDeadPlayerLogoutLocation", resourceConfig.getBoolean("saveDeadPlayerLogoutLocation"));
        }

        public static void save(){
            FileConfiguration config = getConfig("settings.yml");
            config.set("language", language);
            config.set("maxRegPerIP", maxRegPerIP);
            config.set("maxOnlinePerIP", maxOnlinePerIP);
            config.set("spawnWorld", null);
            config.set("forceStandardPlayerName", forceStandardPlayerName);
            config.set("minPlayerNameLength", minPlayerNameLength);
            config.set("maxPlayerNameLength", maxPlayerNameLength);
            config.set("noDamageBeforeLogin", noDamageBeforeLogin);
            config.set("rejoinInterval", rejoinInterval);
            config.set("backAfterLogin", backAfterLogin);
            config.set("noMoveBeforeLogin", noMoveBeforeLogin);
            config.set("loginTimeout", loginTimeout);
            config.set("spawnLocation", loc2String(spawnLocation));
            config.set("commandWhitelist", commandWhitelist.stream().map(Pattern::toString).collect(Collectors.toList()));
            config.set("saveDeadPlayerLogoutLocation", saveDeadPlayerLogoutLocation);
            try {
                config.save(new File(CatSeedLogin.instance.getDataFolder(), "settings.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 邮箱找回密码
     */
    public static class EmailVerify {

        public static boolean enabled;
        public static String account;
        public static String password;
        public static String smtpHost;
        public static String smtpPort;
        public static boolean sslEnabled;
        public static String from;
        public static boolean debug = false;


        public static void load(){
            FileConfiguration config = getConfig("email.yml");
            enabled = config.getBoolean("enabled");
            account = config.getString("account");
            password = config.getString("password");
            smtpHost = config.getString("smtpHost");
            smtpPort = config.getString("smtpPort");
            sslEnabled = config.getBoolean("ssl");
            from = config.getString("from");
            debug = config.getBoolean("debug", false);
        }

    }
    // 获取插件文件夹中的配置文件，如果不存在则从插件jar包中获取配置文件保存到插件文件夹中
    public static FileConfiguration getConfig(String yamlFileName){
        File file = new File(plugin.getDataFolder(), yamlFileName);
        if (!file.exists()) {
            plugin.saveResource(yamlFileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    // 获取插件jar包中的配置文件
    public static FileConfiguration getResourceConfig(String yamlFileName){
        return YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(yamlFileName), StandardCharsets.UTF_8));
    }

    public static void load(){
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        if (config.contains("offlineLocations")) {
            config.getConfigurationSection("offlineLocations").getKeys(false).forEach(key ->
                    offlineLocations.put(key, config.getString("offlineLocations." + key))
            );
        }
        MySQL.load();
        Settings.load();
        EmailVerify.load();
        BungeeCord.load();
    }

    public static void save(){
        Settings.save();
    }

    public static void reload(){
        plugin.reloadConfig();
        load();

    }

    // 获取玩家退出服务器时的位置
    public static Optional<Location> getOfflineLocation(Player player){
        String data = offlineLocations.get(player.getName());
        return data == null ? Optional.empty() : Optional.of(str2Location(data));
    }

    // 保存玩家退出服务器的位置
    public static void setOfflineLocation(Player player){
        String name = player.getName();
        String data = loc2String(player.getLocation());
        offlineLocations.put(name, data);
        plugin.getConfig().set("offlineLocations." + name, data);
        plugin.saveConfig();
    }

    // 字符串转成位置
    private static Location str2Location(String str){
        Location loc;
        try {
            String[] locStrs = str.split(":");
            World world = Bukkit.getWorld(locStrs[0]);
            double x = Double.parseDouble(locStrs[1]);
            double y = Double.parseDouble(locStrs[2]);
            double z = Double.parseDouble(locStrs[3]);
            float yaw = Float.parseFloat(locStrs[4]);
            float pitch = Float.parseFloat(locStrs[5]);
            loc = new Location(world, x, y, z, yaw, pitch);
        } catch (Exception ignored) {
            loc = getDefaultWorld().getSpawnLocation();
        }
        return loc;

    }
    // 位置转成字符串
    private static String loc2String(Location loc){
        try {
            return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
        } catch (Exception ignored) {
            loc = getDefaultWorld().getSpawnLocation();
        }
        return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();

    }

    // 获取默认世界
    private static World getDefaultWorld(){
        try (InputStream is = new BufferedInputStream(Files.newInputStream(new File("server.properties").toPath()))) {
            Properties properties = new Properties();
            properties.load(is);
            String worldName = properties.getProperty("level-name");
            return Bukkit.getWorld(worldName);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Bukkit.getWorlds().get(0);
    }


}
