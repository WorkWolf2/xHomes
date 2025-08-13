package org.curryman.xhomes.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.curryman.xhomes.XHomes;
import org.curryman.xhomes.gui.HomesGUI;
import org.curryman.xhomes.listeners.HomeClickListener;
import org.curryman.xhomes.managers.ConfigManager;


import java.util.List;


public class HomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        User user;

        if (args.length == 0) {
            user = XHomes.getInstance().getEssentials().getUser(player);
        } else {
            if (player.hasPermission("xhomes.home.others")) {
                Player target = Bukkit.getPlayer(args[0]);

                if (target != null) {
                    user = XHomes.getInstance().getEssentials().getUser(target);
                    XHomes.hashMap.put(player.getUniqueId(), user);
                } else {
                    user = XHomes.getInstance().getEssentials().getUser(player);
                }
            } else {
                user = XHomes.getInstance().getEssentials().getUser(player);
            }
        }

        List<String> homes = user.getHomes();
        int countdown = ConfigManager.getInt("teleport.delay");


        if (label.equalsIgnoreCase("homes") || label.equalsIgnoreCase("home") && args.length == 0) {
            try {
                if (!user.hasValidHomes()) {
                    player.sendMessage(ConfigManager.getMessage("messages.no-homes"));
                } else {
                    if (sender.hasPermission("xhomes.gui")) {
                        HomesGUI.openHomesGUI(player, user, 1);
                    }
                    else  {
                        player.sendMessage(ConfigManager.getMessage("messages.noPerms"));
                    }

                }
            } catch (Exception e) {
                player.sendMessage("Failed to fetch homes. Check console");
                e.printStackTrace();
            }
        } else if (label.equalsIgnoreCase("xhomes") && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("xhomes.admin")) {
                ConfigManager.reloadConfig(player);
            }
            else {
                player.sendMessage(ConfigManager.getMessage("messages.noPerms"));
            }
        } else if (label.equalsIgnoreCase("home") && args.length > 0) {
            String homeName = args[0];
            Location homeLocation = user.getHome(homeName);
            String delay = Integer.toString(countdown);
            if (homes.contains(homeName)) {

                if (sender.hasPermission("xhomes.individual")) {
                    if (sender.hasPermission("xhomes.cooldown-bypass")) {
                        ((Player) sender).teleport(homeLocation);
                        sender.sendMessage(ConfigManager.getMessage("messages.teleport-complete").replace("{home}", homeName));
                        return true;
                    }
                    else {
                        HomeClickListener.teleportWithCountdown(player, homeLocation, countdown, true, homeName);
                    }
                }
                else  {
                    player.sendMessage(ConfigManager.getMessage("messages.noPerms"));
                }
            }
            else {
                player.sendMessage(ConfigManager.getMessage("messages.no-home-found").replace("{home}", homeName));
                return true;
            }

        } else if (label.equalsIgnoreCase("xhomes") && args.length == 0) {
            if (sender.hasPermission("xhomes.admin")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f[&cxHomes&f]"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Version &c1.0.2"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Made by &cCurryMan"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', " "));
            }
            else  {
                player.sendMessage(ConfigManager.getMessage("messages.noPerms"));
            }
        }

        return true;
    }
}
