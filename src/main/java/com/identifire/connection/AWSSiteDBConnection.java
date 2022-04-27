package com.identifire.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.identifire.common.log.HLogger;

/**
 * DefaultIDBConnection DefaultIDBConnection pf db connection
 * 
 * @author amirs
 */

public class AWSSiteDBConnection implements IDBConnection {

	@SuppressWarnings("unused")
	private HLogger logger = new HLogger(AWSSiteDBConnection.class.getName());

	public Connection getRemoteConnection() {
		Connection con = null;
		try {
			String hostname 	= "aahagi3z4ntd6l.ctke5fgnjhhy.us-east-2.rds.amazonaws.com";
			String port 		= "3306";
			String dbName 		= "identifire";
			String userName 	= "amirdor";
			String password 	= "aYnIl1973";

			String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password="
					+ password;
			logger.debug("jdbcUrl");
			con = DriverManager.getConnection(jdbcUrl);
			logger.debug("Connection successfull");
			return con;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}
}
