package org.sethomegui.Managers;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.sethomegui.SetHomeGUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeManager {

    private final SetHomeGUI plugin;
    private final File dataFolder;

    public HomeManager(SetHomeGUI plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    public YamlDocument getPlayerFile(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        try {
            return YamlDocument.create(playerFile, GeneralSettings.DEFAULT);
        } catch (IOException e) {
            String loadErrorMsg = plugin.getMainConfig().getString(
                    "messages.system-errors.load-error",
                    "Could not load or create data file for user: %uuid%"
            );
            plugin.getLogger().severe(loadErrorMsg.replace("%uuid%", uuid.toString()));
            e.printStackTrace();
            return null;
        }
    }

    public void saveHome(UUID uuid, String homeName, Location loc) {
        YamlDocument config = getPlayerFile(uuid);
        if (config == null) return;

        List<String> homeList = config.getStringList("homes");
        if (homeList == null) {
            homeList = new ArrayList<>();
        }
        if (!homeList.contains(homeName)) {
            homeList.add(homeName);
        }
        config.set("homes", homeList);

        config.set(homeName + ".world", loc.getWorld().getName());
        config.set(homeName + ".x", loc.getX());
        config.set(homeName + ".y", loc.getY());
        config.set(homeName + ".z", loc.getZ());
        config.set(homeName + ".yaw", (double) loc.getYaw());
        config.set(homeName + ".pitch", (double) loc.getPitch());

        try {
            config.save();
        } catch (IOException e) {
            String saveErrorMsg = plugin.getMainConfig().getString(
                    "messages.system-errors.save-error",
                    "Could not save home data for UUID %uuid%"
            );
            plugin.getLogger().severe(saveErrorMsg.replace("%uuid%", uuid.toString()));
            e.printStackTrace();
        }
    }

    public int getPlayerMaxHomes(org.bukkit.entity.Player player) {
        int maxFromConfig = plugin.getMainConfig().getInt("default-max-homes", 3);
        int maxFromPermission = -2;
        for (org.bukkit.permissions.PermissionAttachmentInfo attachment : player.getEffectivePermissions()) {
            String permission = attachment.getPermission().toLowerCase();

            if (permission.startsWith("sethome.maxhomes.")) {
                try {
                    String numberStr = permission.substring("sethome.maxhomes.".length());
                    int val = Integer.parseInt(numberStr);

                    if (val > maxFromPermission) {
                        maxFromPermission = val;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (maxFromPermission != -2) {
            return maxFromPermission;
        }
        return maxFromConfig;
    }
    public int getHomeCount(UUID uuid) {
        YamlDocument config = getPlayerFile(uuid);
        if (config == null) return 0;
        List<String> homeList = config.getStringList("homes");
        return homeList != null ? homeList.size() : 0;
    }
}