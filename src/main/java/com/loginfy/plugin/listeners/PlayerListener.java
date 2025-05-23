package com.loginfy.plugin.listeners;

import com.loginfy.plugin.Loginfy;
import com.loginfy.plugin.commands.LoginCommand;
import com.loginfy.plugin.commands.RegisterCommand;
import com.loginfy.plugin.commands.ChangePasswordCommand;
import com.loginfy.plugin.commands.RegisterUserCommand;
import com.loginfy.plugin.util.LanguageManager;
import com.loginfy.plugin.util.LoginManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Arrays;
import java.util.UUID;

public class PlayerListener implements Listener {

    private boolean isNotLoggedIn(Player player) {
        return !LoginManager.isLoggedIn(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isNotLoggedIn(player)) {
            if (!event.getFrom().getBlock().equals(event.getTo().getBlock())) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (isNotLoggedIn(player)) {
            event.setCancelled(true);
            player.sendMessage(LanguageManager.getMessage(player, "mustLogin", "chat"));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isNotLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isNotLoggedIn(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isNotLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamageReceive(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isNotLoggedIn(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        // Gizli komutlar: register, login, changepass, registeruser
        if (message.startsWith("/register ") || message.equalsIgnoreCase("/register")) {
            handleCommand(event, new RegisterCommand());
            return;
        }
        if (message.startsWith("/login ") || message.equalsIgnoreCase("/login")) {
            handleCommand(event, new LoginCommand());
            return;
        }
        if (message.startsWith("/changepass ") || message.equalsIgnoreCase("/changepass")) {
            handleCommand(event, new ChangePasswordCommand());
            return;
        }
        if (message.startsWith("/registeruser ") || message.equalsIgnoreCase("/registeruser")) {
            handleCommand(event, new RegisterUserCommand());
            return;
        }

        // Diğer tüm komutlar için giriş kontrolü
        if (isNotLoggedIn(player)) {
            // login veya register dışında herhangi bir komut iptal edilir
            event.setCancelled(true);
            player.sendMessage(LanguageManager.getMessage(player, "mustLogin", "cmd"));
        }
    }

    private void handleCommand(PlayerCommandPreprocessEvent event, CommandExecutor executor) {
        event.setCancelled(true); // Konsola gösterimi engelle
        String[] parts = event.getMessage().substring(1).split(" ");
        String label = parts[0];
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
        executor.onCommand((CommandSender) event.getPlayer(), null, label, args);
    }
}
