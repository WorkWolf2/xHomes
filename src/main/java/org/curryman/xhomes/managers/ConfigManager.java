package org.curryman.xhomes.managers;

import com.earth2me.essentials.libs.kyori.adventure.platform.facet.Facet;
import com.sun.org.apache.xerces.internal.xs.StringList;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.curryman.xhomes.XHomes;
import org.curryman.xhomes.gui.HomesGUI;
import org.curryman.xhomes.listeners.HomeClickListener;

import java.util.List;

public class ConfigManager {

    private static FileConfiguration config;

    public static void loadConfig(Plugin plugin) {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

    }

    public static void reloadConfig(Player player) {
        XHomes.getInstance().reloadConfig();
        config = XHomes.getInstance().getConfig();
        player.sendMessage(getMessage("messages.reload"));
    }

    public static String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, "Message not found").replaceAll("#", "§x"));
    }
    public static String getRawMessage(String path) {
        return ChatColor.stripColor(config.getString(path));
    }
    public static int getInt(String path) {
        return config.getInt(path, 0);
    }
    public static boolean getBoolean(String path) {
        return config.getBoolean(path, true);
    }
    public static List getList(String path) {
        return config.getList(path);
    }
}