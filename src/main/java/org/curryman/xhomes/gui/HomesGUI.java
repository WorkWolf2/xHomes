package org.curryman.xhomes.gui;

import com.earth2me.essentials.User;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.curryman.xhomes.XHomes;
import org.curryman.xhomes.managers.ConfigManager;
import org.curryman.xhomes.utils.SkullUtils;
import me.arcaniax.hdb.api.HeadDatabaseAPI;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomesGUI {

    public static Set<Player> locationToggled = new HashSet<>();
    private static Map<Player, Integer> pageMap = new HashMap<>();
    public static List<Integer> getSlotRanges() {
        List<Integer> slots = new ArrayList<>();
        List<String> ranges = XHomes.getInstance().getConfig().getStringList("homes.slot-ranges");

        for (String range : ranges) {
            String[] parts = range.split("-");
            if (parts.length == 2) {
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
            }
        }
        return slots;
    }

    public static List<Integer> getFillerSlotRanges(){
        List<Integer> slots = new ArrayList<>();
        List<String> ranges = ConfigManager.getList("filler-item.slot-ranges");

        for (String range : ranges) {
            String[] parts = range.split("-");
            if (parts.length==2) {
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
            }
        }
        return slots;
    }
    public static void openHomesGUI(Player player, User user) {
        int page = pageMap.getOrDefault(player, 1);
        openHomesGUI(player, user, page);
    }

    public static void openHomesGUI(Player player, User user, int page) {
        List<Integer> slotRanges = getSlotRanges();
        String Page = Integer.toString(page);
        int size = ConfigManager.getInt("gui.size");
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', ConfigManager.getMessage("gui.title").replace("{page}", Page)));
        int homesPerPage = slotRanges.size();

        String fillerMaterial = ConfigManager.getMessage("filler-item.material");
        ItemStack fillerItem;
        if (fillerMaterial.equalsIgnoreCase("PLAYER_HEAD")) {
            String skin = ConfigManager.getMessage("filler-item.skin");
            fillerItem = SkullUtils.getCustomSkull(skin);
        } else if (fillerMaterial.startsWith("hdb-")) {
            String hdbId = fillerMaterial.split("-")[1];
            HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();
            String fillerSkin = headDatabaseAPI.getBase64(hdbId);
            fillerItem = SkullUtils.getCustomSkull(fillerSkin);
        } else {
            fillerItem = new ItemStack(Material.valueOf(fillerMaterial));
        }
        ItemMeta fillerMeta = fillerItem.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(ConfigManager.getMessage("filler-item.name"));
        }
        fillerItem.setItemMeta(fillerMeta);
        Boolean isFillerAir = ConfigManager.getMessage("filler-item.material").equalsIgnoreCase("AIR");


        if (!isFillerAir) {
            for (int slot : getFillerSlotRanges()) {
                gui.setItem(slot, fillerItem);
            }
        }


        List<String> homes = user.getHomes();
        int totalHomes = getTotalHomes(user);
        int totalPages = (int) Math.ceil((double) totalHomes / homesPerPage);


        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * homesPerPage;
        int end = Math.min(start + homesPerPage, totalHomes);


        int slotIndex = 0;
        for (int i = start; i < end; i++) {
            String homeName = homes.get(i);
            if (slotIndex < slotRanges.size()) {
                int slot = slotRanges.get(slotIndex);
                Location homeLocation = user.getHome(homeName);

                String material = ConfigManager.getMessage("homes.material");
                ItemStack homeItem = null;

                if (material.equalsIgnoreCase("PLAYER_HEAD")) {
                    String skin = ConfigManager.getMessage("homes.skin");
                    homeItem = SkullUtils.getCustomSkull(skin);

                } else if (material.startsWith("hdb-")) {
                    String hdbId = material.split("-")[1];
                    HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();
                    String homeSkin = headDatabaseAPI.getBase64(hdbId);
                    homeItem = SkullUtils.getCustomSkull(homeSkin);

                } else {
                    homeItem = new ItemStack(Material.valueOf(material.toUpperCase()));
                }

                ItemMeta meta = homeItem.getItemMeta();

                List<String> loreLines = ConfigManager.getList("homes.item-lore");
                String world = homeLocation.getWorld().getName();
                List<String> lore = new ArrayList<>();

                if (meta != null) {
                    String displayName = ConfigManager.getMessage("homes.name").replace("{home}", homeName);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

                    for (String line : loreLines) {
                        String formattedLine;
                        if (locationToggled.contains(player)) {
                            formattedLine = line.replace("{home}", homeName)
                                    .replace("{location}", ChatColor.translateAlternateColorCodes('&', "&c&lHIDDEN"))
                                    .replace("{world}", translateWorldName(world));
                        } else {
                            formattedLine = line.replace("{home}", homeName)
                                    .replace("{location}", locationToString(homeLocation))
                                    .replace("{world}", translateWorldName(world));
                        }
                        lore.add(ChatColor.translateAlternateColorCodes('&', formattedLine));
                    }

                    meta.setLore(lore);

                    if (material.equalsIgnoreCase("PAPER")) {
                        meta.setCustomModelData(ConfigManager.getInt("homes.cmd"));
                    }

                    homeItem.setItemMeta(meta);
                }

                gui.setItem(slot, homeItem);
                slotIndex++;

            }
        }


        if (page > 1) {

            String previousPageMaterial = ConfigManager.getMessage("previous-page-item.material");
            ItemStack previousPageItem;

            if (previousPageMaterial.equalsIgnoreCase("PLAYER_HEAD")) {
                String skin = ConfigManager.getMessage("previous-page-item.skin");
                previousPageItem = SkullUtils.getCustomSkull(skin);
            } else if (previousPageMaterial.startsWith("hdb-")) {
                String hdbId = previousPageMaterial.split("-")[1];
                HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();
                String previousSkin = headDatabaseAPI.getBase64(hdbId);
                previousPageItem = SkullUtils.getCustomSkull(previousSkin);

            } else {
                previousPageItem = new ItemStack(Material.valueOf(previousPageMaterial));
            }

            ItemMeta previousMeta = previousPageItem.getItemMeta();
            previousMeta.setDisplayName(ConfigManager.getMessage("previous-page-item.name"));
            List<String> previousLoreLines = ConfigManager.getList("previous-page-item.lore");
            List<String> previousLore = new ArrayList<>();

            for (String line : previousLoreLines) {
                previousLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            previousMeta.setLore(previousLore);
            previousPageItem.setItemMeta(previousMeta);
            gui.setItem(ConfigManager.getInt("previous-page-item.slot"), previousPageItem);
        }

        if (page < totalPages) {

            String nextPageMaterial = ConfigManager.getMessage("next-page-item.material");
            ItemStack nextPageItem;

            if (nextPageMaterial.equalsIgnoreCase("PLAYER_HEAD")) {
                String skin = ConfigManager.getMessage("next-page-item.skin");
                nextPageItem = SkullUtils.getCustomSkull(skin);
            } else if (nextPageMaterial.startsWith("hdb-")) {

                String hdbId = nextPageMaterial.split("-")[1];
                HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();
                String nextSkin = headDatabaseAPI.getBase64(hdbId);
                nextPageItem = SkullUtils.getCustomSkull(nextSkin);
            } else {
                nextPageItem = new ItemStack(Material.valueOf(nextPageMaterial));
            }
            ItemMeta nextMeta = nextPageItem.getItemMeta();
            nextMeta.setDisplayName(ConfigManager.getMessage("next-page-item.name"));
            List<String> nextLoreLines = ConfigManager.getList("next-page-item.lore");
            List<String> nextLore = new ArrayList<>();

            for (String line : nextLoreLines) {
                nextLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            nextMeta.setLore(nextLore);


            nextPageItem.setItemMeta(nextMeta);
            gui.setItem(ConfigManager.getInt("next-page-item.slot"), nextPageItem);
        }


        String infoItemMaterial = ConfigManager.getMessage("info-item.material");
        ItemStack infoItem;

        if (infoItemMaterial.equalsIgnoreCase("PLAYER_HEAD")) {
            String skin = ConfigManager.getMessage("info-item.skin");
            infoItem = SkullUtils.getCustomSkull(skin);
        } else if (infoItemMaterial.startsWith("hdb-")) {
            String hdbId = infoItemMaterial.split("-")[1];
            HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();
            String infoSkin = headDatabaseAPI.getBase64(hdbId);
            infoItem = SkullUtils.getCustomSkull(infoSkin);
        } else {
            infoItem = new ItemStack(Material.valueOf(infoItemMaterial));
        }

        int infoSlot = ConfigManager.getInt("info-item.slot");
        ItemMeta meta = infoItem.getItemMeta();
        List<String> infoLoreLines = ConfigManager.getList("info-item.lore");
        List<String> infoLore = new ArrayList<>();

        if (meta != null) {
            meta.setDisplayName(ConfigManager.getMessage("info-item.name"));
            String totalHomesString = Integer.toString(totalHomes);

            for (String line : infoLoreLines) {
                String formattedLine = line.replace("{player}", user.getDisplayName())
                        .replace("{homes}", totalHomesString);
                infoLore.add(ChatColor.translateAlternateColorCodes('&', formattedLine));
            }
            meta.setLore(infoLore);
        }

        infoItem.setItemMeta(meta);
        Boolean isInfoAir = ConfigManager.getMessage("info-item.material").equalsIgnoreCase("AIR");

        if (!isInfoAir) {
            gui.setItem(infoSlot, infoItem);
        }



        String openSound = ConfigManager.getMessage("sounds.gui_open");
        player.playSound(player.getLocation(), Sound.valueOf(openSound), 1.0F, 1.0F);
        player.openInventory(gui);
        pageMap.put(player, page);
    }
    public static int getPage(Player player) {
        return pageMap.getOrDefault(player, 1);
    }
    private static String locationToString (Location location){
        return "X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ();
    }
    public static String translateWorldName(String world) {
        String netherReplacement = ConfigManager.getMessage("world-replacements.world_nether");
        String endReplacement = ConfigManager.getMessage("world-replacements.world_the_end");
        String worldReplacement = ConfigManager.getMessage("world-replacements.world");

        if (world.equalsIgnoreCase("world_the_end")){
            return endReplacement;
        } else if (world.equalsIgnoreCase("world_nether")) {
            return netherReplacement;
        } else if (world.equalsIgnoreCase("world")) {
            return worldReplacement;
        } else {
            return world;
        }
    }
    public static int getTotalHomes (User user){
        List<String> homes = user.getHomes();
        int homeCount = 0;
        for (String homeName : homes) {
            homeCount++;
        }
        return homeCount;
    }





}


