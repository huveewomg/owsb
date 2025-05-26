package com.owsb.view.requisition;

import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.inventory.Item;
import com.owsb.model.procurement.PRItem;
import com.owsb.model.procurement.PurchaseRequisition;
import com.owsb.model.supplier.Supplier;
import com.owsb.util.Constants;
import com.owsb.util.SupplierUtils;
import com.owsb.view.PanelHeaderUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel for creating purchase requisitions
 * Demonstrates separation of concerns following MVC pattern
 */
public class PurchaseRequisitionCreationPanel extends JPanel {
    // UI components
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    
    private JLabel requiredDateLabel;
    private JSpinner requiredDateSpinner;
    
    private JLabel notesLabel;
    private JTextArea notesArea;
    private JScrollPane notesScrollPane;
    
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<ItemWrapper> itemComboBox;
    private JSpinner quantitySpinner;
    private JButton addButton;
    private JButton removeButton;
    private JButton quickAddButton;
    
    private JCheckBox urgentCheckBox;
    private JLabel totalItemsLabel;
    private JLabel estimatedValueLabel;
    
    private JButton cancelButton;
    private JButton saveButton;
    private JButton submitButton;
    private String previousSupplierName = null; // To track previous supplier selection
    
    private JComboBox<String> supplierComboBox;
    
    // Controller
    private final PurchaseRequisitionController prController;
    
    // Data
    private final List<PRItem> prItems = new ArrayList<>();
    private Date requiredDate = null;
    private String prId = null; // If editing an existing PR
    private boolean editMode = false;
    
    // Formatters
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor for PurchaseRequisitionCreationPanel
     * @param prController Purchase requisition controller
     */
    public PurchaseRequisitionCreationPanel(PurchaseRequisitionController prController) {
        this.prController = prController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        loadSuppliers();
        // Add listeners
        addListeners();
        updateLabels();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Add a main title/header using PanelHeaderUtils
        JLabel mainTitle = PanelHeaderUtils.createHeaderLabel("Create Purchase Requisition");
        add(mainTitle, BorderLayout.NORTH);
        
        // Top panel - Required date and urgent checkbox
        topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel supplierLabel = new JLabel("Supplier:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(supplierLabel, gbc);
        
        supplierComboBox = new JComboBox<>();
        supplierComboBox.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(supplierComboBox, gbc);
        
        // Required date
        requiredDateLabel = new JLabel("Required By:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        topPanel.add(requiredDateLabel, gbc);
        
        // Calculate default required date (2 weeks from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 14);
        
        // Required date spinner with default date
        SpinnerDateModel dateModel = new SpinnerDateModel(calendar.getTime(), null, null, Calendar.DAY_OF_MONTH);
        requiredDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(requiredDateSpinner, "yyyy-MM-dd");
        requiredDateSpinner.setEditor(dateEditor);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(requiredDateSpinner, gbc);
        
        // Urgent checkbox
        urgentCheckBox = new JCheckBox("Urgent Order (bypass minimum item requirement)");
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        topPanel.add(urgentCheckBox, gbc);
        
        // Notes label
        notesLabel = new JLabel("Notes:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        topPanel.add(notesLabel, gbc);
        
        // Notes text area
        notesArea = new JTextArea(3, 50);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesScrollPane = new JScrollPane(notesArea);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(notesScrollPane, gbc);
        
        // Center panel - Items table and controls
        centerPanel = new JPanel(new BorderLayout(5, 5));
        
        // Create quick add button
        JPanel quickAddPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickAddButton = new JButton("Quick Add Below Minimum");
        quickAddPanel.add(quickAddButton);
        
        // Create table model
        tableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Current Stock", "Min Stock", "Max Stock", "Quantity", "Unit Price", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only quantity column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2, 3, 4, 5 -> {
                        return Integer.class;
                    }
                    case 6, 7 -> {
                        return Double.class;
                    }
                    default -> {
                        return String.class;
                    }
                }
            }
        };
        
        // Create table
        itemsTable = new JTable(tableModel);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create a simple currency renderer
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        // Set column renderers
        itemsTable.getColumnModel().getColumn(6).setCellRenderer(currencyRenderer);
        itemsTable.getColumnModel().getColumn(7).setCellRenderer(currencyRenderer);
        
