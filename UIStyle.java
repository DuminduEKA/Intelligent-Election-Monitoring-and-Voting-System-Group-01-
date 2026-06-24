package util;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Central UI styling utility — gives every screen in the app a consistent,
 * modern "web app" look instead of default Swing/AWT styling.
 *
 * Usage: call UIStyle.applyGlobal() once at app startup (in Main), then use
 * the factory methods (primaryButton, textField, card, etc.) when building
 * each screen instead of raw `new JButton(...)`.
 */
public final class UIStyle {

    private UIStyle() {}

    // ---------------- PALETTE ----------------
    public static final Color PRIMARY        = new Color(0x2F, 0x5C, 0xFF); // indigo-blue
    public static final Color PRIMARY_DARK   = new Color(0x1E, 0x40, 0xC9);
    public static final Color PRIMARY_LIGHT  = new Color(0xEC, 0xF1, 0xFF);
    public static final Color ACCENT         = new Color(0x14, 0xB8, 0xA6); // teal
    public static final Color DANGER         = new Color(0xE5, 0x3E, 0x3E);
    public static final Color DANGER_DARK    = new Color(0xC4, 0x2C, 0x2C);
    public static final Color SUCCESS        = new Color(0x16, 0xA3, 0x4A);
    public static final Color WARNING        = new Color(0xD9, 0x8C, 0x0E);

    public static final Color BG             = new Color(0xF4, 0xF6, 0xFB); // app background
    public static final Color SURFACE        = Color.WHITE;                 // card background
    public static final Color SIDEBAR_BG     = new Color(0x16, 0x1E, 0x3A); // dark navy sidebar
    public static final Color SIDEBAR_HOVER  = new Color(0x22, 0x2C, 0x52);
    public static final Color SIDEBAR_TEXT   = new Color(0xC9, 0xD2, 0xF0);

    public static final Color TEXT_PRIMARY   = new Color(0x1A, 0x1F, 0x36);
    public static final Color TEXT_SECONDARY = new Color(0x6B, 0x72, 0x88);
    public static final Color BORDER         = new Color(0xE2, 0xE5, 0xEE);

    // ---------------- TYPOGRAPHY ----------------
    private static final String FONT_FAMILY = pickFont();

