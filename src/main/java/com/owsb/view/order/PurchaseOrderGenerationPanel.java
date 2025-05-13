package com.owsb.view.order;

import com.owsb.controller.PurchaseOrderController;
import com.owsb.model.POItem;
import com.owsb.model.PRItem;
import com.owsb.model.PurchaseRequisition;
// import com.owsb.model.Supplier;
import com.owsb.repository.SupplierRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel for generating a purchase order from a purchase requisition
 * Demonstrates separation of concerns following MVC pattern
 */
public class PurchaseOrderGenerationPanel extends JPanel {
    // UI components
    private JPanel headerPanel;
    private JPanel itemsPanel;
    private JPanel notesPanel;
    private JPanel buttonPanel;
    
    private JLabel prIdLabel;
    private JLabel prDateLabel;
    private JLabel requiredDateLabel;
    private JLabel createdByLabel;
    private JLabel statusLabel;
    
    private JLabel deliveryDateLabel;
    private JSpinner deliveryDateSpinner;
    
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    
    private JLabel notesLabel;
    private JTextArea notesArea;
    private JScrollPane notesScrollPane;
    
    private JLabel totalValueLabel;
    
    private JButton cancelButton;
    private JButton generateButton;
    
    // Controller
    private final PurchaseOrderController poController;
    private final SupplierRepository supplierRepository;
    
