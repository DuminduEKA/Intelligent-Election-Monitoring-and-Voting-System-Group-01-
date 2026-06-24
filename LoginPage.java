package ui;

import dao.AdminDAO;
import dao.VoterDAO;
import model.AdminUser;
import model.Voter;
import model.VoterUser;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public LoginPage() {
        setTitle("Election Management System - Login");
        setSize(880, 620);
        setMinimumSize(new Dimension(760, 580));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIStyle.BG);
        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.gridx = 0; rgbc.gridy = 0;
        rgbc.weightx = 1; rgbc.weighty = 1;
        rgbc.fill = GridBagConstraints.BOTH;
        rgbc.insets = new Insets(40, 40, 40, 40);

        UIStyle.RoundedPanel shell = new UIStyle.RoundedPanel(20, UIStyle.SURFACE, UIStyle.BORDER);
        shell.setLayout(new GridLayout(1, 2));
        root.add(shell, rgbc);

        shell.add(buildBrandPanel());
        shell.add(buildFormPanel());

        add(root);
    }

    private JPanel buildBrandPanel() {
        JPanel brand = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIStyle.PRIMARY, getWidth(), getHeight(), UIStyle.PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.fillRect(getWidth() - 20, 0, 20, getHeight());
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fill(new Ellipse2D.Float(-60, getHeight() - 160, 260, 260));
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fill(new Ellipse2D.Float(getWidth() - 140, -80, 220, 220));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        brand.setOpaque(false);
        brand.setPreferredSize(new Dimension(380, 0));

        JLabel badge = new JLabel(UIStyle.icon(UIStyle.IconType.BRAND, 40, Color.WHITE));
        badge.setBounds(48, 56, 60, 50);
        brand.add(badge);

        JLabel title = new JLabel("<html>Election<br>Management<br>System</html>");
        title.setFont(new Font(UIStyle.display().getFamily(), Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setBounds(48, 120, 300, 140);
        brand.add(title);

        JLabel subtitle = new JLabel("<html>Secure, transparent voting<br>for every district.</html>");
        subtitle.setFont(UIStyle.body());
        subtitle.setForeground(new Color(255, 255, 255, 215));
        subtitle.setBounds(48, 250, 300, 60);
        brand.add(subtitle);

        JPanel featureRow = new JPanel();
        featureRow.setOpaque(false);
        featureRow.setLayout(new BoxLayout(featureRow, BoxLayout.X_AXIS));
        featureRow.setBounds(48, 420, 320, 24);
        String[] features = {"Role-based access", "Live results", "Audit-ready"};
        for (int i = 0; i < features.length; i++) {
            JLabel check = new JLabel(UIStyle.icon(UIStyle.IconType.CHECK, 12, new Color(255, 255, 255, 200)));
            JLabel text = new JLabel(features[i]);
            text.setFont(UIStyle.bodySmall());
            text.setForeground(new Color(255, 255, 255, 180));
            text.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, i < features.length - 1 ? 14 : 0));
            featureRow.add(check);
            featureRow.add(text);
        }
        brand.add(featureRow);
        return brand;
    }

    private JPanel buildFormPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UIStyle.SURFACE);
        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0; outer.gridy = 0;

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(320, 460));

        JLabel heading = UIStyle.heading("Welcome back", 22);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIStyle.muted("Sign in to continue to your dashboard");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(heading);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(26));

        form.add(fieldLabel("Username"));
        usernameField = UIStyle.textField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));

        form.add(fieldLabel("Password"));
        passwordField = UIStyle.passwordField(0);
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(14));

        form.add(fieldLabel("Login as"));
        roleBox = UIStyle.comboBox(new String[]{"Admin", "Voter"});
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(roleBox);
        form.add(Box.createVerticalStrut(24));

        // Sign In button
        JButton loginBtn = UIStyle.primaryButton("Sign In");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(loginBtn);

        form.add(Box.createVerticalStrut(12));

        // Divider line
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIStyle.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sep);

        form.add(Box.createVerticalStrut(12));

        // Self-register button — secondary style
        JButton registerBtn = UIStyle.ghostButton("New voter? Register here");
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(registerBtn);

        wrap.add(form, outer);

        loginBtn.addActionListener(e -> attemptLogin());
        getRootPane().setDefaultButton(loginBtn);

        registerBtn.addActionListener(e -> {
            // Open self-registration — no admin login needed
            new VoterRegistrationPage(true).setVisible(true);
        });

        return wrap;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = UIStyle.sectionLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Admin".equals(role)) {
            boolean ok = new AdminDAO().login(username, password);
            if (ok) {
                AdminUser admin = new AdminUser(1, username);
                dispose();
                new AdminDashboard(admin).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid admin credentials.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Voter v = new VoterDAO().login(username, password);
            if (v != null) {
                VoterUser voterUser = new VoterUser(v.getVoterId(), v.getVoterName(),
                        v.getVoterId(), v.getNic(), v.getDistrictId(), v.isHasVoted());
                dispose();
                new VoterDashboardPage(voterUser).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid voter credentials.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}