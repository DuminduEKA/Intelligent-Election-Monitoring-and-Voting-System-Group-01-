package dao;

import model.Election;
import util.DBConnection;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ElectionDAO {

    public List<Election> getAllElections() {
        List<Election> list = new ArrayList<>();
        String sql = "SELECT * FROM Election";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Election(
                        rs.getInt("election_id"),
                        rs.getString("election_title"),
                        rs.getDate("election_date"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    public Election getActiveElection() {
        String sql = "SELECT * FROM Election WHERE status='Active' ORDER BY election_id DESC LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new Election(
                        rs.getInt("election_id"),
                        rs.getString("election_title"),
                        rs.getDate("election_date"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public boolean addElection(String title, Date date) {
        String closeOthersSql = "UPDATE Election SET status='Closed' WHERE status='Active'";
        String insertSql = "INSERT INTO Election (election_title, election_date, status) VALUES (?, ?, 'Active')";
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement closeOthers = con.prepareStatement(closeOthersSql)) {
                closeOthers.executeUpdate();
            }
            try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                insert.setString(1, title);
                insert.setDate(2, date);
                return insert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean closeElection(int electionId) {
        String sql = "UPDATE Election SET status='Closed' WHERE election_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, electionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Makes the given election the sole Active one: closes every other
     * election first, then marks this one Active. Used when re-activating
     * a previously closed election from the Election Management screen.
     */
    public boolean setActiveElection(int electionId) {
        String closeOthersSql = "UPDATE Election SET status='Closed' WHERE election_id<>?";
        String activateSql = "UPDATE Election SET status='Active' WHERE election_id=?";
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement closeOthers = con.prepareStatement(closeOthersSql)) {
                closeOthers.setInt(1, electionId);
                closeOthers.executeUpdate();
            }
            try (PreparedStatement activate = con.prepareStatement(activateSql)) {
                activate.setInt(1, electionId);
                return activate.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
public Election getElectionById(int electionId) {
    String sql = "SELECT * FROM Election WHERE election_id=?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, electionId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Election(
                        rs.getInt("election_id"),
                        rs.getString("election_title"),
                        rs.getDate("election_date"),
                        rs.getString("status")
                );
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                null,
                "Database connection failed. Please contact admin.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    return null;
}
}
