package com.logmonitor.ui;

import com.logmonitor.model.LogEntry;
import com.logmonitor.service.LogService;
import com.logmonitor.service.LogStatistics;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LogMonitorFrame extends JFrame {
    private static final Color APP_BACKGROUND = new Color(11, 15, 24);
    private static final Color PANEL_BACKGROUND = new Color(18, 25, 38);
    private static final Color PANEL_BACKGROUND_ALT = new Color(23, 32, 48);
    private static final Color PANEL_BORDER = new Color(55, 69, 92);
    private static final Color TEXT_PRIMARY = new Color(244, 247, 252);
    private static final Color TEXT_MUTED = new Color(151, 163, 186);
    private static final Color ACCENT = new Color(38, 166, 154);
    private static final Color ACCENT_BRIGHT = new Color(247, 181, 0);
    private static final Color ERROR = new Color(239, 83, 80);
    private static final Color WARNING = new Color(255, 167, 38);
    private static final Color INFO = new Color(66, 165, 245);
    private static final Color CHECKBOX_BORDER = new Color(116, 132, 158);
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00");
    private static final Border INPUT_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PANEL_BORDER, 1),
            new EmptyBorder(0, 0, 0, 0)
    );

    private final LogService logService;
    private final LogTableModel tableModel;
    private final JTable logTable;
    private final JComboBox<String> levelFilter;
    private final JComboBox<String> sourceFilter;
    private final JTextField searchField;
    private final JLabel statusLabel;
    private final JLabel dbLabel;
    private final JLabel totalLogsValue;
    private final JLabel errorLogsValue;
    private final JLabel successRateValue;
    private final JLabel selectedLogValue;
    private final JTextArea detailArea;
    private final JLabel detailTimestampValue;
    private final JLabel detailSourceValue;
    private final JLabel detailLevelValue;
    private final JCheckBox autoRefreshCheckBox;
    private final Timer autoRefreshTimer;
    private boolean suppressFilterEvents;

    public LogMonitorFrame(LogService logService) {
        this.logService = logService;
        this.tableModel = new LogTableModel();
        this.logTable = createTable();
        this.levelFilter = createComboBox(new String[]{"ALL", "ERROR", "WARNING", "INFO"});
        this.sourceFilter = createComboBox(new String[]{"ALL"});
        this.searchField = createSearchField();
        this.statusLabel = createLabel("Loading dashboard...", TEXT_MUTED, 13, Font.PLAIN);
        this.dbLabel = createLabel("Database online", ACCENT, 13, Font.BOLD);
        this.totalLogsValue = createLabel("0", TEXT_PRIMARY, 26, Font.BOLD);
        this.errorLogsValue = createLabel("0", TEXT_PRIMARY, 26, Font.BOLD);
        this.successRateValue = createLabel("0%", TEXT_PRIMARY, 26, Font.BOLD);
        this.selectedLogValue = createLabel("0", TEXT_PRIMARY, 26, Font.BOLD);
        this.detailArea = createDetailArea();
        this.detailTimestampValue = createDetailValueLabel("No log selected");
        this.detailSourceValue = createDetailValueLabel("-");
        this.detailLevelValue = createDetailValueLabel("-");
        this.autoRefreshCheckBox = new JCheckBox("Auto refresh every 20s");
        this.autoRefreshTimer = new Timer(20_000, event -> loadDashboard(true));

        configureWindow();
        setContentPane(buildContent());
        configureInteractions();
        loadDashboard(false);
    }

    public static void launch(LogService logService) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("GUI mode is not available in a headless environment.");
        }

        configureLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            LogMonitorFrame frame = new LogMonitorFrame(logService);
            frame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private void configureWindow() {
        setTitle("Server Log Monitor");
        setSize(1480, 920);
        setMinimumSize(new Dimension(1220, 760));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getRootPane().setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    private Container buildContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(APP_BACKGROUND);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildHeader() {
        GradientPanel header = new GradientPanel(new Color(18, 57, 74), new Color(11, 15, 24));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));

        JLabel eyebrow = createLabel("Observability Console", ACCENT_BRIGHT, 14, Font.BOLD);
        JLabel title = createLabel("Server Log Monitor", TEXT_PRIMARY, 32, Font.BOLD);
        JLabel subtitle = createLabel(
                "Premium desktop dashboard for browsing incidents, tracking health, and curating logs in real time.",
                new Color(212, 221, 235),
                15,
                Font.PLAIN
        );

        titleGroup.add(eyebrow);
        titleGroup.add(Box.createVerticalStrut(10));
        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(8));
        titleGroup.add(subtitle);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionGroup.setOpaque(false);
        actionGroup.add(createHeaderButton("Refresh", ACCENT, event -> loadDashboard(true)));
        actionGroup.add(createHeaderButton("Seed Demo Data", ACCENT_BRIGHT, event -> seedDemoData()));
        actionGroup.add(createHeaderButton("Add Log", INFO, event -> openAddLogDialog()));

        header.add(titleGroup, BorderLayout.CENTER);
        header.add(actionGroup, BorderLayout.EAST);
        return header;
    }

    private JComponent buildMainArea() {
        JPanel center = new JPanel(new BorderLayout(18, 18));
        center.setOpaque(false);

        center.add(buildStatsPanel(), BorderLayout.NORTH);
        center.add(buildContentPanel(), BorderLayout.CENTER);

        return center;
    }

    private JComponent buildStatsPanel() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);

        stats.add(createStatCard("Total Logs", "Tracked events in PostgreSQL", totalLogsValue, ACCENT));
        stats.add(createStatCard("Error Logs", "Events requiring attention", errorLogsValue, ERROR));
        stats.add(createStatCard("Success Rate", "Non-error event ratio", successRateValue, ACCENT_BRIGHT));
        stats.add(createStatCard("Visible Logs", "Entries after applying your active filters", selectedLogValue, INFO));

        return stats;
    }

    private JComponent buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setOpaque(false);

        panel.add(buildFiltersPanel(), BorderLayout.NORTH);
        panel.add(buildWorkspacePanel(), BorderLayout.CENTER);

        return panel;
    }

    private JComponent buildFiltersPanel() {
        RoundedPanel filters = new RoundedPanel(PANEL_BACKGROUND, PANEL_BORDER, 26);
        filters.setLayout(new GridBagLayout());
        filters.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.2;
        filters.add(buildFilterBlock("Level", levelFilter), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.4;
        filters.add(buildFilterBlock("Source", sourceFilter), gbc);

        gbc.gridx = 2;
        gbc.weightx = 2.0;
        filters.add(buildFilterBlock("Search", searchField), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.1;
        gbc.anchor = GridBagConstraints.WEST;
        filters.add(buildAutoRefreshBlock(), gbc);

        gbc.gridx = 4;
        gbc.weightx = 1.4;
        gbc.anchor = GridBagConstraints.EAST;
        filters.add(createActionStrip(), gbc);

        return filters;
    }

    private JComponent buildAutoRefreshBlock() {
        JPanel block = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        block.setOpaque(false);
        autoRefreshCheckBox.setOpaque(false);
        autoRefreshCheckBox.setForeground(TEXT_MUTED);
        autoRefreshCheckBox.setFocusPainted(false);
        autoRefreshCheckBox.setBorder(new EmptyBorder(0, 0, 0, 0));
        autoRefreshCheckBox.setIcon(new CheckIcon(false));
        autoRefreshCheckBox.setSelectedIcon(new CheckIcon(true));
        autoRefreshCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        block.add(autoRefreshCheckBox);
        return block;
    }

    private JComponent createActionStrip() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton applyButton = createSecondaryButton("Apply");
        applyButton.addActionListener(event -> loadDashboard(false));

        JButton clearButton = createSecondaryButton("Clear");
        clearButton.addActionListener(event -> clearFilters());

        JButton deleteButton = createDangerButton("Delete Selected");
        deleteButton.addActionListener(event -> deleteSelectedLog());

        actions.add(applyButton);
        actions.add(clearButton);
        actions.add(deleteButton);
        return actions;
    }

    private JComponent buildWorkspacePanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTablePanel(), buildDetailPanel());
        splitPane.setResizeWeight(0.7);
        splitPane.setOpaque(false);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(10);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JComponent buildTablePanel() {
        RoundedPanel panel = new RoundedPanel(PANEL_BACKGROUND, PANEL_BORDER, 28);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = createLabel("Live Log Stream", TEXT_PRIMARY, 20, Font.BOLD);
        JLabel subtitle = createLabel("Filter by severity, service, or keyword while keeping context on the selected event.", TEXT_MUTED, 13, Font.PLAIN);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        JScrollPane scrollPane = new JScrollPane(logTable);
        styleScrollPane(scrollPane);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildDetailPanel() {
        RoundedPanel panel = new RoundedPanel(PANEL_BACKGROUND, PANEL_BORDER, 28);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = createLabel("Inspector", TEXT_PRIMARY, 20, Font.BOLD);
        JLabel subtitle = createLabel("Expanded event details for triage, reviews, and handoff notes.", TEXT_MUTED, 13, Font.PLAIN);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        JPanel detailsGrid = new JPanel(new GridLayout(3, 1, 0, 12));
        detailsGrid.setOpaque(false);
        detailsGrid.add(createDetailChip("Timestamp", detailTimestampValue));
        detailsGrid.add(createDetailChip("Source", detailSourceValue));
        detailsGrid.add(createDetailChip("Severity", detailLevelValue));

        JScrollPane detailScrollPane = new JScrollPane(detailArea);
        styleScrollPane(detailScrollPane);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(detailsGrid, BorderLayout.NORTH);
        body.add(detailScrollPane, BorderLayout.CENTER);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildStatusBar() {
        RoundedPanel statusBar = new RoundedPanel(PANEL_BACKGROUND, PANEL_BORDER, 22);
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(dbLabel, BorderLayout.EAST);
        return statusBar;
    }

    private void configureInteractions() {
        searchField.addActionListener(event -> loadDashboard(false));
        levelFilter.addActionListener(event -> {
            if (suppressFilterEvents || levelFilter.isPopupVisible()) {
                return;
            }
            loadDashboard(false);
        });
        sourceFilter.addActionListener(event -> {
            if (suppressFilterEvents || sourceFilter.isPopupVisible()) {
                return;
            }
            loadDashboard(false);
        });

        autoRefreshCheckBox.addActionListener(event -> {
            if (autoRefreshCheckBox.isSelected()) {
                autoRefreshTimer.start();
                setStatus("Auto refresh enabled.");
            } else {
                autoRefreshTimer.stop();
                setStatus("Auto refresh paused.");
            }
        });

        logTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateDetailPanel(tableModel.getLogAt(logTable.getSelectedRow()));
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control R"), "refreshLogs");
        getRootPane().getActionMap().put("refreshLogs", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                loadDashboard(true);
            }
        });
    }

    private JTable createTable() {
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(34);
        table.setBackground(PANEL_BACKGROUND_ALT);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(30, 54, 76));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(34, 45, 64));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(17, 24, 39));
        header.setForeground(TEXT_PRIMARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new TableHeaderRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(170);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(520);
        table.setDefaultRenderer(Object.class, new LogTableCellRenderer());
        return table;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBackground(PANEL_BACKGROUND_ALT);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFocusable(false);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBorder(INPUT_BORDER);
        comboBox.setUI(new ModernComboBoxUI());
        comboBox.setRenderer(new ComboBoxItemRenderer());
        comboBox.setMaximumRowCount(8);
        return comboBox;
    }

    private JTextField createSearchField() {
        JTextField field = new JTextField();
        field.setBackground(PANEL_BACKGROUND_ALT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(INPUT_BORDER, new EmptyBorder(10, 12, 10, 12)));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return field;
    }

    private JTextArea createDetailArea() {
        JTextArea area = new JTextArea(10, 20);
        area.setEditable(false);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setForeground(TEXT_PRIMARY);
        area.setBackground(PANEL_BACKGROUND_ALT);
        area.setCaretColor(TEXT_PRIMARY);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBorder(new EmptyBorder(16, 16, 16, 16));
        area.setText("Select a log entry to inspect its message.");
        return area;
    }

    private JPanel buildFilterBlock(String label, JComponent component) {
        JPanel block = new JPanel(new BorderLayout(0, 8));
        block.setOpaque(false);
        block.add(createLabel(label, TEXT_MUTED, 12, Font.BOLD), BorderLayout.NORTH);
        block.add(component, BorderLayout.CENTER);
        return block;
    }

    private JPanel createStatCard(String title, String subtitle, JLabel valueLabel, Color accentColor) {
        RoundedPanel card = new RoundedPanel(PANEL_BACKGROUND, PANEL_BORDER, 28);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(createLabel(title, TEXT_PRIMARY, 15, Font.BOLD), BorderLayout.WEST);

        JLabel accentDot = new JLabel("\u25cf");
        accentDot.setForeground(accentColor);
        accentDot.setFont(new Font("Dialog", Font.BOLD, 14));
        top.add(accentDot, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(valueLabel);
        body.add(Box.createVerticalStrut(6));
        body.add(createLabel(subtitle, TEXT_MUTED, 12, Font.PLAIN));

        card.add(top, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailChip(String title, JLabel value) {
        RoundedPanel chip = new RoundedPanel(PANEL_BACKGROUND_ALT, PANEL_BORDER, 22);
        chip.setLayout(new BorderLayout(0, 6));
        chip.setBorder(new EmptyBorder(12, 14, 12, 14));

        chip.add(createLabel(title, TEXT_MUTED, 12, Font.BOLD), BorderLayout.NORTH);
        chip.add(value, BorderLayout.CENTER);
        return chip;
    }

    private JButton createHeaderButton(String text, Color color, java.awt.event.ActionListener listener) {
        JButton button = new AccentButton(text, color);
        button.addActionListener(listener);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        return new AccentButton(text, new Color(61, 90, 128));
    }

    private JButton createDangerButton(String text) {
        return new AccentButton(text, new Color(168, 52, 69));
    }

    private JLabel createLabel(String text, Color color, int size, int style) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", style, size));
        return label;
    }

    private JLabel createDetailValueLabel(String text) {
        return createLabel(text, TEXT_PRIMARY, 14, Font.BOLD);
    }

    private void loadDashboard(boolean preserveSelection) {
        int selectedLogId = preserveSelection ? getSelectedLogId() : -1;
        setStatus("Refreshing logs from PostgreSQL...");

        new SwingWorker<DashboardSnapshot, Void>() {
            @Override
            protected DashboardSnapshot doInBackground() throws Exception {
                List<LogEntry> logs = logService.getLogs(
                        levelFilter.getSelectedItem() == null ? null : levelFilter.getSelectedItem().toString(),
                        sourceFilter.getSelectedItem() == null ? null : sourceFilter.getSelectedItem().toString(),
                        searchField.getText()
                );
                LogStatistics statistics = logService.getStatistics();
                List<String> sources = logService.getDistinctSources();
                return new DashboardSnapshot(logs, statistics, sources);
            }

            @Override
            protected void done() {
                try {
                    DashboardSnapshot snapshot = get();
                    updateSourceFilter(snapshot.sources);
                    tableModel.setLogs(snapshot.logs);
                    updateStatistics(snapshot.statistics, snapshot.logs.size());
                    restoreSelection(selectedLogId);
                    updateDetailPanel(tableModel.getLogAt(logTable.getSelectedRow()));
                    dbLabel.setText("Database online");
                    dbLabel.setForeground(ACCENT);
                    setStatus("Loaded " + snapshot.logs.size() + " log entries.");
                } catch (Exception e) {
                    dbLabel.setText("Database issue");
                    dbLabel.setForeground(ERROR);
                    setStatus("Unable to refresh logs.");
                    showError("Failed to load logs", e);
                }
            }
        }.execute();
    }

    private void updateSourceFilter(List<String> sources) {
        Object previous = sourceFilter.getSelectedItem();
        List<String> options = new ArrayList<>();
        options.add("ALL");
        options.addAll(sources);
        suppressFilterEvents = true;
        try {
            sourceFilter.setModel(new DefaultComboBoxModel<>(options.toArray(new String[0])));
            sourceFilter.setRenderer(new ComboBoxItemRenderer());
            if (previous != null && options.contains(previous.toString())) {
                sourceFilter.setSelectedItem(previous.toString());
            } else {
                sourceFilter.setSelectedItem("ALL");
            }
        } finally {
            suppressFilterEvents = false;
        }
    }

    private void updateStatistics(LogStatistics statistics, int visibleCount) {
        totalLogsValue.setText(String.valueOf(statistics.getTotalLogs()));
        errorLogsValue.setText(String.valueOf(statistics.getErrorLogs()));
        successRateValue.setText(PERCENT_FORMAT.format(statistics.getSuccessRate()) + "%");
        selectedLogValue.setText(String.valueOf(Math.max(visibleCount, 0)));
    }

    private void updateDetailPanel(LogEntry log) {
        if (log == null) {
            detailTimestampValue.setText("No log selected");
            detailSourceValue.setText("-");
            detailLevelValue.setText("-");
            detailLevelValue.setForeground(TEXT_PRIMARY);
            detailArea.setText("Select a log entry to inspect its message.");
            return;
        }

        detailTimestampValue.setText(log.getTimestamp());
        detailSourceValue.setText(log.getSource());
        detailLevelValue.setText(log.getLevel());
        detailLevelValue.setForeground(colorForLevel(log.getLevel()));
        detailArea.setText(log.getMessage());
    }

    private void clearFilters() {
        suppressFilterEvents = true;
        try {
            levelFilter.setSelectedItem("ALL");
            sourceFilter.setSelectedItem("ALL");
            searchField.setText("");
        } finally {
            suppressFilterEvents = false;
        }
        loadDashboard(false);
    }

    private void openAddLogDialog() {
        JDialog dialog = new JDialog(this, "Create Log Entry", true);
        dialog.setSize(520, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        RoundedPanel formPanel = new RoundedPanel(PANEL_BACKGROUND, PANEL_BORDER, 24);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JComboBox<String> levelBox = createComboBox(new String[]{"ERROR", "WARNING", "INFO"});
        JTextField sourceField = createSearchField();
        JTextArea messageArea = createDetailArea();
        messageArea.setEditable(true);
        messageArea.setRows(6);
        messageArea.setText("");
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        styleScrollPane(messageScrollPane);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(buildFilterBlock("Severity", levelBox), gbc);

        gbc.gridy = 1;
        formPanel.add(buildFilterBlock("Source", sourceField), gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formPanel.add(buildFilterBlock("Message", messageScrollPane), gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = createSecondaryButton("Cancel");
        JButton save = createHeaderButton("Save Log", ACCENT, event -> {
            String source = sourceField.getText().trim();
            String message = messageArea.getText().trim();
            if (source.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Source and message are required.", "Missing details", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                logService.addLog(levelBox.getSelectedItem().toString(), message, source);
                dialog.dispose();
                loadDashboard(true);
                setStatus("New log added from " + source + ".");
            } catch (SQLException e) {
                showError("Failed to add log", e);
            }
        });

        cancel.addActionListener(event -> dialog.dispose());
        buttons.add(cancel);
        buttons.add(save);

        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(APP_BACKGROUND);
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(wrapper);
        dialog.setVisible(true);
    }

    private void deleteSelectedLog() {
        LogEntry selectedLog = tableModel.getLogAt(logTable.getSelectedRow());
        if (selectedLog == null) {
            JOptionPane.showMessageDialog(this, "Select a log before deleting.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int decision = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected log from " + selectedLog.getSource() + "?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (decision != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            logService.deleteLog(selectedLog.getId());
            loadDashboard(false);
            setStatus("Deleted log #" + selectedLog.getId() + ".");
        } catch (SQLException e) {
            showError("Failed to delete log", e);
        }
    }

    private void seedDemoData() {
        try {
            int inserted = logService.seedDemoLogs();
            loadDashboard(true);
            setStatus("Inserted " + inserted + " demo logs.");
        } catch (SQLException e) {
            showError("Failed to seed demo logs", e);
        }
    }

    private int getSelectedLogId() {
        LogEntry log = tableModel.getLogAt(logTable.getSelectedRow());
        return log == null ? -1 : log.getId();
    }

    private void restoreSelection(int selectedLogId) {
        if (tableModel.getRowCount() == 0) {
            return;
        }

        int fallbackIndex = 0;
        if (selectedLogId != -1) {
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                LogEntry log = tableModel.getLogAt(row);
                if (log != null && log.getId() == selectedLogId) {
                    fallbackIndex = row;
                    break;
                }
            }
        }
        logTable.setRowSelectionInterval(fallbackIndex, fallbackIndex);
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String title, Exception exception) {
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private Color colorForLevel(String level) {
        if ("ERROR".equalsIgnoreCase(level)) {
            return ERROR;
        }
        if ("WARNING".equalsIgnoreCase(level)) {
            return WARNING;
        }
        return INFO;
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_BACKGROUND_ALT);
        scrollPane.setBackground(PANEL_BACKGROUND_ALT);
        scrollPane.getVerticalScrollBar().setBackground(PANEL_BACKGROUND);
        scrollPane.getHorizontalScrollBar().setBackground(PANEL_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
    }

    private static class DashboardSnapshot {
        private final List<LogEntry> logs;
        private final LogStatistics statistics;
        private final List<String> sources;

        private DashboardSnapshot(List<LogEntry> logs, LogStatistics statistics, List<String> sources) {
            this.logs = logs;
            this.statistics = statistics;
            this.sources = sources;
        }
    }

    private static class LogTableModel extends AbstractTableModel {
        private final String[] columns = {"Timestamp", "Level", "Source", "Message"};
        private final List<LogEntry> logs = new ArrayList<>();

        @Override
        public int getRowCount() {
            return logs.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            LogEntry log = logs.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return log.getTimestamp();
                case 1:
                    return log.getLevel();
                case 2:
                    return log.getSource();
                case 3:
                    return log.getMessage();
                default:
                    return "";
            }
        }

        public void setLogs(List<LogEntry> newLogs) {
            logs.clear();
            logs.addAll(newLogs);
            fireTableDataChanged();
        }

        public LogEntry getLogAt(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= logs.size()) {
                return null;
            }
            return logs.get(rowIndex);
        }
    }

    private class LogTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
            renderer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            renderer.setToolTipText(value == null ? null : value.toString());

            LogEntry log = tableModel.getLogAt(row);
            Color rowBackground = row % 2 == 0 ? PANEL_BACKGROUND_ALT : new Color(27, 36, 53);
            renderer.setBackground(isSelected ? new Color(27, 62, 85) : rowBackground);
            renderer.setForeground(TEXT_PRIMARY);

            if (log != null && column == 1) {
                renderer.setForeground(colorForLevel(log.getLevel()));
                renderer.setFont(new Font("Segoe UI", Font.BOLD, 13));
                renderer.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                renderer.setHorizontalAlignment(SwingConstants.LEFT);
            }

            return renderer;
        }
    }

    private class ComboBoxItemRenderer extends DefaultTableCellRenderer implements ListCellRenderer<Object> {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getTableCellRendererComponent(null, value, isSelected, cellHasFocus, -1, -1);
            setText(value == null ? "" : value.toString());
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setOpaque(true);

            if (isSelected) {
                setBackground(new Color(27, 62, 85));
                setForeground(TEXT_PRIMARY);
            } else {
                setBackground(PANEL_BACKGROUND_ALT);
                setForeground(TEXT_PRIMARY);
            }

            if (value != null) {
                String text = value.toString();
                if ("ERROR".equalsIgnoreCase(text)) {
                    setForeground(LogMonitorFrame.ERROR);
                } else if ("WARNING".equalsIgnoreCase(text)) {
                    setForeground(LogMonitorFrame.WARNING);
                } else if ("INFO".equalsIgnoreCase(text)) {
                    setForeground(LogMonitorFrame.INFO);
                }
            }

            return this;
        }
    }

    private static class TableHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, column);
            renderer.setOpaque(true);
            renderer.setBackground(new Color(17, 24, 39));
            renderer.setForeground(TEXT_PRIMARY);
            renderer.setFont(new Font("Segoe UI", Font.BOLD, 12));
            renderer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(34, 45, 64)),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            return renderer;
        }
    }

    private static class GradientPanel extends JPanel {
        private final Color startColor;
        private final Color endColor;

        private GradientPanel(Color startColor, Color endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint paint = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
            g2.setPaint(paint);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
            g2.setColor(new Color(255, 255, 255, 26));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 32, 32);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color fillColor;
        private final Color borderColor;
        private final int arc;

        private RoundedPanel(Color fillColor, Color borderColor, int arc) {
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class AccentButton extends JButton {
        private final Color baseColor;

        private AccentButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(new EmptyBorder(11, 18, 11, 18));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getModel().isRollover() ? baseColor.brighter() : baseColor;
            if (getModel().isPressed()) {
                fill = baseColor.darker();
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class CheckIcon implements javax.swing.Icon {
        private final boolean selected;

        private CheckIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PANEL_BACKGROUND_ALT);
            g2.fillRoundRect(x, y, 16, 16, 6, 6);
            g2.setColor(selected ? ACCENT : CHECKBOX_BORDER);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(x, y, 16, 16, 6, 6);

            if (selected) {
                g2.setColor(ACCENT_BRIGHT);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 4, y + 8, x + 7, y + 11);
                g2.drawLine(x + 7, y + 11, x + 12, y + 5);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class ModernComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            BasicArrowButton button = new BasicArrowButton(
                    SwingConstants.SOUTH,
                    PANEL_BACKGROUND_ALT,
                    PANEL_BACKGROUND_ALT,
                    TEXT_MUTED,
                    PANEL_BACKGROUND_ALT
            );
            button.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics graphics, Rectangle bounds, boolean hasFocus) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setColor(PANEL_BACKGROUND_ALT);
            g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g2.dispose();
        }

        @Override
        protected Insets getInsets() {
            return new Insets(4, 8, 4, 8);
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(77, 95, 123);
            trackColor = PANEL_BACKGROUND;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected void paintThumb(Graphics graphics, javax.swing.JComponent component, java.awt.Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 3, thumbBounds.y + 3, thumbBounds.width - 6, thumbBounds.height - 6, 12, 12);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics graphics, javax.swing.JComponent component, java.awt.Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setColor(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }
}
