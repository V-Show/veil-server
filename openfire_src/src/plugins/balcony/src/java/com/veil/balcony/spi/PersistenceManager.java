package com.veil.balcony.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.muc.spi.MUCPersistenceManager;
import org.jivesoftware.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceManager {

	private static final Logger Log = LoggerFactory.getLogger(MUCPersistenceManager.class);
	
    private static final String ADD_CONVERSATION_LOG =
            "INSERT INTO ofMucConversationLog (roomID,sender,nickname,logTime,subject,body) " +
            "VALUES (?,?,?,?,?,?)";
    
    /**
     * Saves the conversation log entry to the database.
     * 
     * @param entry the ConversationLogEntry to save to the database.
     * @return true if the ConversationLogEntry was saved successfully to the database.
     */
    public static boolean saveConversationLogEntry(ConversationLogEntry entry) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(ADD_CONVERSATION_LOG);
            pstmt.setLong(1, entry.getRoomID());
            pstmt.setString(2, entry.getSender().toString());
            pstmt.setString(3, entry.getNickname());
            pstmt.setString(4, StringUtils.dateToMillis(entry.getDate()));
            pstmt.setString(5, entry.getSubject());
            pstmt.setString(6, entry.getBody());
            pstmt.executeUpdate();
            return true;
        }
        catch (SQLException sqle) {
            Log.error("Error saving conversation log entry", sqle);
            return false;
        }
        finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
}
