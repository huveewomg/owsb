package com.owsb.view.supplier;

import com.owsb.controller.ItemController;
import com.owsb.controller.SupplierController;
import com.owsb.model.inventory.Item;
import com.owsb.model.supplier.Supplier;
import com.owsb.view.PanelHeaderUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * panel for supplier management operations
 * Allows managing which items a supplier can provide
 */
public class SupplierManagementPanel extends JPanel {
    // UI Components
    private JLabel supplierCodeLabel;
    private JTextField nameField;
    private JTextField contactPersonField;
    private JTextField phoneField;
    private JTable supplierTable;
    private DefaultTableModel supplierTableModel;
    
    // Item assignment components
    private JTable supplierItemsTable;
    private DefaultTableModel supplierItemsTableModel;
    private JTable availableItemsTable;
    private DefaultTableModel availableItemsTableModel;
    
    // Controllers
    private final SupplierController supplierController;
    private final ItemController itemController;
    
    // Current supplier
    private Supplier currentSupplier;
    
    /**
     * Constructor
     * @param supplierController Supplier controller
     * @param itemController Item controller
     */
    public SupplierManagementPanel(SupplierController supplierController, ItemController itemController) {
        this.supplierController = supplierController;
        this.itemController = itemController;
        
        // Set up panel
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create header
        JLabel headerLabel = PanelHeaderUtils.createHeaderLabel("Supplier Management");
        add(headerLabel, BorderLayout.NORTH);
        
        // Create main content panel with supplier details and item management
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(300);
        
        // Top section: Supplier details and list
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplitPane.setDividerLocation(400);
        
        // Add form panel to left side
        JPanel formPanel = createFormPanel();
        topSplitPane.setLeftComponent(formPanel);
        
        // Add supplier table panel to right side
        JPanel tablePanel = createSupplierTablePanel();
        topSplitPane.setRightComponent(tablePanel);
        
        mainSplitPane.setTopComponent(topSplitPane);
        
        // Bottom section: Item assignment
        JPanel itemAssignmentPanel = createItemAssignmentPanel();
        mainSplitPane.setBottomComponent(itemAssignmentPanel);
        
        // Add main split pane to center
        add(mainSplitPane, BorderLayout.CENTER);
        
        // Initial data load
        refreshSupplierTable();
    }
    
