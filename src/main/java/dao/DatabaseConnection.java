package dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.PropertyReader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by ashan on 2017-05-03.
 */
public class DatabaseConnection implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final String DB_PW = PropertyReader.getProperty("server.database.password");
    private static final String DB_USER = PropertyReader.getProperty("server.database.user");

    private static Connection connection = null;
    private static String DB_URL;

    private Statement statement = null;
    protected ResultSet resultSet = null;

    static {
        try {
            String dbIP = InetAddress.getByName(
                    PropertyReader.getProperty("server.database.hostname")
            ).getHostAddress();
            DB_URL = String.format("jdbc:mysql://%s:3306/mini-exchange", dbIP);
        } catch (UnknownHostException e) {
            LOGGER.error("unable to resolve database server ip {}", e.getMessage());
        }
    }

    public static Connection getDatabaseConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            LOGGER.debug("requesting new database connection");
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PW);
        }
        return connection;
    }

    public boolean executeQuery(String query) throws Exception {
        LOGGER.debug("Execute new database record {}", query);
        getDatabaseConnection();
        statement = connection.createStatement();
        return statement.execute(query);

    }

    public ResultSet retrieveData(String query) throws Exception {
        LOGGER.debug("Reading database record");
        getDatabaseConnection();
        statement = connection.createStatement();
        return statement.executeQuery(query);

    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            LOGGER.debug("database connection closed successfully");
        }
    }
}
