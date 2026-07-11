// NyzerForgePlugin.java
package com.nyzerforge;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NyzerForgePlugin extends JavaPlugin {
    private static NyzerForgePlugin instance;
    private ForgeManager forgeManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        configManager = new ConfigManager(this);
        forgeManager = new ForgeManager(this);
        
        getCommand("forge").setExecutor(new ForgeCommand(this));
        getCommand("forgeremove").setExecutor(new ForgeRemoveCommand(this));
        getCommand("forgereload").setExecutor(new ForgeReloadCommand(this));
        
        Bukkit.getPluginManager().registerEvents(new ForgeListener(this), this);
        
        getLogger().info("§aNyzerForge đã được kích hoạt!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§cNyzerForge đã được tắt!");
    }

    public static NyzerForgePlugin getInstance() {
        return instance;
    }

    public ForgeManager getForgeManager() {
        return forgeManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
