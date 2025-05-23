package com.loginfy.plugin.commands;

import com.loginfy.plugin.Loginfy;
import com.loginfy.plugin.util.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class RegisterCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Yalnızca oyuncular
        if (!(sender instanceof Player)) {
            sender.sendMessage(LanguageManager.getMessage(null, "register", "onlyPlayer"));
            return true;
        }

        Player player = (Player) sender;

        // Argüman sayısı kontrolü
        if (args.length != 2) {
            player.sendMessage(LanguageManager.getMessage(player, "register", "usage"));
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        // Şifre eşleşme kontrolü
        if (!password.equals(confirmPassword)) {
            player.sendMessage(LanguageManager.getMessage(player, "register", "mismatch"));
            return true;
        }

        try {
            Connection conn = Loginfy.getInstance().getDatabaseManager().getConnection();
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            checkStmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = checkStmt.executeQuery();

            // Zaten kayıtlı mı?
            if (rs.next()) {
                player.sendMessage(LanguageManager.getMessage(player, "register", "already"));
                return true;
            }

            // Şifre hash ve veritabanına ekle
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String sql = "INSERT INTO users (uuid, username, password, register_date) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, LocalDate.now().toString());
            stmt.executeUpdate();

            player.sendMessage(LanguageManager.getMessage(player, "register", "success"));

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(LanguageManager.getMessage(player, "general", "error"));
        }

        return true;
    }
}