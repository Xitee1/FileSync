package de.xite.filesync.manager;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.xite.filesync.main.FileSync;
import de.xite.filesync.main.MySQL;
import de.xite.filesync.utils.Updater;
import net.md_5.bungee.api.ChatColor;

public class FileSyncCommand implements CommandExecutor {
	static FileSync pl = FileSync.pl;
	String pr = FileSync.pr;
	String designLine1 = FileSync.pr+"�7X�e�m-----�6FileSync�e�m-----�7X";
	String designLine2 = FileSync.pr+"�7X�e�m-----�6FileSync�e�m-----�7X";
	
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
			s.sendMessage(pr+ChatColor.YELLOW+"Your version: �3v"+pl.getDescription().getVersion());
			s.sendMessage(pr+ChatColor.YELLOW+"Newest version: �3v"+Updater.getVersion());
			s.sendMessage(pr+ChatColor.YELLOW+"Author: �3Xitee");
			s.sendMessage(designLine2);
		}else if(args.length == 1 && args[0].equalsIgnoreCase("menu")) {
			// Open the Menu
		}else if(args.length == 2 && args[0].equalsIgnoreCase("list")) {
			// List all files from group
			// Syntax: /fs list <group>
			String group = args[1].toLowerCase();
			if(!MySQL.checkExists(MySQL.prefix+"files", "group", "`group`='"+group+"'")) {
				s.sendMessage(FileSync.getMessage("listFiles.notExist"));
				return true;
			}
			s.sendMessage(FileSync.getMessage("listFiles.msg").replace("%group%", group));
			for(String file : MySQL.getStringList(MySQL.prefix+"files", "path", "`group`='"+group+"'"))
				s.sendMessage(FileSync.getMessage("listFiles.list").replace("%file%", file));
		}else if(args.length == 1 && args[0].equalsIgnoreCase("groups")) {
			// List all groups
			s.sendMessage(FileSync.getMessage("listGroups.msg"));
			for(String group : MySQL.getStringList(MySQL.prefix+"files", "group"))
				s.sendMessage(FileSync.getMessage("listGroups.list").replace("%group%", group));
		}else if(args.length >= 3 && args[0].equalsIgnoreCase("add")) {
			// Add new file
			// Syntax: /fs add <group> <[path]<file>> <commands>
			String group = args[1].toLowerCase();
			String file = args[2];
			File f = new File(file);
			if(!f.exists()) {
				s.sendMessage(FileSync.getMessage("addFile.doesNotExists").replace("%file%", file).replace("%group%", group));
				return true;
			}
			
			FileSyncManager fsm = new FileSyncManager(group, file);
			if(fsm.fileExists()) {
				s.sendMessage(FileSync.getMessage("addFile.alreadyExist").replace("%file%", file).replace("%group%", group));
				return true;
			}
			fsm.writeFile();
			s.sendMessage(FileSync.getMessage("addFile.successful").replace("%file%", file).replace("%group%", group));
			return true;
		}else if(args.length == 3 && args[0].equalsIgnoreCase("remove")) {
			// Delete files (Only from sync - no files on the servers will be deleted)
			// Syntax: /fs remove <group> <file>
			String group = args[1].toLowerCase();
			String file = args[2];

			FileSyncManager fsm = new FileSyncManager(group, file);
			if(!fsm.fileExists()) {
				s.sendMessage(FileSync.getMessage("removeFile.notExist").replace("%file%", file).replace("%group%", group));
				return true;
			}
			fsm.deleteFile();
			s.sendMessage(FileSync.getMessage("removeFile.successful").replace("%file%", file).replace("%group%", group));
			return true;
		}
		return true;
	}
}
