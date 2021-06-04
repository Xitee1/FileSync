package de.xite.filesync.main;

import java.util.ArrayList;

import org.bukkit.plugin.java.JavaPlugin;

import de.xite.fiilesync.manager.FileSyncCommand;
import de.xite.fiilesync.manager.FileSyncManager;
import de.xite.filesync.utils.BStatsMetrics;
import net.md_5.bungee.api.ChatColor;

public class FileSync extends JavaPlugin{
	public static FileSync pl;
	public static String pr;
	
	public static int scheduler = 0;
	public static ArrayList<String> groups;
	
	@Override
	public void onEnable() {
		pl = this;
		
		pl.getConfig().options().copyDefaults(false);
		pl.saveDefaultConfig();
		pl.reloadConfig();
		
		pr = ChatColor.translateAlternateColorCodes('&', pl.getConfig().getString("messages.prefix"));
		// ---- Register Commands ---- //
		for(String s : pl.getConfig().getStringList("commands"))
			getCommand(s).setExecutor(new FileSyncCommand());
		
		MySQL.connect();
		
		// Send BStats analytics
		int pluginId = 11567; // <-- Replace with the id of your plugin!
		BStatsMetrics metrics = new BStatsMetrics(this, pluginId);
		//Costom charts
		metrics.addCustomChart(new BStatsMetrics.SimplePie("update_auto_update", () -> pl.getConfig().getBoolean("update.autoupdater") ? "Aktiviert" : "Deaktiviert"));
		metrics.addCustomChart(new BStatsMetrics.SimplePie("update_notifications", () -> pl.getConfig().getBoolean("update.notification") ? "Aktiviert" : "Deaktiviert"));
		
		if(!pl.getConfig().getBoolean("api")) {
			FileSyncManager.setGroups(FileSync.pl.getConfig().getStringList("sync.groups"));
			FileSyncManager.startSyncScheduler(pl.getConfig().getInt("sync.interval"));
		}
	}
	
	public static String getMessage(String config) {
		return pr + ChatColor.translateAlternateColorCodes('&', pl.getConfig().getString("messages."+config));
	}
	
	@Override
	public void onDisable() {
		MySQL.disconnect();
	}
}
