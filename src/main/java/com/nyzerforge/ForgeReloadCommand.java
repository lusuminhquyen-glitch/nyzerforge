// ForgeReloadCommand.java
package com.nyzerforge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ForgeReloadCommand implements CommandExecutor {
    private final NyzerForgePlugin plugin;

    public ForgeReloadCommand(NyzerForgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        plugin.getForgeManager().loadMaterials();
        sender.sendMessage(plugin.getForgeManager().colorize("&a✅ Đã reload config thành công!"));
        return true;
    }
}
