package com.owsb.view.order;

import com.owsb.controller.PurchaseOrderController;
import com.owsb.model.POItem;
import com.owsb.model.PurchaseOrder;
import com.owsb.model.User;
import com.owsb.model.user.FinanceManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for viewing purchase orders
 * Demonstrates role-based access control and MVC pattern
 */
public class PurchaseOrderListPanel extends JPanel {
    // UI components
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    
    private JTable poTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<StatusFilter> statusFilterComboBox;
    private JButton refreshButton;
    
    private JButton viewButton;
    private JButton approveButton; // For Finance Managers
    private JButton rejectButton; // For Finance Managers
    
    // Controller
    private final PurchaseOrderController poController;
    
    // Data
    private List<PurchaseOrder> pos;
    private Map<String, String> managerNames;
    
    // Current user
    private final User currentUser;
    
    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor for PurchaseOrderListPanel
     * @param poController Purchase order controller
     * @param currentUser Current user
     */
    public PurchaseOrderListPanel(PurchaseOrderController poController, User currentUser) {
        this.poController = poController;
        this.currentUser = currentUser;
        this.managerNames = new HashMap<>();
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize components
        initComponents();
        
        // Add listeners
        addListeners();
        
        // Load data
        loadPurchaseOrders();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Top panel - Filters and refresh button
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel statusFilterLabel = new JLabel("Status:");
        statusFilterComboBox = new JComboBox<>(getStatusFilters());
        
        refreshButton = new JButton("Refresh");
        
        topPanel.add(statusFilterLabel);
        topPanel.add(statusFilterComboBox);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(refreshButton);
        
        // Center panel - PO table
        centerPanel = new JPanel(new BorderLayout());
        
        // Create table model
        tableModel = new DefaultTableModel(
                new Object[]{"PO ID", "PR ID", "Date", "Delivery Date", "Created By", "Status", "Items", "Total Value", "Notes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        poTable = new JTable(tableModel);
        poTable.getTableHeader().setReorderingAllowed(false);
        poTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        poTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Create simple renderers
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        // Set column renderers
        poTable.getColumnModel().getColumn(2).setCellRenderer(dateRenderer);
        poTable.getColumnModel().getColumn(3).setCellRenderer(dateRenderer);
        poTable.getColumnModel().getColumn(7).setCellRenderer(currencyRenderer);
        
        // Set column widths
        poTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        poTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        poTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        poTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        poTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        poTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        poTable.getColumnModel().getColumn(6).setPreferredWidth(60);
        poTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        poTable.getColumnModel().getColumn(8).setPreferredWidth(200);
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(poTable);
        tableScrollPane.setPreferredSize(new Dimension(900, 350));
        
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        viewButton = new JButton("View");
        viewButton.setEnabled(false);
        
        approveButton = new JButton("Approve");
        approveButton.setEnabled(false);
        
        rejectButton = new JButton("Reject");
        rejectButton.setEnabled(false);
        
        bottomPanel.add(viewButton);
        
        // Only add Approve and Reject buttons for Finance Managers
        if (currentUser instanceof FinanceManager) {
            bottomPanel.add(approveButton);
            bottomPanel.add(rejectButton);
        }
        
        // Add panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Get status filters for combo box
     * @return Array of status filters
     */
    private StatusFilter[] getStatusFilters() {
        return new StatusFilter[]{
                new StatusFilter("All", null),
                new StatusFilter("Pending", PurchaseOrder.Status.PENDING),
                new StatusFilter("Approved", PurchaseOrder.Status.APPROVED),
                new StatusFilter("Rejected", PurchaseOrder.Status.REJECTED),
                new StatusFilter("Completed", PurchaseOrder.Status.COMPLETED),
                new StatusFilter("Cancelled", PurchaseOrder.Status.CANCELLED)
        };
    }
    
    /**
     * Add listeners to components
     */
    private void addListeners() {
        // Status filter combo box listener
        statusFilterComboBox.addActionListener(e -> filterPOs());
        
        // Refresh button listener
        refreshButton.addActionListener(e -> loadPurchaseOrders());
        
        // Table selection listener
        poTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = poTable.getSelectedRow();
            boolean hasSelection = selectedRow != -1;
            
            viewButton.setEnabled(hasSelection);
            
            // Enable/disable approve and reject buttons based on selection and status
            if (hasSelection && currentUser instanceof FinanceManager) {
                String poId = (String) poTable.getValueAt(selectedRow, 0);
                
                // Find the PO in our list
                PurchaseOrder selectedPO = null;
                for (PurchaseOrder po : pos) {
                    if (po.getPoID().equals(poId)) {
                        selectedPO = po;
                        break;
                    }
                }
                
                if (selectedPO != null) {
                    // Only enable approve and reject for PENDING POs
                    boolean canApprove = selectedPO.getStatus() == PurchaseOrder.Status.PENDING;
                    
                    approveButton.setEnabled(canApprove);
                    rejectButton.setEnabled(canApprove);
                }
            } else {
                approveButton.setEnabled(false);
                rejectButton.setEnabled(false);
            }
        });
        
        // View button listener
        viewButton.addActionListener(e -> viewPO());
        
        // Approve button listener
        approveButton.addActionListener(e -> approvePO());
        
        // Reject button listener
        rejectButton.addActionListener(e -> rejectPO());
    }
    
    /**
     * Load purchase orders
     */
    public void loadPurchaseOrders() {
        // Get all POs
        pos = poController.getAllPurchaseOrders();
        
        // Apply filter
        filterPOs();
    }
    
    /**
     * Filter POs based on selected status
     */
    private void filterPOs() {
        // Clear table
        tableModel.setRowCount(0);
        
        // Get selected filter
        StatusFilter filter = (StatusFilter) statusFilterComboBox.getSelectedItem();
        
        if (filter == null) {
            return;
        }
        
        // Filter POs
        List<PurchaseOrder> filteredPOs;
        
        if (filter.getStatus() == null) {
            // Show all
            filteredPOs = pos;
        } else {
            // Filter by status
            filteredPOs = pos.stream()
                    .filter(po -> po.getStatus() == filter.getStatus())
                    .toList();
        }
        
        // Add to table
        for (PurchaseOrder po : filteredPOs) {
            // Get the manager name
            String managerName = getManagerName(po.getPurchaseManagerID());
            
            tableModel.addRow(new Object[]{
                    po.getPoID(),
                    po.getPrID(),
                    po.getDate(),
                    po.getDeliveryDate(),
                    managerName,
                    po.getStatus().getDisplayName(),
                    po.getItemCount(),
                    po.getTotalValue(),
                    po.getNotes()
            });
        }
        
        // Reset button states
        viewButton.setEnabled(false);
        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);
    }
    
