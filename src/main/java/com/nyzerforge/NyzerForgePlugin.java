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
        
        // Tạo thư mục và config
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();
        
        // Khởi tạo managers
        configManager = new ConfigManager(this);
        forgeManager = new ForgeManager(this);
        
        // Đăng ký commands
        getCommand("forge").setExecutor(new ForgeCommand(this));
        getCommand("forgeremove").setExecutor(new ForgeRemoveCommand(this));
        getCommand("forgereload").setExecutor(new ForgeReloadCommand(this));
        
        // Đăng ký listener
        Bukkit.getPluginManager().registerEvents(new ForgeListener(this), this);
        
        getLogger().info("§aNyzerForge đã được kích hoạt!");
        getLogger().info("§7Version: " + getDescription().getVersion());
        getLogger().info("§7Author: " + getDescription().getAuthors());
    }

    @Override
    public void onDisable() {
        // Lưu config khi tắt
        if (configManager != null) {
            configManager.saveConfig();
        }
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
    
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (configManager != null) {
            configManager.reloadConfig();
        }
    }
}