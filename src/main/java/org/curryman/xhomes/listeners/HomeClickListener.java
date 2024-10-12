package org.curryman.xhomes.listeners;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.curryman.xhomes.XHomes;
import org.curryman.xhomes.gui.HomesGUI;
import org.curryman.xhomes.managers.ConfigManager;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeClickListener implements Listener {

    int delay = ConfigManager.getInt("teleport.delay");

    private Map<UUID, Boolean> teleportingPlayers = new HashMap<>();
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        String homeName = clickedItem.getItemMeta().getDisplayName().replace(ConfigManager.getMessage("gui.homes.name").replace("{home}", ""), "");
        int currentPage = HomesGUI.getPage(player);
        String Page = Integer.toString(currentPage);

        if (event.getView().getTitle().equals(ConfigManager.getMessage("gui.title").replace("{page}", Page))) {

            event.setCancelled(true);

            if (event.getClick() == ClickType.SWAP_OFFHAND){
                event.setCancelled(true);
            }



            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true);
            }







            User essentialsUser = XHomes.getInstance().getEssentials().getUser(player);

            Boolean itemName = clickedItem.getType().equals(Material.valueOf(ConfigManager.getMessage("homes.material")));
            Boolean infoItemName = clickedItem.getItemMeta().getDisplayName().equals(ConfigManager.getMessage("info-item.name"));

            if (event.getClick() == ClickType.SHIFT_LEFT && !itemName) {
                event.setCancelled(true);
            }




            event.setCancelled(true);
            if (event.isLeftClick()) {
                if (event.isShiftClick()) {
                    event.setCancelled(true);
                    return;
                }
                if (clickedItem.getItemMeta().getDisplayName().equals(ConfigManager.getMessage("next-page-item.name"))) {
                    HomesGUI.openHomesGUI(player, essentialsUser, currentPage + 1);
                } else if (clickedItem.getItemMeta().getDisplayName().equals(ConfigManager.getMessage("previous-page-item.name"))) {
                    HomesGUI.openHomesGUI(player, essentialsUser, currentPage - 1);
                }
                event.setCancelled(true);
            }

            if (itemName) {
                if (event.isLeftClick()) {
                    if (event.isShiftClick()) {
                        event.setCancelled(true);
                        return;
                    }
                    if (essentialsUser != null) {
                        try {
                            List<String> homeNames = essentialsUser.getHomes();

                            if (homeNames.contains(homeName)) {


                                if (!teleportingPlayers.getOrDefault(player.getUniqueId(), false)) {
                                    teleportingPlayers.put(player.getUniqueId(), true);


                                    player.closeInventory();


                                    Location homeLocation = null;
                                    try {
                                        homeLocation = essentialsUser.getHome(homeName);
                                    } catch (Exception e) {
                                        player.sendMessage(ChatColor.RED + "Failed to find home location!");
                                        return;
                                    }

                                    if (homeLocation == null) {
                                        player.sendMessage(ChatColor.RED + "Home location is not set or invalid!");
                                        return;
                                    }

                                    String leftClickSound = ConfigManager.getMessage("sounds.left_click");
                                    player.playSound(player.getLocation(), Sound.valueOf(leftClickSound), 1.0F, 1.0F);
                                    if (player.hasPermission("xhomes.cooldown-bypass")) {
                                        player.teleport(homeLocation);
                                        player.sendMessage(ConfigManager.getMessage("messages.teleport-complete").replace("{home}", homeName));
                                    }
                                    else {
                                        teleportWithCountdown(player, homeLocation, delay, true, homeName);
                                    }


                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            teleportingPlayers.remove(player.getUniqueId());
                                        }
                                    }.runTaskLater(XHomes.getInstance(), delay * 20);
                                } else {
                                    player.sendMessage(ConfigManager.getMessage("messages.already-in-progress"));
                                }
                            } else {
                                player.sendMessage(ConfigManager.getMessage("messages.no-home-found").replace("{home}", homeName));
                            }
                        } catch (Exception e) {
                            player.sendMessage("Action Failed. Please check console");
                            e.printStackTrace();
                        }
                    }
                }
            } else if (infoItemName) {
                if (event.isRightClick()) {
                    if (HomesGUI.locationToggled.contains(player)) {
                        HomesGUI.locationToggled.remove(player);
                        player.sendMessage(ConfigManager.getMessage("messages.location-toggled-off"));
                        Boolean enabled = ConfigManager.getBoolean("sounds.enabled");
                        String rightClickSound = ConfigManager.getMessage("sounds.right_click");
                        player.playSound(player.getLocation(), Sound.valueOf(rightClickSound), 1.0F, 1.0F);
                    } else  {
                        HomesGUI.locationToggled.add(player);
                        player.sendMessage(ConfigManager.getMessage("messages.location-toggled-on"));

                    }

                    HomesGUI.openHomesGUI(player, essentialsUser, 1);
                }
            }
        }
    }


    public static void teleportWithCountdown(Player player, Location location, int countdown, boolean cancelOnMove, String homeName) {
        Location initialLocation = player.getLocation();
        String Countdown = Integer.toString(countdown);
        player.sendMessage(ConfigManager.getMessage("messages.teleport-start").replace("{seconds}", Countdown));
        new BukkitRunnable() {
            int timeLeft = countdown;

            @Override
            public void run() {
                Location currentLocation = player.getLocation();
                if (cancelOnMove && hasPlayerMoved(initialLocation, currentLocation)) {
                    player.sendMessage(ConfigManager.getMessage("messages.cancelled-move"));
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    player.teleport(location);
                    String teleportSound = ConfigManager.getMessage("sounds.teleport");
                    player.playSound(player.getLocation(), Sound.valueOf(teleportSound), 1.0F, 1.0F);
                    player.sendMessage(ConfigManager.getMessage("messages.teleport-complete").replace("{home}", homeName));
                    cancel();
                } else {

                    String title = ConfigManager.getMessage("messages.title").replace("{seconds}", String.valueOf(timeLeft));
                    String subtitle = ConfigManager.getMessage("messages.subtitle").replace("{seconds}", String.valueOf(timeLeft));
                    String actionBar = ConfigManager.getMessage("messages.actionbar").replace("{seconds}", String.valueOf(timeLeft));

                    player.sendTitle(title, subtitle, 0, 20, 0);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBar));
                    String countdownSound = ConfigManager.getMessage("sounds.countdown");
                    player.playSound(player.getLocation(), Sound.valueOf(countdownSound), 1.0F, 1.0F);

                    timeLeft--;
                }
            }
        }.runTaskTimer(XHomes.getInstance(), 0, 20);
    }

    private static boolean hasPlayerMoved(Location originalLocation, Location currentLocation) {
        return originalLocation.getBlockX() != currentLocation.getBlockX() ||
                originalLocation.getBlockY() != currentLocation.getBlockY() ||
                originalLocation.getBlockZ() != currentLocation.getBlockZ();
    }

    @EventHandler

    public static void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(ConfigManager.getMessage("gui.title"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onDeleteHome(InventoryClickEvent event) {


        if (event.getView().getTitle().equals(ConfigManager.getMessage("gui.title"))) {

            if (event.getClick().equals(ClickType.DROP)) {
                Player player = (Player) event.getWhoClicked();
                Inventory inventory = event.getInventory();
                ItemStack clickedItem = event.getCurrentItem();
                Boolean itemName = clickedItem.getType().equals(Material.valueOf(ConfigManager.getMessage("homes.material")));

                if (itemName) {
                    if (clickedItem != null && clickedItem.hasItemMeta()) {
                        String homeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());


                        Essentials essentials = XHomes.getInstance().getEssentials();
                        User user = essentials.getUser(player);
                        Boolean enabled = ConfigManager.getBoolean("sounds.enabled");

                        try {
                            user.delHome(homeName);
                            player.sendMessage(ConfigManager.getMessage("messages.home-deleted").replace("{home}", homeName));
                            if (enabled) {
                                try {
                                    String dropSound = ConfigManager.getMessage("sounds.drop_item");
                                    player.playSound(player.getLocation(), Sound.valueOf(dropSound), 1.0F, 1.0F);
                                } catch (Exception e) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lERROR! &7Please check console"));
                                    XHomes.getInstance().getLogger().info(XHomes.ANSI_RED  + "========== ERROR IN CONFIGURATION ==========" + XHomes.ANSI_RESET);
                                    XHomes.getInstance().getLogger().info(XHomes.ANSI_GRAY + "There is an error in the config.yml file!" + XHomes.ANSI_RESET);
                                    XHomes.getInstance().getLogger().info(XHomes.ANSI_GRAY + "Please check the " + XHomes.ANSI_RED + "sounds" + XHomes.ANSI_GRAY + "section");
                                    XHomes.getInstance().getLogger().info(XHomes.ANSI_GRAY + "It could be " + XHomes.ANSI_RED + "SYNTAX or LOGIC");
                                }
                            }



                            inventory.setItem(event.getSlot(), null);
                            player.updateInventory();
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "Failed to delete home.");
                        }
                    }
                }
            }

        }

    }
}
