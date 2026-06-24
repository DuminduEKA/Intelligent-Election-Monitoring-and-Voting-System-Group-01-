package dao;

import model.Candidate;
import util.DBConnection;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles which candidates are contesting which election.
 * A Candidate row is a reusable entity (same person/party/district can be
 * reused across many elections); ElectionCandidate is the join table that
 * decides who is actually standing in a specific election.
 */
public class ElectionCandidateDAO {

    /** All candidates assigned to the given election (any district). */
    public List<Candidate> getCandidatesForElection(int electionId) {
        List<Candidate> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            boolean hasPhoto = columnExists(con, "Candidate", "candidate_photo");
            boolean hasShort = columnExists(con, "Party", "party_short_name");
            String sql = "SELECT c.candidate_id, c.candidate_name, c.party_id, c.district_id, " +
                    (hasPhoto ? "c.candidate_photo, " : "") +
                    "p.party_name, " + (hasShort ? "p.party_short_name, " : "") +
                    "d.district_name FROM ElectionCandidate ec " +
                    "JOIN Candidate c ON ec.candidate_id = c.candidate_id " +
                    "LEFT JOIN Party p ON c.party_id = p.party_id " +
                    "LEFT JOIN District d ON c.district_id = d.district_id " +
                    "WHERE ec.election_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, electionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs, hasPhoto, hasShort));
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    /** Candidates assigned to the given election AND belonging to the given district —
     *  this is exactly what a voter should see on the ballot. */
    public List<Candidate> getCandidatesForElectionAndDistrict(int electionId, int districtId) {
        List<Candidate> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            boolean hasPhoto = columnExists(con, "Candidate", "candidate_photo");
            boolean hasShort = columnExists(con, "Party", "party_short_name");
            String sql = "SELECT c.candidate_id, c.candidate_name, c.party_id, c.district_id, " +
                    (hasPhoto ? "c.candidate_photo, " : "") +
                    "p.party_name, " + (hasShort ? "p.party_short_name, " : "") +
                    "d.district_name FROM ElectionCandidate ec " +
                    "JOIN Candidate c ON ec.candidate_id = c.candidate_id " +
                    "LEFT JOIN Party p ON c.party_id = p.party_id " +
                    "LEFT JOIN District d ON c.district_id = d.district_id " +
                    "WHERE ec.election_id = ? AND c.district_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, electionId);
                ps.setInt(2, districtId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs, hasPhoto, hasShort));
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    /** Candidate IDs currently assigned to an election (used to pre-check the assignment dialog). */
    public Set<Integer> getAssignedCandidateIds(int electionId) {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT candidate_id FROM ElectionCandidate WHERE election_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, electionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("candidate_id"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return ids;
    }

    /**
     * Replaces the full set of candidates assigned to an election in one
     * transaction: clears existing assignments, then inserts the given list.
     */
    public boolean setCandidatesForElection(int electionId, List<Integer> candidateIds) {
        String deleteSql = "DELETE FROM ElectionCandidate WHERE election_id = ?";
        String insertSql = "INSERT INTO ElectionCandidate (election_id, candidate_id) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement del = con.prepareStatement(deleteSql)) {
                    del.setInt(1, electionId);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = con.prepareStatement(insertSql)) {
                    for (int candidateId : candidateIds) {
                        ins.setInt(1, electionId);
                        ins.setInt(2, candidateId);
                        ins.addBatch();
                    }
                    if (!candidateIds.isEmpty()) ins.executeBatch();
                }
                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private Candidate mapRow(ResultSet rs, boolean hasPhoto, boolean hasShort) throws SQLException {
        Candidate c = new Candidate(
                rs.getInt("candidate_id"),
                rs.getString("candidate_name"),
                rs.getInt("party_id"),
                rs.getInt("district_id")
        );
        c.setPartyName(rs.getString("party_name"));
        c.setDistrictName(rs.getString("district_name"));
        if (hasShort) c.setPartyShortName(rs.getString("party_short_name"));
        if (hasPhoto) {
            byte[] photoBytes = rs.getBytes("candidate_photo");
            if (photoBytes != null) c.setPhotoData(photoBytes);
        }
        return c;
    }

    private boolean columnExists(Connection con, String tableName, String columnName) {
        try {
            DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}