    /**
     * Get manager name from ID
     * @param managerId Manager ID
     * @return Manager name
     */
    private String getManagerName(String managerId) {
        // Check cache first
        if (managerNames.containsKey(managerId)) {
            return managerNames.get(managerId);
        }
        
        // TODO: Get manager name from user repository
        // For now, just return the ID
        String name = managerId + " (Manager)";
        
        // Cache the name
        managerNames.put(managerId, name);
        
        return name;
    }
    
    /**
     * View selected PO
     */
    private void viewPO() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        // Get the PO ID
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        
        // Get the PO
        PurchaseOrder po = pos.stream()
                .filter(p -> p.getPoID().equals(poId))
                .findFirst()
                .orElse(null);
        
        if (po == null) {
            JOptionPane.showMessageDialog(this, 
                    "Purchase order not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create and show PO viewer dialog
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                "Purchase Order Details: " + poId, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        // Create PO details panel
        JPanel poDetailsPanel = createPODetailsPanel(po);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        // Add panels to dialog
        dialog.add(poDetailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Create PO details panel
     * @param po Purchase order
     * @return Panel with PO details
     */
    private JPanel createPODetailsPanel(PurchaseOrder po) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create header panel
        JPanel headerPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("PO Details"));
        
        // Add header fields
        addLabelField(headerPanel, "PO ID:", po.getPoID());
        addLabelField(headerPanel, "PR ID:", po.getPrID());
        addLabelField(headerPanel, "Date:", dateFormat.format(po.getDate()));
        addLabelField(headerPanel, "Delivery Date:", dateFormat.format(po.getDeliveryDate()));
        addLabelField(headerPanel, "Created By:", getManagerName(po.getPurchaseManagerID()));
        
        // Add finance manager if available
        if (po.getFinanceManagerID() != null) {
            addLabelField(headerPanel, "Approved/Rejected By:", getManagerName(po.getFinanceManagerID()));
        }
        
        addLabelField(headerPanel, "Status:", po.getStatus().getDisplayName());
        addLabelField(headerPanel, "Notes:", po.getNotes());
        
        // Create items table
        DefaultTableModel itemsTableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Quantity", "Supplier", "Unit Price", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable itemsTable = new JTable(itemsTableModel);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column renderers
        DefaultTableCellRenderer rightAlignRenderer = new DefaultTableCellRenderer();
        rightAlignRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(rightAlignRenderer);
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(rightAlignRenderer);
        
        // Add items to table
        for (POItem item : po.getItems()) {
            itemsTableModel.addRow(new Object[]{
                    item.getItemID(),
                    item.getItemName(),
                    item.getQuantity(),
                    item.getSupplierName(),
                    item.getUnitPrice(),
                    item.getTotalCost()
            });
        }
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Items"));
        
        // Add summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.add(new JLabel("Total Items: " + po.getItemCount()));
        summaryPanel.add(Box.createHorizontalStrut(20));
        summaryPanel.add(new JLabel("Total Value: " + currencyFormat.format(po.getTotalValue())));
        
        // Add panels to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Add label and field to panel
     * @param panel Panel to add to
     * @param label Label text
     * @param value Field value
     */
    private void addLabelField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
        
        JLabel valueComponent = new JLabel(value);
        
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
    
    /**
     * Approve selected PO
     */
    private void approvePO() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        // Get the PO ID
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        
        // Ask for confirmation
        int response = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to approve this purchase order?", 
                "Confirm Approval", 
                JOptionPane.YES_NO_OPTION);
        
        if (response == JOptionPane.YES_OPTION) {
            // Approve the PO
            boolean approved = poController.approvePurchaseOrder(poId);
            
            if (approved) {
                JOptionPane.showMessageDialog(this, 
                        "Purchase order approved successfully.", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                // Reload POs
                loadPurchaseOrders();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to approve purchase order. It may have already been processed.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Reject selected PO
     */
    private void rejectPO() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        // Get the PO ID
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        
        // Ask for rejection reason
        String reason = JOptionPane.showInputDialog(this, 
                "Please provide a reason for rejection:", 
                "Rejection Reason", 
                JOptionPane.QUESTION_MESSAGE);
        
        if (reason != null && !reason.trim().isEmpty()) {
            // Reject the PO
            boolean rejected = poController.rejectPurchaseOrder(poId, reason);
            
            if (rejected) {
                JOptionPane.showMessageDialog(this, 
                        "Purchase order rejected successfully.", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                // Reload POs
                loadPurchaseOrders();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to reject purchase order. It may have already been processed.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Status filter class for combo box
     */
    private static class StatusFilter {
        private final String displayName;
        private final PurchaseOrder.Status status;
        
        public StatusFilter(String displayName, PurchaseOrder.Status status) {
            this.displayName = displayName;
            this.status = status;
        }
        
        public PurchaseOrder.Status getStatus() {
            return status;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}