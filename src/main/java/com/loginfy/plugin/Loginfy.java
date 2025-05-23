package com.loginfy.plugin;

import com.loginfy.plugin.database.DatabaseManager;
import com.loginfy.plugin.commands.RegisterCommand;
import com.loginfy.plugin.commands.LoginCommand;
import com.loginfy.plugin.commands.ChangePasswordCommand;
import com.loginfy.plugin.commands.RegisterUserCommand;
import com.loginfy.plugin.listeners.PlayerListener;
import com.loginfy.plugin.listeners.AutoLoginListener;
import com.loginfy.plugin.util.LanguageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Loginfy extends JavaPlugin {

    private static Loginfy instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        // Config dosyalarını hazırla
        saveDefaultConfig();

        // Dil dosyalarını yükle
        LanguageManager.init(this);

        // Veritabanını başlat
        databaseManager = new DatabaseManager();
        databaseManager.setupDatabase();

        // Komutları kaydet
        getCommand("register").setExecutor(new RegisterCommand());
        getCommand("login").setExecutor(new LoginCommand());
        getCommand("changepass").setExecutor(new ChangePasswordCommand());
        getCommand("registeruser").setExecutor(new RegisterUserCommand());

        // Listener'ları kaydet
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new AutoLoginListener(), this);

        // Başlatma mesajı
        getLogger().info("Loginfy aktif!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Loginfy devre dışı!");
    }

    public static Loginfy getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