    // Data
    private String prId;
    private PurchaseRequisition pr;
    private final List<POItem> poItems = new ArrayList<>();
    
    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor for PurchaseOrderGenerationPanel
     * @param poController Purchase order controller
     */
    public PurchaseOrderGenerationPanel(PurchaseOrderController poController) {
        this.poController = poController;
        this.supplierRepository = new SupplierRepository();
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize components
        initComponents();
        
        // Add listeners
        addListeners();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Header panel - PR details and delivery date
        headerPanel = new JPanel(new GridLayout(0, 4, 10, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Purchase Requisition Details"));
        
        // PR details labels
        prIdLabel = new JLabel("PR ID: ");
        prDateLabel = new JLabel("PR Date: ");
        requiredDateLabel = new JLabel("Required By: ");
        createdByLabel = new JLabel("Created By: ");
        statusLabel = new JLabel("Status: ");
        
        // Add PR details to header panel
        headerPanel.add(prIdLabel);
        headerPanel.add(prDateLabel);
        headerPanel.add(requiredDateLabel);
        headerPanel.add(createdByLabel);
        headerPanel.add(statusLabel);
        
        // Delivery date
        deliveryDateLabel = new JLabel("Expected Delivery Date:");
        
        // Calculate default delivery date (2 weeks from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 14);
        
        // Delivery date spinner with default date
        SpinnerDateModel dateModel = new SpinnerDateModel(calendar.getTime(), null, null, Calendar.DAY_OF_MONTH);
        deliveryDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(deliveryDateSpinner, "yyyy-MM-dd");
        deliveryDateSpinner.setEditor(dateEditor);
        
        // Add delivery date to header panel
        headerPanel.add(deliveryDateLabel);
        headerPanel.add(deliveryDateSpinner);
        
        // Items panel - Table of items from PR
        itemsPanel = new JPanel(new BorderLayout(5, 5));
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items"));
        
        // Create table model
        tableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Quantity", "Supplier", "Unit Price", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only supplier column is editable (for future enhancement)
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2:
                        return Integer.class;
                    case 4, 5:
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };
        
        // Create table
        itemsTable = new JTable(tableModel);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Create a simple currency renderer
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        // Set column renderers
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(currencyRenderer);
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        itemsPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Notes panel
        notesPanel = new JPanel(new BorderLayout(5, 5));
        notesPanel.setBorder(BorderFactory.createTitledBorder("Notes"));
        
        notesLabel = new JLabel("Additional Notes:");
        notesArea = new JTextArea(3, 50);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesScrollPane = new JScrollPane(notesArea);
        
        notesPanel.add(notesLabel, BorderLayout.NORTH);
        notesPanel.add(notesScrollPane, BorderLayout.CENTER);
        
        // Button panel
        buttonPanel = new JPanel(new BorderLayout(5, 5));
        
        // Total value
        totalValueLabel = new JLabel("Total Value: " + currencyFormat.format(0));
        totalValueLabel.setFont(totalValueLabel.getFont().deriveFont(Font.BOLD));
        
        // Buttons
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Cancel");
        generateButton = new JButton("Generate Purchase Order");
        
        actionButtonPanel.add(cancelButton);
        actionButtonPanel.add(generateButton);
        
        buttonPanel.add(totalValueLabel, BorderLayout.WEST);
        buttonPanel.add(actionButtonPanel, BorderLayout.EAST);
        
        // Add panels to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(itemsPanel, BorderLayout.CENTER);
        add(notesPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Add listeners to components
     */
    private void addListeners() {
        // Cancel button listener
        cancelButton.addActionListener(e -> cancel());
        
        // Generate button listener
        generateButton.addActionListener(e -> generatePurchaseOrder());
    }
    
    /**
     * Load purchase requisition details
     * @param prId Purchase requisition ID
     */
    public void loadPurchaseRequisition(String prId) {
        this.prId = prId;
        
        // Get the purchase requisition
        pr = poController.getPurchaseRequisition(prId);
        if (pr == null) {
            JOptionPane.showMessageDialog(this, 
                    "Purchase requisition not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update PR details
        prIdLabel.setText("PR ID: " + pr.getPrID());
        prDateLabel.setText("PR Date: " + dateFormat.format(pr.getDate()));
        requiredDateLabel.setText("Required By: " + dateFormat.format(pr.getRequiredDate()));
        createdByLabel.setText("Created By: " + pr.getSalesManagerID());
        statusLabel.setText("Status: " + pr.getStatus().getDisplayName());
        
        // Set default delivery date based on PR required date
        deliveryDateSpinner.setValue(pr.getRequiredDate());
        
        // Clear existing items
        tableModel.setRowCount(0);
        poItems.clear();
        
        // Add PR items to table
        for (PRItem prItem : pr.getItems()) {
            // Get the supplier name
            String supplierName = poController.getSupplierName(prItem.getSuggestedSupplierID());
            
            // Create a PO item
            POItem poItem = new POItem(
                    prItem.getItemID(),
                    prItem.getItemName(),
                    prItem.getQuantity(),
                    prItem.getSuggestedSupplierID(),
                    supplierName,
                    prItem.getUnitPrice()
            );
            
            poItems.add(poItem);
            
            // Add to table
            tableModel.addRow(new Object[]{
                    prItem.getItemID(),
                    prItem.getItemName(),
                    prItem.getQuantity(),
                    supplierName,
                    prItem.getUnitPrice(),
                    poItem.getTotalCost()
            });
        }
        
        // Update total value
        updateTotalValue();
    }
    
    /**
     * Update total value label
     */
    private void updateTotalValue() {
        double totalValue = 0.0;
        for (POItem item : poItems) {
            totalValue += item.getTotalCost();
        }
        
        totalValueLabel.setText("Total Value: " + currencyFormat.format(totalValue));
    }
    
    /**
     * Cancel purchase order generation
     */
    private void cancel() {
        // Ask for confirmation
        int response = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to cancel? All changes will be lost.", 
                "Confirm Cancel", 
                JOptionPane.YES_NO_OPTION);
        
        if (response == JOptionPane.YES_OPTION) {
            // Notify parent
            firePropertyChange("poCancelled", false, true);
        }
    }
    
    /**
     * Generate purchase order
     */
    private void generatePurchaseOrder() {
        // Get delivery date
        Date deliveryDate = (Date) deliveryDateSpinner.getValue();
        
        // Get notes
        String notes = notesArea.getText().trim();
        
        // Create PO
        boolean success = poController.createPurchaseOrder(prId, deliveryDate, notes, poItems);
        
        // Show message
        if (success) {
            JOptionPane.showMessageDialog(this, 
                    "Purchase order generated successfully.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Notify parent
            firePropertyChange("poGenerated", false, true);
        } else {
            JOptionPane.showMessageDialog(this, 
                    "There was a problem generating the purchase order.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}