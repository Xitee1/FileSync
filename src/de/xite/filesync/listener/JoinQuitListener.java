package de.xite.filesync.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.xite.filesync.main.FileSync;
import de.xite.filesync.utils.Updater;


public class JoinQuitListener implements Listener {
	FileSync pl = FileSync.pl;
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(p.hasPermission("scoreboard.update") || p.isOp()) {
			if(Updater.checkVersion()) {
				if(pl.getConfig().getBoolean("update.notification")) {
					p.sendMessage(FileSync.pr+ChatColor.RED+"A new version is available (§bv"+Updater.version+ChatColor.RED+")! Your version: §bv"+pl.getDescription().getVersion());
					if(pl.getConfig().getBoolean("update.autoupdater")) {
						p.sendMessage(FileSync.pr+ChatColor.GREEN+"The plugin will be updated automatically after a server restart.");
					}else {
						p.sendMessage(FileSync.pr+ChatColor.RED+"The auto-updater is disabled in your config.yml. Type §6/fs update §cto update to the newest version.");
					}
				}
				pl.getLogger().info("-> A new version (v."+Updater.getVersion()+") is available! Your version: "+pl.getDescription().getVersion());
				pl.getLogger().info("-> Update me! :)");
			}
		}

	}
}
