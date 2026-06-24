package ui;

import dao.ElectionDAO;
import model.Election;
import util.UIStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ElectionManagementPage extends JFrame {

    private final ElectionDAO electionDAO = new ElectionDAO();

    private JTextField titleField;
    private JSpinner dateSpinner;
    private JTable table;
    private DefaultTableModel tableModel;
    private int selectedElectionId = -1;

    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");

    public ElectionManagementPage() {

        setTitle("Manage Elections");
        setSize(820, 600);
        setMinimumSize(new Dimension(680, 480));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = UIStyle.pageBackground(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel heading = UIStyle.heading("Manage Elections", 22);
        JLabel sub = UIStyle.muted(
                "Create a new election and choose which one is currently active for voting."
        );

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(heading);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        header.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 18, 0)
        );

        root.add(header, BorderLayout.NORTH);

        UIStyle.RoundedPanel formCard =
                UIStyle.card(new BorderLayout(0, 14));

        formCard.setBorder(
                BorderFactory.createEmptyBorder(20, 22, 20, 22)
        );

        JPanel fields = new JPanel(
                new GridLayout(1, 2, 16, 0)
        );

        fields.setOpaque(false);

        titleField = UIStyle.textField();

        fields.add(
                labeledField("Election Title", titleField)
        );

        dateSpinner = new JSpinner(
                new SpinnerDateModel()
        );

        dateSpinner.setEditor(
                new JSpinner.DateEditor(
                        dateSpinner,
                        "yyyy-MM-dd"
                )
        );

        styleSpinner(dateSpinner);

        fields.add(
                labeledField("Election Date", dateSpinner)
        );

        formCard.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        0
                )
        );

        buttons.setOpaque(false);

        JButton create =
                UIStyle.primaryButton("Create Election");

        JButton activate =
                UIStyle.secondaryButton("Set as Active");

        JButton assignCandidates =
                UIStyle.secondaryButton("Assign Candidates");

        JButton close =
                UIStyle.dangerButton("Close Election");

        JButton clear =
                UIStyle.ghostButton("Clear");

        buttons.add(create);
        buttons.add(activate);
        buttons.add(assignCandidates);
        buttons.add(close);
        buttons.add(clear);

        formCard.add(buttons, BorderLayout.SOUTH);

        JPanel formWrap =
                new JPanel(new BorderLayout(0, 14));

        formWrap.setOpaque(false);

        formWrap.add(
                formCard,
                BorderLayout.NORTH
        );

        tableModel = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "Title",
                        "Date",
                        "Status"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(
                    int r,
                    int c
            ) {
                return false;
            }
        };

        table = new JTable(tableModel);

        UIStyle.styleTable(table);

        table.getSelectionModel()
                .addListSelectionListener(e -> {

                    int row = table.getSelectedRow();

                    if (row < 0) {
                        return;
                    }

                    selectedElectionId =
                            (int) tableModel.getValueAt(
                                    row,
                                    0
                            );

                    titleField.setText(
                            tableModel.getValueAt(
                                    row,
                                    1
                            ).toString()
                    );
                });

        create.addActionListener(e -> {

            String title =
                    titleField.getText().trim();

            if (title.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Election title is required.",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            Date chosen =
                    (Date) dateSpinner.getValue();

            java.sql.Date sqlDate =
                    new java.sql.Date(
                            stripTime(chosen).getTime()
                    );

            if (electionDAO.addElection(
                    title,
                    sqlDate
            )) {

                JOptionPane.showMessageDialog(
                        this,
                        "Election created successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                refreshTable();
                clearForm();
            }
        });

        activate.addActionListener(e -> {

            if (selectedElectionId == -1) {

                JOptionPane.showMessageDialog(
                        this,
                        "Select an election first.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (electionDAO.setActiveElection(
                    selectedElectionId
            )) {

                refreshTable();

                JOptionPane.showMessageDialog(
                        this,
                        "Election activated.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        assignCandidates.addActionListener(e -> {

            if (selectedElectionId == -1) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select an election first.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            Election election =
                    electionDAO.getElectionById(
                            selectedElectionId
                    );

            if (election == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Election not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            AssignCandidatesDialog dialog =
                    new AssignCandidatesDialog(
                            this,
                            election
                    );

            dialog.setVisible(true);
        });

        close.addActionListener(e -> {

            if (selectedElectionId == -1) {

                JOptionPane.showMessageDialog(
                        this,
                        "Select an election first.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            int confirm =
                    JOptionPane.showConfirmDialog(
                            this,
                            "Close this election?",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION
                    );

            if (confirm ==
                    JOptionPane.YES_OPTION
                    &&
                    electionDAO.closeElection(
                            selectedElectionId
                    )) {

                refreshTable();
                clearForm();
            }
        });

        clear.addActionListener(
                e -> clearForm()
        );

        JPanel centerWrap =
                new JPanel(
                        new BorderLayout(0, 16)
                );

        centerWrap.setOpaque(false);

        centerWrap.add(
                formWrap,
                BorderLayout.NORTH
        );

        centerWrap.add(
                UIStyle.scrollWrap(table),
                BorderLayout.CENTER
        );

        root.add(
                centerWrap,
                BorderLayout.CENTER
        );

        add(root);

        refreshTable();
    }

    private void clearForm() {
        selectedElectionId = -1;
        titleField.setText("");
        dateSpinner.setValue(new Date());
        table.clearSelection();
    }

    private void refreshTable() {

        tableModel.setRowCount(0);

        List<Election> elections =
                electionDAO.getAllElections();

        for (Election e : elections) {

            String dateStr =
                    e.getElectionDate() != null
                            ? DISPLAY_FORMAT.format(
                            e.getElectionDate()
                    )
                            : "";

            tableModel.addRow(
                    new Object[]{
                            e.getElectionId(),
                            e.getElectionTitle(),
                            dateStr,
                            e.getStatus()
                    }
            );
        }
    }

    private Date stripTime(Date date) {

        Calendar cal =
                Calendar.getInstance();

        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    private void styleSpinner(
            JSpinner spinner
    ) {

        spinner.setFont(UIStyle.body());

        JComponent editor =
                spinner.getEditor();

        if (editor instanceof
                JSpinner.DefaultEditor de) {

            de.getTextField().setFont(
                    UIStyle.body()
            );
        }
    }

    private JComponent labeledField(
            String label,
            JComponent field
    ) {

        JPanel p = new JPanel();

        p.setOpaque(false);

        p.setLayout(
                new BoxLayout(
                        p,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel l =
                UIStyle.sectionLabel(label);

        p.add(l);
        p.add(field);

        return p;
    }
}