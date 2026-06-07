package org.sethomegui.Listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.Managers.HomesHolder;
import org.sethomegui.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomesMenuClickListener implements Listener {

    private final SetHomeGUI plugin;

    public HomesMenuClickListener(SetHomeGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof HomesHolder)) return;

        HomesHolder homesHolder = (HomesHolder) holder;
        UUID uuid = player.getUniqueId();

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        int clickedSlot = event.getSlot();

        YamlDocument guiConfig = plugin.getGuisConfig();
        Section homesSection = guiConfig.getSection("gui.homes-gui");
        if (homesSection == null) return;

        YamlDocument playerFile = plugin.getHomeManager().getPlayerFile(uuid);
        List<String> rawHomesList = playerFile != null ? playerFile.getStringList("homes") : new ArrayList<>();
        if (rawHomesList == null) rawHomesList = new ArrayList<>();

        List<Integer> homeSlots = homesSection.getIntList("home-slots");
        int homesPerPage = homeSlots.size();
        int maxPages = (int) Math.ceil((double) rawHomesList.size() / homesPerPage);
        if (maxPages == 0) maxPages = 1;

        int currentPage = homesHolder.getCurrentPage();

        Section itemsSection = homesSection.getSection("items");
        String action = null;
        if (itemsSection != null) {
            for (Object keyObj : itemsSection.getKeys()) {
                String key = String.valueOf(keyObj);
                Section itemData = itemsSection.getSection(key);
                if (itemData == null) continue;

                if (itemData.contains("slot") && itemData.getInt("slot") == clickedSlot) {
                    action = itemData.getString("action");
                    break;
                }
            }
        }

        if (action != null) {
            plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");

            switch (action.toLowerCase()) {
                case "back":
                    player.closeInventory();
                    return;

                case "previous_page":
                    if (currentPage > 1) {
                        plugin.getGuiManager().setPlayerPage(uuid, currentPage - 1);
                        plugin.getGuiManager().openHomesGUI(player);
                    }
                    return;

                case "next_page":
                    if (currentPage < maxPages) {
                        plugin.getGuiManager().setPlayerPage(uuid, currentPage + 1);
                        plugin.getGuiManager().openHomesGUI(player);
                    }
                    return;
            }
        }

        if (homeSlots.contains(clickedSlot)) {
            int slotIndexInPage = homeSlots.indexOf(clickedSlot);
            int globalHomeIndex = ((currentPage - 1) * homesPerPage) + slotIndexInPage;

            if (globalHomeIndex >= rawHomesList.size()) return;
            String homeName = rawHomesList.get(globalHomeIndex);

            plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");

            if (event.getClick() == ClickType.LEFT) {
                player.closeInventory();

                String worldName = playerFile.getString(homeName + ".world");
                org.bukkit.World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    String worldNotLoadedMsg = plugin.getMainConfig().getString(
                            "messages.home-action-messages.world-not-loaded",
                            "&#ef6603[SetHomeGUI] &cError: Destination world '%world%' is not loaded."
                    );
                    player.sendMessage(Utils.color(worldNotLoadedMsg.replace("%world%", worldName)));
                    return;
                }

                double x = playerFile.getDouble(homeName + ".x");
                double y = playerFile.getDouble(homeName + ".y");
                double z = playerFile.getDouble(homeName + ".z");
                float yaw = playerFile.getDouble(homeName + ".yaw").floatValue();
                float pitch = playerFile.getDouble(homeName + ".pitch").floatValue();

                Location targetLoc = new Location(world, x, y, z, yaw, pitch);
                plugin.getTeleportManager().queueTeleport(player, targetLoc);
            } else if (event.getClick() == ClickType.RIGHT) {
                plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");
                plugin.getGuiManager().openConfirmationGUI(player, homeName);
            }
        }
    }
}