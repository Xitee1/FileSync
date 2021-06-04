package de.xite.fiilesync.manager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.xite.filesync.main.FileSync;
import de.xite.filesync.utils.Updater;
import net.md_5.bungee.api.ChatColor;

public class FileSyncCommand implements CommandExecutor {
	static FileSync pl = FileSync.pl;
	String pr = FileSync.pr;
	String designLine1 = FileSync.pr+"§7X§e§m-----§6FileSync§e§m-----§7X";
	String designLine2 = FileSync.pr+"§7X§e§m-----§6FileSync§e§m-----§7X";
	
	@Override
	public boolean onCommand(CommandSender s, Command arg1, String arg2, String[] args) {
		if(s instanceof Player) {
			Player p = (Player) s;
			if(pl.getConfig().getString("permission").equalsIgnoreCase("none")) {
				p.sendMessage(FileSync.getMessage("onlyConsole"));
				return true;
			}
			if(!p.hasPermission(pl.getConfig().getString("permission"))) {
				p.sendMessage(FileSync.getMessage("noPerm"));
				return true;
			}
		}
		
		if(args.length == 1 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("about"))) {
			// Plugin informations
			s.sendMessage(designLine1);
			s.sendMessage(pr+ChatColor.YELLOW+"Your version: §3v"+pl.getDescription().getVersion());
			s.sendMessage(pr+ChatColor.YELLOW+"Newest version: §3v"+Updater.getVersion());
			s.sendMessage(pr+ChatColor.YELLOW+"Author: §3Xitee");
			s.sendMessage(designLine2);
		}else if(args.length == 1 && args[0].equalsIgnoreCase("menu")) {
			// Open the Menu
		}else if(args.length == 2 && args[0].equalsIgnoreCase("list")) {
			// List all files from group
			// Syntax: /fs list <group>
			String group = args[1];
		}else if(args.length == 1 && args[0].equalsIgnoreCase("groups")) {
			// List all groups
		}else if(args.length >= 3 && args[0].equalsIgnoreCase("add")) {
			String group = args[1];
			String file = args[2];
			// Add new file
			// Syntax: /fs add <group> <[path]<file>> <commands>
		}else if(args.length == 3 && args[0].equalsIgnoreCase("remove")) {
			String group = args[1];
			String file = args[2];
			// Delete files (Only from sync - no files on the servers will be deleted)
			// Syntax: /fs remove <group> <[path]<file>>
		}

		return true;
	}
}
