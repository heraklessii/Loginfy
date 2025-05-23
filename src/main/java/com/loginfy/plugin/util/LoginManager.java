package com.loginfy.plugin.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LoginManager {

    private static final Set<UUID> loggedInUsers = new HashSet<>();

    public static void login(UUID uuid) {
        loggedInUsers.add(uuid);
    }

    public static boolean isLoggedIn(UUID uuid) {
        return loggedInUsers.contains(uuid);
    }

    public static void logout(UUID uuid) {
        loggedInUsers.remove(uuid);
    }
}
