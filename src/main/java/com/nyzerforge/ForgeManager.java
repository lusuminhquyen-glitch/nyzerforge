// ForgeManager.java
package com.nyzerforge;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgeManager {
    private final NyzerForgePlugin plugin;
    private final Map<String, List<ForgeMaterial>> forgeMaterials = new HashMap<>();
    private final Random random = new Random();
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public ForgeManager(NyzerForgePlugin plugin) {
        this.plugin = plugin;
        loadMaterials();
    }

    public void loadMaterials() {
        forgeMaterials.clear();
        ConfigurationSection materials = plugin.getConfig().getConfigurationSection("materials");
        if (materials == null) return;

        for (String key : materials.getKeys(false)) {
            List<ForgeMaterial> list = new ArrayList<>();
            ConfigurationSection section = materials.getConfigurationSection(key);
            if (section == null) continue;

            for (String matKey : section.getKeys(false)) {
                ConfigurationSection matSection = section.getConfigurationSection(matKey);
                if (matSection == null) continue;

                Material material = Material.getMaterial(matSection.getString("item", "DIAMOND"));
                if (material == null) continue;

                String displayName = matSection.getString("name", "&f" + matKey);
                List<String> lore = matSection.getStringList("lore");
                int forgeChance = matSection.getInt("forge-chance", 100);
                double minValue = matSection.getDouble("min-value", 1.0);
                double maxValue = matSection.getDouble("max-value", 5.0);
                String statType = matSection.getString("stat-type", "DAMAGE");

                list.add(new ForgeMaterial(material, displayName, lore, forgeChance, minValue, maxValue, statType));
            }
            forgeMaterials.put(key, list);
        }
    }

    public String colorize(String message) {
        if (message == null) return "";
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public List<String> colorizeList(List<String> list) {
        List<String> colored = new ArrayList<>();
        for (String line : list) {
            colored.add(colorize(line));
        }
        return colored;
    }

    public void forgeItem(Player player, ItemStack tool, ItemStack materialItem) {
        if (tool == null || tool.getType() == Material.AIR) {
            player.sendMessage(colorize("&c❌ Vui lòng đặt vật phẩm cần rèn!"));
            return;
        }

        if (materialItem == null || materialItem.getType() == Material.AIR) {
            player.sendMessage(colorize("&c❌ Vui lòng đặt nguyên liệu!"));
            return;
        }

        ForgeMaterial forgeMat = getForgeMaterial(materialItem);
        if (forgeMat == null) {
            player.sendMessage(colorize("&c❌ Nguyên liệu không hợp lệ!"));
            return;
        }

        if (random.nextInt(100) >= forgeMat.forgeChance) {
            player.sendMessage(colorize("&c❌ Rèn thất bại! Nguyên liệu đã bị mất."));
            materialItem.setAmount(materialItem.getAmount() - 1);
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            player.sendMessage(colorize("&c❌ Không thể rèn vật phẩm này!"));
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        int forgeLines = getForgeLines(lore);
        if (forgeLines >= 6) {
            player.sendMessage(colorize("&c❌ Vật phẩm đã đạt tối đa 6 dòng rèn!"));
            return;
        }

        double value = forgeMat.minValue + (forgeMat.maxValue - forgeMat.minValue) * random.nextDouble();
        value = Math.round(value * 10.0) / 10.0;

        String statLine = generateStatLine(forgeMat.statType, value);
        lore.add(colorize(statLine));
        meta.setLore(lore);
        tool.setItemMeta(meta);

        materialItem.setAmount(materialItem.getAmount() - 1);
        player.sendMessage(colorize("&a✅ Rèn thành công! &fĐã thêm: " + statLine));
    }

    private ForgeMaterial getForgeMaterial(ItemStack item) {
        for (List<ForgeMaterial> list : forgeMaterials.values()) {
            for (ForgeMaterial mat : list) {
                if (mat.material == item.getType()) {
                    return mat;
                }
            }
        }
        return null;
    }

    private int getForgeLines(List<String> lore) {
        int count = 0;
        for (String line : lore) {
            for (String stat : plugin.getConfig().getStringList("stat-identifiers")) {
                if (line.contains(stat)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private String generateStatLine(String statType, double value) {
        String format = plugin.getConfig().getString("stat-formats." + statType, "&7+{value} {stat}");
        String display = plugin.getConfig().getString("stat-display." + statType, statType);
        return format.replace("{value}", String.valueOf(value)).replace("{stat}", display);
    }

    public void removeForgeLine(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(colorize("&c❌ Vui lòng cầm vật phẩm cần gỡ dòng rèn!"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(colorize("&c❌ Không thể gỡ dòng rèn!"));
            return;
        }

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            player.sendMessage(colorize("&c❌ Vật phẩm không có dòng rèn nào!"));
            return;
        }

        List<String> forgeLines = new ArrayList<>();
        List<Integer> forgeIndices = new ArrayList<>();
        for (int i = 0; i < lore.size(); i++) {
            for (String stat : plugin.getConfig().getStringList("stat-identifiers")) {
                if (lore.get(i).contains(stat)) {
                    forgeLines.add(lore.get(i));
                    forgeIndices.add(i);
                    break;
                }
            }
        }

        if (forgeLines.isEmpty()) {
            player.sendMessage(colorize("&c❌ Vật phẩm không có dòng rèn nào!"));
            return;
        }

        int lastIndex = forgeIndices.get(forgeIndices.size() - 1);
        lore.remove(lastIndex);
        meta.setLore(lore);
        item.setItemMeta(meta);
        player.sendMessage(colorize("&a✅ Đã gỡ dòng rèn cuối cùng!"));
    }

    public static class ForgeMaterial {
        public final Material material;
        public final String displayName;
        public final List<String> lore;
        public final int forgeChance;
        public final double minValue;
        public final double maxValue;
        public final String statType;

        public ForgeMaterial(Material material, String displayName, List<String> lore, 
                            int forgeChance, double minValue, double maxValue, String statType) {
            this.material = material;
            this.displayName = displayName;
            this.lore = lore;
            this.forgeChance = forgeChance;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.statType = statType;
        }
    }
}
