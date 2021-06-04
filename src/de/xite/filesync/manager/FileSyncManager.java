package de.xite.filesync.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
		return MySQL.checkExists("files", "id", "`group`='"+group+"' AND `path`='"+path+"'");
	}
    public boolean writeFile() {
        try {
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
            String sql = "INSERT INTO files(`id`, `group`, `commands`, `path`, `data`, `modified`) VALUES"
            		+ " (NULL, '"+group+"', NULL, '"+path+"', ?, '"+file.lastModified()+"')";
            if(fileExists())
            	sql = "UPDATE `files` SET `data`=?, `modified`='"+file.lastModified()+"' WHERE `group`='"+group+"' AND `path`='"+path+"';";
            
            PreparedStatement ps = MySQL.c.prepareStatement(sql);
			ps.setBinaryStream(1, input);
	        System.out.println("Uploading file "+file.getAbsolutePath()+" to MySQL...");
	        
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
	        // Check and prepare the file
			FileOutputStream output = new FileOutputStream(file);
			
			// Download file form MySQL
			PreparedStatement ps = MySQL.c.prepareStatement("SELECT `data` FROM `files` WHERE `group`='"+group+"' AND `path`='"+path+"'");
	        rs = ps.executeQuery();
	        System.out.println("Downloading file "+file.getAbsolutePath()+" from MySQL...");
	        while (rs.next()) {
	        	InputStream input = rs.getBinaryStream("data");
	        	byte[] buffer = new byte[1024];
	        	while (input.read(buffer) > 0)
	        		output.write(buffer);
	        }
	        output.close();
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
    	MySQL.deleteEntry("files", "`group`='"+group+"' AND `path`='"+path+"'");
    }
    public int getLastModified() {
    	return MySQL.getInt("files", "modified", "`group`='"+group+"' AND `path`='"+path+"'");
    }
    public boolean setCommands(ArrayList<String> commands) {
    	
    	return true;
    }
    public ArrayList<String> getCommands() {
		return null;
    }
    // ---- Static Methods ---- //
    public static void checkForUpdates(String group) {
    	ArrayList<String> files = MySQL.getStringList("files", "path", "`group`='"+group+"'");
    	for(String s : files)
    		checkForUpdates(group, s);
    }
    public static void checkForUpdates(String group, String path) {
		File file = new File(path);
		FileSyncManager fsm = new FileSyncManager(group, path);
		long local = file.lastModified();
		int cloud = fsm.getLastModified();
		if(cloud > local) {
			// cloud file is newer -> Download file
			fsm.readFile();
		}
		if(local > cloud) {
			// local file is newer -> Upload file
			fsm.writeFile();
		}
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
				for(String group : FileSync.groups)
					syncFiles(group);
			}
		}, interval, interval);
	}
	public static void syncFiles(String group) {
		FileSyncManager.checkForUpdates(group);
	}
	public static void syncFiles(String group, String path) {
		FileSyncManager.checkForUpdates(group, path);
	}
}
