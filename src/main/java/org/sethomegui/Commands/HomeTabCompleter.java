package org.sethomegui.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sethomegui.SetHomeGUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeTabCompleter implements TabCompleter {

    private final SetHomeGUI plugin;

    public HomeTabCompleter(SetHomeGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        boolean requiresPermission = plugin.getMainConfig().getBoolean("requires-default-permission", false);
        if (requiresPermission) {
            String permissionNode = plugin.getMainConfig().getString("default-permission", "sethome.use");
            if (!player.hasPermission(permissionNode)) {
                return Collections.emptyList();
            }
        }
        List<String> playerHomes = plugin.getHomeManager().getPlayerFile(player.getUniqueId()).getStringList("homes");
        if (playerHomes == null || playerHomes.isEmpty()) {
            return Collections.emptyList();
        }
        StringBuilder currentArgBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            currentArgBuilder.append(args[i]);
            if (i < args.length - 1) currentArgBuilder.append(" ");
        }
        String currentSearch = currentArgBuilder.toString();

        if (currentSearch.startsWith("\"")) {
            currentSearch = currentSearch.substring(1);
        }

        List<String> completions = new ArrayList<>();

        for (String home : playerHomes) {
            if (StringUtil.startsWithIgnoreCase(home, currentSearch)) {

                if (home.contains(" ")) {
                    completions.add("\"" + home + "\"");
                } else {
                    completions.add(home);
                }
            }
        }
        Collections.sort(completions);
        return completions;
    }
}