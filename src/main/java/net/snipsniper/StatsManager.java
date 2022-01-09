package net.snipsniper;

import net.snipsniper.utils.enums.LogLevel;

import java.sql.*;

public class StatsManager {
    private static boolean enabled = true;
    public static final String STARTED_AMOUNT = "started_amount";
    public static final String EDITOR_STARTED_AMOUNT = "editor_started_amount";
    public static final String VIEWER_STARTED_AMOUNT = "viewer_started_amount";
    public static final String SCREENSHOTS_TAKEN_AMOUNT = "screenshots_taken_amount";
    public static final String ABOUT_ICON_CLICKED_AMOUNT = "about_icon_clicked_amount";

    public static final String BGAME_STARTED_AMOUNT = "bgame_started_amount";
    public static final String BGAME_STARTED_SPAWNED_PIECES_AMOUNT = "bgame_spawned_pieces_amount";

    public static void init() {
        //TODO: Implement in json
        enabled = false;
    }

    public static void initOld() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            LogManager.log("StatsManager is disabled for now. This should not be called!", LogLevel.WARNING);
        }
        try {
            Connection connection = getConnection();
            if(enabled && connection != null) {
                Statement stmntCreateTables = connection.createStatement();
                stmntCreateTables.executeUpdate("create table IF NOT EXISTS counters (id varchar(255), count int);");
                connection.close();
            }
        } catch (SQLException sqlException) {
            LogManager.log("Issue setting up StatsManager! Message: " + sqlException.getMessage(), LogLevel.ERROR);
            enabled = false;
        }
    }

    public static void incrementCount(String id) {
        if(!enabled) return;

        Connection connection = getConnection();
        if(connection == null) return;

        try {
            if (!doesIDExist(connection, "counters", id)) {
                PreparedStatement prep = connection.prepareStatement("insert into counters values (?, ?);");
                prep.setString(1, id);
                prep.setInt(2, 1);
                prep.execute();
                connection.close();
                return;
            }

            PreparedStatement increment = connection.prepareStatement("update counters set count = count + 1 where id=?");
            increment.setString(1, id);
            increment.execute();

            connection.close();
        } catch(SQLException sqlException) {
            LogManager.log("Error incrementing id: " + id + "! Message: " + sqlException.getMessage(), LogLevel.ERROR);
        }
    }

    private static boolean doesIDExist(Connection connection, String table, String id) {
        if(!enabled) return false;

        try {
            PreparedStatement checkIfExist = connection.prepareStatement("select * from " + table + " where id=?;");
            checkIfExist.setString(1, id);
            ResultSet rs = checkIfExist.executeQuery();
            return rs.isBeforeFirst();
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return false;
    }

    private static Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + SnipSniper.getMainFolder() + "\\stats.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        enabled = false;
        return null;
    }

}
