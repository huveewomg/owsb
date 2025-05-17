package com.owsb.view.inventory;

import com.owsb.controller.ItemController;
import com.owsb.controller.PurchaseOrderController;
import com.owsb.model.inventory.Item;
import com.owsb.model.procurement.POItem;
import com.owsb.model.procurement.PurchaseOrder;
import com.owsb.util.Constants;
import com.owsb.util.SupplierUtils;
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
 * Enhanced panel for updating stock - now supports both PO-based and manual updates
 */
public class StockUpdatePanel extends JPanel {
    // Mode constants
    private static final int PO_UPDATE_MODE = 1;
    private static final int MANUAL_UPDATE_MODE = 2;
    
    // UI components
    private JTabbedPane tabbedPane;
    
    // PO Update panel components
    private JPanel poUpdatePanel;
    private JComboBox<String> poComboBox;
    private JButton loadPoButton;
    private JTable poItemsTable;
    private DefaultTableModel poTableModel;
    private JButton quickFillButton;
    private JButton updatePoStockButton;
    private JButton cancelPoButton;
    
    // Manual Update panel components
    private JPanel manualUpdatePanel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton loadAllButton;
    private JTable manualItemsTable;
    private DefaultTableModel manualTableModel;
    private JButton updateManualStockButton;
    private JButton cancelManualButton;
    
    // Controllers and repositories
    private final PurchaseOrderController poController;
    private final ItemController itemController;
    private final ItemRepository itemRepository;
    
    // Data
    private List<PurchaseOrder> approvedPOs;
    private PurchaseOrder selectedPO;
    private Map<String, Integer> adjustedQuantities;
    private List<Item> allItems;
    private Map<String, Item> modifiedItems;
    
    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor for EnhancedStockUpdatePanel
     * @param poController Purchase order controller
     * @param itemController Item controller
     */
    public StockUpdatePanel(PurchaseOrderController poController, ItemController itemController) {
        this.poController = poController;
        this.itemController = itemController;
        this.itemRepository = new ItemRepository();
        this.adjustedQuantities = new HashMap<>();
        this.modifiedItems = new HashMap<>();
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Initialize components for both panels
        initPoUpdatePanel();
        initManualUpdatePanel();
        
        // Add tabs
        tabbedPane.addTab("PO-Based Updates", poUpdatePanel);
        tabbedPane.addTab("Manual Stock Updates", manualUpdatePanel);
        
        // Add to main panel
        add(tabbedPane, BorderLayout.CENTER);
        
        // Load initial data
        loadApprovedPOs();
    }
    
