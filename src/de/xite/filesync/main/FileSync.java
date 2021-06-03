package de.xite.filesync.main;

import org.bukkit.plugin.java.JavaPlugin;

public class FileSync extends JavaPlugin{
	public static FileSync pl;
	
	@Override
	public void onEnable() {
		pl = this;
		// ---- Register Commands ---- //
	}
}
