package cc.baka9.catseedlogin.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

public class CommonUtil
{
    private static final Pattern passwordDifficultyRegex = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
    public static boolean isStrongPassword(String pwd){
        return passwordDifficultyRegex.matcher(pwd).find();
    }
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String currentTimeString(long time){
        return sdf.format(new Date(time));
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
