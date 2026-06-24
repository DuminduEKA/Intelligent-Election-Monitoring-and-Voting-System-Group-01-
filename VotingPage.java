package ui;

import dao.BallotDAO;
import dao.ElectionCandidateDAO;
import dao.ElectionDAO;
import dao.PartyDAO;
import model.Candidate;
import model.Election;
import model.Party;
import model.VoterUser;
import util.UIStyle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VotingPage extends JFrame {

    private final VoterUser voter;
    private final Election election;
    private ButtonGroup candidateGroup;
    private List<Candidate> candidates;

    // Track all cards so we can re-paint on selection change
    private final Map<Integer, CandidateCardPanel> cardMap = new HashMap<>();
    private int selectedCandidateId = -1;

    private static final int CARD_W       = 190;
    private static final int PHOTO_H      = 185;
    private static final int CARD_H       = 300;
    private static final int CARD_ARC     = 18;
    private static final int PHOTO_ARC    = 14;
    private static final int LOGO_SIZE    = 40;
    private static final Color GREEN_TOP  = new Color(28, 130, 28);
    private static final Color SEL_BORDER = new Color(59, 130, 246);   // blue when selected
    private static final Color NORM_BG    = Color.WHITE;
    private static final Color SEL_BG     = new Color(239, 246, 255); // light blue tint

    // Cache decoded candidate photos (candidateId -> BufferedImage) and
    // party logos (partyId -> BufferedImage) so we only decode each BLOB
    // once, no matter how many times the card repaints.
    private final Map<Integer, BufferedImage> candidatePhotoCache = new HashMap<>();
    private final Map<Integer, BufferedImage> partyLogoCache = new HashMap<>();

    /** Legacy constructor: falls back to whichever election is currently Active. */
    public VotingPage(VoterUser voter) {
        this(voter, new ElectionDAO().getActiveElection());
    }

    public VotingPage(VoterUser voter, Election election) {
        this.voter = voter;
        this.election = election;
        setTitle("Cast Your Vote");
        setSize(720, 620);
        setMinimumSize(new Dimension(CARD_W * 2 + 100, 500));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        if (voter.hasVoted()) {
            JOptionPane.showMessageDialog(this,
                    "You have already cast your vote. You cannot vote again.",
                    "Vote Already Cast", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        if (election == null) {
            JOptionPane.showMessageDialog(this,
                    "There is no active election right now.",
                    "No Active Election", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Pre-decode party logos once.
        for (Party p : new PartyDAO().getAllParties()) {
            BufferedImage img = decode(p.getLogoData());
            if (img != null) partyLogoCache.put(p.getPartyId(), img);
        }

        JPanel root = UIStyle.pageBackground(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = UIStyle.heading("Cast Your Vote", 22);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UIStyle.muted("Voting in: " + election.getElectionTitle()
                + " — select one candidate from your district. This action cannot be undone.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        root.add(header, BorderLayout.NORTH);

        // Load all candidates assigned to this election (all districts).
        candidates = new ElectionCandidateDAO()
                .getCandidatesForElection(election.getElectionId());
        candidateGroup = new ButtonGroup();

        // Pre-decode candidate photos once.
        for (Candidate c : candidates) {
            BufferedImage img = decode(c.getPhotoData());
            if (img != null) candidatePhotoCache.put(c.getCandidateId(), img);
        }

        // Group candidates by district
        java.util.LinkedHashMap<String, java.util.List<Candidate>> byDistrict = new java.util.LinkedHashMap<>();
        for (Candidate c : candidates) {
            String dist = c.getDistrictName() != null ? c.getDistrictName() : "Other";
            byDistrict.computeIfAbsent(dist, k -> new java.util.ArrayList<>()).add(c);
        }

        // Collect all row panels so we can reflow them on resize
        java.util.List<JPanel> districtRows = new java.util.ArrayList<>();

        // Outer panel — width tracks viewport via getPreferredSize override
        JPanel outerPanel = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                if (getParent() != null && getParent().getWidth() > 0)
                    d.width = getParent().getWidth();
                return d;
            }
        };
        outerPanel.setOpaque(false);

        JPanel sectionsPanel = new JPanel();
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setOpaque(false);
        sectionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        outerPanel.add(sectionsPanel, BorderLayout.NORTH);

        if (candidates.isEmpty()) {
            sectionsPanel.add(UIStyle.muted("No candidates have been assigned to this election yet."));
        } else {
            for (java.util.Map.Entry<String, java.util.List<Candidate>> entry : byDistrict.entrySet()) {
                JLabel distLbl = new JLabel(entry.getKey());
                distLbl.setFont(new Font(UIStyle.h3().getFamily(), Font.BOLD, 15));
                distLbl.setForeground(UIStyle.TEXT_PRIMARY);
                distLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                distLbl.setBorder(BorderFactory.createEmptyBorder(16, 4, 8, 0));
                sectionsPanel.add(distLbl);

                JPanel row = new JPanel(new WrapLayout(java.awt.FlowLayout.LEFT, 14, 10)) {
                    @Override public Dimension getPreferredSize() {
                        // Constrain width to outerPanel so WrapLayout knows when to wrap
                        Dimension d = super.getPreferredSize();
                        Container p = getParent();
                        while (p != null && !(p instanceof JScrollPane)) {
                            if (p.getWidth() > 0) { d.width = p.getWidth(); break; }
                            p = p.getParent();
                        }
                        return d;
                    }
                };
                row.setOpaque(false);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                for (Candidate c : entry.getValue()) {
                    CandidateCardPanel card = new CandidateCardPanel(c);
                    cardMap.put(c.getCandidateId(), card);
                    row.add(card);
                }
                sectionsPanel.add(row);
                districtRows.add(row);
            }
        }

        JScrollPane scroll = new JScrollPane(outerPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIStyle.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Reflow on resize
        scroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                outerPanel.revalidate();
                for (JPanel r : districtRows) r.revalidate();
            }
        });
        root.add(scroll, BorderLayout.CENTER);

        // Footer
        JButton castVoteBtn = UIStyle.primaryButton("Cast Vote");
        castVoteBtn.setPreferredSize(new Dimension(150, 42));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        footer.add(castVoteBtn);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
        castVoteBtn.addActionListener(e -> castVote());
    }

    private BufferedImage decode(byte[] data) {
        if (data == null || data.length == 0) return null;
        try {
            return ImageIO.read(new ByteArrayInputStream(data));
        } catch (Exception ex) {
            return null;
        }
    }

    /** Select a candidate - update all card visuals. */
    private void selectCandidate(int candidateId) {
        selectedCandidateId = candidateId;
        for (Map.Entry<Integer, CandidateCardPanel> entry : cardMap.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == candidateId);
        }
        candidateGroup.getElements().asIterator().forEachRemaining(rb -> {
            if (rb.getActionCommand().equals(String.valueOf(candidateId))) rb.setSelected(true);
        });
    }

    // ================================================================
    // Card panel — fully self-painted (no nested layout managers for the
    // photo area), so there is no layout manager anywhere that can stretch
    // or re-scale the already-correctly-scaled candidate photo.
    // ================================================================
    private class CandidateCardPanel extends JPanel {
        private boolean selected = false;
        private final Candidate candidate;
        private String partyText = "";

        CandidateCardPanel(Candidate c) {
            this.candidate = c;
            setOpaque(false);
            setLayout(null); // no layout manager at all for this panel
            setPreferredSize(new Dimension(CARD_W, CARD_H));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Hidden radio button - only used to keep ButtonGroup bookkeeping
            // working; it is never shown or laid out.
            JRadioButton rb = new JRadioButton();
            rb.setActionCommand(String.valueOf(c.getCandidateId()));
            rb.setVisible(false);
            rb.setBounds(0, 0, 0, 0);
            candidateGroup.add(rb);
            add(rb);

            // Name label — full width centered, HTML for wrapping
            JLabel nameLbl = new JLabel(
                    "<html><p style='text-align:center;margin:0;padding:0'>"
                            + escape(c.getCandidateName()) + "</p></html>");
            nameLbl.setFont(new Font(UIStyle.h3().getFamily(), Font.BOLD, 13));
            nameLbl.setForeground(UIStyle.TEXT_PRIMARY);
            nameLbl.setHorizontalAlignment(SwingConstants.CENTER);
            nameLbl.setVerticalAlignment(SwingConstants.TOP);
            nameLbl.setBounds(4, PHOTO_H + 6, CARD_W - 8, 44);
            add(nameLbl);

            // partyText drawn in paintComponent (centered with logo)
            partyText = (c.getPartyShortName() != null && !c.getPartyShortName().trim().isEmpty())
                    ? c.getPartyShortName()
                    : (c.getPartyName() != null ? c.getPartyName() : "Independent");

            MouseAdapter click = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    selectCandidate(c.getCandidateId());
                }
            };
            addMouseListener(click);
            nameLbl.addMouseListener(click);
        }

        private String escape(String s) {
            return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        void setSelected(boolean sel) {
            this.selected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();

            // ---- Card shadow + background + selection border ----
            g2.setColor(new Color(0, 0, 0, selected ? 25 : 14));
            g2.fill(new RoundRectangle2D.Float(3, 5, w - 6, h - 7, CARD_ARC, CARD_ARC));

            g2.setColor(selected ? SEL_BG : NORM_BG);
            RoundRectangle2D.Float cardShape = new RoundRectangle2D.Float(0, 0, w - 2, h - 4, CARD_ARC, CARD_ARC);
            g2.fill(cardShape);

            // ---- Photo area (clipped to its own rounded-top rect, drawn
            // directly here - this is the ONLY place the photo is painted,
            // so there is no other layout/component that can re-stretch it) ----
            Shape oldClip = g2.getClip();
            RoundRectangle2D.Float photoShape = new RoundRectangle2D.Float(0, 0, w - 2, PHOTO_H, PHOTO_ARC, PHOTO_ARC);
            g2.clip(photoShape);
            g2.setColor(GREEN_TOP);
            g2.fillRect(0, 0, w, PHOTO_H);

            BufferedImage photo = candidatePhotoCache.get(candidate.getCandidateId());
            if (photo != null) {
                drawCover(g2, photo, 0, 0, w - 2, PHOTO_H);
            } else {
                drawPersonPlaceholder(g2, w - 2, PHOTO_H);
            }
            g2.setClip(oldClip);

            // ---- Party logo + name row — centered horizontally ----
            // Row Y: below name area (PHOTO_H + 10 + 52) + small gap
            int rowY = PHOTO_H + 8 + 38;
            int gap = 20; // space between logo and text

            // Measure text width to calculate total row width
            Font partyFont = new Font(UIStyle.bodySmall().getFamily(), Font.BOLD, 13);
            g2.setFont(partyFont);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(partyText);
            int rowW = LOGO_SIZE + gap + textW;
            int rowX = (w - rowW) / 2; // center start X

            BufferedImage logo = partyLogoCache.get(candidate.getPartyId());
            if (logo != null) {
                g2.drawImage(logo, rowX, rowY, LOGO_SIZE, LOGO_SIZE, null);
            } else {
                g2.setColor(new Color(255, 150, 30));
                g2.fill(new Ellipse2D.Float(rowX + 4, rowY + 4, LOGO_SIZE - 8, LOGO_SIZE - 8));
            }
            // Draw party text vertically centered with logo
            int textX = rowX + LOGO_SIZE + gap;
            int textY = rowY + (LOGO_SIZE + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(UIStyle.TEXT_SECONDARY);
            g2.drawString(partyText, textX, textY);

            // ---- Selection border on top ----
            if (selected) {
                g2.setColor(SEL_BORDER);
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 4, h - 6, CARD_ARC - 1, CARD_ARC - 1));
            } else {
                g2.setColor(UIStyle.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 4, h - 6, CARD_ARC - 1, CARD_ARC - 1));
            }

            g2.dispose();
            super.paintComponent(g); // paints the nameLbl/partyLbl children
        }
    }

    /**
     * Draws an image into the given box using "cover" scaling: the image is
     * scaled proportionally so it completely fills the box (no empty space,
     * no distortion) and any overflow is center-cropped. This is the single
     * place image scaling happens for candidate photos.
     */
    private void drawCover(Graphics2D g2, BufferedImage img, int x, int y, int boxW, int boxH) {
        double scaleX = (double) boxW / img.getWidth();
        double scaleY = (double) boxH / img.getHeight();
        double scale = Math.max(scaleX, scaleY); // cover = max, not min
        int drawW = (int) Math.ceil(img.getWidth() * scale);
        int drawH = (int) Math.ceil(img.getHeight() * scale);
        int drawX = x + (boxW - drawW) / 2;
        int drawY = y + (boxH - drawH) / 2;
        g2.drawImage(img, drawX, drawY, drawW, drawH, null);
    }

    private void drawPersonPlaceholder(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRect(0, 0, w, h);
        g2.setColor(new Color(255, 255, 255, 150));
        int head = w / 5;
        g2.fillOval(w / 2 - head / 2, h / 6, head, head);
        g2.fillArc(w / 2 - head, h / 2 - 4, head * 2, head + 10, 0, 180);
    }

    private void castVote() {
        if (selectedCandidateId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a candidate before voting.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cast your vote? This cannot be undone.",
                "Confirm Vote", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (new BallotDAO().castVote(voter.getVoterId(), selectedCandidateId, election.getElectionId())) {
            voter.setHasVoted(true);
            JOptionPane.showMessageDialog(this,
                    "Your vote has been recorded. Thank you for voting!",
                    "Vote Cast", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    // WrapLayout - cards wrap to next row properly inside scroll pane
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container t) { return layout(t, true); }
        @Override public Dimension minimumLayoutSize(Container t)   { return layout(t, false); }
        private Dimension layout(Container t, boolean pref) {
            synchronized (t.getTreeLock()) {
                int w = t.getSize().width;
                // Walk up to find the actual viewport/scroll width
                if (w == 0) {
                    Container p = t.getParent();
                    while (p != null && w == 0) { w = p.getSize().width; p = p.getParent(); }
                }
                if (w == 0) w = 600;
                Insets ins = t.getInsets();
                int maxW = w - ins.left - ins.right;
                int x = 0, y = ins.top + getVgap(), rh = 0;
                for (Component c : t.getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = pref ? c.getPreferredSize() : c.getMinimumSize();
                    if (x > 0 && x + getHgap() + d.width > maxW) {
                        x = 0; y += rh + getVgap(); rh = 0;
                    }
                    x += d.width + getHgap();
                    rh = Math.max(rh, d.height);
                }
                return new Dimension(w, y + rh + getVgap() + ins.bottom);
            }
        }
    }
}