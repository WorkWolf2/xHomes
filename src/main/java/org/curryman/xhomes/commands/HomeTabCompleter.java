package org.curryman.xhomes.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HomeTabCompleter implements TabCompleter {

    private Essentials essentials;

    public HomeTabCompleter(Essentials essentials) {
        this.essentials = essentials;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = essentials.getUser(player);

            if (command.getName().equalsIgnoreCase("home") && args.length == 1) {

                List<String> homeNames = new ArrayList<>(user.getHomes());


                List<String> completions = new ArrayList<>();
                for (String home : homeNames) {
                    if (home.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(home);
                    }
                }
                return completions;
            } else if (command.getName().equalsIgnoreCase("xhomes") && args.length == 1) {
                List<String> commands = new ArrayList<>();

                commands.add("reload");

                return commands;
            }

        }
        return null;
    }
}