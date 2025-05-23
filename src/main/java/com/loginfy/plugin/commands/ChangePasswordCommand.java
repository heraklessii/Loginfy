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

public class ChangePasswordCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Yalnızca oyuncular
        if (!(sender instanceof Player)) {
            sender.sendMessage(LanguageManager.getMessage(null, "changepass", "onlyPlayer"));
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Giriş kontrolü
        if (!LoginManager.isLoggedIn(uuid)) {
            player.sendMessage(LanguageManager.getMessage(player, "changepass", "notLoggedIn"));
            return true;
        }

        // Argüman kontrolü
        if (args.length != 3) {
            player.sendMessage(LanguageManager.getMessage(player, "changepass", "usage"));
            return true;
        }

        String oldPass = args[0];
        String newPass = args[1];
        String newPassRepeat = args[2];

        // Yeni şifre eşleşme kontrolü
        if (!newPass.equals(newPassRepeat)) {
            player.sendMessage(LanguageManager.getMessage(player, "changepass", "mismatch"));
            return true;
        }

        try {
            Connection conn = Loginfy.getInstance().getDatabaseManager().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashed = rs.getString("password");

                // Eski şifre kontrolü
                if (!BCrypt.checkpw(oldPass, hashed)) {
                    player.sendMessage(LanguageManager.getMessage(player, "changepass", "wrongOld"));
                    return true;
                }

                // Yeni şifreyi güncelle
                String newHashed = BCrypt.hashpw(newPass, BCrypt.gensalt());
                PreparedStatement update = conn.prepareStatement(
                        "UPDATE users SET password = ? WHERE uuid = ?"
                );
                update.setString(1, newHashed);
                update.setString(2, uuid.toString());
                update.executeUpdate();

                player.sendMessage(LanguageManager.getMessage(player, "changepass", "success"));

            } else {
                // Kullanıcı bulunamadı (genellikle giriş kontrolüne takılır)
                player.sendMessage(LanguageManager.getMessage(player, "changepass", "notLoggedIn"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(LanguageManager.getMessage(player, "general", "error"));
        }

        return true;
    }
}