    /**
     * Create form panel with input fields and buttons
     * @return Form panel
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder("Supplier Details")
        ));
        
        // Form fields panel
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        
        // Supplier code (generated by controller)
        supplierCodeLabel = new JLabel(supplierController.generateNextSupplierId());
        nameField = new JTextField();
        contactPersonField = new JTextField();
        phoneField = new JTextField();
        
        fieldsPanel.add(new JLabel("Supplier Code:"));
        fieldsPanel.add(supplierCodeLabel);
        fieldsPanel.add(new JLabel("Supplier Name:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(new JLabel("Contact Person:"));
        fieldsPanel.add(contactPersonField);
        fieldsPanel.add(new JLabel("Phone:"));
        fieldsPanel.add(phoneField);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Supplier");
        JButton updateButton = new JButton("Update Supplier");
        JButton deleteButton = new JButton("Delete Supplier");
        JButton clearButton = new JButton("Clear Form");
        
        // Add Supplier action
        addButton.addActionListener((ActionEvent e) -> {
            String name = nameField.getText().trim();
            String contactPerson = contactPersonField.getText().trim();
            String phone = phoneField.getText().trim();
            
            // Basic validation
            if (name.isEmpty() || contactPerson.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all fields.");
                return;
            }
            
            // Use controller to add supplier
            boolean success = supplierController.addSupplier(name, contactPerson, phone);
            
            if (success) {
                JOptionPane.showMessageDialog(panel, "Supplier added successfully.");
                clearForm();
                supplierCodeLabel.setText(supplierController.generateNextSupplierId());
                refreshSupplierTable();
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to add supplier.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Update Supplier action
        updateButton.addActionListener((ActionEvent e) -> {
            if (currentSupplier == null) {
                JOptionPane.showMessageDialog(panel, "Please select a supplier to update.");
                return;
            }
            
            String supplierId = currentSupplier.getSupplierID();
            String name = nameField.getText().trim();
            String contactPerson = contactPersonField.getText().trim();
            String phone = phoneField.getText().trim();
            
            // Use controller to update supplier
            boolean success = supplierController.updateSupplier(supplierId, name, contactPerson, phone);
            
            if (success) {
                JOptionPane.showMessageDialog(panel, "Supplier updated successfully.");
                refreshSupplierTable();
                // Keep the same supplier selected
                for (int i = 0; i < supplierTable.getRowCount(); i++) {
                    if (supplierTable.getValueAt(i, 0).equals(supplierId)) {
                        supplierTable.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to update supplier.", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Delete Supplier action
        deleteButton.addActionListener((ActionEvent e) -> {
            if (currentSupplier == null) {
                JOptionPane.showMessageDialog(panel, "Please select a supplier to delete.");
                return;
            }
            
            String supplierId = currentSupplier.getSupplierID();
            
            int confirm = JOptionPane.showConfirmDialog(panel, 
                "Are you sure you want to delete this supplier?", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = supplierController.deleteSupplier(supplierId);
                
                if (success) {
                    JOptionPane.showMessageDialog(panel, "Supplier deleted successfully.");
                    clearForm();
                    refreshSupplierTable();
                    clearItemTables();
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed to delete supplier.", 
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Clear form action
        clearButton.addActionListener(e -> {
            clearForm();
            supplierTable.clearSelection();
            clearItemTables();
        });
        
        // Add buttons to panel
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        
        // Combine components
        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create supplier table panel
     * @return Supplier table panel
     */
    private JPanel createSupplierTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder("Supplier List")
        ));
        
        // Table model with non-editable cells
        supplierTableModel = new DefaultTableModel(
            new String[]{"Supplier Code", "Name", "Contact Person", "Phone"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        supplierTable = new JTable(supplierTableModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierTable.setFillsViewportHeight(true);
        
        // Add selection listener
        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = supplierTable.getSelectedRow();
                if (selectedRow != -1) {
                    String supplierId = (String) supplierTableModel.getValueAt(selectedRow, 0);
                    currentSupplier = supplierController.getSupplierById(supplierId);
                    
                    if (currentSupplier != null) {
                        nameField.setText(currentSupplier.getName());
                        contactPersonField.setText(currentSupplier.getContactPerson());
                        phoneField.setText(currentSupplier.getPhone());
                        
                        // Update item tables
                        refreshItemTables();
                    }
                } else {
                    currentSupplier = null;
                    clearItemTables();
                }
            }
        });
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshSupplierTable());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create item assignment panel
     * @return Item assignment panel
     */
    private JPanel createItemAssignmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder("Item Assignment")
        ));
        
        // Split pane for supplier items and available items
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        
        // Supplier items panel
        JPanel supplierItemsPanel = new JPanel(new BorderLayout());
        supplierItemsPanel.setBorder(BorderFactory.createTitledBorder("Supplier Items"));
        
        supplierItemsTableModel = new DefaultTableModel(
            new String[]{"Item Code", "Name", "Description", "Price", "Category"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        supplierItemsTable = new JTable(supplierItemsTableModel);
        supplierItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierItemsTable.setFillsViewportHeight(true);
        
        JScrollPane supplierItemsScrollPane = new JScrollPane(supplierItemsTable);
        supplierItemsPanel.add(supplierItemsScrollPane, BorderLayout.CENTER);
        
        JButton removeItemButton = new JButton("Remove Selected Item");
        removeItemButton.addActionListener(e -> removeItemFromSupplier());
        
        JPanel supplierItemsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        supplierItemsButtonPanel.add(removeItemButton);
        supplierItemsPanel.add(supplierItemsButtonPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(supplierItemsPanel);
        
        // Available items panel
        JPanel availableItemsPanel = new JPanel(new BorderLayout());
        availableItemsPanel.setBorder(BorderFactory.createTitledBorder("Available Items"));
        
        availableItemsTableModel = new DefaultTableModel(
            new String[]{"Item Code", "Name", "Description", "Price", "Category"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        availableItemsTable = new JTable(availableItemsTableModel);
        availableItemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableItemsTable.setFillsViewportHeight(true);
        
        JScrollPane availableItemsScrollPane = new JScrollPane(availableItemsTable);
        availableItemsPanel.add(availableItemsScrollPane, BorderLayout.CENTER);
        
        JButton addItemButton = new JButton("Add Selected Item");
        addItemButton.addActionListener(e -> addItemToSupplier());
        
        JPanel availableItemsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        availableItemsButtonPanel.add(addItemButton);
        availableItemsPanel.add(availableItemsButtonPanel, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(availableItemsPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Clear form fields
     */
    private void clearForm() {
        nameField.setText("");
        contactPersonField.setText("");
        phoneField.setText("");
        currentSupplier = null;
    }
    
    /**
     * Clear item tables
     */
    private void clearItemTables() {
        supplierItemsTableModel.setRowCount(0);
        availableItemsTableModel.setRowCount(0);
    }
    
    /**
     * Refresh supplier table with latest data
     */
    private void refreshSupplierTable() {
        // Remember selected supplier
        String selectedSupplierId = null;
        if (currentSupplier != null) {
            selectedSupplierId = currentSupplier.getSupplierID();
        }
        
        supplierTableModel.setRowCount(0);
        
        List<Supplier> suppliers = supplierController.getAllSuppliers();
        
        for (Supplier supplier : suppliers) {
            supplierTableModel.addRow(new Object[]{
                supplier.getSupplierID(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getPhone()
            });
        }
        
        // Restore selection if possible
        if (selectedSupplierId != null) {
            for (int i = 0; i < supplierTable.getRowCount(); i++) {
                if (supplierTable.getValueAt(i, 0).equals(selectedSupplierId)) {
                    supplierTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }
    
    /**
     * Refresh item tables based on current supplier
     */
    private void refreshItemTables() {
        if (currentSupplier == null) {
            clearItemTables();
            return;
        }
        
        // Clear tables
        supplierItemsTableModel.setRowCount(0);
        availableItemsTableModel.setRowCount(0);
        
        // Get all items
        List<Item> allItems = itemController.getAllItems();
        
        // Get supplier items
        List<String> supplierItemIds = currentSupplier.getItemIDs();
        if (supplierItemIds == null) {
            supplierItemIds = new ArrayList<>();
        }
        
        // Populate supplier items table
        for (Item item : allItems) {
            if (supplierItemIds.contains(item.getItemID())) {
                supplierItemsTableModel.addRow(new Object[]{
                    item.getItemID(),
                    item.getName(),
                    item.getDescription(),
                    item.getUnitPrice(),
                    item.getCategory()
                });
            }
        }
        
        // Populate available items table (items not already assigned to supplier)
        for (Item item : allItems) {
            if (!supplierItemIds.contains(item.getItemID())) {
                availableItemsTableModel.addRow(new Object[]{
                    item.getItemID(),
                    item.getName(),
                    item.getDescription(),
                    item.getUnitPrice(),
                    item.getCategory()
                });
            }
        }
    }
    
    /**
     * Add selected item to supplier
     */
    private void addItemToSupplier() {
        if (currentSupplier == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a supplier first.", 
                    "No Supplier Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = availableItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                    "Please select an item to add.", 
                    "No Item Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String itemId = (String) availableItemsTableModel.getValueAt(selectedRow, 0);
        
        // Update supplier's items
        boolean success = supplierController.addItemToSupplier(currentSupplier.getSupplierID(), itemId);
        
        if (success) {
            // Refresh current supplier
            currentSupplier = supplierController.getSupplierById(currentSupplier.getSupplierID());
            
            // Refresh tables
            refreshItemTables();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to add item to supplier.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Remove selected item from supplier
     */
    private void removeItemFromSupplier() {
        if (currentSupplier == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a supplier first.", 
                    "No Supplier Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = supplierItemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                    "Please select an item to remove.", 
                    "No Item Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String itemId = (String) supplierItemsTableModel.getValueAt(selectedRow, 0);
        
        // Check if this is the primary supplier for the item
        Item item = itemController.getItemById(itemId);
        if (item != null && item.getSupplierID().equals(currentSupplier.getSupplierID())) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "This supplier is the primary supplier for this item. Are you sure you want to remove it?", 
                    "Primary Supplier Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Update supplier's items
        boolean success = supplierController.removeItemFromSupplier(currentSupplier.getSupplierID(), itemId);
        
        if (success) {
            // Refresh current supplier
            currentSupplier = supplierController.getSupplierById(currentSupplier.getSupplierID());
            
            // Refresh tables
            refreshItemTables();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to remove item from supplier.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}