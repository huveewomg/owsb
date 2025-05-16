package com.owsb.view.inventory;

import com.owsb.controller.PurchaseOrderController;
import com.owsb.model.Item;
import com.owsb.model.POItem;
import com.owsb.model.PurchaseOrder;
import com.owsb.util.Constants;
import com.owsb.repository.ItemRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for updating stock when receiving purchase orders
 * Demonstrates MVC pattern and stock management functionality
 */
public class StockUpdatePanel extends JPanel {
    // UI components
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    
    private JComboBox<String> poComboBox;
    private JButton loadButton;
    
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JButton quickFillButton;
    
    private JButton updateStockButton;
    private JButton cancelButton;
    
    // Controllers and repositories
    private final PurchaseOrderController poController;
    private final ItemRepository itemRepository;
    
    // Data
    private List<PurchaseOrder> approvedPOs;
    private PurchaseOrder selectedPO;
    private Map<String, Integer> adjustedQuantities;
    
    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor for StockUpdatePanel
     * @param poController Purchase order controller
     */
    public StockUpdatePanel(PurchaseOrderController poController) {
        this.poController = poController;
        this.itemRepository = new ItemRepository();
        this.adjustedQuantities = new HashMap<>();
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize components
        initComponents();
        
        // Add listeners
        addListeners();
        
        // Load data
        loadApprovedPOs();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Top panel - PO selection
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel poLabel = new JLabel("Select Purchase Order:");
        poComboBox = new JComboBox<>();
        loadButton = new JButton("Load Items");
        
        topPanel.add(poLabel);
        topPanel.add(poComboBox);
        topPanel.add(loadButton);
        
        // Center panel - Items table
        centerPanel = new JPanel(new BorderLayout());
        
        // Create table model
        tableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Supplier", "Ordered Qty", "Current Stock", "Received Qty", "After Update"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only received quantity column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 4 || columnIndex == 5 || columnIndex == 6) {
                    return Integer.class;
                } 
                return String.class;
            }
        };
        
        // Create table
        itemsTable = new JTable(tableModel);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        
        // Quick Fill button
        quickFillButton = new JButton("Quick Fill (Use Ordered Qty)");
        quickFillButton.setEnabled(false);
        
        JPanel quickFillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickFillPanel.add(quickFillButton);
        
        // Add to center panel
        centerPanel.add(quickFillPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        updateStockButton = new JButton("Update Stock & Complete PO");
        updateStockButton.setEnabled(false);
        
        cancelButton = new JButton("Cancel");
        
        bottomPanel.add(cancelButton);
        bottomPanel.add(updateStockButton);
        
        // Add panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Add listeners to components
     */
    private void addListeners() {
        // Load button listener
        loadButton.addActionListener(e -> loadPOItems());
        
        // Quick Fill button listener
        quickFillButton.addActionListener(e -> quickFillQuantities());
        
        // Table cell edit listener
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 5) { // Received Qty column
                updateAdjustedQuantities();
            }
        });
        
        // Update stock button listener
        updateStockButton.addActionListener(e -> updateStock());
        
        // Cancel button listener
        cancelButton.addActionListener(e -> cancel());
    }
    
    /**
     * Load approved purchase orders
     */
    private void loadApprovedPOs() {
        // Get approved POs that are pending arrival
        approvedPOs = poController.getPurchaseOrdersByStatus(Constants.PurchaseOrderStatus.PENDING_ARRIVAL);
        
        // Clear combo box
        poComboBox.removeAllItems();
        
        // Add POs to combo box
        for (PurchaseOrder po : approvedPOs) {
            poComboBox.addItem(po.getPoID() + " - PR: " + po.getPrID() + " - Date: " + dateFormat.format(po.getDate()));
        }
        
        // Reset button states
        updateStockButton.setEnabled(false);
        quickFillButton.setEnabled(false);
        
        // Clear table
        tableModel.setRowCount(0);
        
        // Clear adjusted quantities
        adjustedQuantities.clear();
    }
    
    /**
     * Load items from selected PO
     */
    private void loadPOItems() {
        int selectedIndex = poComboBox.getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex >= approvedPOs.size()) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a purchase order.", 
                    "No PO Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected PO
        selectedPO = approvedPOs.get(selectedIndex);
        
        // Clear table
        tableModel.setRowCount(0);
        
        // Clear adjusted quantities
        adjustedQuantities.clear();
        
        // Add items to table
        for (POItem poItem : selectedPO.getItems()) {
            // Get the item from repository
            Item item = itemRepository.findById(poItem.getItemID());
            int currentStock = item != null ? item.getCurrentStock() : 0;
            
            // Default received quantity to ordered quantity
            int receivedQty = 0; // Start with 0, let user fill in or use quick fill
            int afterUpdate = currentStock + receivedQty;
            
            // Save the initial quantity
            adjustedQuantities.put(poItem.getItemID(), receivedQty);
            
            tableModel.addRow(new Object[]{
                    poItem.getItemID(),
                    poItem.getItemName(),
                    poItem.getSupplierName(),
                    poItem.getQuantity(),
                    currentStock,
                    receivedQty,
                    afterUpdate
            });
        }
        
        // Enable buttons if items loaded
        boolean hasItems = tableModel.getRowCount() > 0;
        quickFillButton.setEnabled(hasItems);
        updateStockButton.setEnabled(false); // Don't enable until quantities are set
    }
    
    /**
     * Quick fill all received quantities with ordered quantities
     */
    private void quickFillQuantities() {
        if (selectedPO == null) {
            return;
        }
        
        // Fill each row
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String itemId = (String) tableModel.getValueAt(i, 0);
            int orderedQty = (int) tableModel.getValueAt(i, 3);
            int currentStock = (int) tableModel.getValueAt(i, 4);
            
            // Set received quantity to ordered quantity
            tableModel.setValueAt(orderedQty, i, 5);
            
            // Update after update column
            tableModel.setValueAt(currentStock + orderedQty, i, 6);
            
            // Update adjusted quantities
            adjustedQuantities.put(itemId, orderedQty);
        }
        
        // Enable update button
        updateStockButton.setEnabled(true);
    }
    
    /**
     * Update adjusted quantities based on table values
     */
    private void updateAdjustedQuantities() {
        boolean hasNonZeroQuantity = false;
        
        // Update quantities for each row
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String itemId = (String) tableModel.getValueAt(i, 0);
            int receivedQty = (int) tableModel.getValueAt(i, 5);
            int currentStock = (int) tableModel.getValueAt(i, 4);
            
            // Update adjusted quantities
            adjustedQuantities.put(itemId, receivedQty);
            
            // Update after update column
            tableModel.setValueAt(currentStock + receivedQty, i, 6);
            
            if (receivedQty > 0) {
                hasNonZeroQuantity = true;
            }
        }
        
        // Enable update button if at least one item has a non-zero quantity
        updateStockButton.setEnabled(hasNonZeroQuantity);
    }
    
    /**
     * Update stock levels and complete PO
     */
    private void updateStock() {
        // Check if all items have received quantities
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int receivedQty = (int) tableModel.getValueAt(i, 5);
            if (receivedQty <= 0) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Some items have zero received quantity. Do you want to continue?",
                        "Confirm Update",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
                break;
            }
        }
        
        // Confirm update
        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to update stock and mark this purchase order as received?", 
                "Confirm Stock Update", 
                JOptionPane.YES_NO_OPTION);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Update stock for each item
        boolean success = true;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String itemId = (String) tableModel.getValueAt(i, 0);
            int receivedQty = (int) tableModel.getValueAt(i, 5);
            
            if (receivedQty <= 0) {
                continue; // Skip items with zero quantity
            }
            
            // Get the item from repository
            Item item = itemRepository.findById(itemId);
            if (item != null) {
                // Update stock
                boolean itemUpdateSuccess = item.updateStock(receivedQty);
                if (!itemUpdateSuccess) {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update stock for item " + itemId + ". Operation would result in negative stock.",
                        "Stock Update Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
                success = itemRepository.update(item) && success && itemUpdateSuccess;
            }
        }
        
        // Mark the PO as received
        if (success) {
            success = poController.markPurchaseOrderReceived(selectedPO.getPoID());
        }
        
        // Show result
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Stock updated and purchase order marked as received successfully. It's now ready for payment.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Reload approved POs
            loadApprovedPOs();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "There was a problem updating stock or marking the purchase order as received.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Cancel stock update
     */
    private void cancel() {
        // Clear table
        tableModel.setRowCount(0);
        
        // Reset button states
        updateStockButton.setEnabled(false);
        quickFillButton.setEnabled(false);
        
        // Reload approved POs
        loadApprovedPOs();
    }
}