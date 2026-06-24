package ui;

import dao.DistrictDAO;
import dao.VoterDAO;
import model.District;
import model.Voter;
import util.UIStyle;
import util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VoterRegistrationPage extends JFrame {

    private final boolean selfRegister;
    private JTextField nameField, nicField, dobField, usernameField;
    private JPasswordField passwordField;
    private JComboBox<District> districtBox;

    public VoterRegistrationPage(boolean selfRegister) {
        this.selfRegister = selfRegister;
        init();
    }

    public VoterRegistrationPage() { this(true); }

    private void init() {
        setTitle(selfRegister ? "Voter Self-Registration" : "Voter Registration");
        setSize(520, 740);
        setMinimumSize(new Dimension(460, 680));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UIStyle.BG);
        outer.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        UIStyle.RoundedPanel card = UIStyle.card(null);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Header
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(UIStyle.heading(selfRegister ? "Register to Vote" : "Register New Voter", 20), BorderLayout.NORTH);
        header.add(UIStyle.muted(selfRegister
                ? "Create your voter account. You must be 18+ and hold a valid NIC."
                : "Approve a new voter with verified NIC and age eligibility."), BorderLayout.CENTER);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 20, 0);
        card.add(header, gbc);

        // Fields
        nameField     = UIStyle.textField();
        nicField      = UIStyle.textField();
        dobField      = UIStyle.textField();
        usernameField = UIStyle.textField();
        passwordField = UIStyle.passwordField(0);
        districtBox   = UIStyle.comboBox();
        for (District d : new DistrictDAO().getAllDistricts()) districtBox.addItem(d);

        gbc.insets = new Insets(0, 0, 14, 0);
        gbc.gridy = 1; card.add(formRow("Full Name", nameField), gbc);
        gbc.gridy = 2; card.add(formRow("NIC Number", nicField), gbc);
        gbc.gridy = 3; card.add(formRow("Date of Birth (yyyy-mm-dd)", dobField), gbc);
        gbc.gridy = 4; card.add(formRow("District", districtBox), gbc);
        gbc.gridy = 5; card.add(formRow("Choose a Username", usernameField), gbc);
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 24, 0);
        card.add(formRow("Choose a Password", passwordField), gbc);

        // Buttons — BoxLayout X_AXIS so they sit left naturally
        JButton submitBtn = UIStyle.primaryButton(selfRegister ? "Register" : "Approve Voter");
        submitBtn.setPreferredSize(new Dimension(150, 42));
        submitBtn.setMaximumSize(new Dimension(150, 42));

        JPanel btnRow = new JPanel();
        btnRow.setOpaque(false);
        btnRow.setLayout(new BoxLayout(btnRow, BoxLayout.X_AXIS));
        btnRow.add(submitBtn);
        if (selfRegister) {
            JButton backBtn = UIStyle.ghostButton("← Back to Login");
            backBtn.addActionListener(e -> dispose());
            btnRow.add(Box.createHorizontalStrut(12));
            btnRow.add(backBtn);
        }
        btnRow.add(Box.createHorizontalGlue()); // push buttons to left

        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnRow, gbc);

        // Filler to push everything up
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0; filler.gridy = 8; filler.weighty = 1.0;
        filler.fill = GridBagConstraints.VERTICAL;
        card.add(Box.createVerticalGlue(), filler);

        outer.add(card, BorderLayout.CENTER);
        add(outer);

        submitBtn.addActionListener(e -> registerVoter());
    }

    private JPanel formRow(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(UIStyle.sectionLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void registerVoter() {
        String name     = nameField.getText().trim();
        String nic      = nicField.getText().trim();
        String dobText  = dobField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        District district = (District) districtBox.getSelectedItem();

        if (name.isEmpty() || nic.isEmpty() || dobText.isEmpty()
                || username.isEmpty() || password.isEmpty() || district == null) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidationUtil.isValidNic(nic)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid NIC format.\nOld: 9 digits + V/X  (e.g. 901234567V)\nNew: 12 digits (e.g. 199012345678)",
                    "Invalid NIC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (new VoterDAO().nicExists(nic)) {
            JOptionPane.showMessageDialog(this, "A voter with this NIC is already registered.", "Duplicate NIC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int age;
        try {
            age = ValidationUtil.calculateAge(LocalDate.parse(dobText));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date. Use yyyy-mm-dd (e.g. 1990-05-20).", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtil.isEligibleAge(age)) {
            JOptionPane.showMessageDialog(this, "You must be at least 18 years old to register.", "Age Restriction", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Voter v = new Voter(0, name, nic, age, district.getDistrictId(), false, username, password);
        if (new VoterDAO().addVoter(v)) {
            if (selfRegister) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful!\nYou can now log in with username: " + username,
                        "Welcome!", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Voter registered!\nUsername: " + username, "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText(""); nicField.setText(""); dobField.setText("");
                usernameField.setText(""); passwordField.setText("");
            }
        }
    }
}