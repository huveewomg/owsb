package com.owsb.view.sales;

import com.owsb.controller.SalesController;
import com.owsb.model.Item;
import com.owsb.model.Sale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel for daily item-wise sales entry
 * Demonstrates separation of concerns by keeping UI separate from business logic
 */
public class SalesEntryPanel extends JPanel {
    // UI components
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    
    private JLabel dateLabel;
    private JSpinner dateSpinner;
    
    private JTable salesTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<Item> itemComboBox;
    private JSpinner quantitySpinner;
    private JButton addButton;
    private JButton removeButton;
    
    private JLabel notesLabel;
    private JTextArea notesArea;
    private JScrollPane notesScrollPane;
    
    private JButton saveButton;
    private JButton clearButton;
    
    // For editing mode
    private boolean editMode = false;
    private String editingSaleId = null;
    
    // Controller
    private final SalesController salesController;
    
    // Data
    private final List<Map<String, Object>> currentSaleItems = new ArrayList<>();
    private Date selectedDate = new Date();
    
    /**
     * Constructor for SalesEntryPanel
     * @param salesController Sales controller
     */
    public SalesEntryPanel(SalesController salesController) {
        this.salesController = salesController;
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize components
        initComponents();
        loadItems();
        
        // Add listeners
        addListeners();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Top panel - Date selection
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        dateLabel = new JLabel("Sales Date:");
        
        // Date spinner with today's date
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        
        // Add view sales button
        JButton viewSalesButton = new JButton("View Sales");
        viewSalesButton.addActionListener(e -> viewSales());
        
        topPanel.add(dateLabel);
        topPanel.add(dateSpinner);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(viewSalesButton);
        
        // Center panel - Sales table
        centerPanel = new JPanel(new BorderLayout(5, 5));
        
        // Create table model with columns
        tableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Quantity", "Unit Price", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        
        // Create table
        salesTable = new JTable(tableModel);
        salesTable.getTableHeader().setReorderingAllowed(false);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(salesTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 200));
        
