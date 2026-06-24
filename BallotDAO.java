package dao;

import util.DBConnection;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class BallotDAO {

    /**
     * Casts a vote. Returns false (and shows a warning) if the voter
     * has already voted - enforces the "One Vote Per User" rule at
     * the DB layer as well as the UI layer.
     */
    public boolean castVote(int voterId, int candidateId, int electionId) {
        String checkSql = "SELECT has_voted FROM Voter WHERE voter_id = ?";
        String insertSql = "INSERT INTO Ballot (voter_id, candidate_id, election_id) VALUES (?, ?, ?)";
        String updateSql = "UPDATE Voter SET has_voted = TRUE WHERE voter_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setInt(1, voterId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getBoolean("has_voted")) {
                        JOptionPane.showMessageDialog(null,
                                "You have already cast your vote in this election.",
                                "Vote Already Cast", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                }
            }

            try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                insert.setInt(1, voterId);
                insert.setInt(2, candidateId);
                insert.setInt(3, electionId);
                insert.executeUpdate();
            }

            try (PreparedStatement update = con.prepareStatement(updateSql)) {
                update.setInt(1, voterId);
                update.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /** Vote count per party, for the Pie Chart. */
    public Map<String, Integer> getVotesByParty() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT p.party_name, COUNT(*) AS votes FROM Ballot b " +
                     "JOIN Candidate c ON b.candidate_id = c.candidate_id " +
                     "JOIN Party p ON c.party_id = p.party_id " +
                     "GROUP BY p.party_name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("party_name"), rs.getInt("votes"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return map;
    }

    /** Vote count per district, for the Bar Chart. */
    public Map<String, Integer> getVotesByDistrict() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT d.district_name, COUNT(*) AS votes FROM Ballot b " +
                     "JOIN Candidate c ON b.candidate_id = c.candidate_id " +
                     "JOIN District d ON c.district_id = d.district_id " +
                     "GROUP BY d.district_name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("district_name"), rs.getInt("votes"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return map;
    }

    public int getTotalVotes() {
        String sql = "SELECT COUNT(*) AS total FROM Ballot";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return 0;
    }
}
