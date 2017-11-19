package edu.neu.cs6650_clients;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ConnectionPool {

	// User to connect to your database instance. By default, this is "root2".
	private static final String user = "root";
	// Password for the user.
	private static final String password = "cs6650root";
	// URI to your database server. If running on the same machine, then this is "localhost".
	private static final String hostName = "cs6650.cxmb9zqrnn2g.us-west-1.rds.amazonaws.com";
	// Port to your database server. By default, this is 3307.
	private static final int port= 3306;
	// Name of the MySQL schema that contains your tables.
	private static final String schema = "Resort";

	
	/** Get the connection to the database instance. */
	public static Connection getConnection() throws SQLException {
		Connection connection = null;
		try {
			Properties connectionProperties = new Properties();
			connectionProperties.put("user", ConnectionPool.user);
			connectionProperties.put("password", ConnectionPool.password);
			// Ensure the JDBC driver is loaded by retrieving the runtime Class descriptor.
			// Otherwise, Tomcat may have issues loading libraries in the proper order.
			// One alternative is calling this in the HttpServlet init() override.
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new SQLException(e);
			}
			connection = DriverManager.getConnection(
			    "jdbc:mysql://" + ConnectionPool.hostName + ":" + ConnectionPool.port + "/" + ConnectionPool.schema,
			    connectionProperties);
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
		return connection;
	}
	
	public static void testDbConnection() {
		try {
			Connection connection = ConnectionPool.getConnection();
			System.out.println("succ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Close the connection to the database instance. */
	public void closeConnection(Connection connection) throws SQLException {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
