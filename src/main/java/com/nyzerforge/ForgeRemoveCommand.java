// ForgeRemoveCommand.java
package com.nyzerforge;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ForgeRemoveCommand implements CommandExecutor {
    private final NyzerForgePlugin plugin;

    public ForgeRemoveCommand(NyzerForgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này!");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        plugin.getForgeManager().removeForgeLine(player, item);
        return true;
    }
}
