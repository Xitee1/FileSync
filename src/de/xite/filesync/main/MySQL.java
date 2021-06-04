package de.xite.filesync.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
						+ "(`id` INT NOT NULL AUTO_INCREMENT, `group` VARCHAR(255) NOT NULL, `commands` VARCHAR(5555), `path` VARCHAR(5555) NOT NULL, `data` LONGBLOB NOT NULL, `modified` BIGINT, PRIMARY KEY (`id`)) "
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
	
	// --- API --- //
	// Checks
	public static boolean checkExists(String table, String value, String where) {
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"` WHERE "+where);
			if(rs.next())
				if(rs.getString(value) != null)
					return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public static boolean checkEnabled(String table, String value, String where) {
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"` WHERE "+where);
			if(rs.next())
				if(rs.getString(value).equals("true"))
					return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	// Get informations
	public static Integer getInt(String table, String value, String where) {
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"` WHERE "+where);
			if(rs.next())
				return rs.getInt(value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Boolean getBoolean(String table, String value, String where) {
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"` WHERE "+where);
			if(rs.next())
				return rs.getBoolean(value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public static Double getDouble(String table, String value, String where) {
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"` WHERE "+where);
			if(rs.next())
				return rs.getDouble(value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Float getFloat(String table, String value, String where) {
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"` WHERE "+where);
			if(rs.next())
				return rs.getFloat(value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String getString(String table, String value, String where) {
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"` WHERE "+where);
			if(rs.next())
				return rs.getString(value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static ArrayList<String> getStringList(String table, String value, String where){
		ArrayList<String> result = new ArrayList<String>();
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"` WHERE "+where+"");
			if(rs.first())
				while (rs.next())
					if(!result.contains(rs.getString(value)))
						result.add(rs.getString(value));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	public static ArrayList<String> getStringList(String table, String value){
		ArrayList<String> result = new ArrayList<String>();
		try {
			ResultSet rs = MySQL.query("SELECT `"+value+"` FROM `"+table+"`");
			if(rs.first())
				while (rs.next())
					if(!result.contains(rs.getString(value)))
						result.add(rs.getString(value));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void deleteEntry(String table, String where) {
		MySQL.update("DELETE FROM `"+table+"` WHERE "+where);
	}
}
