package cc.baka9.catseedlogin.bukkit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Manages the I18n (internationalization) messages.
 * The translated messages are stored in the 'lang_xx_XX.yml' files.
 * @author MCUmbrella
 */
public class I18nManager
{
    private static FileConfiguration l;
    private static String lang = null;

    /**
     * Set the language of the plugin.
     * @param language Locale code of the language to use (for example, 'en_US' or 'zh_CN').
     * @return The language code.
     */
    public static String setLanguage(String language)
    {
        if(language == null || language.isEmpty())
            throw new IllegalArgumentException("Language cannot be null or empty");
        lang = language;
        File langFile = new File(CatSeedLogin.instance.getDataFolder(), "lang_" + lang + ".yml");
        if(!langFile.exists()) CatSeedLogin.instance.saveResource("lang_" + lang + ".yml", false);
        l = YamlConfiguration.loadConfiguration(langFile);
        return lang;
    }

    /**
     * Translate a string.
     * @param key The key of the string.
     * @return The translated string. If the key does not exist, return "[NO TRANSLATION: key]"
     * @throws IllegalStateException If the language is not set.
     */
    public static String translate(String key)
    {
        if(l == null) throw new IllegalStateException("Translation engine not initialized");
        return l.getString(key) == null ? "[NO TRANSLATION: " + key + "]" : l.getString(key);
    }

    /**
     * Get the language of the plugin.
     * @return The language of the plugin. If the language is not set, return null.
     */
    public static String getLanguage()
    {
        return lang;
    }
}
