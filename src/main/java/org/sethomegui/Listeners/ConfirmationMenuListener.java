package org.sethomegui.Listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.Managers.ConfirmHolder;
import org.sethomegui.Utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfirmationMenuListener implements Listener {

    private final SetHomeGUI plugin;

    public ConfirmationMenuListener(SetHomeGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof ConfirmHolder)) return;

        ConfirmHolder confirmHolder = (ConfirmHolder) holder;
        UUID uuid = player.getUniqueId();
        String homeName = confirmHolder.getHomeName();

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        int clickedSlot = event.getSlot();

        YamlDocument guiConfig = plugin.getGuisConfig();
        Section confirmSection = guiConfig.getSection("gui.confirmation-gui");
        if (confirmSection == null) return;

        Section itemsSection = confirmSection.getSection("items");
        if (itemsSection == null) return;

        String clickedKey = null;

        for (Object keyObj : itemsSection.getKeys()) {
            String key = String.valueOf(keyObj);
            Section itemData = itemsSection.getSection(key);
            if (itemData == null) continue;

            if (itemData.contains("slots") && itemData.getIntList("slots").contains(clickedSlot)) {
                clickedKey = key;
                break;
            } else if (itemData.contains("slot") && itemData.getInt("slot") == clickedSlot) {
                clickedKey = key;
                break;
            }
        }

        if (clickedKey == null) return;

        plugin.getGuiManager().playConfiguredClickSound(player, "confirmation-gui");

        if (clickedKey.equalsIgnoreCase("confirm-button")) {
            player.closeInventory();
            plugin.getGuiManager().clearPendingDeletion(uuid);

            YamlDocument playerFile = plugin.getHomeManager().getPlayerFile(uuid);
            List<String> rawHomesList = playerFile != null ? playerFile.getStringList("homes") : new ArrayList<>();
            if (rawHomesList == null) rawHomesList = new ArrayList<>();

            if (rawHomesList.contains(homeName)) {
                rawHomesList.remove(homeName);
                playerFile.set("homes", rawHomesList);
                playerFile.remove(homeName);

                try {
                    playerFile.save();
                    String deleteMsg = plugin.getMainConfig().getString("messages.home-action-messages.home-deleted",
                            "&#ef6603[SetHomeGUI] &#f9a805Home &f%name% &#f9a805has been successfully deleted.");
                    player.sendMessage(Utils.setPlaceholders(player, deleteMsg.replace("%name%", homeName), plugin));
                } catch (IOException e) {
                    String deletionErrorMsg = plugin.getMainConfig().getString(
                            "messages.home-action-messages.file-deletion-error",
                            "&#ef6603[SetHomeGUI] &cAn error occurred while deleting the file."
                    );
                    player.sendMessage(Utils.color(deletionErrorMsg));
                    e.printStackTrace();
                }
            }
        } else if (clickedKey.equalsIgnoreCase("cancel-button")) {
            player.closeInventory();
            plugin.getGuiManager().clearPendingDeletion(uuid);
            plugin.getGuiManager().openHomesGUI(player);
        }
    }
}