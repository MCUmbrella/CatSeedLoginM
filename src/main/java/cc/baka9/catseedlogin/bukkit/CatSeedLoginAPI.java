package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;

public class CatSeedLoginAPI {
    public static boolean isLoggedIn(String name){
        return LoginPlayerHelper.isLoggedIn(name);
    }

    public static boolean isRegistered(String name){
        return LoginPlayerHelper.isRegistered(name);
    }
}
