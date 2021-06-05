package de.xite.filesync.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import de.xite.filesync.main.FileSync;

public class Updater {
	private static FileSync pl = FileSync.pl;
	private static int pluginID = 00000;
	public static String version = null;
    public static String getVersion() {
        /*try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + pluginID).openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
            	String d = scanner.next();
            	version = d;
                return d;
            }
        } catch (IOException e) {
            pl.getLogger().info("Updater -> Cannot look for updates: " + e.getMessage());
        }
        return "Could not check for updates! You probably restarted your server to often, so SpigotMC blocked your IP.";*/
    	// I can't get my plugin ID before the plugin exists on SpigotMCs
    	return "Beta 0.2";
    }
    
    public static boolean checkVersion() {
    	if(version == null) {
    		version = getVersion();
    		// Set it to null again after an hour to check again
    		Bukkit.getScheduler().runTaskLater(FileSync.pl, new Runnable() {
				@Override
				public void run() {
					version = null;
				}
			}, 20*60*60);
    	}
    	
    	if(version.equals(pl.getDescription().getVersion()))
    		return false;
    	return true;
    }
    public static boolean downloadFile() {
    	try {
			Bukkit.getScheduler().runTask(pl, () -> pl.getLogger().info("Updater -> Downloading newest version..."));
			File file = new File("plugins/"+pl.getDescription().getName()+".jar");
			String url = "https://xitecraft.de/downloads/"+pl.getDescription().getName()+".jar";
			
	    	HttpURLConnection connection = (HttpURLConnection)(new URL(url)).openConnection();
		    connection.connect();
		    
		    InputStream is = connection.getInputStream();
	    	file.delete();
	    	FileUtils.copyInputStreamToFile(is, file);
		    is.close();
		    
		    connection.disconnect();
		    Bukkit.getScheduler().runTask(pl, () -> pl.getLogger().info("Updater -> Download finished! To apply the new update, you have to restart your server."));
		    return true;
		}catch(Exception e) {
	    	Bukkit.getScheduler().runTask(pl, new Runnable() {
				@Override
				public void run() {
					pl.getLogger().info("Updater -> Download failed! Please try it later again.");
					if(FileSync.debug)
						e.printStackTrace();
				}
			});
	    	return false;
		}
	}
}
