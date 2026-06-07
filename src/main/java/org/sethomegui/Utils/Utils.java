package org.sethomegui.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.sethomegui.SetHomeGUI;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Gson gson = new Gson();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})|#([A-Fa-f0-9]{6})");

    public static String color(String message) {
        if (message == null || message.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);

            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String formatSoundName(String input) {
        if (input == null || input.isEmpty()) return "minecraft:entity.player.levelup";
        String sound = input.toLowerCase().replace('_', '.');
        if (!sound.contains(":")) {
            sound = "minecraft:" + sound;
        }
        return sound;
    }

    public static String setPlaceholders(Player player, String text, SetHomeGUI plugin) {
        if (text == null) return "";

        text = text.replace("%player%", player.getName());

        if (player != null) {
            if (text.contains("%sethome_current%")) {
                int current = plugin.getHomeManager().getHomeCount(player.getUniqueId());
                text = text.replace("%sethome_current%", String.valueOf(current));
            }
            if (text.contains("%sethome_max%")) {
                int max = plugin.getHomeManager().getPlayerMaxHomes(player);
                String maxStr = (max == -1) ? "∞" : String.valueOf(max);
                text = text.replace("%sethome_max%", maxStr);
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return color(text);
    }

    public static void sendUsage(Player player, String correctUsage, SetHomeGUI plugin) {
        if (player == null) return;

        String baseMessage = plugin.getConfig().getString("messages.invalid-usage", "&#ef6603[SetHomeGUI] &#f9a805Usage: &f%usage%");

        String formattedMessage = baseMessage.replace("%usage%", correctUsage);

        player.sendMessage(color(formattedMessage));
    }

    public static ItemStack getHeadFromBase64(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta == null) return head;

        if (supportsPlayerProfile()) {
            try {
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                String decoded = new String(Base64.getDecoder().decode(base64));
                JsonObject obj = gson.fromJson(decoded, JsonObject.class);
                JsonElement urlElement = obj.getAsJsonObject("textures")
                        .getAsJsonObject("SKIN")
                        .get("url");

                if (urlElement != null) {
                    PlayerTextures textures = profile.getTextures();
                    textures.setSkin(new URL(urlElement.getAsString()));
                    profile.setTextures(textures);
                    meta.setOwnerProfile(profile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", base64));
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        head.setItemMeta(meta);
        return head;
    }

    public static ItemStack getPlayerHead(java.util.UUID uuid) {
        org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(uuid);

        if (op.getName() == null || !op.hasPlayedBefore()) {
            String misteryUserTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWRiMGU1YzZmOTllNjZiNzRmYTg0Mjc3ODk0YjdlNGI4ZjA5YjI0NTJmYjdlNDg2ZDZlZTQyYjA0NWMyNjEzNyJ9fX0=";
            return getHeadFromBase64(misteryUserTexture);
        }
        org.bukkit.inventory.ItemStack head = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(op);
            head.setItemMeta(meta);
        }
        return head;
    }

    private static boolean supportsPlayerProfile() {
        try {
            Class.forName("org.bukkit.profile.PlayerProfile");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}