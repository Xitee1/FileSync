package de.xite.filesync.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;

public class MySQL {
	static FileSync pl = FileSync.pl;
	public static String host = pl.getConfig().getString("mysql.host");
	public static int port = pl.getConfig().getInt("mysql.port");
	public static String username = pl.getConfig().getString("mysql.user");
	public static String password = pl.getConfig().getString("mysql.password");
	public static String database = pl.getConfig().getString("mysql.database");
	public static String prefix = pl.getConfig().getString("mysql.table-prefix");
	public static boolean useSSL = pl.getConfig().getBoolean("mysql.useSSL");
	
	public static Connection c;
	public static void connect() {
		try {
			if(!isConnected()) {
				c = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database+"?autoReconnect=true&useSSL="+useSSL, username, password);
				MySQL.update("CREATE TABLE IF NOT EXISTS `files` "
						+ "(`id` INT NOT NULL AUTO_INCREMENT, `group` VARCHAR(555) NOT NULL, `commands` VARCHAR(555) NOT NULL, `path` VARCHAR(555) NOT NULL, `data` LONGBLOB NOT NULL, PRIMARY KEY (`id`)) "
						+"ENGINE = InnoDB; DEFAULT CHARSET=utf8mb4;");
				FileSync.pl.getLogger().info("MySQL connected!");
			}
			
		} catch (SQLException e) {
			FileSync.pl.getLogger().severe("Could not connect to MySQL! Please check you settings.");
			e.printStackTrace();
			FileSync.pl.getLogger().severe("Disabling plugin...");
			Bukkit.getPluginManager().disablePlugin(FileSync.pl);
		}
	}
	public static void disconnect() {
		if(isConnected()) {
			try {
				c.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
		}
	}
	public static void update(String qry) {
		try {
			c.createStatement();
			PreparedStatement ps = c.prepareStatement(qry);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static ResultSet query(String qry) {
		try {
			c.createStatement();
			PreparedStatement ps = c.prepareStatement(qry);
			return ps.executeQuery();
		} catch (SQLException e) {
			FileSync.pl.getLogger().severe("There was an error whilst executing query: "+qry);
			FileSync.pl.getLogger().severe("Error:");
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isConnected() {
		if(c == null)
			return false;
		try {
			if(c.isClosed())
				return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	
    public static boolean writeFile(String filename) {
        try {
            // Check and prepare the file
            File file = new File(filename);
            // Check if File is bigger than 4GB (More is not supported)
            if(file.length() >= 4e+9) {
            	FileSync.pl.getLogger().severe("Could not upload File '"+file.getAbsolutePath()+"' (files bigger than 4GB are not supported)!");
            	return false;
            }
            FileInputStream input = new FileInputStream(file);
            
            // Insert file in MySQL
            PreparedStatement ps = c.prepareStatement("INSERT INTO files(`id`, `path`, `data`) VALUES (NULL, '"+filename+"', ?)");
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
    public static boolean readFile(String path) {
    	ResultSet rs = null;
		try {
	        // Check and prepare the file
	        File file = new File(path);
			FileOutputStream output = new FileOutputStream(file);
			
			// Download file form MySQL
			PreparedStatement ps = c.prepareStatement("SELECT `data` FROM `files` WHERE `path`='"+path+"'");
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
	public static boolean checkExists(String table, String value, String where) {
		try {
			ResultSet rs = MySQL.query("SELECT "+value+" FROM "+table+" WHERE "+where);
			if(rs.next()) {
				if(rs.getString(value.replace("`", "")) != null)
					return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