    private static String pickFont() {
        String[] preferred = {"Segoe UI", "SF Pro Display", "Helvetica Neue", "Inter", "Arial"};
        java.util.List<String> available = java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String f : preferred) {
            if (available.contains(f)) return f;
        }
        return Font.SANS_SERIF;
    }

    public static Font display()    { return new Font(FONT_FAMILY, Font.BOLD, 26); }
    public static Font h1()         { return new Font(FONT_FAMILY, Font.BOLD, 20); }
    public static Font h2()         { return new Font(FONT_FAMILY, Font.BOLD, 16); }
    public static Font h3()         { return new Font(FONT_FAMILY, Font.BOLD, 14); }
    public static Font body()       { return new Font(FONT_FAMILY, Font.PLAIN, 14); }
    public static Font bodySmall()  { return new Font(FONT_FAMILY, Font.PLAIN, 12); }
    public static Font label()      { return new Font(FONT_FAMILY, Font.PLAIN, 13); }
    public static Font mono()       { return new Font(Font.MONOSPACED, Font.PLAIN, 13); }

    // ================================================================
    //  ICONS — drawn with Java2D so they never depend on font glyph
    //  coverage (unlike unicode symbols, which render as empty boxes
    //  on systems whose default font is missing that glyph).
    // ================================================================

    public enum IconType { DASHBOARD, BALLOT, CHECK, RESULTS, LOGOUT, REFRESH, ARROW_RIGHT, BRAND, CALENDAR }

    /** A small, crisp vector icon drawn with strokes — safe on every system. */
    public static class VectorIcon implements Icon {
        private final IconType type;
        private final int size;
        private final Color color;

        public VectorIcon(IconType type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.6f, size / 11f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float s = size;

            switch (type) {
                case DASHBOARD -> {
                    // 2x2 grid of small rounded squares
                    float gap = s * 0.12f, cell = (s - gap) / 2f - gap / 2f;
                    g2.draw(new RoundRectangle2D.Float(0, 0, cell, cell, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(cell + gap, 0, cell, cell, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(0, cell + gap, cell, cell, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(cell + gap, cell + gap, cell, cell, 2, 2));
                }
                case BALLOT -> {
                    // simple ballot box with a checkmark slot
                    g2.draw(new RoundRectangle2D.Float(s * 0.1f, s * 0.15f, s * 0.8f, s * 0.7f, 3, 3));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.28f, s * 0.5f, s * 0.45f, s * 0.65f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.45f, s * 0.65f, s * 0.75f, s * 0.32f));
                }
                case CHECK -> {
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.18f, s * 0.52f, s * 0.4f, s * 0.75f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.4f, s * 0.75f, s * 0.85f, s * 0.22f));
                }
                case RESULTS -> {
                    // simple bar chart
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.15f, s * 0.85f, s * 0.85f, s * 0.85f));
                    g2.fill(new RoundRectangle2D.Float(s * 0.18f, s * 0.55f, s * 0.16f, s * 0.28f, 1.5f, 1.5f));
                    g2.fill(new RoundRectangle2D.Float(s * 0.42f, s * 0.35f, s * 0.16f, s * 0.48f, 1.5f, 1.5f));
                    g2.fill(new RoundRectangle2D.Float(s * 0.66f, s * 0.15f, s * 0.16f, s * 0.68f, 1.5f, 1.5f));
                }
                case LOGOUT -> {
                    // door frame + arrow out
                    g2.draw(new RoundRectangle2D.Float(s * 0.12f, s * 0.12f, s * 0.42f, s * 0.76f, 3, 3));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.38f, s * 0.5f, s * 0.88f, s * 0.5f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.65f, s * 0.3f, s * 0.88f, s * 0.5f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.65f, s * 0.7f, s * 0.88f, s * 0.5f));
                }
                case REFRESH -> {
                    g2.draw(new java.awt.geom.Arc2D.Float(s * 0.12f, s * 0.12f, s * 0.76f, s * 0.76f, 40, 280, java.awt.geom.Arc2D.OPEN));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.78f, s * 0.12f, s * 0.88f, s * 0.3f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.6f, s * 0.18f, s * 0.88f, s * 0.3f));
                }
                case ARROW_RIGHT -> {
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.15f, s * 0.5f, s * 0.78f, s * 0.5f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.55f, s * 0.28f, s * 0.82f, s * 0.5f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.55f, s * 0.72f, s * 0.82f, s * 0.5f));
                }
                case BRAND -> {
                    // hexagon mark
                    java.awt.geom.Path2D.Float hex = new java.awt.geom.Path2D.Float();
                    for (int i = 0; i < 6; i++) {
                        double ang = Math.PI / 3 * i - Math.PI / 2;
                        float px = (float) (s / 2 + s * 0.42 * Math.cos(ang));
                        float py = (float) (s / 2 + s * 0.42 * Math.sin(ang));
                        if (i == 0) hex.moveTo(px, py); else hex.lineTo(px, py);
                    }
                    hex.closePath();
                    g2.fill(hex);
                }
                case CALENDAR -> {
                    // simple calendar: outer rect, header divider, two hangers
                    g2.draw(new RoundRectangle2D.Float(s * 0.1f, s * 0.18f, s * 0.8f, s * 0.72f, 3, 3));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.1f, s * 0.38f, s * 0.9f, s * 0.38f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.3f, s * 0.08f, s * 0.3f, s * 0.26f));
                    g2.draw(new java.awt.geom.Line2D.Float(s * 0.7f, s * 0.08f, s * 0.7f, s * 0.26f));
                }
            }
            g2.dispose();
        }
    }

    public static Icon icon(IconType type, int size, Color color) {
        return new VectorIcon(type, size, color);
    }

    // ================================================================
    //  GLOBAL LOOK & FEEL
    // ================================================================

    /** Call once at app startup. Sets sane global defaults via UIManager. */
    public static void applyGlobal() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { /* fall back to default */ }

        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageFont", body());
        UIManager.put("OptionPane.buttonFont", body());
        UIManager.put("ToolTip.font", bodySmall());

        // JOptionPane button styling fallback (best-effort; varies per L&F)
        UIManager.put("Button.font", body());
        UIManager.put("Button.background", SURFACE);
    }

    // ================================================================
    //  ROOT CONTAINER HELPERS
    // ================================================================

    /** A plain background panel matching the app's off-white canvas. */
    public static JPanel pageBackground(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BG);
        return p;
    }

    // ================================================================
    //  BUTTONS
    // ================================================================

    public static JButton primaryButton(String text) {
        return buildButton(text, PRIMARY, PRIMARY_DARK, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        RoundedButton b = buildButton(text, SURFACE, PRIMARY_LIGHT, PRIMARY);
        b.setBorderColor(BORDER);
        return b;
    }

    public static JButton dangerButton(String text) {
        return buildButton(text, DANGER, DANGER_DARK, Color.WHITE);
    }

    public static JButton ghostButton(String text) {
        RoundedButton b = buildButton(text, BG, PRIMARY_LIGHT, TEXT_SECONDARY);
        b.setBorderColor(BORDER);
        return b;
    }

    private static RoundedButton buildButton(String text, Color bg, Color hover, Color fg) {
        RoundedButton b = new RoundedButton(text);
        b.setBackground(bg);
        b.setHoverBackground(hover);
        b.setForeground(fg);
        b.setFont(h3());
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(10, 22, 10, 22));
        return b;
    }

    /** Rounded-rect button with smooth hover feedback, no default Swing chrome. */
    public static class RoundedButton extends JButton {
        private Color hoverBackground;
        private Color borderColor = null;
        private boolean hovering = false;
        private static final int ARC = 10;

        public RoundedButton(String text) {
            super(text);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovering = false; repaint(); }
            });
        }

        public void setHoverBackground(Color c) { this.hoverBackground = c; }
        public void setBorderColor(Color c) { this.borderColor = c; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = (hovering && isEnabled() && hoverBackground != null) ? hoverBackground : getBackground();
            g2.setColor(isEnabled() ? base : new Color(0xE5, 0xE7, 0xEE));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================================================================
    //  TEXT FIELDS / COMBO BOXES
    // ================================================================

    public static JTextField textField() {
        JTextField f = new RoundedTextField();
        styleField(f);
        return f;
    }

    public static JTextField textField(int columns) {
        JTextField f = new RoundedTextField();
        f.setColumns(columns);
        styleField(f);
        return f;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField f = new RoundedPasswordField();
        f.setColumns(columns);
        styleField(f);
        return f;
    }

    private static void styleField(JTextField f) {
        f.setFont(body());
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(SURFACE);
        f.setOpaque(false); // the RoundedTextField paints its own rounded background
        f.setBorder(new RoundedLineBorder(BORDER, 8, new Insets(9, 12, 9, 12)));
        f.setCaretColor(PRIMARY);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(new RoundedLineBorder(PRIMARY, 8, new Insets(9, 12, 9, 12)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(new RoundedLineBorder(BORDER, 8, new Insets(9, 12, 9, 12)));
            }
        });
    }

    /**
     * JTextField that paints its own rounded-rect background BEFORE the
     * text is drawn (via super.paintComponent), so the text always renders
     * on top and is never hidden. The border (added separately) only draws
     * the outline stroke.
     */
    public static class RoundedTextField extends JTextField {
        private static final int ARC = 8;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g); // draws the actual text/caret/selection on top
        }
    }

    /** Same as RoundedTextField but for password input. */
    public static class RoundedPasswordField extends JPasswordField {
        private static final int ARC = 8;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static <T> JComboBox<T> comboBox() {
        JComboBox<T> box = new JComboBox<>();
        styleCombo(box);
        return box;
    }

    public static <T> JComboBox<T> comboBox(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        styleCombo(box);
        return box;
    }

    private static void styleCombo(JComboBox<?> box) {
        box.setFont(body());
        box.setBackground(SURFACE);
        box.setForeground(TEXT_PRIMARY);
        box.setBorder(new RoundedLineBorder(BORDER, 8, new Insets(4, 8, 4, 8)));
        box.setFocusable(true);
    }

    /** Rounded border that also reserves interior padding (used for fields). */
    public static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final Insets insets;

        public RoundedLineBorder(Color color, int radius, Insets insets) {
            this.color = color;
            this.radius = radius;
            this.insets = insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            // IMPORTANT: never fill here. Swing paints the component's own
            // background + text BEFORE the border is painted, so filling in
            // paintBorder() draws over (and hides) whatever text the field
            // just rendered. The border's only job is the outline stroke.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return insets; }
        @Override public Insets getBorderInsets(Component c, Insets i) {
            i.set(insets.top, insets.left, insets.bottom, insets.right);
            return i;
        }
        @Override public boolean isBorderOpaque() { return false; }
    }

    // ================================================================
    //  LABELS
    // ================================================================

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(label());
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    public static JLabel heading(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT_FAMILY, Font.BOLD, size));
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(bodySmall());
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    // ================================================================
    //  CARD PANEL (rounded white surface with subtle shadow)
    // ================================================================

    public static RoundedPanel card() {
        return new RoundedPanel(16, SURFACE, BORDER);
    }

    public static RoundedPanel card(LayoutManager layout) {
        RoundedPanel p = new RoundedPanel(16, SURFACE, BORDER);
        p.setLayout(layout);
        return p;
    }

    /** Rounded rectangle panel with soft drop shadow, used as a "card" surface. */
    public static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color border;
        private boolean shadow = true;

        public RoundedPanel(int arc, Color fill, Color border) {
            this.arc = arc;
            this.fill = fill;
            this.border = border;
            setOpaque(false);
        }

        public void setShadowEnabled(boolean enabled) { this.shadow = enabled; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int pad = shadow ? 4 : 0;

            if (shadow) {
                for (int i = 4; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 6));
                    g2.fill(new RoundRectangle2D.Float(pad - i, pad - i + 2, w - 2 * pad + 2 * i, h - 2 * pad + 2 * i, arc, arc));
                }
            }
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(pad, pad, w - 2 * pad, h - 2 * pad, arc, arc));
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(pad, pad, w - 2 * pad - 1, h - 2 * pad - 1, arc, arc));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================================================================
    //  STAT / METRIC CARD
    // ================================================================

    public static JPanel statCard(String label, String value, Color accent) {
        RoundedPanel card = card(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(4, 10));

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));
        JLabel valueLbl = heading(value, 24);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelLbl = muted(label);
        labelLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        textWrap.add(valueLbl);
        textWrap.add(Box.createVerticalStrut(4));
        textWrap.add(labelLbl);

        JPanel wrapper = new JPanel(new BorderLayout(14, 0));
        wrapper.setOpaque(false);
        JPanel stripeWrap = new JPanel(new BorderLayout());
        stripeWrap.setOpaque(false);
        stripeWrap.add(roundedStripe(accent), BorderLayout.CENTER);
        wrapper.add(stripeWrap, BorderLayout.WEST);
        wrapper.add(textWrap, BorderLayout.CENTER);

        card.add(wrapper, BorderLayout.CENTER);
        return card;
    }

    private static JComponent roundedStripe(Color color) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(5, 40));
        return p;
    }

    // ================================================================
    //  TABLE STYLING
    // ================================================================

    public static void styleTable(JTable table) {
        table.setFont(body());
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(h3());
        header.setBackground(BG);
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        table.setBorder(BorderFactory.createEmptyBorder());
        table.setDefaultRenderer(Object.class, new StripedRenderer());
    }

    private static class StripedRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? SURFACE : new Color(0xFA, 0xFB, 0xFD));
                c.setForeground(TEXT_PRIMARY);
            }
            return c;
        }
    }

    public static JScrollPane scrollWrap(JComponent comp) {
        JScrollPane sp = new JScrollPane(comp);
        sp.setBorder(new RoundedLineBorder(BORDER, 10, new Insets(0, 0, 0, 0)));
        sp.getViewport().setBackground(SURFACE);
        return sp;
    }

    // ================================================================
    //  TABBED PANE STYLING
    // ================================================================

    public static void styleTabs(JTabbedPane tabs) {
        tabs.setFont(h3());
        tabs.setBackground(SURFACE);
        tabs.setForeground(TEXT_PRIMARY);
    }
}
