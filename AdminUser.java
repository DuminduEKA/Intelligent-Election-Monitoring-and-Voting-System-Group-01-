package model;

public class AdminUser extends User {

    public AdminUser(int id, String name) {
        super(id, name);
    }

    @Override
    public String displayDashboard() {
        return "AdminDashboard";
    }
}
