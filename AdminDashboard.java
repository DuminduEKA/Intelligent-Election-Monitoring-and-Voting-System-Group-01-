package ui;

import dao.BallotDAO;
import model.AdminUser;
import ui.components.Sidebar;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard(AdminUser admin) {
        setTitle("Admin Dashboard - " + admin.getName());
        setSize(980, 640);
        setMinimumSize(new Dimension(820, 540));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        // ---- Sidebar ----
        Sidebar sidebar = new Sidebar("VoteSys", admin.getName(), "Administrator");
        sidebar.addItem(UIStyle.IconType.DASHBOARD, "Dashboard", () -> {});
        sidebar.addItem(UIStyle.IconType.CALENDAR, "Elections", () -> new ElectionManagementPage().setVisible(true));
        sidebar.addItem(UIStyle.IconType.BALLOT, "Parties & Candidates", () -> new EntityManagementPage().setVisible(true));
        sidebar.addItem(UIStyle.IconType.RESULTS, "Live Results", () -> new LiveResultPage().setVisible(true));
        sidebar.addBottomAction(UIStyle.IconType.LOGOUT, "Logout", () -> {
            dispose();
            new LoginPage().setVisible(true);
        });
        root.add(sidebar, BorderLayout.WEST);

        // ---- Main content ----
        JPanel content = UIStyle.pageBackground(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(34, 36, 34, 36));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel welcome = UIStyle.heading("Welcome back, " + admin.getName(), 24);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIStyle.muted("Here's what's happening with the election right now.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(welcome);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        content.add(header, BorderLayout.NORTH);

        int totalVotes = new BallotDAO().getTotalVotes();
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 18, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));
        statsRow.add(UIStyle.statCard("Total Votes Cast", String.valueOf(totalVotes), UIStyle.PRIMARY));
        statsRow.add(UIStyle.statCard("Your Role", "Administrator", UIStyle.ACCENT));
        statsRow.add(UIStyle.statCard("System Status", "Active", UIStyle.SUCCESS));
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.add(statsRow);

        JComponent quickActions = buildQuickActions();
        quickActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        middle.add(quickActions);
        middle.add(Box.createVerticalGlue());

        content.add(middle, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        add(root);
    }

    private JComponent buildQuickActions() {
        JLabel label = UIStyle.heading("Quick actions", 16);

        // 3 cards now — Elections, Parties & Candidates, Live Results
        JPanel grid = new JPanel(new GridLayout(1, 3, 18, 0));
        grid.setOpaque(false);
        grid.add(actionCard(UIStyle.IconType.CALENDAR, "Manage Elections",
                "Create a new election and choose which one is active for voting.",
                () -> new ElectionManagementPage().setVisible(true)));
        grid.add(actionCard(UIStyle.IconType.BALLOT, "Manage Parties & Candidates",
                "Add, edit, or remove parties and candidates running in each district.",
                () -> new EntityManagementPage().setVisible(true)));
        grid.add(actionCard(UIStyle.IconType.RESULTS, "Live Results",
                "View real-time vote tallies by district and by party.",
                () -> new LiveResultPage().setVisible(true)));
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        grid.setPreferredSize(new Dimension(grid.getPreferredSize().width, 140));

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(label);
        wrap.add(Box.createVerticalStrut(14));
        wrap.add(grid);
        return wrap;
    }

    private JComponent actionCard(UIStyle.IconType iconType, String title, String desc, Runnable onClick) {
        UIStyle.RoundedPanel card = UIStyle.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(UIStyle.icon(iconType, 22, UIStyle.PRIMARY));
        iconLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titleLbl = UIStyle.heading(title, 14);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descLbl = new JLabel("<html><div style='width:220px'>" + desc + "</div></html>");
        descLbl.setFont(UIStyle.bodySmall());
        descLbl.setForeground(UIStyle.TEXT_SECONDARY);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(descLbl);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { onClick.run(); }
        });
        return card;
    }
}
