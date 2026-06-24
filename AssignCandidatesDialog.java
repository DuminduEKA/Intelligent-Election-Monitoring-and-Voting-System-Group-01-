package ui;

import dao.CandidateDAO;
import dao.ElectionCandidateDAO;
import model.Candidate;
import model.Election;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Lets the admin pick exactly which candidates are contesting a given
 * election. A candidate row can be reused across many elections, but only
 * the ones checked here will show up on that election's ballot.
 */
public class AssignCandidatesDialog extends JDialog {

    public AssignCandidatesDialog(JFrame parent, Election election) {
        super(parent, "Assign Candidates", true);
        setSize(560, 600);
        setMinimumSize(new Dimension(440, 420));
        setLocationRelativeTo(parent);

        ElectionCandidateDAO ecDAO = new ElectionCandidateDAO();
        List<Candidate> allCandidates = new CandidateDAO().getAllCandidates();
        Set<Integer> assigned = ecDAO.getAssignedCandidateIds(election.getElectionId());

        JPanel root = UIStyle.pageBackground(new BorderLayout(0, 14));
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UIStyle.heading("Assign Candidates", 18);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIStyle.muted("Election: " + election.getElectionTitle()
                + " — tick everyone contesting in this election.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        List<JCheckBox> checkBoxes = new ArrayList<>();

        if (allCandidates.isEmpty()) {
            listPanel.add(UIStyle.muted("No candidates exist yet. Add some from \"Parties & Candidates\" first."));
        } else {
            for (Candidate c : allCandidates) {
                String label = c.getCandidateName()
                        + " — " + (c.getPartyName() != null ? c.getPartyName() : "Independent")
                        + " (" + c.getDistrictName() + ")";
                JCheckBox cb = new JCheckBox(label, assigned.contains(c.getCandidateId()));
                cb.setOpaque(false);
                cb.setFont(UIStyle.body());
                cb.setAlignmentX(Component.LEFT_ALIGNMENT);
                cb.putClientProperty("candidateId", c.getCandidateId());
                checkBoxes.add(cb);
                listPanel.add(cb);
                listPanel.add(Box.createVerticalStrut(6));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIStyle.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton save = UIStyle.primaryButton("Save");
        JButton cancel = UIStyle.ghostButton("Cancel");
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);

        save.addActionListener(e -> {
            List<Integer> selectedIds = new ArrayList<>();
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    selectedIds.add((Integer) cb.getClientProperty("candidateId"));
                }
            }
            if (ecDAO.setCandidatesForElection(election.getElectionId(), selectedIds)) {
                JOptionPane.showMessageDialog(this,
                        "Candidate list saved for this election.",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        cancel.addActionListener(e -> dispose());

        setContentPane(root);
    }
}
