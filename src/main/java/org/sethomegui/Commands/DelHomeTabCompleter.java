package org.sethomegui.Commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.sethomegui.SetHomeGUI;

import java.util.ArrayList;
import java.util.List;

public class DelHomeTabCompleter implements TabCompleter {

    private final SetHomeGUI plugin;

    public DelHomeTabCompleter(SetHomeGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player)) return completions;

        Player player = (Player) sender;
        YamlDocument playerFile = plugin.getHomeManager().getPlayerFile(player.getUniqueId());
        if (playerFile == null) return completions;

        List<String> playerHomes = playerFile.getStringList("homes");
        if (playerHomes == null || playerHomes.isEmpty()) return completions;
        StringBuilder currentInputBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            currentInputBuilder.append(args[i]);
            if (i < args.length - 1) currentInputBuilder.append(" ");
        }
        String currentInput = currentInputBuilder.toString().toLowerCase();
        if (currentInput.startsWith("\"")) {
            currentInput = currentInput.substring(1);
        }
        if (currentInput.endsWith("\"")) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        }

        for (String home : playerHomes) {
            if (home.toLowerCase().startsWith(currentInput)) {
                if (home.contains(" ")) {
                    completions.add("\"" + home + "\"");
                } else {
                    completions.add(home);
                }
            }
        }

        return completions;
    }
}