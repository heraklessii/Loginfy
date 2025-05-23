package com.loginfy.plugin.listeners;

import com.loginfy.plugin.Loginfy;
import com.loginfy.plugin.util.LoginManager;
import com.loginfy.plugin.util.LanguageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AutoLoginListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String currentIp = player.getAddress().getAddress().getHostAddress();

        Connection conn = Loginfy.getInstance().getDatabaseManager().getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(
                    "SELECT last_ip FROM users WHERE uuid = ?"
            );
            stmt.setString(1, uuid.toString());
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Kayıtlı kullanıcı
                String savedIp = rs.getString("last_ip");
                if (currentIp.equals(savedIp)) {
                    // IP eşleşiyorsa otomatik login
                    LoginManager.login(uuid);

                    // Başlık gönder
                    String title = LanguageManager.getMessage(player, "welcome", "title").replace("{player}", player.getName());
                    String subtitle = LanguageManager.getMessage(player, "welcome", "subtitle");
                    int in = Loginfy.getInstance().getConfig().getInt("welcome.fade-in");
                    int stay = Loginfy.getInstance().getConfig().getInt("welcome.stay");
                    int out = Loginfy.getInstance().getConfig().getInt("welcome.fade-out");
                    player.sendTitle(title, subtitle, in, stay, out);

                    player.sendMessage(LanguageManager.getMessage(player, "autologin", "success"));
                } else {
                    // IP uyuşmadı: normal login iste
                    player.sendMessage(LanguageManager.getMessage(player, "autologin", "fail"));
                }
            } else {
                // Kayıtlı değil: önce register iste
                player.sendMessage(LanguageManager.getMessage(player, "autologin", "notRegistered"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(LanguageManager.getMessage(player, "general", "error"));
        } finally {
            // Kaynakları kapat
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
        }
    }
}
