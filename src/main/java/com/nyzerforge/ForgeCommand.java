// ForgeCommand.java
package com.nyzerforge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForgeCommand implements CommandExecutor {
    private final NyzerForgePlugin plugin;

    public ForgeCommand(NyzerForgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này!");
            return true;
        }

        Player player = (Player) sender;
        new ForgeGUI(plugin, player).open();
        return true;
    }
}
