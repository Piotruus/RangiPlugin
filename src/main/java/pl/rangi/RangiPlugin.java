\
package pl.rangi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class RangiPlugin extends JavaPlugin implements Listener {
    private final Map<UUID, String> detailViews = new HashMap<>();
    private final Map<UUID, String> menuViews = new HashMap<>();
    private static final List<String> RANKS = List.of("vip", "mvip", "elita", "special");

    @Override public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("RangiPlugin wlaczony. Komenda: /rangi");
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Tej komendy moze uzyc tylko gracz.");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("rangi.admin")) {
                player.sendMessage(color("&cNie masz uprawnien."));
                return true;
            }
            reloadConfig();
            player.sendMessage(color("&aKonfiguracja rang przeladowana."));
            return true;
        }
        openMain(player);
        return true;
    }

    private void openMain(Player player) {
        int rows = Math.max(1, Math.min(6, getConfig().getInt("menu.rows", 3)));
        String title = color(getConfig().getString("menu.title", "&8RANGI"));
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);
        Material filler = material(getConfig().getString("menu.filler", "BLACK_STAINED_GLASS_PANE"), Material.BLACK_STAINED_GLASS_PANE);
        ItemStack blank = item(filler, " ", List.of());
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, blank);
        for (String rank : RANKS) {
            String path = "menu.items." + rank;
            int slot = getConfig().getInt("menu.slots." + rank, defaultSlot(rank));
            if (slot >= 0 && slot < inv.getSize()) {
                inv.setItem(slot, configuredItem(path, Material.PAPER));
            }
        }
        menuViews.put(player.getUniqueId(), title);
        player.openInventory(inv);
    }

    private void openDetails(Player player, String rank) {
        String path = "ranks." + rank;
        String title = color(getConfig().getString("details.title", "&8Ranga: {rank}")
                .replace("{rank}", ChatColor.stripColor(color(getConfig().getString(path + ".title", rank.toUpperCase())))));
        int rows = Math.max(1, Math.min(6, getConfig().getInt("details.rows", 3)));
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);
        Material icon = material(getConfig().getString(path + ".material", "PAPER"), Material.PAPER);
        List<String> lore = getConfig().getStringList(path + ".description");
        inv.setItem(13, item(icon, getConfig().getString(path + ".title", rank.toUpperCase()), lore));
        int back = getConfig().getInt("details.back-slot", 18);
        if (back >= 0 && back < inv.getSize()) {
            inv.setItem(back, item(material(getConfig().getString("details.back-material", "ARROW"), Material.ARROW),
                    getConfig().getString("details.back-name", "&cPowrót do rang"), List.of("&7Kliknij, aby wrócić.")));
        }
        detailViews.put(player.getUniqueId(), rank);
        player.openInventory(inv);
    }

    @EventHandler public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID id = player.getUniqueId();
        String rankView = detailViews.get(id);
        String mainView = menuViews.get(id);
        if (rankView == null && mainView == null) return;
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) return;
        int slot = event.getSlot();
        if (rankView != null) {
            int back = getConfig().getInt("details.back-slot", 18);
            if (slot == back) {
                detailViews.remove(id);
                openMain(player);
            }
            return;
        }
        for (String rank : RANKS) {
            if (slot == getConfig().getInt("menu.slots." + rank, defaultSlot(rank))) {
                detailViews.remove(id);
                openDetails(player, rank);
                return;
            }
        }
    }

    @Override public void onDisable() {
        detailViews.clear();
        menuViews.clear();
    }

    private int defaultSlot(String rank) {
        return switch (rank) { case "vip" -> 11; case "mvip" -> 13; case "elita" -> 15; default -> 22; };
    }

    private ItemStack configuredItem(String path, Material fallback) {
        return item(material(getConfig().getString(path + ".material", fallback.name()), fallback),
                getConfig().getString(path + ".name", "&fRanga"),
                getConfig().getStringList(path + ".lore"));
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            if (lore != null && !lore.isEmpty()) {
                List<String> colored = new ArrayList<>();
                for (String line : lore) colored.add(color(line));
                meta.setLore(colored);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private Material material(String name, Material fallback) {
        Material parsed = Material.matchMaterial(name == null ? "" : name);
        return parsed == null ? fallback : parsed;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
