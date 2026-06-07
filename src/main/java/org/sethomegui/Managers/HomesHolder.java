package org.sethomegui.Managers;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import java.util.UUID;

public class HomesHolder implements InventoryHolder {
    private final UUID playerUuid;
    private final int currentPage;

    public HomesHolder(UUID playerUuid, int currentPage) {
        this.playerUuid = playerUuid;
        this.currentPage = currentPage;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public int getCurrentPage() { return currentPage; }

    @Override
    public Inventory getInventory() { return null; }
}