package dao;

import model.District;
import util.DBConnection;

import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DistrictDAO {

    public List<District> getAllDistricts() {
        List<District> list = new ArrayList<>();
        String sql = "SELECT * FROM District";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new District(
                        rs.getInt("district_id"),
                        rs.getString("district_name"),
                        rs.getInt("total_registered_voters")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    public boolean addDistrict(String name) {
        String sql = "INSERT INTO District (district_name, total_registered_voters) VALUES (?, 0)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void incrementRegisteredVoters(int districtId) {
        String sql = "UPDATE District SET total_registered_voters = total_registered_voters + 1 WHERE district_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, districtId);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed. Please contact admin.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
