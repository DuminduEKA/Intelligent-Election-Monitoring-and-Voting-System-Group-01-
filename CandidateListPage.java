package ui;

import dao.CandidateDAO;
import model.Candidate;
import util.UIStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Read-only list of candidates — available to voters so they can see who is contesting. */
public class CandidateListPage extends JFrame {

    public CandidateListPage() {
        setTitle("Candidates Contesting");
        setSize(760, 520);
        setMinimumSize(new Dimension(560, 400));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = UIStyle.pageBackground(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UIStyle.heading("Candidates Contesting", 22);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIStyle.muted("All candidates registered for this election.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Candidate Name", "Party", "District"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Candidate c : new CandidateDAO().getAllCandidates()) {
            model.addRow(new Object[]{
                    c.getCandidateName(),
                    c.getPartyName() != null ? c.getPartyName() : "Independent",
                    c.getDistrictName()
            });
        }
        JTable table = new JTable(model);
        UIStyle.styleTable(table);

        root.add(UIStyle.scrollWrap(table), BorderLayout.CENTER);
        add(root);
    }
}
