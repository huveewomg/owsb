package com.owsb.view.sales;

import com.owsb.controller.SalesController;
import com.owsb.model.inventory.Item;
import com.owsb.model.sales.Sale;
import com.owsb.model.sales.SaleItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel for daily item-wise sales entry with profit ratio
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
    private JSpinner profitRatioSpinner;
    private JButton addButton;
    private JButton removeButton;
    
    private JLabel notesLabel;
    private JTextArea notesArea;
    private JScrollPane notesScrollPane;
    
    private JButton saveButton;
    private JButton clearButton;
    private JButton viewButton;
    
    // For editing mode
    private boolean editMode = false;
    private String editingSaleId = null;
    
    // Controller
    private final SalesController salesController;
    
    // Data
    private final List<SaleItem> saleItems = new ArrayList<>();
    private Date selectedDate = new Date();
    
    // Formatters
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    
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
        
        // Update totals
        updateTotals();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Create header panel
        topPanel = createHeaderPanel();
        
        // Center panel - Sales table
        centerPanel = new JPanel(new BorderLayout(5, 5));
        
        // Create table model with columns
        tableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Quantity", "Unit Price", "Profit %", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 4; // Only quantity and profit columns are editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: return Integer.class; // Quantity
                    case 3: return Double.class;  // Unit Price
                    case 4: return Double.class;  // Profit %
                    case 5: return Double.class;  // Subtotal
                    default: return String.class;
                }
            }
        };
        
        // Create table
        salesTable = new JTable(tableModel);
        salesTable.getTableHeader().setReorderingAllowed(false);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set custom renderers
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        
        // Currency renderer
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        // Percentage renderer
        DefaultTableCellRenderer percentRenderer = new DefaultTableCellRenderer();
        percentRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        salesTable.getColumnModel().getColumn(3).setCellRenderer(currencyRenderer); // Unit Price
        salesTable.getColumnModel().getColumn(4).setCellRenderer(percentRenderer);  // Profit %
        salesTable.getColumnModel().getColumn(5).setCellRenderer(currencyRenderer); // Subtotal
        
        // Scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(salesTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 200));
        
        // Item selection panel
        JPanel itemSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        itemComboBox = new JComboBox<>();
        itemComboBox.setPreferredSize(new Dimension(250, 25));
        itemComboBox.setRenderer(new ItemRenderer());
        
        JLabel quantityLabel = new JLabel("Quantity:");
        
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(1, 1, 10000, 1);
        quantitySpinner = new JSpinner(quantityModel);
        quantitySpinner.setPreferredSize(new Dimension(80, 25));
        
        JLabel profitRatioLabel = new JLabel("Profit %:");
        
        SpinnerNumberModel profitModel = new SpinnerNumberModel(
                SalesController.DEFAULT_PROFIT_RATIO, 0.0, 1.0, 0.01);
        profitRatioSpinner = new JSpinner(profitModel);
        profitRatioSpinner.setEditor(new JSpinner.NumberEditor(profitRatioSpinner, "##0.##%"));
        profitRatioSpinner.setPreferredSize(new Dimension(80, 25));
        
        addButton = new JButton("Add Item");
        removeButton = new JButton("Remove Selected");
        removeButton.setEnabled(false);
        
        itemSelectionPanel.add(new JLabel("Item:"));
        itemSelectionPanel.add(itemComboBox);
        itemSelectionPanel.add(quantityLabel);
        itemSelectionPanel.add(quantitySpinner);
        itemSelectionPanel.add(profitRatioLabel);
        itemSelectionPanel.add(profitRatioSpinner);
        itemSelectionPanel.add(addButton);
        itemSelectionPanel.add(removeButton);
        
        // Add to center panel
        centerPanel.add(itemSelectionPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Add total panel below the table
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(new JLabel("Total Cost: "));
        JLabel totalCostLabel = new JLabel(currencyFormat.format(0.0));
        totalPanel.add(totalCostLabel);
        
        totalPanel.add(new JLabel("Total Profit: "));
        JLabel totalProfitLabel = new JLabel(currencyFormat.format(0.0));
        totalPanel.add(totalProfitLabel);
        
        totalPanel.add(new JLabel("Grand Total: "));
        JLabel grandTotalLabel = new JLabel(currencyFormat.format(0.0));
        totalPanel.add(grandTotalLabel);
        
        centerPanel.add(totalPanel, BorderLayout.SOUTH);
        
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
        viewButton = new JButton("View Sales");
        
        buttonPanel.add(viewButton);
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
     * Create header panel with title and date selection
     * @return Header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Sales Entry");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        dateLabel = new JLabel("Sales Date:");
        
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        
        datePanel.add(dateLabel);
        datePanel.add(dateSpinner);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(datePanel, BorderLayout.CENTER);
        
        return panel;
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
        
        // Table model listener for edits
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 2 || e.getColumn() == 4) { // Quantity or Profit %
                int row = e.getFirstRow();
                if (row >= 0 && row < saleItems.size()) {
                    // Update the sale item
                    SaleItem saleItem = saleItems.get(row);
                    
                    if (e.getColumn() == 2) { // Quantity
                        int quantity = (int) tableModel.getValueAt(row, 2);
                        saleItem.setQuantity(quantity);
                    } else if (e.getColumn() == 4) { // Profit %
                        double profitRatio = (double) tableModel.getValueAt(row, 4);
                        saleItem.setProfitRatio(profitRatio);
                    }
                    
                    // Update the subtotal in the table
                    tableModel.setValueAt(saleItem.getSubtotal(), row, 5);
                    
                    // Update totals
                    updateTotals();
                }
            }
        });
        
        // Save button listener
        saveButton.addActionListener(e -> saveSales());
        
        // Clear button listener
        clearButton.addActionListener(e -> clearForm());
        
        // View button listener
        viewButton.addActionListener(e -> viewSales());
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
        
        // Get profit ratio
        double profitRatio = (double) profitRatioSpinner.getValue();
        
        // Check if item already in table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(selectedItem.getItemID())) {
                JOptionPane.showMessageDialog(this, 
                        "This item is already in the table. Please remove it first if you want to change the quantity or change directly in the table.", 
                        "Duplicate Item", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Create a new sale item
        SaleItem saleItem = new SaleItem(
                selectedItem.getItemID(),
                selectedItem.getName(),
                quantity,
                selectedItem.getUnitPrice(),
                profitRatio
        );
        
        // Add to list
        saleItems.add(saleItem);
        
        // Add to table model
        tableModel.addRow(new Object[]{
                saleItem.getItemID(),
                saleItem.getItemName(),
                saleItem.getQuantity(),
                saleItem.getUnitPrice(),
                saleItem.getProfitRatio(),
                saleItem.getSubtotal()
        });
        
        // Update totals
        updateTotals();
        
        // Reset quantity and profit spinners
        quantitySpinner.setValue(1);
        profitRatioSpinner.setValue(SalesController.DEFAULT_PROFIT_RATIO);
    }
    
    /**
     * Remove selected item from table
     */
    private void removeSelectedItem() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow != -1) {
            saleItems.remove(selectedRow);
            tableModel.removeRow(selectedRow);
            removeButton.setEnabled(false);
            
            // Update totals    
            updateTotals();
        }
    }
    
    /**
     * Update the totals display
     */
    private void updateTotals() {
        double totalCost = 0.0;
        double totalProfit = 0.0;
        double grandTotal = 0.0;
        
        for (SaleItem item : saleItems) {
            totalCost += item.getCostPrice();
            totalProfit += item.getProfitAmount();
            grandTotal += item.getSubtotal();
        }
        
        // Update the labels
        Component[] components = ((JPanel)centerPanel.getComponent(2)).getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JLabel) {
                JLabel label = (JLabel) components[i];
                if (i == 1) { // Total Cost
                    label.setText(currencyFormat.format(totalCost));
                } else if (i == 3) { // Total Profit
                    label.setText(currencyFormat.format(totalProfit));
                } else if (i == 5) { // Grand Total
                    label.setText(currencyFormat.format(grandTotal));
                }
            }
        }
    }
    
    /**
     * Save the sales
     */
    private void saveSales() {
        // Check if there are any items
        if (saleItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please add at least one item.", 
                    "No Items", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get notes
        String notes = notesArea.getText().trim();
        
        boolean success;
        
        // Save the sale
        if (editMode && editingSaleId != null) {
            success = salesController.updateSale(editingSaleId, selectedDate, saleItems, notes);
        } else {
            success = salesController.createSale(selectedDate, saleItems, notes);
        }
        
        // Show message
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Sale saved successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Failed to save sale.", 
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
        saleItems.clear();
        
        // Reset components
        dateSpinner.setValue(new Date());
        quantitySpinner.setValue(1);
        profitRatioSpinner.setValue(SalesController.DEFAULT_PROFIT_RATIO);
        notesArea.setText("");
        removeButton.setEnabled(false);
        
        // Reset edit mode
        editMode = false;
        editingSaleId = null;
        saveButton.setText("Save Sales");
        
        // Update totals
        updateTotals();
    }
    
    /**
     * View sales for the selected date
     */
    private void viewSales() {
        // Get the date
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
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        // Create panel for sales list
        JPanel salesListPanel = new JPanel(new BorderLayout(5, 5));
        salesListPanel.setBorder(BorderFactory.createTitledBorder("Sales List"));
        
        // Create table model for sales
        DefaultTableModel salesModel = new DefaultTableModel(
                new Object[]{"Sale ID", "Items", "Total Amount", "Profit", "Notes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        JTable salesTable = new JTable(salesModel);
        salesTable.getTableHeader().setReorderingAllowed(false);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set custom renderers
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        
        salesTable.getColumnModel().getColumn(2).setCellRenderer(rightAlign); // Total
        salesTable.getColumnModel().getColumn(3).setCellRenderer(rightAlign); // Profit
        
        // Fill table
        for (Sale sale : sales) {
            salesModel.addRow(new Object[]{
                    sale.getSaleID(),
                    sale.getItemCount(),
                    currencyFormat.format(sale.getTotalAmount()),
                    currencyFormat.format(sale.getTotalProfitAmount()),
                    sale.getNotes()
            });
        }
        
        // Add to scroll pane
        JScrollPane salesScrollPane = new JScrollPane(salesTable);
        salesListPanel.add(salesScrollPane, BorderLayout.CENTER);
        
        // Create panel for sale details
        JPanel saleDetailsPanel = new JPanel(new BorderLayout(5, 5));
        saleDetailsPanel.setBorder(BorderFactory.createTitledBorder("Sale Details"));
        
        // Create table model for sale items
        DefaultTableModel itemsModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Quantity", "Unit Price", "Profit %", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        JTable itemsTable = new JTable(itemsModel);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set custom renderers
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(rightAlign); // Unit Price
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(rightAlign); // Profit %
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(rightAlign); // Subtotal
        
        // Add to scroll pane
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        saleDetailsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        
        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, salesListPanel, saleDetailsPanel);
        splitPane.setDividerLocation(200);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton viewButton = new JButton("View Sale");
        JButton editButton = new JButton("Edit Sale");
        JButton deleteButton = new JButton("Delete Sale");
        JButton closeButton = new JButton("Close");
        
        viewButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        
        // Add listeners
        salesTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = salesTable.getSelectedRow();
            boolean hasSelection = selectedRow != -1;
            
            viewButton.setEnabled(hasSelection);
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
            
            if (hasSelection) {
                // Get the selected sale
                String saleId = (String) salesTable.getValueAt(selectedRow, 0);
                Sale sale = salesController.getSaleById(saleId);
                
                if (sale != null) {
                    // Clear the items table
                    itemsModel.setRowCount(0);
                    
                    // Fill with items from the selected sale
                    for (SaleItem item : sale.getItems()) {
                        itemsModel.addRow(new Object[]{
                                item.getItemID(),
                                item.getItemName(),
                                item.getQuantity(),
                                currencyFormat.format(item.getUnitPrice()),
                                percentFormat.format(item.getProfitRatio()),
                                currencyFormat.format(item.getSubtotal())
                        });
                    }
                }
            } else {
                // Clear the items table
                itemsModel.setRowCount(0);
            }
        });
        
        viewButton.addActionListener(e -> {
            int selectedRow = salesTable.getSelectedRow();
            if (selectedRow != -1) {
                String saleId = (String) salesTable.getValueAt(selectedRow, 0);
                viewSaleDetails(saleId, dialog);
            }
        });
        
        editButton.addActionListener(e -> {
            int selectedRow = salesTable.getSelectedRow();
            if (selectedRow != -1) {
                String saleId = (String) salesTable.getValueAt(selectedRow, 0);
                editSale(saleId);
                dialog.dispose();
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = salesTable.getSelectedRow();
            if (selectedRow != -1) {
                String saleId = (String) salesTable.getValueAt(selectedRow, 0);
                
                int confirm = JOptionPane.showConfirmDialog(dialog, 
                        "Are you sure you want to delete this sale?", 
                        "Confirm Delete", 
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleted = salesController.deleteSale(saleId);
                    
                    if (deleted) {
                        salesModel.removeRow(selectedRow);
                        itemsModel.setRowCount(0);
                        
                        JOptionPane.showMessageDialog(dialog, 
                                "Sale deleted successfully.", 
                                "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        
                        if (salesModel.getRowCount() == 0) {
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
        
        // Add components to dialog
        dialog.add(splitPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * View details of a sale
     * @param saleId Sale ID
     * @param parent Parent dialog
     */
    private void viewSaleDetails(String saleId, JDialog parent) {
        // Get the sale
        Sale sale = salesController.getSaleById(saleId);
        if (sale == null) {
            JOptionPane.showMessageDialog(parent, 
                    "Sale not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create dialog
        JDialog dialog = new JDialog(parent, "Sale Details: " + saleId, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parent);
        
        // Create details panel
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Sale Information"));
        
        detailsPanel.add(new JLabel("Sale ID:"));
        detailsPanel.add(new JLabel(sale.getSaleID()));
        
        detailsPanel.add(new JLabel("Date:"));
        detailsPanel.add(new JLabel(sale.getFormattedDate()));
        
        detailsPanel.add(new JLabel("Created By:"));
        detailsPanel.add(new JLabel(sale.getSalesManagerID()));
        
        detailsPanel.add(new JLabel("Total Items:"));
        detailsPanel.add(new JLabel(String.valueOf(sale.getItemCount())));
        
        detailsPanel.add(new JLabel("Total Cost:"));
        detailsPanel.add(new JLabel(currencyFormat.format(sale.getTotalCostPrice())));
        
        detailsPanel.add(new JLabel("Total Profit:"));
        detailsPanel.add(new JLabel(currencyFormat.format(sale.getTotalProfitAmount())));
        
        detailsPanel.add(new JLabel("Grand Total:"));
        detailsPanel.add(new JLabel(currencyFormat.format(sale.getTotalAmount())));
        
        detailsPanel.add(new JLabel("Average Profit:"));
        detailsPanel.add(new JLabel(percentFormat.format(sale.getAverageProfitRatio())));
        
        detailsPanel.add(new JLabel("Notes:"));
        detailsPanel.add(new JLabel(sale.getNotes()));
        
        // Create table for items
        DefaultTableModel itemsModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Quantity", "Unit Price", "Profit %", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable itemsTable = new JTable(itemsModel);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set custom renderers
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(rightAlign); // Unit Price
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(rightAlign); // Profit %
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(rightAlign); // Subtotal
        
        // Fill with items
        for (SaleItem item : sale.getItems()) {
            itemsModel.addRow(new Object[]{
                    item.getItemID(),
                    item.getItemName(),
                    item.getQuantity(),
                    currencyFormat.format(item.getUnitPrice()),
                    percentFormat.format(item.getProfitRatio()),
                    currencyFormat.format(item.getSubtotal())
            });
        }
        
        // Add to scroll pane
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setBorder(BorderFactory.createTitledBorder("Items"));
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        // Add components to dialog
        dialog.add(detailsPanel, BorderLayout.NORTH);
        dialog.add(itemsScrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Edit a sale
     * @param saleId Sale ID
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
        
        // Set up for editing
        editMode = true;
        editingSaleId = saleId;
        saveButton.setText("Update Sale");
        
        // Set date
        dateSpinner.setValue(sale.getDate());
        selectedDate = sale.getDate();
        
        // Set notes
        notesArea.setText(sale.getNotes());
        
        // Clear existing items
        tableModel.setRowCount(0);
        saleItems.clear();
        
        // Add sale items
        for (SaleItem item : sale.getItems()) {
            saleItems.add(item);
            
            tableModel.addRow(new Object[]{
                    item.getItemID(),
                    item.getItemName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getProfitRatio(),
                    item.getSubtotal()
            });
        }
        
        // Update totals
        updateTotals();
    }
    
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