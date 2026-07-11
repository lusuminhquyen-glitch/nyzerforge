package com.nyzerforge;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ForgeGUI implements Listener {
    private final NyzerForgePlugin plugin;
    private final Player player;
    private Inventory inventory;
    private static final Map<UUID, ForgeGUI> activeGUIs = new HashMap<>();

    public ForgeGUI(NyzerForgePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        int size = 27;
        inventory = Bukkit.createInventory(null, size, plugin.getForgeManager().colorize("&8⚒ &6&lNyzerForge &8⚒"));

        // Anvil center
        ItemStack anvil = new ItemStack(Material.ANVIL);
        ItemMeta anvilMeta = anvil.getItemMeta();
        anvilMeta.setDisplayName(plugin.getForgeManager().colorize("&6&l⚒ BỀ RÈN ⚒"));
        anvilMeta.setLore(Arrays.asList(
            plugin.getForgeManager().colorize("&7Đặt công cụ/vũ khí/giáp cần rèn"),
            plugin.getForgeManager().colorize("&7và nguyên liệu để bắt đầu!")
        ));
        anvil.setItemMeta(anvilMeta);
        inventory.setItem(13, anvil);

        // Tool slot
        ItemStack toolSlot = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta toolMeta = toolSlot.getItemMeta();
        toolMeta.setDisplayName(plugin.getForgeManager().colorize("&e⚔ &fVật phẩm cần rèn"));
        toolMeta.setLore(Arrays.asList(
            plugin.getForgeManager().colorize("&7Click chuột trái để đặt vật phẩm"),
            plugin.getForgeManager().colorize("&7Click chuột phải để lấy ra")
        ));
        toolSlot.setItemMeta(toolMeta);
        inventory.setItem(11, toolSlot);

        // Material slot
        ItemStack materialSlot = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta materialMeta = materialSlot.getItemMeta();
        materialMeta.setDisplayName(plugin.getForgeManager().colorize("&a📦 Nguyên liệu rèn"));
        materialMeta.setLore(Arrays.asList(
            plugin.getForgeManager().colorize("&7Click chuột trái để đặt nguyên liệu"),
            plugin.getForgeManager().colorize("&7Click chuột phải để lấy ra")
        ));
        materialSlot.setItemMeta(materialMeta);
        inventory.setItem(15, materialSlot);

        // Forge button
        ItemStack forgeButton = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta forgeMeta = forgeButton.getItemMeta();
        forgeMeta.setDisplayName(plugin.getForgeManager().colorize("&6&l🔥 RÈN NGAY 🔥"));
        forgeMeta.setLore(Arrays.asList(
            plugin.getForgeManager().colorize("&7Nhấn để bắt đầu rèn"),
            plugin.getForgeManager().colorize("&7với vật phẩm và nguyên liệu đã chọn!")
        ));
        forgeButton.setItemMeta(forgeMeta);
        inventory.setItem(22, forgeButton);

        // Decorative borders
        decorateInventory();

        // Lưu GUI vào map để theo dõi
        activeGUIs.put(player.getUniqueId(), this);
        player.openInventory(inventory);
    }

    private void decorateInventory() {
        Material borderMat = Material.BLACK_STAINED_GLASS_PANE;
        ItemStack border = new ItemStack(borderMat);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        int[] slots = {0,1,2,3,4,5,6,7,8,9,17,18,19,20,21,23,24,25,26};
        for (int slot : slots) {
            inventory.setItem(slot, border);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getInventory().equals(inventory)) return;

        Player p = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // Cho phép click vào các slot đặc biệt
        if (slot == 11 || slot == 15) {
            // Cho phép tương tác với slot vật phẩm và nguyên liệu
            event.setCancelled(false);
            return;
        }

        if (slot == 22) {
            // Nút rèn
            event.setCancelled(true);
            ItemStack tool = inventory.getItem(11);
            ItemStack material = inventory.getItem(15);
            
            if (tool == null || tool.getType() == Material.AIR) {
                p.sendMessage(plugin.getForgeManager().colorize("&c❌ Vui lòng đặt vật phẩm cần rèn!"));
                return;
            }
            
            if (material == null || material.getType() == Material.AIR) {
                p.sendMessage(plugin.getForgeManager().colorize("&c❌ Vui lòng đặt nguyên liệu!"));
                return;
            }
            
            plugin.getForgeManager().forgeItem(p, tool, material);
            updateInventory();
            return;
        }

        // Chặn tương tác với các slot khác
        if (slot < inventory.getSize()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        // Cho phép kéo thả vào slot 11 và 15
        for (int slot : event.getRawSlots()) {
            if (slot != 11 && slot != 15) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        // Xóa GUI khỏi map
        activeGUIs.remove(event.getPlayer().getUniqueId());
        
        // Trả lại vật phẩm cho người chơi nếu có
        Player p = (Player) event.getPlayer();
        ItemStack tool = inventory.getItem(11);
        ItemStack material = inventory.getItem(15);
        
        if (tool != null && tool.getType() != Material.AIR) {
            p.getInventory().addItem(tool);
        }
        if (material != null && material.getType() != Material.AIR) {
            p.getInventory().addItem(material);
        }
        
        // Hủy đăng ký listener
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
    }

    private void updateInventory() {
        player.updateInventory();
    }
}