package ui;

import dao.BallotDAO;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Live Result Tallying Page.
 * Draws a Bar Chart (votes by district) and a Pie Chart (votes by party)
 * using plain Java2D so the project runs without needing the JFreeChart
 * jar on the classpath. If you have jfreechart-1.5.x + jcommon on your
 * classpath, you can swap ChartPanel components in here instead -
 * see the README for instructions.
 */
public class LiveResultPage extends JFrame {

    public LiveResultPage() {
        setTitle("Live Election Results");
        setSize(960, 600);
        setMinimumSize(new Dimension(760, 480));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Map<String, Integer> byDistrict = new BallotDAO().getVotesByDistrict();
        Map<String, Integer> byParty = new BallotDAO().getVotesByParty();
        int total = new BallotDAO().getTotalVotes();

        JPanel root = UIStyle.pageBackground(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = UIStyle.heading("Live Election Results", 22);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIStyle.muted("Real-time tally across all districts and parties.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(sub);
        header.add(titleBlock, BorderLayout.WEST);

        JButton refresh = UIStyle.secondaryButton("Refresh");
        refresh.setIcon(UIStyle.icon(UIStyle.IconType.REFRESH, 14, UIStyle.PRIMARY));
        refresh.setHorizontalTextPosition(SwingConstants.RIGHT);
        refresh.setIconTextGap(8);
        JPanel refreshWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        refreshWrap.setOpaque(false);
        refreshWrap.add(refresh);
        header.add(refreshWrap, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        root.add(header, BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(0, 18));
        middle.setOpaque(false);

        JPanel statsRow = new JPanel(new GridLayout(1, 1));
        statsRow.setOpaque(false);
        statsRow.add(UIStyle.statCard("Total Votes Cast", String.valueOf(total), UIStyle.PRIMARY));
        middle.add(statsRow, BorderLayout.NORTH);

        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 18, 0));
        chartsPanel.setOpaque(false);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        UIStyle.RoundedPanel barCard = UIStyle.card(new BorderLayout());
        barCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        barCard.add(cardTitle("Votes by District"), BorderLayout.NORTH);
        barCard.add(new BarChartPanel(byDistrict), BorderLayout.CENTER);

        UIStyle.RoundedPanel pieCard = UIStyle.card(new BorderLayout());
        pieCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        pieCard.add(cardTitle("Votes by Party"), BorderLayout.NORTH);
        pieCard.add(new PieChartPanel(byParty), BorderLayout.CENTER);

        chartsPanel.add(barCard);
        chartsPanel.add(pieCard);
        middle.add(chartsPanel, BorderLayout.CENTER);

        root.add(middle, BorderLayout.CENTER);
        add(root);

        refresh.addActionListener(e -> {
            dispose();
            new LiveResultPage().setVisible(true);
        });
    }

    private JComponent cardTitle(String text) {
        JLabel l = UIStyle.heading(text, 15);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return l;
    }

    /** Simple custom bar chart panel, styled with the app's color palette. */
    static class BarChartPanel extends JPanel {
        private final Map<String, Integer> data;
        private static final Color[] PALETTE = {
                UIStyle.PRIMARY, UIStyle.ACCENT, new Color(0xF5, 0x9E, 0x0B),
                new Color(0xEC, 0x48, 0x99), new Color(0x84, 0x5E, 0xF7)
        };

        BarChartPanel(Map<String, Integer> data) {
            this.data = data;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(UIStyle.bodySmall());

            int width = getWidth();
            int height = getHeight();
            int padding = 46;
            int n = data.size();
            if (n == 0) {
                g2.setColor(UIStyle.TEXT_SECONDARY);
                g2.drawString("No data yet", width / 2 - 30, height / 2);
                g2.dispose();
                return;
            }

            int max = data.values().stream().max(Integer::compareTo).orElse(1);
            if (max == 0) max = 1;

            int barWidth = (width - 2 * padding) / n;
            int x = padding;
            int chartBottom = height - padding;

            int colorIndex = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int barHeight = (int) ((entry.getValue() / (double) max) * (height - 2 * padding));
                g2.setColor(PALETTE[colorIndex % PALETTE.length]);
                g2.fillRoundRect(x + 6, chartBottom - barHeight, Math.max(barWidth - 24, 8), barHeight, 6, 6);

                g2.setColor(UIStyle.TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                String label = entry.getKey();
                int labelWidth = fm.stringWidth(label);
                g2.drawString(label, x + (barWidth - labelWidth) / 2, chartBottom + 16);

                g2.setColor(UIStyle.TEXT_PRIMARY);
                String valueStr = String.valueOf(entry.getValue());
                int valueWidth = fm.stringWidth(valueStr);
                g2.drawString(valueStr, x + (barWidth - valueWidth) / 2, chartBottom - barHeight - 6);

                x += barWidth;
                colorIndex++;
            }
            g2.setColor(UIStyle.BORDER);
            g2.drawLine(padding, chartBottom, width - padding / 2, chartBottom);
            g2.dispose();
        }
    }

    /** Simple custom pie chart panel, styled with the app's color palette. */
    static class PieChartPanel extends JPanel {
        private final Map<String, Integer> data;
        private static final Color[] PALETTE = {
                UIStyle.PRIMARY, UIStyle.ACCENT, new Color(0xF5, 0x9E, 0x0B),
                new Color(0xEC, 0x48, 0x99), new Color(0x84, 0x5E, 0xF7)
        };

        PieChartPanel(Map<String, Integer> data) {
            this.data = data;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(UIStyle.bodySmall());

            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            if (total == 0) {
                g2.setColor(UIStyle.TEXT_SECONDARY);
                g2.drawString("No data yet", getWidth() / 2 - 30, getHeight() / 2);
                g2.dispose();
                return;
            }

            int diameter = Math.min(getWidth() - 40, getHeight() / 2);
            int x = (getWidth() - diameter) / 2;
            int y = 10;

            int colorIndex = 0;
            double startAngle = 0;

            int legendY = y + diameter + 24;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                double angle = (entry.getValue() / (double) total) * 360.0;
                g2.setColor(PALETTE[colorIndex % PALETTE.length]);
                g2.fillArc(x, y, diameter, diameter, (int) startAngle, (int) Math.round(angle));
                startAngle += angle;

                g2.setColor(PALETTE[colorIndex % PALETTE.length]);
                g2.fillRoundRect(20, legendY, 12, 12, 3, 3);
                g2.setColor(UIStyle.TEXT_PRIMARY);
                double pct = (entry.getValue() / (double) total) * 100;
                g2.drawString(String.format("%s: %d (%.1f%%)", entry.getKey(), entry.getValue(), pct), 40, legendY + 11);
                legendY += 20;
                colorIndex++;
            }
            g2.dispose();
        }
    }
}
