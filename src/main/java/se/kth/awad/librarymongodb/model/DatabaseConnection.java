package se.kth.awad.librarymongodb.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Hanterar databasanslutningar för applikationen.
 * Singelton desgin eftersom säkerställer att en endast databasanslutning finns
 * i hela programmet.
 */
public class DatabaseConnection {

    // endast en intans tillåten
    private static DatabaseConnection instance = null;
    private Connection connection;
    private String currentDatabase;

    // Database configuration MySQL-installation
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String USERNAME = "library_client";
    private static final String PASSWORD = "LibClient2025!";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    /**
     * Privat konstruktor för att förhindra direkt instansiering
     * (Singleton-mönster).
     */
    private DatabaseConnection() {
        this.connection = null;
        this.currentDatabase = null;
    }

    /**
     * Trådsäker implementation.
     * 
     * @return DatabaseConnection-instansen
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * @param database (LibraryDB)
     * @return true = anslutat
     * @throws BooksDbException om anslutningen misslyckas
     */
    public boolean connect(String database) throws BooksDbException {
        try {
            // Stäng befintlig anslutning om det finns någon
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            try {
                Class.forName(DRIVER_CLASS);
            } catch (ClassNotFoundException e) {
                // Moderna Java-versioner behöver inte explicit drivrutinsladdning
            }

            String fullUrl = SERVER_URL + database + "?useUnicode=true&characterEncoding=UTF-8";
            connection = DriverManager.getConnection(fullUrl, USERNAME, PASSWORD);
            currentDatabase = database;
            return true;

        } catch (SQLException e) {
            // Fångar alla SQLException (både från close() och getConnection())
            String errorMsg = String.format(
                    "Failed to connect to database '%s' at %s%n" +
                            "Error: %s%n" +
                            "SQL State: %s%n" +
                            "Error Code: %d%n" +
                            "%nTroubleshooting:%n" +
                            "1. Verify the database name is correct (case-sensitive on some systems)%n" +
                            "2. Check if user '%s' has GRANT privileges on database '%s'%n" +
                            "3. Verify MySQL server is running%n" +
                            "4. Check user credentials",
                    database, SERVER_URL, e.getMessage(),
                    e.getSQLState(), e.getErrorCode(), USERNAME, database);
            throw new BooksDbException(errorMsg, e);
        }
    }

    /**
     * Kopplar bort från nuvarande databas.
     * 
     * @return true om nedkoppling lyckades, false om inte ansluten
     * @throws BooksDbException om nedkoppling misslyckas
     */
    public boolean disconnect() throws BooksDbException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                currentDatabase = null;
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new BooksDbException("Error disconnecting from database", e);
        }
    }

    /**
     * Hämtar den aktiva databasanslutningen.
     * 
     * @return det aktiva Connection-objektet för SQL-operationer
     * @throws BooksDbException om inte ansluten till någon databas
     */
    public Connection getConnection() throws BooksDbException {
        try {
            if (connection == null || connection.isClosed()) {
                throw new BooksDbException("Not connected to any database. Call connect() first.");
            }
            return connection;

        } catch (SQLException e) {
            throw new BooksDbException("Error checking connection status", e);
        }
    }

    /**
     * Kontrollerar om för närvarande ansluten till en databas.
     * 
     * @return true om ansluten, false annars
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Hämtar namnet på nuvarande databas.
     * 
     * @return databasnamn, eller null om inte ansluten
     */
    public String getCurrentDatabase() {
        return currentDatabase;
    }
}
