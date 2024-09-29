package org.curryman.xhomes;

import com.earth2me.essentials.Essentials;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.curryman.xhomes.commands.HomeCommand;
import org.curryman.xhomes.commands.HomeTabCompleter;
import org.curryman.xhomes.listeners.HomeClickListener;
import org.curryman.xhomes.managers.ConfigManager;
import org.bstats.bukkit.Metrics;

public class XHomes extends JavaPlugin {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GRAY = "\u001B[90m";
    public static final String ANSI_RED = "\u001B[31m";

    private static XHomes instance;
    private Essentials essentials;

    @Override
    public void onEnable() {
        instance = this;
        int pluginId = 	23484;
        Metrics metrics = new Metrics(this, pluginId);

        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage(ANSI_RED + "██---██-" + ANSI_GRAY +"-██---██---██████--███----███-███████-███████-" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage(ANSI_RED + "-██-██-" + ANSI_GRAY + "--██---██--██----██-████--████-██------██------" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage(ANSI_RED + "--███-" + ANSI_GRAY + "---███████--██----██-██-████-██ █████---███████-" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage(ANSI_RED + "-██ ██-" + ANSI_GRAY + "--██---██--██----██-██--██--██-██-----------██-" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage(ANSI_RED + "██---██-" + ANSI_GRAY +"-██---██---██████--██------██-███████-███████-" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage("");
        hookEssentials();
        getServer().getConsoleSender().sendMessage(ANSI_GREEN + "Initialising Configuration" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage(ANSI_GREEN + "Config Initialised!" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage(ANSI_GREEN + "xHomes has been enabled successfully!" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage(ANSI_YELLOW + "Version 1.0" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage(ANSI_YELLOW + "Made by CurryMan" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage(ANSI_YELLOW + "Get it now on SpigotMC!" + ANSI_RESET);
        getServer().getConsoleSender().sendMessage("");



        ConfigManager.loadConfig(this);


        this.getCommand("homes").setExecutor(new HomeCommand());
        this.getCommand("xhomes").setExecutor(new HomeCommand());
        this.getCommand("home").setExecutor(new HomeCommand());
        this.getCommand("home").setTabCompleter(new HomeTabCompleter(getEssentials()));
        this.getCommand("xhomes").setTabCompleter(new HomeTabCompleter(getEssentials()));

        getServer().getPluginManager().registerEvents(new HomeClickListener(), this);

        getLogger().info(ANSI_GREEN + "xHomes Sartup complete!");
    }

    @Override
    public void onDisable() {
        getLogger().info("xHomes has been disabled!");
    }

    public Essentials getEssentials() {
        return essentials;
    }

    public static XHomes getInstance() {
        return instance;
    }
    private void hookEssentials() {
        Plugin essentialsPlugin = getServer().getPluginManager().getPlugin("Essentials");

        if (essentialsPlugin != null && essentialsPlugin instanceof Essentials) {
            essentials = (Essentials) essentialsPlugin;
            getServer().getConsoleSender().sendMessage(ANSI_GREEN + "EssentialsX Found! Initialising Hook" + ANSI_RESET);
            getServer().getConsoleSender().sendMessage(ANSI_GREEN + "Hook Successful! Essentials Data loaded" + ANSI_RESET);
        } else {
            getLogger().severe("EssentialsX not found! Disabling xHomes.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
