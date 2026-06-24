package ui;

import dao.PartyDAO;
import model.Party;
import util.UIStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Read-only list of parties — available to voters so they can see who is contesting. */
public class PartyListPage extends JFrame {

    public PartyListPage() {
        setTitle("Parties Contesting");
        setSize(700, 520);
        setMinimumSize(new Dimension(520, 400));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = UIStyle.pageBackground(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UIStyle.heading("Parties Contesting", 22);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIStyle.muted("All political parties registered for this election.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Party Name", "Short Name", "Leader"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Party p : new PartyDAO().getAllParties()) {
            model.addRow(new Object[]{p.getPartyName(), p.getShortName(), p.getLeaderName()});
        }
        JTable table = new JTable(model);
        UIStyle.styleTable(table);

        root.add(UIStyle.scrollWrap(table), BorderLayout.CENTER);
        add(root);
    }
}
