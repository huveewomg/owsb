package com.owsb.view.inventory;

import com.owsb.controller.ItemController;
import com.owsb.controller.MessageController;
import com.owsb.model.inventory.Item;
import com.owsb.model.user.User;
import com.owsb.util.SupplierUtils;
import com.owsb.util.UserRole;
import com.owsb.view.PanelHeaderUtils;
import com.owsb.view.message.MessagePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

/**
 * Panel for viewing low stock items that need reordering
 */
public class LowStockAlertsPanel extends JPanel {
    private JTable lowStockTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton notifySalesButton;
    private final ItemController itemController;
    private final MessageController messageController;
    private final User currentUser;
    private List<Item> lowStockItems;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    public LowStockAlertsPanel(ItemController itemController, MessageController messageController, User currentUser) {
        this.itemController = itemController;
        this.messageController = messageController;
        this.currentUser = currentUser;
        this.messageController.setCurrentUser(currentUser);
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        addListeners();
        loadLowStockItems();
    }

    private void initComponents() {
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table model
        tableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Name", "Description", "Price", "Supplier", "Current Stock", "Min Stock", "Status", "Reorder Qty"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5 || columnIndex == 6 || columnIndex == 8) {
                    return Integer.class;
                } 
                return String.class;
            }
        };
        
        lowStockTable = new JTable(tableModel);
        lowStockTable.getTableHeader().setReorderingAllowed(false);
        lowStockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lowStockTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        int[] widths = {80, 120, 180, 80, 120, 60, 60, 80, 80};
        for (int i = 0; i < widths.length; i++) {
            lowStockTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        
        // Price column right align
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        lowStockTable.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
        lowStockTable.getColumnModel().getColumn(5).setCellRenderer(rightAlign);
        lowStockTable.getColumnModel().getColumn(6).setCellRenderer(rightAlign);
        lowStockTable.getColumnModel().getColumn(8).setCellRenderer(rightAlign);
        
        // Status column renderer with color
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                if ("CRITICAL".equals(status)) {
                    c.setBackground(new Color(255, 102, 102)); // Light red
                    c.setForeground(Color.WHITE);
                } else if ("LOW".equals(status)) {
                    c.setBackground(new Color(255, 204, 102)); // Light orange
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
                return c;
            }
        };
        lowStockTable.getColumnModel().getColumn(7).setCellRenderer(statusRenderer);
        
        JScrollPane tableScrollPane = new JScrollPane(lowStockTable);
        tableScrollPane.setPreferredSize(new Dimension(900, 350));
        add(tableScrollPane, BorderLayout.CENTER);

        // Bottom panel with notify sales button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        notifySalesButton = new JButton("Notify Sales Manager");
        notifySalesButton.setEnabled(false);
        bottomPanel.add(notifySalesButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Create header panel with title and refresh button
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel titleLabel = PanelHeaderUtils.createHeaderLabel("Low Stock Alerts");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        buttonPanel.add(refreshButton);
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private void addListeners() {
        refreshButton.addActionListener(e -> loadLowStockItems());
        
        lowStockTable.getSelectionModel().addListSelectionListener(e -> {
            notifySalesButton.setEnabled(lowStockTable.getSelectedRow() != -1);
        });
        
        notifySalesButton.addActionListener(e -> notifySalesManager());
    }

    public void loadLowStockItems() {
        tableModel.setRowCount(0);
        lowStockItems = itemController.getLowStockItems();
        Map<String, String> supplierMap = SupplierUtils.getSupplierIdToNameMap();
        
        for (Item item : lowStockItems) {
            String supplierName = supplierMap.getOrDefault(item.getSupplierID(), "Unknown");
            int reorderQty = calculateReorderQuantity(item);
            
            tableModel.addRow(new Object[]{
                    item.getItemID(),
                    item.getName(),
                    item.getDescription(),
                    currencyFormat.format(item.getUnitPrice()),
                    supplierName,
                    item.getCurrentStock(),
                    item.getMinimumStock(),
                    item.getStockStatus(),
                    reorderQty
            });
        }
        
        notifySalesButton.setEnabled(false);
        
    }
    
    /**
     * Calculate recommended reorder quantity (max stock - current stock)
     */
    private int calculateReorderQuantity(Item item) {
        return item.getMaximumStock() - item.getCurrentStock();
    }
    
    /**
     * Notify Sales Manager about low stock item
     */
    private void notifySalesManager() {
        int selectedRow = lowStockTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String itemId = (String) tableModel.getValueAt(selectedRow, 0);
        String itemName = (String) tableModel.getValueAt(selectedRow, 1);
        int currentStock = (int) tableModel.getValueAt(selectedRow, 5);
        int minStock = (int) tableModel.getValueAt(selectedRow, 6);
        String status = (String) tableModel.getValueAt(selectedRow, 7);
        int reorderQty = (int) tableModel.getValueAt(selectedRow, 8);
        
        // Show message composition dialog
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "Send Message to Sales Manager", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Message subject - automatically generated
        String subject = status + " Stock Alert: " + itemName + " (" + itemId + ")";
        JLabel subjectLabel = new JLabel("Subject:");
        JTextField subjectField = new JTextField(subject);
        subjectField.setEditable(false);
        
        JPanel subjectPanel = new JPanel(new BorderLayout());
        subjectPanel.add(subjectLabel, BorderLayout.WEST);
        subjectPanel.add(subjectField, BorderLayout.CENTER);
        
        // Message content - auto-generated but editable
        JLabel contentLabel = new JLabel("Message:");
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Attention Sales Manager,\n\n");
        contentBuilder.append("This is an automated stock alert notification.\n\n");
        contentBuilder.append("Item: ").append(itemName).append(" (").append(itemId).append(")\n");
        contentBuilder.append("Current Stock: ").append(currentStock).append("\n");
        contentBuilder.append("Minimum Stock Level: ").append(minStock).append("\n");
        contentBuilder.append("Status: ").append(status).append("\n");
        contentBuilder.append("Recommended Reorder Quantity: ").append(reorderQty).append("\n\n");
        contentBuilder.append("Please consider creating a Purchase Requisition for this item.\n\n");
        contentBuilder.append("Regards,\n");
        contentBuilder.append(currentUser.getName()).append("\n"); 
        contentBuilder.append("Inventory Manager");
        
        JTextArea contentArea = new JTextArea(contentBuilder.toString());
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentScrollPane.setPreferredSize(new Dimension(450, 250));
        
        JPanel contentAreaPanel = new JPanel(new BorderLayout());
        contentAreaPanel.add(contentLabel, BorderLayout.NORTH);
        contentAreaPanel.add(contentScrollPane, BorderLayout.CENTER);
        
        // Add to content panel
        contentPanel.add(subjectPanel, BorderLayout.NORTH);
        contentPanel.add(contentAreaPanel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton sendButton = new JButton("Send Message");
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(sendButton);
        
        // Add to dialog
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Add listeners
        cancelButton.addActionListener(e -> dialog.dispose());
        
        sendButton.addActionListener(e -> {
            // Send message
            boolean success = messageController.sendMessage(
                    UserRole.SALES_MANAGER,
                    subjectField.getText(),
                    contentArea.getText(),
                    itemId
            );
            
            dialog.dispose();
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Message sent successfully to Sales Managers.",
                        "Message Sent",
                        JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh the message panel if it exists in the parent components
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JFrame) {
                    JFrame frame = (JFrame) window;
                    Container contentPane = frame.getContentPane();
                    if (contentPane instanceof JPanel) {
                        JPanel mainPanel = (JPanel) contentPane;
                        for (Component comp : mainPanel.getComponents()) {
                            if (comp instanceof MessagePanel) {
                                ((MessagePanel) comp).loadMessages();
                                break;
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to send message. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.setVisible(true);
    }
}