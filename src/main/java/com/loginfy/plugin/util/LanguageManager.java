package com.loginfy.plugin.util;

import com.loginfy.plugin.Loginfy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class LanguageManager {

    private static FileConfiguration trConfig;
    private static FileConfiguration enConfig;
    private static Loginfy plugin;

    public static void init(Loginfy pluginInstance) {
        plugin = pluginInstance;
        // dataFolder’a kopyala (ilk çalışmada)
        plugin.saveResource("messages_tr.yml", false);
        plugin.saveResource("messages_en.yml", false);
        plugin.saveResource("config.yml", false);

        trConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages_tr.yml"));
        enConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages_en.yml"));
    }

    private static String getServerLang() {
        return plugin.getConfig().getString("language", "server");
    }

    private static FileConfiguration getConfigFor(Player player) {
        String serverLang = getServerLang();
        if (serverLang.equalsIgnoreCase("tr")) return trConfig;
        if (serverLang.equalsIgnoreCase("en")) return enConfig;
        // server: client diline bak
        String locale = player.getLocale().toLowerCase(); // ex: "tr_tr", "en_us"
        return locale.startsWith("tr") ? trConfig : enConfig;
    }

    public static String getMessage(Player player, String section, String key) {
        FileConfiguration cfg = getConfigFor(player);
        String path = section + "." + key;
        String msg = cfg.getString(path, path);
        return ChatColor.translateAlternateColorCodes('&', msg.replace("{player}", player.getName()));
    }
}
