package com.onarandombox.Rigby.MultiVerse;

import java.util.logging.Logger;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.nijikokun.bukkit.Permissions.Permissions;

public class MVPluginListener extends ServerListener {
    private final Logger log = Logger.getLogger("Minecraft");

    MultiVerse plugin;

    public MVPluginListener(MultiVerse instance) {
        this.plugin = instance;
    }

    public void onPluginEnabled(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Permissions")) {
            MultiVerse.Permissions = Permissions.Security;
            log.info("[MultiVerse] Found Permissions, enabling commands.");
        }

        if (event.getPlugin().getDescription().getName().equals("iConomy")) {
            MultiVerse.useiConomy = true;
            log.info("[MultiVerse] Found iConomy, enabling payments.");
        }

    }
}