        // Item selection panel
        JPanel itemSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        itemComboBox = new JComboBox<>();
        itemComboBox.setPreferredSize(new Dimension(250, 25));
        
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
        centerPanel.add(itemSelectionPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Notes and save/clear buttons
        bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        notesLabel = new JLabel("Notes:");
        notesArea = new JTextArea(3, 40);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesScrollPane = new JScrollPane(notesArea);
        
        JPanel notesPanel = new JPanel(new BorderLayout(5, 5));
        notesPanel.add(notesLabel, BorderLayout.NORTH);
        notesPanel.add(notesScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save Sales");
        clearButton = new JButton("Clear");
        
        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);
        
        bottomPanel.add(notesPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load items for combo box
     */
    private void loadItems() {
        // Clear existing items
        itemComboBox.removeAllItems();
        
        // Get all items
        List<Item> items = salesController.getAllItems();
        
        // Add items to combo box
        for (Item item : items) {
            itemComboBox.addItem(item);
        }
        
        // Set the custom renderer for the combo box
        itemComboBox.setRenderer(new ItemRenderer());
    }
    
    /**
     * Add listeners to components
     */
    private void addListeners() {
        // Date spinner listener
        dateSpinner.addChangeListener(e -> selectedDate = (Date) dateSpinner.getValue());
        
        // Add item button listener
        addButton.addActionListener(e -> addItemToTable());
        
        // Remove item button listener
        removeButton.addActionListener(e -> removeSelectedItem());
        
        // Table selection listener
        salesTable.getSelectionModel().addListSelectionListener(e -> {
            removeButton.setEnabled(salesTable.getSelectedRow() != -1);
        });
        
        // Save button listener
        saveButton.addActionListener(e -> saveSales());
        
        // Clear button listener
        clearButton.addActionListener(e -> clearForm());
    }
    
    /**
     * Add an item to the sales table
     */
    private void addItemToTable() {
        // Get selected item
        Item selectedItem = (Item) itemComboBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select an item.", 
                    "No Item Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get quantity
        int quantity = (int) quantitySpinner.getValue();
        
        // Check stock
        if (selectedItem.getCurrentStock() < quantity) {
            JOptionPane.showMessageDialog(this, 
                    "Not enough stock. Available: " + selectedItem.getCurrentStock(), 
                    "Insufficient Stock", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check if item already in table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(selectedItem.getItemID())) {
                JOptionPane.showMessageDialog(this, 
                        "This item is already in the table. Please remove it first if you want to change the quantity.", 
                        "Duplicate Item", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Calculate total
        double total = selectedItem.getUnitPrice() * quantity;
        
        // Add to table model
        tableModel.addRow(new Object[]{
                selectedItem.getItemID(),
                selectedItem.getName(),
                quantity,
                selectedItem.getUnitPrice(),
                total
        });
        
        // Add to current sale items
        Map<String, Object> saleItem = new HashMap<>();
        saleItem.put("item", selectedItem);
        saleItem.put("quantity", quantity);
        saleItem.put("total", total);
        currentSaleItems.add(saleItem);
        
        // Reset quantity spinner
        quantitySpinner.setValue(1);
    }
    
    /**
     * Remove selected item from table
     */
    private void removeSelectedItem() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow != -1) {
            currentSaleItems.remove(selectedRow);
            tableModel.removeRow(selectedRow);
            removeButton.setEnabled(false);
        }
    }
    
    /**
     * Save sales entries
     */
    private void saveSales() {
        // Check if there are any items
        if (currentSaleItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please add at least one item.", 
                    "No Items", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get notes
        String notes = notesArea.getText().trim();
        
        // If in edit mode, first delete the existing sale
        if (editMode && editingSaleId != null) {
            salesController.deleteSale(editingSaleId);
            editMode = false;
            editingSaleId = null;
            saveButton.setText("Save Sales");
        }
        
        // Save each sale
        boolean allSaved = true;
        for (Map<String, Object> saleItem : currentSaleItems) {
            Item item = (Item) saleItem.get("item");
            int quantity = (int) saleItem.get("quantity");
            
            boolean saved = salesController.createSale(
                    selectedDate,
                    item.getItemID(),
                    quantity,
                    notes
            );
            
            if (!saved) {
                allSaved = false;
            }
        }
        
        // Show message
        if (allSaved) {
            JOptionPane.showMessageDialog(this, 
                    "Sales entries saved successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "There was a problem saving some sales entries.", 
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
        currentSaleItems.clear();
        
        // Reset components
        dateSpinner.setValue(new Date());
        quantitySpinner.setValue(1);
        notesArea.setText("");
        removeButton.setEnabled(false);
        
        // Reset edit mode
        editMode = false;
        editingSaleId = null;
        saveButton.setText("Save Sales");
    }
    
    /**
     * View sales for a specific date
     */
    private void viewSales() {
        // Get selected date
        Date date = (Date) dateSpinner.getValue();
        
        // Get sales for that date
        List<Sale> sales = salesController.getSalesByDate(date);
        
        if (sales.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No sales found for the selected date.", 
                    "No Sales", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create and show sales viewer dialog
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                "Sales for " + new SimpleDateFormat("yyyy-MM-dd").format(date), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);
        
        // Create table model
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Sale ID", "Item Code", "Item Name", "Quantity", "Amount", "Notes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Fill table with sales data
        for (Sale sale : sales) {
            model.addRow(new Object[]{
                    sale.getSaleID(),
                    sale.getItemID(),
                    sale.getItemName(),
                    sale.getQuantity(),
                    sale.getSalesAmount(),
                    sale.getNotes()
            });
        }
        
        // Create table
        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Create buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton editButton = new JButton("Edit");
        editButton.setEnabled(false);
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        
        JButton closeButton = new JButton("Close");
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        
        // Add components to dialog
        dialog.add(new JLabel("Sales for " + new SimpleDateFormat("yyyy-MM-dd").format(date)), 
                BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add listeners
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
        
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String saleId = (String) table.getValueAt(row, 0);
                editSale(saleId);
                dialog.dispose();
            }
        });
        
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String saleId = (String) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(dialog, 
                        "Are you sure you want to delete this sale?", 
                        "Confirm Delete", 
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleted = salesController.deleteSale(saleId);
                    if (deleted) {
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(dialog, 
                                "Sale deleted successfully.", 
                                "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        
                        if (model.getRowCount() == 0) {
                            dialog.dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(dialog, 
                                "Failed to delete sale.", 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        closeButton.addActionListener(e -> dialog.dispose());
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Edit an existing sale
     * @param saleId Sale ID to edit
     */
    private void editSale(String saleId) {
        // Get the sale
        Sale sale = salesController.getSaleById(saleId);
        if (sale == null) {
            JOptionPane.showMessageDialog(this, 
                    "Sale not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Set edit mode
        editMode = true;
        editingSaleId = saleId;
        saveButton.setText("Update Sale");
        
        // Clear current items
        tableModel.setRowCount(0);
        currentSaleItems.clear();
        
        // Set date
        dateSpinner.setValue(sale.getDate());
        selectedDate = sale.getDate();
        
        // Set notes
        notesArea.setText(sale.getNotes());
        
        // Get the item
        Item item = salesController.getItemById(sale.getItemID());
        if (item == null) {
            JOptionPane.showMessageDialog(this, 
                    "Item not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Add to table
        tableModel.addRow(new Object[]{
                item.getItemID(),
                item.getName(),
                sale.getQuantity(),
                sale.getSalesAmount() / sale.getQuantity(),
                sale.getSalesAmount()
        });
        
        // Add to current sale items
        Map<String, Object> saleItem = new HashMap<>();
        saleItem.put("item", item);
        saleItem.put("quantity", sale.getQuantity());
        saleItem.put("total", sale.getSalesAmount());
        currentSaleItems.add(saleItem);
    }
    
    /**
     * Custom renderer for item combo box
     */
    /**
     * Custom renderer for item combo box
     */
    private static class ItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                     boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Item) {
                Item item = (Item) value;
                value = item.getItemID() + " - " + item.getName() + " (Stock: " + item.getCurrentStock() + ")";
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}