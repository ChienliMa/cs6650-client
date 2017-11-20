package edu.neu.cs6650_clients;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LogDao {
	public static List<Log> getLogWithinTime(Long start, Long end) {
		List<Log> logs = new ArrayList<Log>();
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
		    connection = (Connection) ConnectionPool.getConnection();
		    statement = connection.prepareStatement("select status, dbTime, totalTime from Logs");
		    rs = statement.executeQuery();
		    while (rs.next()) {
		    		logs.add(new Log(rs.getString(1), rs.getLong(2), rs.getLong(3)));
		    }
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		finally {
			try {
				if (rs != null) rs.close();
				if (statement != null) statement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return logs;
	}
}
