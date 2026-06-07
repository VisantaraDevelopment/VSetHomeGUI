package org.sethomegui.Managers;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import java.util.UUID;

public class ConfirmHolder implements InventoryHolder {
    private final UUID playerUuid;
    private final String homeName;

    public ConfirmHolder(UUID playerUuid, String homeName) {
        this.playerUuid = playerUuid;
        this.homeName = homeName;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public String getHomeName() { return homeName; }

    @Override
    public Inventory getInventory() { return null; }
}