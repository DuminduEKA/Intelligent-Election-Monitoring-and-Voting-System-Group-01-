package ui;

import util.UIStyle;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIStyle.applyGlobal();
            new LoginPage().setVisible(true);
        });
    }
}
