package dao;

import model.Party;
import util.DBConnection;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartyDAO {

    public List<Party> getAllParties() {
        List<Party> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            boolean hasShortName = columnExists(con, "Party", "party_short_name");
            String sql = hasShortName
                    ? "SELECT party_id, party_name, party_short_name, party_logo_path, leader_name, party_logo FROM Party"
                    : "SELECT party_id, party_name, party_logo_path, leader_name, party_logo FROM Party";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Party p = new Party(
                            rs.getInt("party_id"),
                            rs.getString("party_name"),
                            hasShortName ? rs.getString("party_short_name") : null,
                            rs.getString("party_logo_path"),
                            rs.getString("leader_name")
                    );
                    p.setLogoData(rs.getBytes("party_logo")); // may be null
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    public boolean addParty(Party p) {
        try (Connection con = DBConnection.getConnection()) {
            boolean hasShortName = columnExists(con, "Party", "party_short_name");
            String sql = hasShortName
                    ? "INSERT INTO Party (party_name, party_short_name, party_logo_path, leader_name, party_logo) VALUES (?, ?, ?, ?, ?)"
                    : "INSERT INTO Party (party_name, party_logo_path, leader_name, party_logo) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int idx = 1;
                ps.setString(idx++, p.getPartyName());
                if (hasShortName) ps.setString(idx++, p.getShortName());
                ps.setString(idx++, p.getPartyLogoPath());
                ps.setString(idx++, p.getLeaderName());
                if (p.getLogoData() != null) {
                    ps.setBytes(idx, p.getLogoData());
                } else {
                    ps.setNull(idx, Types.BLOB);
                }
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateParty(Party p) {
        try (Connection con = DBConnection.getConnection()) {
            boolean hasShortName = columnExists(con, "Party", "party_short_name");
            boolean updateLogo = p.getLogoData() != null;

            StringBuilder sql = new StringBuilder("UPDATE Party SET party_name=?");
            if (hasShortName) sql.append(", party_short_name=?");
            sql.append(", party_logo_path=?, leader_name=?");
            if (updateLogo) sql.append(", party_logo=?");
            sql.append(" WHERE party_id=?");

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int idx = 1;
                ps.setString(idx++, p.getPartyName());
                if (hasShortName) ps.setString(idx++, p.getShortName());
                ps.setString(idx++, p.getPartyLogoPath());
                ps.setString(idx++, p.getLeaderName());
                if (updateLogo) ps.setBytes(idx++, p.getLogoData());
                ps.setInt(idx, p.getPartyId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deleteParty(int partyId) {
        String sql = "DELETE FROM Party WHERE party_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, partyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
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
