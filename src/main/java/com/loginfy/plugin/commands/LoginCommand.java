package com.loginfy.plugin.commands;

import com.loginfy.plugin.Loginfy;
import com.loginfy.plugin.util.LoginManager;
import com.loginfy.plugin.util.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class LoginCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Yalnızca oyuncular
        if (!(sender instanceof Player)) {
            sender.sendMessage(LanguageManager.getMessage(null, "login", "onlyPlayer"));
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Zaten giriş yaptıysa
        if (LoginManager.isLoggedIn(uuid)) {
            player.sendMessage(LanguageManager.getMessage(player, "login", "already"));
            return true;
        }

        // Argüman kontrolü
        if (args.length != 1) {
            player.sendMessage(LanguageManager.getMessage(player, "login", "usage"));
            return true;
        }

        String password = args[0];

        try {
            Connection conn = Loginfy.getInstance().getDatabaseManager().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            // Kayıtlı değilse
            if (!rs.next()) {
                player.sendMessage(LanguageManager.getMessage(player, "login", "notRegistered"));
                return true;
            }

            String hashedPassword = rs.getString("password");
            if (BCrypt.checkpw(password, hashedPassword)) {
                // 1) login işaretle
                LoginManager.login(uuid);
                // 2) IP güncelle
                String ip = player.getAddress().getAddress().getHostAddress();
                PreparedStatement upd = conn.prepareStatement(
                        "UPDATE users SET last_ip = ? WHERE uuid = ?"
                );
                upd.setString(1, ip);
                upd.setString(2, uuid.toString());
                upd.executeUpdate();

// Başlık metinlerini al
                String title = LanguageManager.getMessage(player, "welcome", "title")
                        .replace("{player}", player.getName());
                String subtitle = LanguageManager.getMessage(player, "welcome", "subtitle");

// Süreleri config’ten al
                int in   = Loginfy.getInstance().getConfig().getInt("welcome.fade-in");
                int stay = Loginfy.getInstance().getConfig().getInt("welcome.stay");
                int out  = Loginfy.getInstance().getConfig().getInt("welcome.fade-out");

// Başlığı gönder
                player.sendTitle(title, subtitle, in, stay, out);

                player.sendMessage(LanguageManager.getMessage(player, "login", "success"));
            } else {
                player.sendMessage(LanguageManager.getMessage(player, "login", "wrongPassword"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(LanguageManager.getMessage(player, "general", "error"));
        }

        return true;
    }
}
