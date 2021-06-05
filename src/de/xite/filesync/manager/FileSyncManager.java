package de.xite.filesync.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import de.xite.filesync.main.FileSync;
import de.xite.filesync.main.MySQL;

public class FileSyncManager {
	File file;
	String path;
	String group;
	
	public FileSyncManager(String group, String path) {
		this.file = new File(path);
		this.path = path;
		this.group = group;
	}
	
	
	public boolean fileExists() {
		return MySQL.checkExists(MySQL.prefix+"files", "id", "`group`='"+group+"' AND `path`='"+path+"'");
	}
	public boolean writeFile() {
		if(file.isDirectory()) {
			for(File f : FileUtils.listFiles(file, null, true)) {
				FileSyncManager fsm = new FileSyncManager(group, path+"/"+f.getName());
				fsm.writeFile();
			}
			return true;
		}
		try {
			if(FileSync.debug)
				FileSync.pl.getLogger().info("Uploading file "+path+" to group "+group+"...");
			// Check if File is bigger than 4GB (More is not supported)
			if(file.length() >= 4e+9) {
				FileSync.pl.getLogger().severe("Could not upload File '"+file.getAbsolutePath()+"' (files bigger than 4GB are not supported)!");
            	return false;
			}
			// This should never be the case, but i just implemented it to make sure it never happens
			if(file.getAbsolutePath().length() > 5555) {
				FileSync.pl.getLogger().severe("Could not upload File '"+file.getAbsolutePath()+"' (The filepath and/or the folders have to long names)!");
				return false;
			}
			FileInputStream input = new FileInputStream(file);
			// Insert file in MySQL
			String sql = "INSERT INTO "+MySQL.prefix+"files(`id`, `group`, `commands`, `path`, `data`, `modified`) VALUES"
					+ " (NULL, '"+group+"', NULL, '"+path+"', ?, '"+file.lastModified()+"')";
			if(fileExists())
				sql = "UPDATE `"+MySQL.prefix+"files` SET `data`=?, `modified`='"+file.lastModified()+"' WHERE `group`='"+group+"' AND `path`='"+path+"';";
            
			PreparedStatement ps = MySQL.c.prepareStatement(sql);
			ps.setBinaryStream(1, input);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean readFile() {
		ResultSet rs = null;
		try {
			if(FileSync.debug)
				FileSync.pl.getLogger().info("Downloading file "+path+" from group "+group+"...");
			// Check and prepare the file
			FileOutputStream output = new FileOutputStream(file);
			file.mkdirs();
			// Download file form MySQL
			PreparedStatement ps = MySQL.c.prepareStatement("SELECT `commands`,`data` FROM `"+MySQL.prefix+"files` WHERE `group`='"+group+"' AND `path`='"+path+"'");
			rs = ps.executeQuery();
			if(file.exists())
				file.delete();
			if(rs.next()) {
				// Write file
				FileUtils.copyInputStreamToFile(rs.getBinaryStream("data"), file);
				
				// Execute commands
				String commands = rs.getString("commands");
				if(commands != null) {
					if(commands.contains("%new%")) {
						for(String s : commands.split("%new%"))
							Bukkit.getScheduler().runTask(FileSync.pl, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s));
					}else
						Bukkit.getScheduler().runTask(FileSync.pl, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commands));
				}
			}
			output.close();
			file.setLastModified(getLastModified());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	public void deleteFile() {
		MySQL.deleteEntry(MySQL.prefix+"files", "`group`='"+group+"' AND `path`='"+path+"'");
	}
	public Long getLastModified() {
		return MySQL.getLong(MySQL.prefix+"files", "modified", "`group`='"+group+"' AND `path`='"+path+"'");
	}
	public void setCommands(ArrayList<String> commands) {
		
	}
	public ArrayList<String> getCommands() {
		return null;
	}
	// ---- Static Methods ---- //
	public static void setAllowUpload(boolean b) {
		FileSync.allowUpload = b;
	}
	public static void setGroups(List<String> groups) {
		FileSync.groups.clear();
		FileSync.groups.addAll(groups);
	}
	public static void startSyncScheduler(int interval) {
		if(FileSync.scheduler != 0)
			Bukkit.getScheduler().cancelTask(FileSync.scheduler);
		FileSync.scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(FileSync.pl, new Runnable() {
			@Override
			public void run() {
				Bukkit.getScheduler().runTaskAsynchronously(FileSync.pl, new Runnable() {
					@Override
					public void run() {
						if(FileSync.debug)
							FileSync.pl.getLogger().info("Starting synchronize all files...");
						for(String group : FileSync.groups)
							syncFiles(group);
						if(FileSync.debug)
							FileSync.pl.getLogger().info("Finished! Starting again in "+interval+" seconds.");
					}
				});
			}
		}, interval*20, interval*20);
	}
	public static void syncFiles(String group) {
		if(FileSync.debug)
			FileSync.pl.getLogger().info("Synchronizing group "+group+"...");
		for(String s : MySQL.getStringList(MySQL.prefix+"files", "path", "`group`='"+group+"'"))
			syncFiles(group, s);
	}
	public static void syncFiles(String group, String path) {
		File file = new File(path);
		FileSyncManager fsm = new FileSyncManager(group, path);
		
		long local = 0;
		long cloud = fsm.getLastModified();
		if(file.exists())
			local = file.lastModified();
		
		if(cloud > local) {
			// cloud file is newer -> Download file
			fsm.readFile();
		}
		if(local > cloud && FileSync.allowUpload) {
			// local file is newer -> Upload file
			fsm.writeFile();
		}
		if(local == cloud) {
			if(FileSync.debug)
				FileSync.pl.getLogger().info("File "+path+" has no changes.");
		}
	}
}
