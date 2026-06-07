package org.sethomegui.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.sethomegui.SetHomeGUI;

public class GlobalPlaceholders extends PlaceholderExpansion {

    private final SetHomeGUI plugin;

    public GlobalPlaceholders(SetHomeGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() { return "Beeted_"; }

    @Override
    @NotNull
    public String getIdentifier() { return "sethomegui"; }

    @Override
    @NotNull
    public String getVersion() { return "3.0.1"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "0";
        }

        if (params.equalsIgnoreCase("homes")) {
            int currentHomes = plugin.getHomeManager().getHomeCount(player.getUniqueId());
            return String.valueOf(currentHomes);
        }

        if (params.equalsIgnoreCase("maxhomes")) {
            if (player.isOnline()) {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer != null) {
                    int maxHomes = plugin.getHomeManager().getPlayerMaxHomes(onlinePlayer);
                    return maxHomes == -1 ? "∞" : String.valueOf(maxHomes);
                }
            }
            return "0";
        }

        return null;
    }
}