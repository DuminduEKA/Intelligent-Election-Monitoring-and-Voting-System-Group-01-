package model;

/**
 * Base class for all system users.
 * Demonstrates INHERITANCE and ENCAPSULATION.
 */
public abstract class User {
    private int id;
    private String name;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * POLYMORPHISM: each subclass overrides this to describe
     * which dashboard it should open.
     */
    public abstract String displayDashboard();
}
