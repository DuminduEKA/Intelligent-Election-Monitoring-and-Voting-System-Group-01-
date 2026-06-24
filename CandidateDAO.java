package dao;

import model.Candidate;
import util.DBConnection;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {

    public List<Candidate> getAllCandidates() {
        List<Candidate> list = new ArrayList<>();
        // Check if candidate_photo column exists first; if not, skip it
        String sql;
        try (Connection con = DBConnection.getConnection()) {
            boolean hasPhotoCol = columnExists(con, "Candidate", "candidate_photo");
            if (hasPhotoCol) {
                sql = "SELECT c.candidate_id, c.candidate_name, c.party_id, c.district_id, " +
                      "c.candidate_photo, p.party_name, d.district_name FROM Candidate c " +
                      "LEFT JOIN Party p ON c.party_id = p.party_id " +
                      "LEFT JOIN District d ON c.district_id = d.district_id";
            } else {
                sql = "SELECT c.candidate_id, c.candidate_name, c.party_id, c.district_id, " +
                      "p.party_name, d.district_name FROM Candidate c " +
                      "LEFT JOIN Party p ON c.party_id = p.party_id " +
                      "LEFT JOIN District d ON c.district_id = d.district_id";
            }
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Candidate c = new Candidate(
                            rs.getInt("candidate_id"),
                            rs.getString("candidate_name"),
                            rs.getInt("party_id"),
                            rs.getInt("district_id")
                    );
                    c.setPartyName(rs.getString("party_name"));
                    c.setDistrictName(rs.getString("district_name"));
                    if (hasPhotoCol) {
                        c.setPhotoData(rs.getBytes("candidate_photo"));
                    }
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "getAllCandidates error:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    public List<Candidate> getCandidatesByDistrict(int districtId) {
        List<Candidate> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            boolean hasPhotoCol = columnExists(con, "Candidate", "candidate_photo");
            String sql;
            if (hasPhotoCol) {
                sql = "SELECT c.candidate_id, c.candidate_name, c.party_id, c.district_id, " +
                      "c.candidate_photo, p.party_name, d.district_name FROM Candidate c " +
                      "LEFT JOIN Party p ON c.party_id = p.party_id " +
                      "LEFT JOIN District d ON c.district_id = d.district_id " +
                      "WHERE c.district_id = ?";
            } else {
                sql = "SELECT c.candidate_id, c.candidate_name, c.party_id, c.district_id, " +
                      "p.party_name, d.district_name FROM Candidate c " +
                      "LEFT JOIN Party p ON c.party_id = p.party_id " +
                      "LEFT JOIN District d ON c.district_id = d.district_id " +
                      "WHERE c.district_id = ?";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, districtId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Candidate c = new Candidate(
                                rs.getInt("candidate_id"),
                                rs.getString("candidate_name"),
                                rs.getInt("party_id"),
                                rs.getInt("district_id")
                        );
                        c.setPartyName(rs.getString("party_name"));
                        c.setDistrictName(rs.getString("district_name"));
                        if (hasPhotoCol) {
                            c.setPhotoData(rs.getBytes("candidate_photo"));
                        }
                        list.add(c);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "getCandidatesByDistrict error:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    public boolean addCandidate(Candidate c) {
        try (Connection con = DBConnection.getConnection()) {
            boolean hasPhotoCol = columnExists(con, "Candidate", "candidate_photo");
            String sql;
            if (hasPhotoCol) {
                sql = "INSERT INTO Candidate (candidate_name, party_id, district_id, candidate_photo) VALUES (?, ?, ?, ?)";
            } else {
                sql = "INSERT INTO Candidate (candidate_name, party_id, district_id) VALUES (?, ?, ?)";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, c.getCandidateName());
                ps.setInt(2, c.getPartyId());
                ps.setInt(3, c.getDistrictId());
                if (hasPhotoCol) {
                    if (c.getPhotoData() != null) {
                        ps.setBytes(4, c.getPhotoData());
                    } else {
                        ps.setNull(4, Types.BLOB);
                    }
                }
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "addCandidate error:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateCandidate(Candidate c) {
        try (Connection con = DBConnection.getConnection()) {
            boolean hasPhotoCol = columnExists(con, "Candidate", "candidate_photo");
            String sql;
            boolean updatePhoto = hasPhotoCol && c.getPhotoData() != null;
            if (updatePhoto) {
                sql = "UPDATE Candidate SET candidate_name=?, party_id=?, district_id=?, candidate_photo=? WHERE candidate_id=?";
            } else {
                sql = "UPDATE Candidate SET candidate_name=?, party_id=?, district_id=? WHERE candidate_id=?";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, c.getCandidateName());
                ps.setInt(2, c.getPartyId());
                ps.setInt(3, c.getDistrictId());
                if (updatePhoto) {
                    ps.setBytes(4, c.getPhotoData());
                    ps.setInt(5, c.getCandidateId());
                } else {
                    ps.setInt(4, c.getCandidateId());
                }
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "updateCandidate error:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deleteCandidate(int candidateId) {
        String sql = "DELETE FROM Candidate WHERE candidate_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "deleteCandidate error:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /** Check whether a column exists in a table (case-insensitive) */
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