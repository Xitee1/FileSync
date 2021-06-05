package de.xite.filesync.listener;

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
					p.sendMessage(FileSync.getMessage("update.msg").replace("%current_version%", Updater.version).replace("%newest_version%", pl.getDescription().getVersion()));
					if(pl.getConfig().getBoolean("update.autoupdater")) {
						p.sendMessage(FileSync.getMessage("update.updaterEnabled"));
					}else {
						p.sendMessage(FileSync.getMessage("update.updaterDisabled"));
					}
				}
				pl.getLogger().info("-> A new version (v."+Updater.version+") is available! Your version: "+pl.getDescription().getVersion());
				pl.getLogger().info("-> Update me! :)");
			}
		}

	}
}
