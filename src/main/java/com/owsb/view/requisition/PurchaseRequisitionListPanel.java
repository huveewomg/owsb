package com.owsb.view.requisition;

import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.PRItem;
import com.owsb.model.PurchaseRequisition;
import com.owsb.model.PurchaseRequisition.Status;
import com.owsb.model.User;
import com.owsb.model.user.SalesManager;
import com.owsb.model.user.PurchaseManager;
import com.owsb.util.UserRole;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Panel for viewing purchase requisitions
 * Demonstrates role-based access control and MVC pattern
 */
public class PurchaseRequisitionListPanel extends JPanel {
    // UI components
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    
    private JTable prTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<StatusFilter> statusFilterComboBox;
    private JButton refreshButton;
    
    private JButton viewButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton createPoButton; // For Purchase Managers
    
    // Controller
    private final PurchaseRequisitionController prController;
    
    // Data
    private List<PurchaseRequisition> prs;
    private Map<String, String> salesManagerNames;
    
    // Current user
    private final User currentUser;
    
    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor for PurchaseRequisitionListPanel
     * @param prController Purchase requisition controller
     * @param currentUser Current user
     */
    public PurchaseRequisitionListPanel(PurchaseRequisitionController prController, User currentUser) {
        this.prController = prController;
        this.currentUser = currentUser;
        this.salesManagerNames = new HashMap<>();
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize components
        initComponents();
        
        // Add listeners
        addListeners();
        
        // Load data
        loadPurchaseRequisitions();
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
        
        // Center panel - PR table
        centerPanel = new JPanel(new BorderLayout());
        
        // Create table model
        tableModel = new DefaultTableModel(
                new Object[]{"PR ID", "Date", "Required By", "Created By", "Status", "Items", "Est. Value", "Notes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        prTable = new JTable(tableModel);
        prTable.getTableHeader().setReorderingAllowed(false);
        prTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        prTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Create simple renderers
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        // Set column renderers
        prTable.getColumnModel().getColumn(1).setCellRenderer(dateRenderer);
        prTable.getColumnModel().getColumn(2).setCellRenderer(dateRenderer);
        prTable.getColumnModel().getColumn(6).setCellRenderer(currencyRenderer);
        
        // Set column widths
        prTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        prTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        prTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        prTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        prTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        prTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        prTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        prTable.getColumnModel().getColumn(7).setPreferredWidth(200);
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(prTable);
        tableScrollPane.setPreferredSize(new Dimension(900, 350));
        
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        viewButton = new JButton("View");
        viewButton.setEnabled(false);
        
        editButton = new JButton("Edit");
        editButton.setEnabled(false);
        
        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        
        createPoButton = new JButton("Generate PO");
        createPoButton.setEnabled(false);
        
        bottomPanel.add(viewButton);
        
        // Only add Edit and Delete buttons for Sales Managers
        if (currentUser instanceof SalesManager) {
            bottomPanel.add(editButton);
            bottomPanel.add(deleteButton);
        }
        
        // Only add Generate PO button for Purchase Managers
        if (currentUser instanceof PurchaseManager || 
            currentUser.getRole() == UserRole.PURCHASE_MANAGER) {
            bottomPanel.add(createPoButton);
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
                new StatusFilter("New", Status.NEW),
                new StatusFilter("Pending Approval", Status.PENDING_APPROVAL),
                new StatusFilter("Approved", Status.APPROVED),
                new StatusFilter("Rejected", Status.REJECTED),
                new StatusFilter("Processed", Status.PROCESSED)
        };
    }
    
    /**
     * Add listeners to components
     */
    private void addListeners() {
        // Status filter combo box listener
        statusFilterComboBox.addActionListener(e -> filterPRs());
        
        // Refresh button listener
        refreshButton.addActionListener(e -> loadPurchaseRequisitions());
        
        // Table selection listener
        prTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = prTable.getSelectedRow();
            boolean hasSelection = selectedRow != -1;
            
            viewButton.setEnabled(hasSelection);
            
            // Enable/disable edit and delete buttons based on selection and status
            if (hasSelection && currentUser instanceof SalesManager) {
                String prId = (String) prTable.getValueAt(selectedRow, 0);
                
                // Find the PR in our list
                PurchaseRequisition selectedPR = null;
                for (PurchaseRequisition pr : prs) {
                    if (pr.getPrID().equals(prId)) {
                        selectedPR = pr;
                        break;
                    }
                }
                
                if (selectedPR != null) {
                    // Only enable edit and delete for NEW PRs created by the current user
                    boolean canEdit = selectedPR.getStatus() == Status.NEW && 
                                      selectedPR.getSalesManagerID().equals(currentUser.getUserId());
                    
                    editButton.setEnabled(canEdit);
                    deleteButton.setEnabled(canEdit);
                }
            } else {
                editButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
            
            // Enable/disable create PO button based on selection and status
            if (hasSelection && (currentUser instanceof PurchaseManager || 
                                currentUser.getRole() == UserRole.PURCHASE_MANAGER)) {
                String prId = (String) prTable.getValueAt(selectedRow, 0);
                
                // Find the PR in our list
                PurchaseRequisition selectedPR = null;
                for (PurchaseRequisition pr : prs) {
                    if (pr.getPrID().equals(prId)) {
                        selectedPR = pr;
                        break;
                    }
                }
                
                if (selectedPR != null) {
                    // Only enable create PO for PENDING_APPROVAL PRs
                    boolean canCreatePO = selectedPR.getStatus() == Status.PENDING_APPROVAL;
                    
                    createPoButton.setEnabled(canCreatePO);
                }
            } else {
                createPoButton.setEnabled(false);
            }
        });
        
        // View button listener
        viewButton.addActionListener(e -> viewPR());
        
        // Edit button listener
        editButton.addActionListener(e -> editPR());
        
        // Delete button listener
        deleteButton.addActionListener(e -> deletePR());
        
        // Create PO button listener
        createPoButton.addActionListener(e -> createPO());
    }
    
    /**
     * Load purchase requisitions
     */
    public void loadPurchaseRequisitions() {
        // Get PRs based on user role
        if (currentUser instanceof SalesManager) {
            // For sales managers, show only their PRs
            prs = prController.getMyPurchaseRequisitions();
        } else {
            // For others, show all PRs
            prs = prController.getAllPurchaseRequisitions();
        }
        
        // Apply filter
        filterPRs();
    }
    
    /**
     * Filter PRs based on selected status
     */
    private void filterPRs() {
        // Clear table
        tableModel.setRowCount(0);
        
        // Get selected filter
        StatusFilter filter = (StatusFilter) statusFilterComboBox.getSelectedItem();
        
        if (filter == null) {
            return;
        }
        
        // Filter PRs
        List<PurchaseRequisition> filteredPRs;
        
        if (filter.getStatus() == null) {
            // Show all
            filteredPRs = new ArrayList<>(prs);
        } else {
            // Filter by status
            filteredPRs = prs.stream()
                    .filter(pr -> pr.getStatus() == filter.getStatus())
                    .toList();
        }
        
        // Add to table
        for (PurchaseRequisition pr : filteredPRs) {
            // Get the sales manager name
            String salesManagerName = getSalesManagerName(pr.getSalesManagerID());
            
            tableModel.addRow(new Object[]{
                    pr.getPrID(),
                    pr.getDate(),
                    pr.getRequiredDate(),
                    salesManagerName,
                    pr.getStatus().getDisplayName(),
                    pr.getItemCount(),
                    pr.getEstimatedTotal(),
                    pr.getNotes()
            });
        }
        
        // Reset button states
        viewButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        createPoButton.setEnabled(false);
    }
    
    /**
     * Get sales manager name from ID
     * @param salesManagerID Sales manager ID
     * @return Sales manager name
     */
    private String getSalesManagerName(String salesManagerID) {
        // Check cache first
        if (salesManagerNames.containsKey(salesManagerID)) {
            return salesManagerNames.get(salesManagerID);
        }
        
        // TODO: Get sales manager name from user repository
        // For now, just return the ID
        String name = salesManagerID + " (Sales Manager)";
        
        // Cache the name
        salesManagerNames.put(salesManagerID, name);
        
        return name;
    }
    
    /**
     * View selected PR
     */
    private void viewPR() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        // Get the PR ID
        String prId = (String) prTable.getValueAt(selectedRow, 0);
        
        // Get the PR
        PurchaseRequisition pr = prs.stream()
                .filter(p -> p.getPrID().equals(prId))
                .findFirst()
                .orElse(null);
        
        if (pr == null) {
            JOptionPane.showMessageDialog(this, 
                    "Purchase requisition not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create and show PR viewer dialog
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                "Purchase Requisition Details: " + prId, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        // Create PR details panel
        JPanel prDetailsPanel = createPRDetailsPanel(pr);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        // Add panels to dialog
        dialog.add(prDetailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Create PR details panel
     * @param pr Purchase requisition
     * @return Panel with PR details
     */
    private JPanel createPRDetailsPanel(PurchaseRequisition pr) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create header panel
        JPanel headerPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("PR Details"));
        
        // Add header fields
        addLabelField(headerPanel, "PR ID:", pr.getPrID());
        addLabelField(headerPanel, "Date:", dateFormat.format(pr.getDate()));
        addLabelField(headerPanel, "Required By:", dateFormat.format(pr.getRequiredDate()));
        addLabelField(headerPanel, "Created By:", getSalesManagerName(pr.getSalesManagerID()));
        addLabelField(headerPanel, "Status:", pr.getStatus().getDisplayName());
        addLabelField(headerPanel, "Notes:", pr.getNotes());
        
        // Create items table
        DefaultTableModel itemsTableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Quantity", "Unit Price", "Total", "Supplier"}, 0) {
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
        
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(rightAlignRenderer);
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(rightAlignRenderer);
        
        // Add items to table
        for (PRItem item : pr.getItems()) {
            // Get the item and supplier names
            // In a real implementation, these would be fetched from the repositories
            String itemName = item.getItemName();
            String supplierName = item.getSuggestedSupplierID();
            
            itemsTableModel.addRow(new Object[]{
                    item.getItemID(),
                    itemName,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getEstimatedCost(),
                    supplierName
            });
        }
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Items"));
        
        // Add summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.add(new JLabel("Total Items: " + pr.getItemCount()));
        summaryPanel.add(Box.createHorizontalStrut(20));
        summaryPanel.add(new JLabel("Estimated Value: " + currencyFormat.format(pr.getEstimatedTotal())));
        
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
     * Edit selected PR
     */
    private void editPR() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        // Get the PR ID
        String prId = (String) prTable.getValueAt(selectedRow, 0);
        
        // Notify parent to switch to PR creation panel in edit mode
        firePropertyChange("editPR", null, prId);
    }
    
    /**
     * Delete selected PR
     */
    private void deletePR() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        // Get the PR ID
        String prId = (String) prTable.getValueAt(selectedRow, 0);
        
        // Ask for confirmation
        int response = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this purchase requisition?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
        
        if (response == JOptionPane.YES_OPTION) {
            // Delete the PR
            boolean deleted = prController.deletePurchaseRequisition(prId);
            
            if (deleted) {
                JOptionPane.showMessageDialog(this, 
                        "Purchase requisition deleted successfully.", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                // Reload PRs
                loadPurchaseRequisitions();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to delete purchase requisition. It may have already been processed.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Create a purchase order from selected PR
     */
    private void createPO() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        // Get the PR ID
        String prId = (String) prTable.getValueAt(selectedRow, 0);
        
        // TODO: Implement PO creation
        // For now, just update the PR status
        boolean updated = prController.changePurchaseRequisitionStatus(prId, Status.PROCESSED);
        
        if (updated) {
            JOptionPane.showMessageDialog(this, 
                    "Purchase requisition processed successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Reload PRs
            loadPurchaseRequisitions();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to process purchase requisition.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Status filter class for combo box
     */
    private static class StatusFilter {
        private final String displayName;
        private final Status status;
        
        public StatusFilter(String displayName, Status status) {
            this.displayName = displayName;
            this.status = status;
        }
        
        public Status getStatus() {
            return status;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
}