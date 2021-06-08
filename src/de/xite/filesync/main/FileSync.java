package de.xite.filesync.main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.xite.filesync.commands.FileSyncCommand;
import de.xite.filesync.listener.JoinQuitListener;
import de.xite.filesync.manager.FileSyncManager;
import de.xite.filesync.utils.BStatsMetrics;
import de.xite.filesync.utils.MySQL;
import net.md_5.bungee.api.ChatColor;

public class FileSync extends JavaPlugin{
	public static FileSync pl;
	public static String pr;
	public static boolean debug = false;
	public static boolean allowUpload = false;
	
	public static int scheduler = 0;
	public static ArrayList<String> groups = new ArrayList<>();
	
	@Override
	public void onEnable() {
		pl = this;
		
		pl.getConfig().options().copyDefaults(false);
		pl.saveDefaultConfig();
		pl.reloadConfig();
		if(pl.getConfig().getBoolean("debug"))
			debug = true;
		
		pr = ChatColor.translateAlternateColorCodes('&', pl.getConfig().getString("messages.prefix"));
		// ---- Register Commands ---- //
		for(String s : pl.getConfig().getStringList("commands"))
			getCommand(s).setExecutor(new FileSyncCommand());
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new JoinQuitListener(), this);
		
		if(!MySQL.connect()) {
			FileSync.pl.getLogger().severe("Disabling plugin because of MySQL errors...");
			Bukkit.getPluginManager().disablePlugin(FileSync.pl);
			return;
		}
		if(getBukkitVersion() > 17) {
			if(debug)
	        	pl.getLogger().info("Sending analytics to BStats");
			// Send BStats analytics
			int pluginId = 11567; // <-- Replace with the id of your plugin!
			BStatsMetrics metrics = new BStatsMetrics(this, pluginId);
			//Costom charts
			metrics.addCustomChart(new BStatsMetrics.SimplePie("update_auto_update", () -> pl.getConfig().getBoolean("update.autoupdater") ? "Aktiviert" : "Deaktiviert"));
			metrics.addCustomChart(new BStatsMetrics.SimplePie("update_notifications", () -> pl.getConfig().getBoolean("update.notification") ? "Aktiviert" : "Deaktiviert"));
			
			if(!pl.getConfig().getBoolean("api")) {
				FileSyncManager.setAllowUpload(pl.getConfig().getBoolean("sync.allowUpload"));
				FileSyncManager.setGroups(pl.getConfig().getStringList("sync.groups"));
				FileSyncManager.startSyncScheduler(pl.getConfig().getInt("sync.interval"));
			}
	        
		}
	}
	
	public static String getMessage(String config) {
		String msg = pl.getConfig().getString("messages."+config);
		if(msg == null) {
			pl.getLogger().severe("You have an error in your config.yml! Please check for spacing errors and if it's up to date.");
			pl.getLogger().severe("Error: Could not find String message."+config);
			return null;
		}
		return pr + ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTask(scheduler);
		MySQL.disconnect();
	}
	
	public static Integer getBukkitVersion() {
		String s = Bukkit.getBukkitVersion();
		String v = "";
		boolean pointCounter = true;
		while(s.length() > 1) {
			if(v.endsWith(".") || v.endsWith("-")) {//Allow only one point. example: from '1.8.8-R0.1-SNAPSHOT' extract to '1.8'. The pointcounter is needed for version 1.10+ because of more decimales.
				if(pointCounter) {
					pointCounter = false;
				}else {
					s = "";
					try {
						return Integer.parseInt(v.substring(0, v.length()-1).replace(".", ""));//decimals are removed. example: 1.8 ->  18 | example 2: 1.12 -> 112
					}catch (Exception e) {
						pl.getLogger().severe("There was a problem whilst checking your minecraft server version! Have you something like a special version? Detected version: "+v);
						pl.getLogger().severe("If you don't know why you get this error and you are using one of the official supported server softwares, please report this bug in our discord!");
						Bukkit.getPluginManager().disablePlugin(pl);
						return 0;
					}
				}
			}
			v += s.substring(0, 1);//add the first char from s to v
			s = s.substring(1);//remove the first char from s
		}
		return 0;
	}
}
