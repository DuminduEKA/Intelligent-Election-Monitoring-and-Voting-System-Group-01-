package dao;

import model.Voter;
import util.DBConnection;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoterDAO {

    public List<Voter> getAllVoters() {
        List<Voter> list = new ArrayList<>();
        String sql = "SELECT * FROM Voter";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    public Voter login(String username, String password) {
        String sql = "SELECT * FROM Voter WHERE username=? AND password=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public boolean addVoter(Voter v) {
        String sql = "INSERT INTO Voter (voter_name, nic, age, district_id, has_voted, username, password) " +
                     "VALUES (?, ?, ?, ?, FALSE, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getVoterName());
            ps.setString(2, v.getNic());
            ps.setInt(3, v.getAge());
            ps.setInt(4, v.getDistrictId());
            ps.setString(5, v.getUsername());
            ps.setString(6, v.getPassword());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                new DistrictDAO().incrementRegisteredVoters(v.getDistrictId());
            }
            return ok;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean markAsVoted(int voterId) {
        String sql = "UPDATE Voter SET has_voted = TRUE WHERE voter_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, voterId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean nicExists(String nic) {
        String sql = "SELECT voter_id FROM Voter WHERE nic=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nic);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private Voter mapRow(ResultSet rs) throws SQLException {
        return new Voter(
                rs.getInt("voter_id"),
                rs.getString("voter_name"),
                rs.getString("nic"),
                rs.getInt("age"),
                rs.getInt("district_id"),
                rs.getBoolean("has_voted"),
                rs.getString("username"),
                rs.getString("password")
        );
    }
}
