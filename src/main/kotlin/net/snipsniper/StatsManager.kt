package net.snipsniper

import org.capturecoop.cclogger.CCLogger
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class StatsManager {
    companion object {
        private var enabled = true
        const val STARTED_AMOUNT = "started_amount"
        const val EDITOR_STARTED_AMOUNT = "editor_started_amount"
        const val VIEWER_STARTED_AMOUNT = "viewer_started_amount"
        const val SCREENSHOTS_TAKEN_AMOUNT = "screenshots_taken_amount"
        const val ABOUT_ICON_CLICKED_AMOUNT = "about_icon_clicked_amount"
        const val BGAME_STARTED_AMOUNT = "bgame_started_amount"
        const val BGAME_STARTED_SPAWNED_PIECES_AMOUNT = "bgame_spawned_pieces_amount"
        fun init() {
            //TODO: Implement in json
            enabled = false
        }

        fun initOld() {
            try {
                Class.forName("org.sqlite.JDBC")
            } catch (e: ClassNotFoundException) {
                CCLogger.warn("StatsManager is disabled for now. This should not be called!")
            }
            try {
                val connection = connection
                if (enabled && connection != null) {
                    val stmntCreateTables = connection.createStatement()
                    stmntCreateTables.executeUpdate("create table IF NOT EXISTS counters (id varchar(255), count int);")
                    connection.close()
                }
            } catch (sqlException: SQLException) {
                CCLogger.error("Issue setting up StatsManager! Message: ${sqlException.message}")
                enabled = false
            }
        }

        fun incrementCount(id: String) {
            if (!enabled) return
            val connection = connection ?: return
            try {
                if (!doesIDExist(connection, "counters", id)) {
                    val prep = connection.prepareStatement("insert into counters values (?, ?);")
                    prep.setString(1, id)
                    prep.setInt(2, 1)
                    prep.execute()
                    connection.close()
                    return
                }
                val increment = connection.prepareStatement("update counters set count = count + 1 where id=?")
                increment.setString(1, id)
                increment.execute()
                connection.close()
            } catch (sqlException: SQLException) {
                CCLogger.error("Error incrementing id: $id! Message: ${sqlException.message}")
            }
        }

        private fun doesIDExist(connection: Connection, table: String, id: String): Boolean {
            if (!enabled) return false
            try {
                val checkIfExist = connection.prepareStatement("select * from $table where id=?;")
                checkIfExist.setString(1, id)
                return checkIfExist.executeQuery().isBeforeFirst
            } catch (sqlException: SQLException) {
                sqlException.printStackTrace()
            }
            return false
        }

        private val connection: Connection?
            get() {
                try {
                    return DriverManager.getConnection("jdbc:sqlite:" + SnipSniper.mainFolder + "\\stats.db")
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
                enabled = false
                return null
            }
    }
}