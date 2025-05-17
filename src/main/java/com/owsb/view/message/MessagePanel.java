package com.owsb.view.message;

import com.owsb.controller.MessageController;
import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.message.Message;
import com.owsb.model.user.User;
import com.owsb.util.UserRole;
import com.owsb.view.dashboard.BaseDashboard;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for viewing and managing messages
 */
public class MessagePanel extends JPanel {
    private JTable messageTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton viewButton;
    private JButton deleteButton;
    private JButton markAllReadButton;
    
    private final MessageController messageController;
    private final PurchaseRequisitionController prController;
    private final User currentUser;
    private List<Message> messages;
    
    /**
     * Constructor
     */
    public MessagePanel(MessageController messageController, PurchaseRequisitionController prController, User currentUser) {
        this.messageController = messageController;
        this.prController = prController;
        this.currentUser = currentUser;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        addListeners();
        loadMessages();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Table model
        tableModel = new DefaultTableModel(
                new Object[]{"", "Subject", "From", "Date & Time", "Related Item"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }
        };
        
        messageTable = new JTable(tableModel);
        messageTable.getTableHeader().setReorderingAllowed(false);
        messageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        messageTable.getColumnModel().getColumn(0).setPreferredWidth(30); // Read status column
        messageTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Subject column
        messageTable.getColumnModel().getColumn(2).setPreferredWidth(150); // From column
        messageTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Date column
        messageTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Related item column
        
        // Custom renderer for read status column
        messageTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setText("");
                
                Boolean isRead = (Boolean) value;
                if (isRead) {
                    // For read messages - empty circle or checkmark
                    label.setText("✓");
                    label.setHorizontalAlignment(JLabel.CENTER);
                } else {
                    // For unread messages - filled circle
                    label.setText("●");
                    label.setForeground(new Color(41, 128, 185)); // Blue dot for unread
                    label.setHorizontalAlignment(JLabel.CENTER);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                }
                
                return label;
            }
        });
        
        // Custom renderer for subject column to make unread bold
        messageTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Boolean isRead = (Boolean) table.getValueAt(row, 0);
                
                if (!isRead) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                
                return c;
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(messageTable);
        add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteButton = new JButton("Delete");
        viewButton = new JButton("View");
        
        bottomPanel.add(deleteButton);
        bottomPanel.add(viewButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Messages");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        markAllReadButton = new JButton("Mark All Read");
        
        buttonPanel.add(markAllReadButton);
        buttonPanel.add(refreshButton);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Add listeners
     */
    private void addListeners() {
        refreshButton.addActionListener(e -> loadMessages());
        
        markAllReadButton.addActionListener(e -> markAllMessagesRead());
        
        messageTable.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = messageTable.getSelectedRow() != -1;
            viewButton.setEnabled(rowSelected);
            deleteButton.setEnabled(rowSelected);
        });
        
        viewButton.addActionListener(e -> viewMessage());
        
        deleteButton.addActionListener(e -> deleteMessage());
    }
    
    /**
     * Load messages
     */
    public void loadMessages() {
        tableModel.setRowCount(0);
        messages = messageController.getMessagesForCurrentUser();
        
        for (Message message : messages) {
            tableModel.addRow(new Object[]{
                    message.isRead(),
                    message.getSubject(),
                    message.getSenderName(),
                    message.getTimestamp(),
                    message.getRelatedItemID() != null ? message.getRelatedItemID() : ""
            });
        }
        
        viewButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    /**
     * Mark all messages as read
     */
    private void markAllMessagesRead() {
        boolean success = messageController.markAllMessagesAsRead();
        if (success) {
            loadMessages();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to mark all messages as read.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * View selected message
     */
    private void viewMessage() {
        int selectedRow = messageTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Message message = messages.get(selectedRow);
        
        // Mark message as read
        if (!message.isRead()) {
            messageController.markMessageAsRead(message.getMessageID());
        }
        
        // Show message dialog
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), message.getSubject(), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Message header
        JPanel headerPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        addField(headerPanel, "From:", message.getSenderName());
        addField(headerPanel, "Date:", message.getTimestamp());
        if (message.getRelatedItemID() != null && !message.getRelatedItemID().isEmpty()) {
            addField(headerPanel, "Related Item:", message.getRelatedItemID());
        }
        
        // Message content
        JTextArea contentArea = new JTextArea(message.getContent());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Only show Create PR button if message is related to an item and user is Sales Manager
        if (message.getRelatedItemID() != null && 
            !message.getRelatedItemID().isEmpty() && 
            currentUser.getRole() == UserRole.SALES_MANAGER) {
            
            JButton createPRButton = new JButton("Create PR for Item");
            createPRButton.addActionListener(e -> {
                dialog.dispose();
                
                // Navigate to the PR panel - find the parent frame
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof BaseDashboard) {
                    BaseDashboard dashboard = (BaseDashboard) window;
                    dashboard.navigateToPRPanel();
                }
            });
            actionPanel.add(createPRButton);
        }
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        actionPanel.add(closeButton);
        
        // Add components to content panel
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(contentScrollPane, BorderLayout.CENTER);
        contentPanel.add(actionPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
        
        // Refresh the table after closing the dialog
        loadMessages();
    }
    
    /**
     * Add a field to a panel
     */
    private void addField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
        JLabel valueComponent = new JLabel(value);
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
    
    /**
     * Delete selected message
     */
    private void deleteMessage() {
        int selectedRow = messageTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Message message = messages.get(selectedRow);
        
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this message?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            boolean success = messageController.deleteMessage(message.getMessageID());
            if (success) {
                loadMessages();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete message.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
}