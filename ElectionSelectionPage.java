package ui;

import dao.ElectionDAO;
import model.Election;
import model.VoterUser;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Shown right after a voter chooses to vote: lets them pick WHICH election
 * they want to cast a ballot in (in case more than one election is open)
 * before moving on to the candidate-selection screen (VotingPage).
 */
public class ElectionSelectionPage extends JFrame {

    public ElectionSelectionPage(VoterUser voter) {
        setTitle("Select Election");
        setSize(560, 460);
        setMinimumSize(new Dimension(440, 360));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = UIStyle.pageBackground(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UIStyle.heading("Select an Election", 22);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIStyle.muted("Choose which election you want to cast your vote in.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        root.add(header, BorderLayout.NORTH);

        // Only elections that are currently Active can be voted in.
        List<Election> elections = new ElectionDAO().getAllElections();
        elections.removeIf(el -> !"Active".equalsIgnoreCase(el.getStatus()));

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (elections.isEmpty()) {
            UIStyle.RoundedPanel empty = UIStyle.card(new BorderLayout());
            empty.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
            empty.add(UIStyle.muted("There is no active election to vote in right now."), BorderLayout.CENTER);
            list.add(empty);
        } else {
            for (Election election : elections) {
                list.add(electionCard(voter, election));
                list.add(Box.createVerticalStrut(14));
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIStyle.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(scroll, BorderLayout.CENTER);

        add(root);
    }

    private JPanel electionCard(VoterUser voter, Election election) {
        UIStyle.RoundedPanel card = UIStyle.card(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setPreferredSize(new Dimension(10, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel name = UIStyle.heading(election.getElectionTitle(), 16);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel date = UIStyle.muted(
                election.getElectionDate() != null ? "Date: " + election.getElectionDate().toString() : "");
        date.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(name);
        text.add(Box.createVerticalStrut(4));
        text.add(date);

        JButton chooseBtn = UIStyle.primaryButton("Vote in this Election");
        chooseBtn.setPreferredSize(new Dimension(190, 40));
        chooseBtn.addActionListener(e -> {
            dispose();
            new VotingPage(voter, election).setVisible(true);
        });

        JPanel btnWrap = new JPanel(new GridBagLayout());
        btnWrap.setOpaque(false);
        btnWrap.add(chooseBtn);

        card.add(text, BorderLayout.CENTER);
        card.add(btnWrap, BorderLayout.EAST);
        return card;
    }
}