package cc.baka9.catseedlogin.util;

import cc.baka9.catseedlogin.bukkit.Config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class CommonUtil
{
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String currentTimeString(long time){
        return sdf.format(new Date(time));
    }

    public static boolean isStrongPassword(String pwd){
        return pwd.matches("^(?!\\d+$)(?![a-zA-Z]+$)[\\dA-Za-z]{6," + Config.Settings.maxPasswordLength + "}$");
    }

    public static boolean isEmailAddress(String email){
        return email.matches("[a-zA-Z0-9_]+@[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+");
    }

    public static String genVerifCode(){
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(10);
        for (int i = 0; i < 10; i++)
            buffer.append((char)(random.nextInt(26) + 0x61));
        return buffer.toString();
    }
}
