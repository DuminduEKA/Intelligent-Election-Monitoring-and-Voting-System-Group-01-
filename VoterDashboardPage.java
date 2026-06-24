package ui;

import dao.DistrictDAO;
import model.District;
import model.VoterUser;
import ui.components.Sidebar;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;

public class VoterDashboardPage extends JFrame {

    public VoterDashboardPage(VoterUser voter) {
        setTitle("Voter Dashboard - " + voter.getName());
        setSize(820, 560);
        setMinimumSize(new Dimension(680, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        Sidebar sidebar = new Sidebar("VoteSys", voter.getName(), "Registered Voter");
        sidebar.addItem(UIStyle.IconType.DASHBOARD, "Dashboard", () -> { /* already here */ });
        sidebar.addItem(UIStyle.IconType.BALLOT, "Parties", () -> new PartyListPage().setVisible(true));
        sidebar.addItem(UIStyle.IconType.CHECK, "Candidates", () -> new CandidateListPage().setVisible(true));
        sidebar.addItem(UIStyle.IconType.RESULTS, "Live Results", () -> new LiveResultPage().setVisible(true));
        sidebar.addBottomAction(UIStyle.IconType.LOGOUT, "Logout", () -> {
            dispose();
            new LoginPage().setVisible(true);
        });
        root.add(sidebar, BorderLayout.WEST);

        JPanel content = UIStyle.pageBackground(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(34, 36, 34, 36));

        JLabel welcome = UIStyle.heading("Welcome, " + voter.getName(), 24);
        JLabel sub = UIStyle.muted("Your voter profile and ballot status.");
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(welcome);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        content.add(header, BorderLayout.NORTH);

        String districtName = "Unknown";
        for (District d : new DistrictDAO().getAllDistricts()) {
            if (d.getDistrictId() == voter.getDistrictId()) {
                districtName = d.getDistrictName();
                break;
            }
        }

        JPanel middle = new JPanel(new BorderLayout());
        middle.setOpaque(false);
        middle.setBorder(BorderFactory.createEmptyBorder(28, 0, 0, 0));

        // ---- Profile card ----
        UIStyle.RoundedPanel profileCard = UIStyle.card(new GridLayout(1, 3, 0, 0));
        profileCard.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        profileCard.add(infoBlock("NIC Number", voter.getNic()));
        profileCard.add(infoBlock("District", districtName));
        profileCard.add(statusBlock(voter.hasVoted()));

        // ---- Vote CTA card ----
        UIStyle.RoundedPanel voteCard = UIStyle.card(new BorderLayout(0, 14));
        voteCard.setBorder(BorderFactory.createEmptyBorder(28, 26, 28, 26));

        JLabel ctaTitle = UIStyle.heading(
                voter.hasVoted() ? "You've already voted" : "Ready to cast your vote?", 18);
        JLabel ctaDesc = UIStyle.muted(
                voter.hasVoted()
                        ? "Thank you for participating. Each voter may only vote once per election."
                        : "Your ballot is private and can only be submitted once. Take your time.");

        JButton voteBtn = UIStyle.primaryButton(voter.hasVoted() ? "Vote Already Cast" : "Proceed to Vote");
        if (!voter.hasVoted()) {
            voteBtn.setIcon(UIStyle.icon(UIStyle.IconType.ARROW_RIGHT, 14, Color.WHITE));
            voteBtn.setHorizontalTextPosition(SwingConstants.LEFT);
            voteBtn.setIconTextGap(8);
        }
        voteBtn.setEnabled(!voter.hasVoted());
        voteBtn.setPreferredSize(new Dimension(220, 46));
        voteBtn.addActionListener(e -> new ElectionSelectionPage(voter).setVisible(true));

        JPanel ctaText = new JPanel();
        ctaText.setOpaque(false);
        ctaText.setLayout(new BoxLayout(ctaText, BoxLayout.Y_AXIS));
        ctaTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        ctaDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        ctaText.add(ctaTitle);
        ctaText.add(Box.createVerticalStrut(6));
        ctaText.add(ctaDesc);

        JPanel ctaBtnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ctaBtnWrap.setOpaque(false);
        ctaBtnWrap.add(voteBtn);

        voteCard.add(ctaText, BorderLayout.NORTH);
        voteCard.add(ctaBtnWrap, BorderLayout.SOUTH);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(profileCard);
        stack.add(Box.createVerticalStrut(18));
        stack.add(voteCard);

        middle.add(stack, BorderLayout.NORTH);
        content.add(middle, BorderLayout.CENTER);

        root.add(content, BorderLayout.CENTER);
        add(root);
    }

    private JComponent infoBlock(String label, String value) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = UIStyle.muted(label);
        JLabel v = UIStyle.heading(value, 16);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(6));
        p.add(v);
        return p;
    }

    private JComponent statusBlock(boolean hasVoted) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = UIStyle.muted("Voting Status");
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badge = new JLabel(hasVoted ? "  Already Voted  " : "  Not Voted Yet  ");
        badge.setOpaque(true);
        badge.setFont(UIStyle.h3());
        badge.setForeground(hasVoted ? UIStyle.SUCCESS : UIStyle.WARNING);
        badge.setBackground(hasVoted ? new Color(0xE7, 0xF8, 0xEC) : new Color(0xFD, 0xF3, 0xDE));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));

        p.add(l);
        p.add(Box.createVerticalStrut(8));
        p.add(badge);
        return p;
    }
}
