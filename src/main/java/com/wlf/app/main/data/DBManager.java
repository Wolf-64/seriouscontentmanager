package com.wlf.app.main.data;

import com.wlf.app.main.io.FileHandler;
import com.wlf.app.preferences.Config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBManager {
    private static final Logger log = Logger.getLogger(DBManager.class.getSimpleName());

    private static DBManager _instance;

    private static final String CONNECTION_STRING = "jdbc:sqlite:files.db";
    //private Connection connection;

    public static synchronized DBManager getInstance() {
        if (_instance == null) {
            _instance = new DBManager();
        }

        return _instance;
    }

    private DBManager() {
        // trying to connect to a non existant file will automatically create it
        boolean dbMissing = false;
        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING)) {
            // sanity check?
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ModID FROM Mod");
            log.log(Level.INFO, "{0} rows fetched.", rs.getFetchSize());
        } catch (SQLException e) {
            log.severe(e.getMessage());
            dbMissing = e.getMessage().contains("no such table:");
        }

        //create DB structure if not present
        if (dbMissing) {
            createTableMod();
            createTableDeployment();
        }
    }

    public synchronized void registerNewFile(ContentEntity fe) {
        String sql = "INSERT INTO Mod(Name, FileName, URL, GameID, ModeID, TypeID, DateAdded) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fe.getName());
            pstmt.setString(2, fe.getDownloadedFileName());
            pstmt.setString(3, fe.getOrigin());
            pstmt.setInt(4, fe.getGame().ordinal());
            pstmt.setInt(5, fe.getModes().ordinal());
            pstmt.setInt(6, fe.getType().ordinal());
            pstmt.setString(7, LocalDateTime.now().toString());
            int rowsUpdated = pstmt.executeUpdate();
            log.log(Level.INFO, "Rows udpated: {0}", rowsUpdated);
            fe.setDateAdded(LocalDateTime.now());
            ResultSet rs = conn.prepareStatement("select last_insert_rowid();").executeQuery();
            if(rs.next()) {
                fe.setId((long) rs.getInt(1));
            }
        } catch (SQLException e) {
            log.severe(e.getMessage());
        }
    }

    public synchronized void update(ContentEntity contentEntity) {
        if (contentEntity.getId() != null) {
            String sql = "UPDATE Mod set " +
                    "Name = ?, " +
                    "FileName = ?, " +
                    "URL = ?, " +
                    "GameID = ?, " +
                    "ModeID = ?, " +
                    "TypeID = ?, " +
                    "DateAdded = ?, " +
                    "Installed = ?, " +
                    "Completed = ?, " +
                    "Rating = ? " +
                    "where ModID = ?";

            try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, contentEntity.getName());
                pstmt.setString(2, contentEntity.getDownloadedFileName());
                pstmt.setString(3, contentEntity.getOrigin());
                pstmt.setInt(4, contentEntity.getGame().ordinal());
                pstmt.setInt(5, contentEntity.getModes().ordinal());
                pstmt.setInt(6, contentEntity.getType().ordinal());
                pstmt.setString(7, contentEntity.getDateAdded() != null ? contentEntity.getDateAdded().toString() : null);
                pstmt.setBoolean(8, contentEntity.isInstalled());
                pstmt.setBoolean(9, contentEntity.isCompleted());
                pstmt.setDouble(10, contentEntity.getRating());
                pstmt.setLong(11, contentEntity.getId());
                int rowsUpdated = pstmt.executeUpdate();
                log.log(Level.INFO, "Rows udpated: {0}", rowsUpdated);
            } catch (SQLException e) {
                log.severe(e.getMessage());
            }
        }
    }

    public synchronized void delete(ContentEntity contentEntity) {
        String sql = "DELETE FROM Mod WHERE ModID = ?";

        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, contentEntity.getId());
            pstmt.execute();
        } catch (SQLException e) {
            log.severe(e.getMessage());
        }
    }

    public synchronized List<ContentEntity> getFileEntries(Filter filter) {
        List<ContentEntity> entries = new ArrayList<>();

        String query = "SELECT * FROM Mod WHERE " +
                "name LIKE ? " +
                (filter.anyGame() ? "AND GameID NOT NULL " : "AND (GameID = ? OR GameID = 2) ") + // GameID 2 = any
                (filter.anyType() ? "AND TypeID NOT NULL " : "AND (TypeID = ? OR TypeID = 3) ") + // TypeID 4 = any
                (filter.anyMode() ? "AND ModeID NOT NULL " : "AND (ModeID = ? OR ModeID = 4) ") + // ModeID 4 = any
                (filter.isInstalled() ? "AND Installed = ? " : "") +
                (filter.isCompleted() ? "AND Completed = ?" : "");

        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            int index = 1;
            pstmt.setString(index, filter.getName() != null && !filter.getName().isEmpty() ? "%" + filter.getName() + "%" : "%");
            if (!filter.anyGame()) {
                pstmt.setInt(++index, filter.getGameSelected());
            }
            if (!filter.anyType()) {
                pstmt.setInt(++index, filter.getTypeSelected());
            }
            if (!filter.anyMode()) {
                pstmt.setInt(++index, filter.getModeSelected());
            }
            if (filter.isInstalled()) {
                pstmt.setInt(++index, 1);
            }
            if (filter.isCompleted()) {
                pstmt.setInt(++index, 1);
            }
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                entries.add(extractFileEntry(rs));
            }
        } catch (SQLException e) {
            log.severe(e.getMessage());
        }

        return entries;
    }

    public synchronized List<ContentEntity> getAllFileEntries() {
        List<ContentEntity> entries = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING)) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from Mod");
            while(rs.next()) {
                entries.add(extractFileEntry(rs));
            }
        } catch (SQLException e) {
            log.severe(e.getMessage());
        }
        
        return entries;
    }

    private ContentEntity extractFileEntry(ResultSet rs) throws SQLException {
        ContentEntity fe = new ContentEntity();
        fe.setId(rs.getLong("ModId"));
        fe.setName(rs.getString("name"));
        fe.setDownloadedFileName(rs.getString("FileName"));
        String url = rs.getString("URL");
        fe.setOrigin(url);
        fe.setGame(Game.values()[rs.getInt("GameID")]);
        fe.setType(Type.values()[rs.getInt("TypeID")]);
        fe.setModes(Mode.values()[rs.getInt("ModeID")]);
        fe.setInstalled(rs.getInt("Installed") == 1);
        fe.setCompleted(rs.getInt("Completed") == 1);
        fe.setRating(rs.getInt("Rating"));
        String date = rs.getString("DateAdded");
        if (date != null) {
            try {
                fe.setDateAdded(LocalDateTime.parse(date));
            } catch (DateTimeParseException ex) {
                log.warning(ex.getMessage());
            }
        }



        // infer paths from settings
        if (fe.getGame() != null) {
            fe.setDownloadedFile(FileHandler.categorizeFile(Config.getInstance().getDirectoryDownloads() + "/" + fe.getDownloadedFileName()));
            if (fe.isInstalled()) {
                fe.setInstallFileLocation(new File(fe.getGame().getGameFolder()));
            }
        }

        return fe;
    }

    @Deprecated
    public void markModInstalled(Long id, boolean value) {
        if (id != null) {
            String sql = "UPDATE Mod set installed = ? where ModID = ?";

            try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, value);
                pstmt.setLong(2, id);
                int rowsUpdated = pstmt.executeUpdate();
                log.log(Level.INFO, "Rows udpated: {0}", rowsUpdated);
            } catch (SQLException e) {
                log.severe(e.getMessage());
            }
        }
    }

    @Deprecated
    public void markModCompleted(Long id, boolean value) {
        if (id != null) {
            String sql = "UPDATE Mod set completed = ? where ModID = ?";

            try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, value);
                pstmt.setLong(2, id);
                int rowsUpdated = pstmt.executeUpdate();
                log.log(Level.INFO, "Rows udpated: {0}", rowsUpdated);
            } catch (SQLException e) {
                log.severe(e.getMessage());
            }
        }
    }

    @Deprecated
    public void updateFileEntry(ContentEntity fe) {
        String sql = "UPDATE Mod set name = ? where ModID = ?";

        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fe.getName());
            pstmt.setString(2, fe.getDownloadedFileName());
            pstmt.setString(3, fe.getOrigin());
            pstmt.setInt(4, fe.getGame().ordinal());
            pstmt.setInt(5, fe.getModes().ordinal());
            pstmt.setInt(6, fe.getType().ordinal());
            pstmt.setString(7, LocalDateTime.now().toString());
            int rowsUpdated = pstmt.executeUpdate();
            log.log(Level.INFO, "Rows udpated: {0}", rowsUpdated);
            fe.setDateAdded(LocalDateTime.now());
        } catch (SQLException e) {
            log.severe(e.getMessage());
        }
    }

    private void createTableMod() {
        String sql = """
                     CREATE TABLE IF NOT EXISTS Mod (
                     ModID INTEGER NOT NULL UNIQUE,
                     Name INTEGER,
                     FileName TEXT NOT NULL,
                     URL TEXT,
                     InstallLocation TEXT,
                     GameID INTEGER,
                     TypeID INTEGER,
                     ModeID INTEGER,
                     Installed INTEGER,
                     Completed INTEGER,
                     Rating INTEGER,
                     DateAdded TEXT,
                     DateCompleted TEXT,
                     DateLastPlayed TEXT,
                     PRIMARY KEY("ModID" AUTOINCREMENT)
                     );""";

        log.info("Creating table 'Mod'...");
        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.severe(e.getMessage());
        }
    }

    private void createTableDeployment() {
        String sql = """
                CREATE TABLE "Deployment" (
                "DeployID" INTEGER NOT NULL UNIQUE,
                "ModID" INTEGER NOT NULL,
                "Files" TEXT,
                "DeployedOn" INTEGER,
                FOREIGN KEY("ModID") REFERENCES "Mod"("ModID"),
                PRIMARY KEY("DeployID" AUTOINCREMENT)
                );""";

        log.info("Creating table 'Deployment'...");
        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            log.severe(e.getMessage());
        }
    }
}