        // Adjust column widths
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 300));
        
        JPanel itemSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        itemComboBox = new JComboBox<>();
        itemComboBox.setPreferredSize(new Dimension(250, 25));
        itemComboBox.setRenderer(new ItemRenderer());
        
        JLabel quantityLabel = new JLabel("Quantity:");
        
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(1, 1, 10000, 1);
        quantitySpinner = new JSpinner(quantityModel);
        quantitySpinner.setPreferredSize(new Dimension(80, 25));
        
        addButton = new JButton("Add Item");
        removeButton = new JButton("Remove Selected");
        removeButton.setEnabled(false);
        
        itemSelectionPanel.add(new JLabel("Item:"));
        itemSelectionPanel.add(itemComboBox);
        itemSelectionPanel.add(quantityLabel);
        itemSelectionPanel.add(quantitySpinner);
        itemSelectionPanel.add(addButton);
        itemSelectionPanel.add(removeButton);
        
        // Add to center panel
        centerPanel.add(quickAddPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        centerPanel.add(itemSelectionPanel, BorderLayout.SOUTH);
        
        // Bottom panel - Summary and buttons
        bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalItemsLabel = new JLabel("Total Items: 0");
        estimatedValueLabel = new JLabel("Estimated Value: " + currencyFormat.format(0));
        
        summaryPanel.add(totalItemsLabel);
        summaryPanel.add(Box.createHorizontalStrut(30));
        summaryPanel.add(estimatedValueLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Cancel");
        saveButton = new JButton("Save Draft");
        submitButton = new JButton("Submit PR");
        submitButton.setEnabled(false);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(submitButton);
        
        bottomPanel.add(summaryPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Add panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load suppliers for combo box
     */
    private void loadSuppliers() {
        supplierComboBox.removeAllItems();
        List<Supplier> suppliers = SupplierUtils.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            supplierComboBox.addItem(supplier.getName());
        }
        if (supplierComboBox.getItemCount() > 0) {
            supplierComboBox.setSelectedIndex(0);
        }
        loadItemsForSelectedSupplier();
    }
    private void loadItemsForSelectedSupplier() {
        itemComboBox.removeAllItems();
        String selectedSupplierName = (String) supplierComboBox.getSelectedItem();
        if (selectedSupplierName == null) return;
        String supplierId = SupplierUtils.getSupplierNameToIdMap().get(selectedSupplierName);
        Supplier supplier = SupplierUtils.getSupplierById(supplierId);
        if (supplier == null || supplier.getItemIDs() == null) return;
        List<Item> allItems = prController.getAllItems();
        for (Item item : allItems) {
            if (supplier.getItemIDs().contains(item.getItemID())) {
                itemComboBox.addItem(new ItemWrapper(item));
            }
        }
    }
    /**
     * Add listeners to components
     */
    private void addListeners() {
        // Required date spinner listener
        requiredDateSpinner.addChangeListener(e -> requiredDate = (Date) requiredDateSpinner.getValue());
        
        // Quick add button listener
        quickAddButton.addActionListener(e -> quickAddBelowMinimum());
        
        // Add item button listener
        addButton.addActionListener(e -> addItemToTable());
        
        // Remove item button listener
        removeButton.addActionListener(e -> removeSelectedItem());
        
        // Table selection listener
        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            removeButton.setEnabled(itemsTable.getSelectedRow() != -1);
        });
        
        // Table cell edit listener for quantity changes
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 5) { // Quantity column
                int row = e.getFirstRow();
                int quantity = (int) tableModel.getValueAt(row, 5);
                
                // Update the PR item
                if (row >= 0 && row < prItems.size()) {
                    // Get the PR item
                    PRItem prItem = prItems.get(row);
                    
                    // Update quantity
                    prItem.setQuantity(quantity);
                    
                    // Update total in table
                    double total = quantity * prItem.getUnitPrice();
                    total = Math.round(total * 100.0) / 100.0; // Round to 2 decimals
                    tableModel.setValueAt(total, row, 7);
                    
                    // Update labels
                    updateLabels();
                }
            }
        });
        
        // Cancel button listener
        cancelButton.addActionListener(e -> cancel());
        
        // Save button listener
        saveButton.addActionListener(e -> savePR());
        
        // Submit button listener
        submitButton.addActionListener(e -> submitPR());
        
        // Urgent checkbox listener
        urgentCheckBox.addActionListener(e -> updateButtonStates());
        
        supplierComboBox.addActionListener(e -> {
            String selectedSupplierName = (String) supplierComboBox.getSelectedItem();
            if (previousSupplierName == null) previousSupplierName = selectedSupplierName;
            if (!prItems.isEmpty() || !notesArea.getText().trim().isEmpty()) {
                int result = JOptionPane.showConfirmDialog(this,
                        "Changing the supplier will reset all items and notes in this PR. Continue?",
                        "Change Supplier", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    // Revert selection
                    supplierComboBox.setSelectedItem(previousSupplierName);
                    return;
                }
            }
            previousSupplierName = selectedSupplierName;
            // Reset PR
            tableModel.setRowCount(0);
            prItems.clear();
            notesArea.setText("");
            urgentCheckBox.setSelected(false);
            quantitySpinner.setValue(1);
            removeButton.setEnabled(false);
            submitButton.setEnabled(false);
            updateLabels();
            loadItemsForSelectedSupplier();
        });
    }
    
    /**
     * Quick add items below minimum stock
     */
    private void quickAddBelowMinimum() {
        String selectedSupplierName = (String) supplierComboBox.getSelectedItem();
        if (selectedSupplierName == null) {
            JOptionPane.showMessageDialog(this, "Please select a supplier first.", "No Supplier Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String supplierId = SupplierUtils.getSupplierNameToIdMap().get(selectedSupplierName);
        Supplier supplier = SupplierUtils.getSupplierById(supplierId);
        if (supplier == null || supplier.getItemIDs() == null) {
            JOptionPane.showMessageDialog(this, "No items found for the selected supplier.", "No Items", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Item> lowStockItems = prController.getItemsWithLowStock();
        // Only add items from this supplier
        List<Item> filteredLowStock = new ArrayList<>();
        for (Item item : lowStockItems) {
            if (supplier.getItemIDs().contains(item.getItemID())) {
                filteredLowStock.add(item);
            }
        }
        if (filteredLowStock.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No low stock items for the selected supplier.", "No Low Stock Items", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        tableModel.setRowCount(0);
        prItems.clear();
        for (Item item : filteredLowStock) {
            int suggestedQuantity = prController.getSuggestedOrderQuantity(item);
            if (suggestedQuantity <= 0) continue;
            String existingPRs = prController.checkExistingPendingPR(item.getItemID());
            if (existingPRs != null) {
                String message = "Item " + item.getItemID() + " already has pending purchase requisitions:\n" +
                        existingPRs + "\n\nDo you want to include this item in the current PR?";
                int result = JOptionPane.showConfirmDialog(this, message, "Duplicate PR Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) continue;
            }
            PRItem prItem = prController.createPRItemFromItem(item, suggestedQuantity);
            prItems.add(prItem);
            addItemToTable(item, suggestedQuantity);
        }
        updateLabels();
        updateButtonStates();
    }
    
    /**
     * Add item to the table
     */
    private void addItemToTable() {
        // Get selected item
        ItemWrapper selectedWrapper = (ItemWrapper) itemComboBox.getSelectedItem();
        if (selectedWrapper == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select an item.", 
                    "No Item Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Item selectedItem = selectedWrapper.getItem();
        
        // Check for existing PRs for this item
        String existingPRs = prController.checkExistingPendingPR(selectedItem.getItemID());
        
        if (existingPRs != null) {
            // Show warning with option to proceed
            String message = "This item already has pending purchase requisitions:\n" + 
                             existingPRs + "\n\n" +
                             "Do you still want to add this item to the current PR?";
            
            int result = JOptionPane.showConfirmDialog(this, 
                    message, 
                    "Duplicate PR Warning", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Get quantity
        int quantity = (int) quantitySpinner.getValue();
        
        // Check if item already in table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(selectedItem.getItemID())) {
                // Ask if user wants to update quantity
                int response = JOptionPane.showConfirmDialog(this, 
                        "This item is already in the table. Do you want to update the quantity?", 
                        "Item Already Added", 
                        JOptionPane.YES_NO_OPTION);
                
                if (response == JOptionPane.YES_OPTION) {
                    // Update quantity
                    tableModel.setValueAt(quantity, i, 5);
                    
                    // Update the PR item
                    PRItem prItem = prItems.get(i);
                    prItem.setQuantity(quantity);
                    
                    // Update total
                    double total = quantity * prItem.getUnitPrice();
                    total = Math.round(total * 100.0) / 100.0; // Round to 2 decimals
                    tableModel.setValueAt(total, i, 7);
                    
                    // Update labels
                    updateLabels();
                }
                
                return;
            }
        }
        
        // Create PR item
        PRItem prItem = prController.createPRItemFromItem(selectedItem, quantity);
        prItems.add(prItem);
        
        // Add to table
        addItemToTable(selectedItem, quantity);
        
        // Update labels
        updateLabels();
        updateButtonStates();
        
        // Reset quantity spinner
        quantitySpinner.setValue(1);
    }
    
    /**
     * Add item to the table
     * @param item Item to add
     * @param quantity Quantity to add
     */
    private void addItemToTable(Item item, int quantity) {
        // Calculate total
        double total = item.getUnitPrice() * quantity;
        total = Math.round(total * 100.0) / 100.0; // Round to 2 decimals
        // Add to table model
        tableModel.addRow(new Object[]{
                item.getItemID(),
                item.getName(),
                item.getCurrentStock(),
                item.getMinimumStock(),
                item.getMaximumStock(),
                quantity,
                item.getUnitPrice(),
                total
        });
    }
    
    /**
     * Remove selected item from table
     */
    private void removeSelectedItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow != -1) {
            prItems.remove(selectedRow);
            tableModel.removeRow(selectedRow);
            removeButton.setEnabled(false);
            
            // Update labels
            updateLabels();
            updateButtonStates();
        }
    }
    
    /**
     * Update labels with current values
     */
    private void updateLabels() {
        // Update total items
        totalItemsLabel.setText("Total Items: " + prItems.size());
        
        // Calculate estimated value
        double estimatedValue = 0.0;
        for (PRItem item : prItems) {
            estimatedValue += item.getEstimatedCost();
        }
        estimatedValue = Math.round(estimatedValue * 100.0) / 100.0; // Round to 2 decimals
        estimatedValueLabel.setText("Estimated Value: " + currencyFormat.format(estimatedValue));
    }
    
    /**
     * Update button states based on current data
     */
    private void updateButtonStates() {
        boolean hasMinimumItems = prItems.size() >= PurchaseRequisitionController.MINIMUM_ITEMS_REQUIRED;
        boolean isUrgent = urgentCheckBox.isSelected();
        
        submitButton.setEnabled(hasMinimumItems || (isUrgent && !prItems.isEmpty()));
    }
    
    /**
     * Cancel the PR creation
     */
    private void cancel() {
        // Ask for confirmation
        int response = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to cancel? All changes will be lost.", 
                "Confirm Cancel", 
                JOptionPane.YES_NO_OPTION);
        
        if (response == JOptionPane.YES_OPTION) {
            // Clear the form
            clearForm();
            
            // Notify parent
            firePropertyChange("prCancelled", false, true);
        }
    }
    
    /**
     * Save the PR as a draft
     */
    private void savePR() {
        // Check if there are any items
        if (prItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please add at least one item.", 
                    "No Items", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get notes
        String notes = notesArea.getText().trim();
        
        // Get required date
        requiredDate = (Date) requiredDateSpinner.getValue();
        
        // For drafts, we don't need to check the minimum items requirement
        // The urgent flag is only needed for submission, not for drafts
        boolean isUrgent = urgentCheckBox.isSelected();
        
        boolean success;
        
        // Save PR
        if (editMode && prId != null) {
            // Update existing PR
            success = prController.updatePurchaseRequisition(prId, requiredDate, notes, prItems, isUrgent);
        } else {
            // Create new PR
            success = prController.createPurchaseRequisition(requiredDate, notes, prItems, isUrgent);
        }
        
        // Show message
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Purchase requisition saved successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Clear the form
            clearForm();
            
            // Notify parent
            firePropertyChange("prSaved", false, true);
        } else {
            JOptionPane.showMessageDialog(this, 
                    "There was a problem saving the purchase requisition.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Submit the PR for approval
     */
    private void submitPR() {
        // Check if there are any items
        if (prItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please add at least one item.", 
                    "No Items", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check minimum items requirement
        boolean hasMinimumItems = prItems.size() >= PurchaseRequisitionController.MINIMUM_ITEMS_REQUIRED;
        boolean isUrgent = urgentCheckBox.isSelected();
        
        if (!hasMinimumItems && !isUrgent) {
            JOptionPane.showMessageDialog(this, 
                    "A minimum of " + PurchaseRequisitionController.MINIMUM_ITEMS_REQUIRED + 
                    " items is required, or the PR must be marked as urgent.", 
                    "Minimum Items Required", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get notes
        String notes = notesArea.getText().trim();
        
        // Get required date
        requiredDate = (Date) requiredDateSpinner.getValue();
        
        boolean success;
        
        // Save and submit PR
        if (editMode && prId != null) {
            // Update existing PR
            success = prController.updatePurchaseRequisition(prId, requiredDate, notes, prItems, isUrgent);
            
            if (success) {
                // Submit PR
                success = prController.submitPurchaseRequisition(prId);
            }
        } else {
            // Create new PR
            success = prController.createPurchaseRequisition(requiredDate, notes, prItems, isUrgent);
            
            if (success) {
                // Submit the last created PR
                List<String> prIds = prController.getAllPurchaseRequisitionIds();
                if (!prIds.isEmpty()) {
                    String lastPrId = prIds.get(prIds.size() - 1);
                    success = prController.submitPurchaseRequisition(lastPrId);
                } else {
                    success = false;
                }
            }
        }
        
        // Show message
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Purchase requisition submitted successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Clear the form
            clearForm();
            
            // Notify parent
            firePropertyChange("prSubmitted", false, true);
        } else {
            JOptionPane.showMessageDialog(this, 
                    "There was a problem submitting the purchase requisition.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clear the form
     */
    private void clearForm() {
        // Clear table
        tableModel.setRowCount(0);
        prItems.clear();
        
        // Reset components
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 14);
        requiredDateSpinner.setValue(calendar.getTime());
        
        notesArea.setText("");
        urgentCheckBox.setSelected(false);
        quantitySpinner.setValue(1);
        removeButton.setEnabled(false);
        submitButton.setEnabled(false);
        
        // Reset edit mode
        editMode = false;
        prId = null;
        
        // Update labels
        updateLabels();
    }
    
    /**
     * Set up for editing an existing PR
     * @param prId PR ID to edit
     */
    public void setupForEdit(String prId) {
        // Get the PR
        PurchaseRequisition pr = prController.getAllPurchaseRequisitions().stream()
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
        
        // Set edit mode
        editMode = true;
        this.prId = prId;
        
        // Set required date
        requiredDateSpinner.setValue(pr.getRequiredDate());
        
        // Set notes
        notesArea.setText(pr.getNotes());
        
        // Clear existing items
        tableModel.setRowCount(0);
        prItems.clear();
        
        // Add PR items
        for (PRItem prItem : pr.getItems()) {
            // Get the item
            Item item = prController.getItemById(prItem.getItemID());
            if (item == null) {
                continue;
            }
            
            // Add to list
            prItems.add(prItem);
            
            // Add to table
            addItemToTable(item, prItem.getQuantity());
        }
        
        // Update labels
        updateLabels();
        updateButtonStates();
    }
    
    /**
     * Wrapper class for Item objects in combo box
     */
    private static class ItemWrapper {
        private final Item item;
        
        public ItemWrapper(Item item) {
            this.item = item;
        }
        
        public Item getItem() {
            return item;
        }
        
        @Override
        public String toString() {
            return item.getItemID() + " - " + item.getName();
        }
    }
    
    /**
     * Custom renderer for item combo box
     */
    private static class ItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                     boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ItemWrapper wrapper) {
                Item item = wrapper.getItem();
                value = item.getItemID() + " - " + item.getName() + 
                       " (Stock: " + item.getCurrentStock() + "/" + item.getMinimumStock() + ")";
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
    
}