    /**
     * Initialize PO Update Panel components
     */
    private void initPoUpdatePanel() {
        poUpdatePanel = new JPanel(new BorderLayout(10, 10));
        
        // Top panel - PO selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel poLabel = new JLabel("Select Purchase Order:");
        poComboBox = new JComboBox<>();
        loadPoButton = new JButton("Load Items");
        
        topPanel.add(poLabel);
        topPanel.add(poComboBox);
        topPanel.add(loadPoButton);
        
        // Center panel - Items table
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Create table model
        poTableModel = new DefaultTableModel(
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
        poItemsTable = new JTable(poTableModel);
        poItemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(poItemsTable);
        
        // Quick Fill button
        quickFillButton = new JButton("Quick Fill (Use Ordered Qty)");
        quickFillButton.setEnabled(false);
        
        JPanel quickFillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickFillPanel.add(quickFillButton);
        
        // Add to center panel
        centerPanel.add(quickFillPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        updatePoStockButton = new JButton("Update Stock & Complete PO");
        updatePoStockButton.setEnabled(false);
        
        cancelPoButton = new JButton("Cancel");
        
        bottomPanel.add(cancelPoButton);
        bottomPanel.add(updatePoStockButton);
        
        // Add panels to PO update panel
        poUpdatePanel.add(topPanel, BorderLayout.NORTH);
        poUpdatePanel.add(centerPanel, BorderLayout.CENTER);
        poUpdatePanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add listeners
        loadPoButton.addActionListener(e -> loadPOItems());
        quickFillButton.addActionListener(e -> quickFillQuantities());
        updatePoStockButton.addActionListener(e -> updatePoStock());
        cancelPoButton.addActionListener(e -> cancelPoUpdate());
        
        // Table cell edit listener
        poTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 5) { // Received Qty column
                updateAdjustedQuantities();
            }
        });
    }
    
    /**
     * Initialize Manual Update Panel components
     */
    private void initManualUpdatePanel() {
        manualUpdatePanel = new JPanel(new BorderLayout(10, 10));
        
        // Top panel - Search and load all
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search Item:");
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        loadAllButton = new JButton("Load All Items");
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(loadAllButton);
        
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Center panel - Items table
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Create table model for manual updates
        manualTableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Category", "Current Stock", "Min Stock", "Max Stock", "Supplier", "Last Updated"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4 || column == 5; // Current, Min, Max stock are editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 4 || columnIndex == 5) {
                    return Integer.class;
                } 
                return String.class;
            }
        };
        
        // Create table
        manualItemsTable = new JTable(manualTableModel);
        manualItemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        manualItemsTable.getColumnModel().getColumn(0).setPreferredWidth(80); // Item Code
        manualItemsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        manualItemsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Category
        manualItemsTable.getColumnModel().getColumn(3).setPreferredWidth(80); // Current Stock
        manualItemsTable.getColumnModel().getColumn(4).setPreferredWidth(80); // Min Stock
        manualItemsTable.getColumnModel().getColumn(5).setPreferredWidth(80); // Max Stock
        manualItemsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Supplier
        manualItemsTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Last Updated
        
        // Status column renderer with color
        DefaultTableCellRenderer stockRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 3) { // Current stock column
                    int currentStock = (int) value;
                    int minStock = (int) table.getValueAt(row, 4);
                    
                    if (currentStock <= minStock / 2) {
                        c.setBackground(new Color(255, 102, 102)); // Light red for critical
                    } else if (currentStock <= minStock) {
                        c.setBackground(new Color(255, 204, 102)); // Light orange for low
                    } else if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                    } else {
                        c.setBackground(table.getBackground());
                    }
                } else if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(table.getBackground());
                }
                
                return c;
            }
        };
        
        manualItemsTable.getColumnModel().getColumn(3).setCellRenderer(stockRenderer);
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(manualItemsTable);
        
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        updateManualStockButton = new JButton("Update Stock Levels");
        updateManualStockButton.setEnabled(false);
        
        cancelManualButton = new JButton("Cancel");
        
        bottomPanel.add(cancelManualButton);
        bottomPanel.add(updateManualStockButton);
        
        // Add panels to manual update panel
        manualUpdatePanel.add(topPanel, BorderLayout.NORTH);
        manualUpdatePanel.add(centerPanel, BorderLayout.CENTER);
        manualUpdatePanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add listeners
        loadAllButton.addActionListener(e -> loadAllItems());
        searchButton.addActionListener(e -> searchItems());
        updateManualStockButton.addActionListener(e -> updateManualStock());
        cancelManualButton.addActionListener(e -> cancelManualUpdate());
        
        // Table cell edit listener
        manualTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 3 || e.getColumn() == 4 || e.getColumn() == 5) {
                trackModifiedItems();
                updateManualStockButton.setEnabled(true);
            }
        });
    }
    
    /**
     * Track modified items in manual update mode
     */
    private void trackModifiedItems() {
        if (allItems == null) return;
        
        // For each row in the table
        for (int i = 0; i < manualTableModel.getRowCount(); i++) {
            String itemId = (String) manualTableModel.getValueAt(i, 0);
            int currentStock = (int) manualTableModel.getValueAt(i, 3);
            int minStock = (int) manualTableModel.getValueAt(i, 4);
            int maxStock = (int) manualTableModel.getValueAt(i, 5);
            
            // Get the original item
            Item originalItem = null;
            for (Item item : allItems) {
                if (item.getItemID().equals(itemId)) {
                    originalItem = item;
                    break;
                }
            }
            
            if (originalItem != null) {
                // Check if any values have changed
                if (originalItem.getCurrentStock() != currentStock ||
                    originalItem.getMinimumStock() != minStock ||
                    originalItem.getMaximumStock() != maxStock) {
                    
                    // Make a copy of the item with updated values
                    Item modifiedItem = itemRepository.findById(itemId);
                    if (modifiedItem != null) {
                        modifiedItem.setCurrentStock(currentStock);
                        modifiedItem.setMinimumStock(minStock);
                        modifiedItem.setMaximumStock(maxStock);
                        modifiedItems.put(itemId, modifiedItem);
                    }
                }
            }
        }
    }
    
    /**
     * Load all items for manual update
     */
    private void loadAllItems() {
        // Clear the table
        manualTableModel.setRowCount(0);
        
        // Get all items
        allItems = itemController.getAllItems();
        Map<String, String> supplierMap = SupplierUtils.getSupplierIdToNameMap();
        
        // Add items to table
        for (Item item : allItems) {
            String supplierName = supplierMap.getOrDefault(item.getSupplierID(), "Unknown");
            
            manualTableModel.addRow(new Object[]{
                    item.getItemID(),
                    item.getName(),
                    item.getCategory(),
                    item.getCurrentStock(),
                    item.getMinimumStock(),
                    item.getMaximumStock(),
                    supplierName,
                    item.getLastUpdated()
            });
        }
        
        // Enable update button if items loaded
        updateManualStockButton.setEnabled(false);
        modifiedItems.clear();
    }
    
    /**
     * Search for items matching the search term
     */
    private void searchItems() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        
        if (searchTerm.isEmpty()) {
            loadAllItems();
            return;
        }
        
        // Clear the table
        manualTableModel.setRowCount(0);
        
        // Get all items and filter
        if (allItems == null) {
            allItems = itemController.getAllItems();
        }
        
        Map<String, String> supplierMap = SupplierUtils.getSupplierIdToNameMap();
        
        // Filter and add matching items to table
        for (Item item : allItems) {
            if (item.getItemID().toLowerCase().contains(searchTerm) ||
                item.getName().toLowerCase().contains(searchTerm) ||
                item.getCategory().toLowerCase().contains(searchTerm)) {
                
                String supplierName = supplierMap.getOrDefault(item.getSupplierID(), "Unknown");
                
                manualTableModel.addRow(new Object[]{
                        item.getItemID(),
                        item.getName(),
                        item.getCategory(),
                        item.getCurrentStock(),
                        item.getMinimumStock(),
                        item.getMaximumStock(),
                        supplierName,
                        item.getLastUpdated()
                });
            }
        }
        
        // Enable update button if items loaded
        updateManualStockButton.setEnabled(false);
        modifiedItems.clear();
    }
    
    /**
     * Update stock manually
     */
    private void updateManualStock() {
        // Confirm update
        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to update these stock levels?", 
                "Confirm Stock Update", 
                JOptionPane.YES_NO_OPTION);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Save all modified items
        boolean success = true;
        int updatedCount = 0;
        
        for (Item item : modifiedItems.values()) {
            success = itemRepository.update(item) && success;
            updatedCount++;
        }
        
        // Show result
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Successfully updated " + updatedCount + " items.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Reload items
            loadAllItems();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "There was a problem updating some items.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Cancel manual stock update
     */
    private void cancelManualUpdate() {
        // Reset
        manualTableModel.setRowCount(0);
        updateManualStockButton.setEnabled(false);
        modifiedItems.clear();
        loadAllItems();
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
        updatePoStockButton.setEnabled(false);
        quickFillButton.setEnabled(false);
        
        // Clear table
        poTableModel.setRowCount(0);
        
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
        poTableModel.setRowCount(0);
        
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
            
            poTableModel.addRow(new Object[]{
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
        boolean hasItems = poTableModel.getRowCount() > 0;
        quickFillButton.setEnabled(hasItems);
        updatePoStockButton.setEnabled(false); // Don't enable until quantities are set
    }
    
    /**
     * Quick fill all received quantities with ordered quantities
     */
    private void quickFillQuantities() {
        if (selectedPO == null) {
            return;
        }
        
        // Fill each row
        for (int i = 0; i < poTableModel.getRowCount(); i++) {
            String itemId = (String) poTableModel.getValueAt(i, 0);
            int orderedQty = (int) poTableModel.getValueAt(i, 3);
            int currentStock = (int) poTableModel.getValueAt(i, 4);
            
            // Set received quantity to ordered quantity
            poTableModel.setValueAt(orderedQty, i, 5);
            
            // Update after update column
            poTableModel.setValueAt(currentStock + orderedQty, i, 6);
            
            // Update adjusted quantities
            adjustedQuantities.put(itemId, orderedQty);
        }
        
        // Enable update button
        updatePoStockButton.setEnabled(true);
    }
    
    /**
     * Update adjusted quantities based on table values
     */
    private void updateAdjustedQuantities() {
        boolean hasNonZeroQuantity = false;
        
        // Update quantities for each row
        for (int i = 0; i < poTableModel.getRowCount(); i++) {
            String itemId = (String) poTableModel.getValueAt(i, 0);
            int receivedQty = (int) poTableModel.getValueAt(i, 5);
            int currentStock = (int) poTableModel.getValueAt(i, 4);
            
            // Update adjusted quantities
            adjustedQuantities.put(itemId, receivedQty);
            
            // Update after update column
            poTableModel.setValueAt(currentStock + receivedQty, i, 6);
            
            if (receivedQty > 0) {
                hasNonZeroQuantity = true;
            }
        }
        
        // Enable update button if at least one item has a non-zero quantity
        updatePoStockButton.setEnabled(hasNonZeroQuantity);
    }
    
    /**
     * Update stock levels based on PO and complete PO
     */
    private void updatePoStock() {
        // Check if all items have received quantities
        for (int i = 0; i < poTableModel.getRowCount(); i++) {
            int receivedQty = (int) poTableModel.getValueAt(i, 5);
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
        for (int i = 0; i < poTableModel.getRowCount(); i++) {
            String itemId = (String) poTableModel.getValueAt(i, 0);
            int receivedQty = (int) poTableModel.getValueAt(i, 5);
            
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
     * Cancel PO stock update
     */
    private void cancelPoUpdate() {
        // Clear table
        poTableModel.setRowCount(0);
        
        // Reset button states
        updatePoStockButton.setEnabled(false);
        quickFillButton.setEnabled(false);
        
        // Reload approved POs
        loadApprovedPOs();
    }
}