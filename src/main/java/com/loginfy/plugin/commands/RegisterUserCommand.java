package com.loginfy.plugin.commands;

import com.loginfy.plugin.Loginfy;
import com.loginfy.plugin.util.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.UUID;

public class RegisterUserCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Yetki kontrolü
        if (!sender.hasPermission("loginfy.admin")) {
            sender.sendMessage(LanguageManager.getMessage(null, "registeruser", "noPerm"));
            return true;
        }

        // Argüman sayısı kontrolü
        if (args.length != 2) {
            sender.sendMessage(LanguageManager.getMessage(null, "registeruser", "usage"));
            return true;
        }

        String playerName = args[0];
        String password = args[1];

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = target.getUniqueId();

        try {
            Connection conn = Loginfy.getInstance().getDatabaseManager().getConnection();

            // Zaten kayıtlı mı?
            PreparedStatement check = conn.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            check.setString(1, uuid.toString());
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                String msg = LanguageManager.getMessage(null, "registeruser", "already");
                sender.sendMessage(msg.replace("{player}", playerName));
                return true;
            }

            // Şifre hash ve veritabanına ekle
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            long now = Instant.now().getEpochSecond();

            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO users (uuid, username, password, register_time) VALUES (?, ?, ?, ?)"
            );
            insert.setString(1, uuid.toString());
            insert.setString(2, playerName);
            insert.setString(3, hashed);
            insert.setLong(4, now);
            insert.executeUpdate();

            String successMsg = LanguageManager.getMessage(null, "registeruser", "success");
            sender.sendMessage(successMsg.replace("{player}", playerName));

        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(LanguageManager.getMessage(null, "general", "error"));
        }

        return true;
    }
}
