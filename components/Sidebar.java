package ui.components;

import util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Dark sidebar navigation panel used by AdminDashboard / VoterDashboardPage
 * to give a "web app" shell feel instead of a plain stack of JButtons.
 */
public class Sidebar extends JPanel {

    private final JPanel itemsPanel;
    private final List<NavItem> navItems = new ArrayList<>();
    private NavItem activeItem;

    public Sidebar(String appName, String userLabel, String roleLabel) {
        setLayout(new BorderLayout());
        setBackground(UIStyle.SIDEBAR_BG);
        setPreferredSize(new Dimension(230, 0));

        // ---- Header / brand ----
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(26, 22, 22, 22));

        JLabel brandIcon = new JLabel(UIStyle.icon(UIStyle.IconType.BRAND, 20, Color.WHITE));
        JLabel logo = new JLabel(appName);
        logo.setFont(new Font(UIStyle.h2().getFamily(), Font.BOLD, 17));
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        header.add(brandIcon);
        header.add(logo);
        header.add(Box.createHorizontalGlue());

        add(header, BorderLayout.NORTH);

        // ---- Nav items ----
        itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // ---- Footer / user info ----
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 30)),
                BorderFactory.createEmptyBorder(14, 22, 18, 22)));

        JLabel user = new JLabel(userLabel);
        user.setFont(UIStyle.h3());
        user.setForeground(Color.WHITE);
        user.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel role = new JLabel(roleLabel);
        role.setFont(UIStyle.bodySmall());
        role.setForeground(UIStyle.SIDEBAR_TEXT);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);

        footer.add(user);
        footer.add(Box.createVerticalStrut(2));
        footer.add(role);

        add(footer, BorderLayout.SOUTH);
    }

    /** Adds a clickable nav item. Returns this for chaining. */
    public Sidebar addItem(UIStyle.IconType icon, String text, Runnable onClick) {
        NavItem item = new NavItem(icon, text, onClick);
        navItems.add(item);
        itemsPanel.add(item);
        itemsPanel.add(Box.createVerticalStrut(2));
        return this;
    }

    public Sidebar addSpacer() {
        itemsPanel.add(Box.createVerticalStrut(16));
        return this;
    }

    /** Adds a footer-pinned action (e.g. Logout) below the main nav list, styled distinctly. */
    public Sidebar addBottomAction(UIStyle.IconType icon, String text, Runnable onClick) {
        NavItem item = new NavItem(icon, text, onClick);
        item.setDanger(true);
        navItems.add(item);
        itemsPanel.add(Box.createVerticalGlue());
        itemsPanel.add(item);
        return this;
    }

    /** A single nav row: icon + label, with hover & active-state styling. */
    private class NavItem extends JPanel {
        private final JLabel iconLabel;
        private final JLabel textLabel;
        private final UIStyle.IconType iconType;
        private boolean active = false;
        private boolean hovering = false;
        private boolean danger = false;

        NavItem(UIStyle.IconType iconType, String text, Runnable onClick) {
            this.iconType = iconType;
            setLayout(new BorderLayout(10, 0));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(200, 42));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            iconLabel = new JLabel(UIStyle.icon(iconType, 16, UIStyle.SIDEBAR_TEXT));
            textLabel = new JLabel(text);
            textLabel.setFont(UIStyle.body());
            textLabel.setForeground(UIStyle.SIDEBAR_TEXT);

            JPanel rowWrap = new JPanel(new BorderLayout(10, 0));
            rowWrap.setOpaque(false);
            rowWrap.add(iconLabel, BorderLayout.WEST);
            rowWrap.add(textLabel, BorderLayout.CENTER);
            add(rowWrap, BorderLayout.WEST);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) {
                    setActiveItem(NavItem.this);
                    onClick.run();
                }
            });
        }

        void setDanger(boolean d) {
            this.danger = d;
            applyColors();
        }

        private void applyColors() {
            Color c;
            if (active) {
                c = Color.WHITE;
            } else if (hovering) {
                c = danger ? new Color(0xFF, 0xB0, 0xB0) : Color.WHITE;
            } else {
                c = danger ? new Color(0xFF, 0xA0, 0xA0) : UIStyle.SIDEBAR_TEXT;
            }
            textLabel.setForeground(c);
            iconLabel.setIcon(UIStyle.icon(iconType, 16, c));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(UIStyle.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (hovering) {
                g2.setColor(UIStyle.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g2.dispose();
            applyColors();
            super.paintComponent(g);
        }
    }

    private void setActiveItem(NavItem item) {
        if (activeItem != null) activeItem.active = false;
        activeItem = item;
        item.active = true;
        for (NavItem n : navItems) n.repaint();
    }
}
