package ui;

import dao.CandidateDAO;
import dao.DistrictDAO;
import dao.PartyDAO;
import model.Candidate;
import model.District;
import model.Party;
import util.UIStyle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class EntityManagementPage extends JFrame {

    private final PartyDAO partyDAO = new PartyDAO();
    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final DistrictDAO districtDAO = new DistrictDAO();

    private JTabbedPane tabs;

    // ---------- Party tab ----------
    private JTextField partyNameField, shortNameField, leaderField, logoPathField;
    private JLabel partyImagePreview;
    private byte[] selectedPartyLogoData = null;   // bytes chosen in this session
    private JTable partyTable;
    private DefaultTableModel partyModel;
    private int selectedPartyId = -1;

    // ---------- Candidate tab ----------
    private JTextField candidateNameField;
    private JLabel candidateImagePreview;
    private byte[] selectedCandidatePhotoData = null;
    private JComboBox<Party> partyBox;
    private JComboBox<District> districtBox;
    private JTable candidateTable;
    private DefaultTableModel candidateModel;
    private int selectedCandidateId = -1;

    // Preview box size
    private static final int PREVIEW_W = 80;
    private static final int PREVIEW_H = 80;

    public EntityManagementPage() {
        setTitle("Manage Parties & Candidates");
        setSize(940, 640);
        setMinimumSize(new Dimension(720, 500));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = UIStyle.pageBackground(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel heading = UIStyle.heading("Parties & Candidates", 22);
        JLabel sub = UIStyle.muted("Add, edit, or remove parties and candidates contesting the election.");
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(heading);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        root.add(header, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        UIStyle.styleTabs(tabs);
        tabs.add("Parties", buildPartyPanel());
        tabs.add("Candidates", buildCandidatePanel());
        root.add(tabs, BorderLayout.CENTER);

        add(root);
        refreshPartyTable();
        refreshCandidateTable();
    }

    // ================================================================
    // PARTY TAB
    // ================================================================
    private JPanel buildPartyPanel() {
        JPanel panel = UIStyle.pageBackground(new BorderLayout(0, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 4, 4, 4));

        UIStyle.RoundedPanel formCard = UIStyle.card(new BorderLayout(0, 14));
        formCard.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // ---- fields row ----
        JPanel fieldsRow = new JPanel(new GridBagLayout());
        fieldsRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1;

        partyNameField = UIStyle.textField();
        shortNameField = UIStyle.textField();
        leaderField    = UIStyle.textField();
        logoPathField  = UIStyle.textField();
        logoPathField.setEditable(false);   // path is read-only; browse button sets it

        gbc.gridx = 0; gbc.weightx = 1.0;
        fieldsRow.add(labeledField("Party Name", partyNameField), gbc);
        gbc.gridx = 1;
        fieldsRow.add(labeledField("Short Name", shortNameField), gbc);
        gbc.gridx = 2;
        fieldsRow.add(labeledField("Leader Name", leaderField), gbc);
        gbc.gridx = 3; gbc.insets = new Insets(0, 0, 0, 0);
        fieldsRow.add(buildLogoUploadPanel(), gbc);

        formCard.add(fieldsRow, BorderLayout.CENTER);

        // ---- buttons ----
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        JButton save   = UIStyle.primaryButton("Save");
        JButton update = UIStyle.secondaryButton("Update");
        JButton delete = UIStyle.dangerButton("Delete");
        JButton clear  = UIStyle.ghostButton("Clear");
        buttons.add(save); buttons.add(update); buttons.add(delete); buttons.add(clear);
        formCard.add(buttons, BorderLayout.SOUTH);

        // ---- table ----
        partyModel = new DefaultTableModel(new Object[]{"ID", "Party Name", "Short Name", "Leader", "Has Logo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        partyTable = new JTable(partyModel);
        UIStyle.styleTable(partyTable);

        partyTable.getSelectionModel().addListSelectionListener(e -> {
            int row = partyTable.getSelectedRow();
            if (row < 0) return;
            selectedPartyId = (int) partyModel.getValueAt(row, 0);
            partyNameField.setText(partyModel.getValueAt(row, 1).toString());
            shortNameField.setText(partyModel.getValueAt(row, 2) == null ? "" : partyModel.getValueAt(row, 2).toString());
            leaderField.setText(partyModel.getValueAt(row, 3) == null ? "" : partyModel.getValueAt(row, 3).toString());
            // Show existing logo from DB
            selectedPartyLogoData = null;  // reset pending selection
            logoPathField.setText("");
            // Load preview from DB
            List<Party> all = partyDAO.getAllParties();
            for (Party p : all) {
                if (p.getPartyId() == selectedPartyId) {
                    setImagePreview(partyImagePreview, p.getLogoData());
                    break;
                }
            }
        });

        save.addActionListener(e -> {
            if (partyNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Party name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (shortNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Short name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Party p = new Party(0, partyNameField.getText().trim(), shortNameField.getText().trim(),
                    logoPathField.getText().trim(), leaderField.getText().trim());
            p.setLogoData(selectedPartyLogoData);
            if (partyDAO.addParty(p)) { refreshPartyTable(); clearPartyForm(); }
        });

        update.addActionListener(e -> {
            if (selectedPartyId == -1) {
                JOptionPane.showMessageDialog(this, "Select a party from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (shortNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Short name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Party p = new Party(selectedPartyId, partyNameField.getText().trim(), shortNameField.getText().trim(),
                    logoPathField.getText().trim(), leaderField.getText().trim());
            p.setLogoData(selectedPartyLogoData);  // null = don't overwrite existing
            if (partyDAO.updateParty(p)) { refreshPartyTable(); clearPartyForm(); }
        });

        delete.addActionListener(e -> {
            if (selectedPartyId == -1) {
                JOptionPane.showMessageDialog(this, "Select a party from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this party?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && partyDAO.deleteParty(selectedPartyId)) {
                refreshPartyTable(); clearPartyForm();
            }
        });

        clear.addActionListener(e -> clearPartyForm());

        panel.add(formCard, BorderLayout.NORTH);
        panel.add(UIStyle.scrollWrap(partyTable), BorderLayout.CENTER);
        return panel;
    }

    /** Panel with preview thumbnail + Browse button for party logo */
    private JPanel buildLogoUploadPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel label = UIStyle.sectionLabel("Party Logo");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        p.add(label);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        partyImagePreview = makePreviewLabel();
        row.add(partyImagePreview);

        JButton browse = UIStyle.secondaryButton("Browse…");
        browse.addActionListener(e -> {
            File f = chooseImageFile();
            if (f == null) return;
            try {
                selectedPartyLogoData = Files.readAllBytes(f.toPath());
                logoPathField.setText(f.getName());
                setImagePreview(partyImagePreview, selectedPartyLogoData);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not read image file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        row.add(browse);
        p.add(row);
        return p;
    }

    private void clearPartyForm() {
        selectedPartyId = -1;
        selectedPartyLogoData = null;
        partyNameField.setText("");
        shortNameField.setText("");
        leaderField.setText("");
        logoPathField.setText("");
        partyImagePreview.setIcon(placeholderIcon());
        partyTable.clearSelection();
    }

    private void refreshPartyTable() {
        partyModel.setRowCount(0);
        for (Party p : partyDAO.getAllParties()) {
            partyModel.addRow(new Object[]{
                p.getPartyId(), p.getPartyName(), p.getShortName(), p.getLeaderName(),
                p.getLogoData() != null ? "✔ Yes" : "—"
            });
        }
        refreshPartyComboBoxIfExists();
    }

    // ================================================================
    // CANDIDATE TAB
    // ================================================================
    private JPanel buildCandidatePanel() {
        JPanel panel = UIStyle.pageBackground(new BorderLayout(0, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 4, 4, 4));

        UIStyle.RoundedPanel formCard = UIStyle.card(new BorderLayout(0, 14));
        formCard.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // ---- fields row ----
        JPanel fieldsRow = new JPanel(new GridBagLayout());
        fieldsRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.weighty = 1;

        candidateNameField = UIStyle.textField();
        partyBox    = UIStyle.comboBox();
        districtBox = UIStyle.comboBox();
        for (Party p  : partyDAO.getAllParties())     partyBox.addItem(p);
        for (District d : districtDAO.getAllDistricts()) districtBox.addItem(d);

        gbc.gridx = 0; gbc.weightx = 1.2;
        fieldsRow.add(labeledField("Candidate Name", candidateNameField), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        fieldsRow.add(labeledField("Party", partyBox), gbc);
        gbc.gridx = 2;
        fieldsRow.add(labeledField("District", districtBox), gbc);
        gbc.gridx = 3; gbc.insets = new Insets(0, 0, 0, 0);
        fieldsRow.add(buildPhotoUploadPanel(), gbc);

        formCard.add(fieldsRow, BorderLayout.CENTER);

        // ---- buttons ----
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        JButton save   = UIStyle.primaryButton("Save");
        JButton update = UIStyle.secondaryButton("Update");
        JButton delete = UIStyle.dangerButton("Delete");
        JButton clear  = UIStyle.ghostButton("Clear");
        buttons.add(save); buttons.add(update); buttons.add(delete); buttons.add(clear);
        formCard.add(buttons, BorderLayout.SOUTH);

        // ---- table ----
        candidateModel = new DefaultTableModel(new Object[]{"ID", "Name", "Party", "District", "Has Photo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        candidateTable = new JTable(candidateModel);
        UIStyle.styleTable(candidateTable);

        candidateTable.getSelectionModel().addListSelectionListener(e -> {
            int row = candidateTable.getSelectedRow();
            if (row < 0) return;
            selectedCandidateId = (int) candidateModel.getValueAt(row, 0);
            candidateNameField.setText(candidateModel.getValueAt(row, 1).toString());
            selectedCandidatePhotoData = null;
            // Load preview from DB
            List<Candidate> all = candidateDAO.getAllCandidates();
            for (Candidate c : all) {
                if (c.getCandidateId() == selectedCandidateId) {
                    setImagePreview(candidateImagePreview, c.getPhotoData());
                    break;
                }
            }
        });

        save.addActionListener(e -> {
            if (candidateNameField.getText().trim().isEmpty()
                    || partyBox.getSelectedItem() == null
                    || districtBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Party   pa = (Party)    partyBox.getSelectedItem();
            District d = (District) districtBox.getSelectedItem();
            Candidate c = new Candidate(0, candidateNameField.getText().trim(), pa.getPartyId(), d.getDistrictId());
            c.setPhotoData(selectedCandidatePhotoData);
            if (candidateDAO.addCandidate(c)) { refreshCandidateTable(); clearCandidateForm(); }
        });

        update.addActionListener(e -> {
            if (selectedCandidateId == -1) {
                JOptionPane.showMessageDialog(this, "Select a candidate from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Party   pa = (Party)    partyBox.getSelectedItem();
            District d = (District) districtBox.getSelectedItem();
            Candidate c = new Candidate(selectedCandidateId, candidateNameField.getText().trim(), pa.getPartyId(), d.getDistrictId());
            c.setPhotoData(selectedCandidatePhotoData); // null = keep existing
            if (candidateDAO.updateCandidate(c)) { refreshCandidateTable(); clearCandidateForm(); }
        });

        delete.addActionListener(e -> {
            if (selectedCandidateId == -1) {
                JOptionPane.showMessageDialog(this, "Select a candidate from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this candidate?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && candidateDAO.deleteCandidate(selectedCandidateId)) {
                refreshCandidateTable(); clearCandidateForm();
            }
        });

        clear.addActionListener(e -> clearCandidateForm());

        panel.add(formCard, BorderLayout.NORTH);
        panel.add(UIStyle.scrollWrap(candidateTable), BorderLayout.CENTER);
        return panel;
    }

    /** Panel with preview thumbnail + Browse button for candidate photo */
    private JPanel buildPhotoUploadPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel label = UIStyle.sectionLabel("Photo");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        p.add(label);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        candidateImagePreview = makePreviewLabel();
        row.add(candidateImagePreview);

        JButton browse = UIStyle.secondaryButton("Browse…");
        browse.addActionListener(e -> {
            File f = chooseImageFile();
            if (f == null) return;
            try {
                selectedCandidatePhotoData = Files.readAllBytes(f.toPath());
                setImagePreview(candidateImagePreview, selectedCandidatePhotoData);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not read image file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        row.add(browse);
        p.add(row);
        return p;
    }

    private void clearCandidateForm() {
        selectedCandidateId = -1;
        selectedCandidatePhotoData = null;
        candidateNameField.setText("");
        candidateImagePreview.setIcon(placeholderIcon());
        candidateTable.clearSelection();
    }

    private void refreshCandidateTable() {
        candidateModel.setRowCount(0);
        for (Candidate c : candidateDAO.getAllCandidates()) {
            candidateModel.addRow(new Object[]{
                c.getCandidateId(), c.getCandidateName(),
                c.getPartyName(), c.getDistrictName(),
                c.getPhotoData() != null ? "✔ Yes" : "—"
            });
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    /** Open a file chooser that only accepts common image types */
    private File chooseImageFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose Image");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Image files (JPG, PNG, GIF, BMP)", "jpg", "jpeg", "png", "gif", "bmp"));
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showOpenDialog(this);
        return result == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile() : null;
    }

    /** Scale BLOB bytes into the preview JLabel */
    private void setImagePreview(JLabel label, byte[] data) {
        if (data == null || data.length == 0) {
            label.setIcon(placeholderIcon());
            return;
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            if (img == null) { label.setIcon(placeholderIcon()); return; }
            Image scaled = img.getScaledInstance(PREVIEW_W, PREVIEW_H, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
        } catch (IOException ex) {
            label.setIcon(placeholderIcon());
        }
    }

    /** A fixed-size label used as image thumbnail box */
    private JLabel makePreviewLabel() {
        JLabel lbl = new JLabel(placeholderIcon());
        lbl.setPreferredSize(new Dimension(PREVIEW_W, PREVIEW_H));
        lbl.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    /** Grey placeholder shown when no image is loaded */
    private ImageIcon placeholderIcon() {
        BufferedImage img = new BufferedImage(PREVIEW_W, PREVIEW_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(230, 230, 230));
        g.fillRect(0, 0, PREVIEW_W, PREVIEW_H);
        g.setColor(new Color(170, 170, 170));
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        FontMetrics fm = g.getFontMetrics();
        String txt = "No Image";
        g.drawString(txt, (PREVIEW_W - fm.stringWidth(txt)) / 2, PREVIEW_H / 2 + fm.getAscent() / 2);
        g.dispose();
        return new ImageIcon(img);
    }

    private void refreshPartyComboBoxIfExists() {
        if (partyBox == null) return;
        Object selected = partyBox.getSelectedItem();
        partyBox.removeAllItems();
        for (Party p : partyDAO.getAllParties()) partyBox.addItem(p);
        if (selected != null) partyBox.setSelectedItem(selected);
    }

    private JComponent labeledField(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = UIStyle.sectionLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(l);
        p.add(field);
        return p;
    }
}
