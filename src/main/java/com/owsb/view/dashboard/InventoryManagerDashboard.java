package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.model.User;
import com.owsb.model.user.InventoryManager;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for Inventory Managers
 * Extends BaseDashboard and adds role-specific functionality
 */
public class InventoryManagerDashboard extends BaseDashboard {
    
    // Specific panels for Inventory Manager
    private JPanel viewItemsPanel;
    private JPanel updateStockPanel;
    private JPanel lowStockPanel;
    private JPanel stockReportsPanel;
    private JPanel viewPOPanel;
    
    /**
     * Constructor for InventoryManagerDashboard
     * @param user Current logged in user (should be InventoryManager)
     * @param authController Authentication controller
     */
    public InventoryManagerDashboard(User user, AuthController authController) {
        super("OWSB - Inventory Manager Dashboard", user, authController);
        
        // Check if user is an InventoryManager
        if (!(user instanceof InventoryManager)) {
            throw new IllegalArgumentException("User must be an Inventory Manager");
        }
    }
    
    /**
     * Override role-specific greeting
     * @return Inventory Manager specific greeting
     */
    @Override
    protected String getRoleSpecificGreeting() {
        InventoryManager inventoryManager = (InventoryManager) currentUser;
        return inventoryManager.getInventoryGreeting();
    }
    
    /**
     * Initialize Inventory Manager specific components
     */
    @Override
    protected void initRoleComponents() {
        // Initialize panel placeholders
        initPanels();
        
        // Add menu buttons for Inventory Manager functions
        addMenuButton("View Items", e -> showViewItemsPanel());
        addMenuButton("Update Stock", e -> showUpdateStockPanel());
        addMenuButton("Low Stock Alerts", e -> showLowStockPanel());
        addMenuButton("Stock Reports", e -> showStockReportsPanel());
        addMenuButton("View Purchase Orders", e -> showViewPOPanel());
    }
    
    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // View Items panel with stock details
        viewItemsPanel = createSimplePanel("View Items", 
            new String[]{"Item Code", "Item Name", "Current Stock", "Min Stock Level", "Status"},
            new Object[][]{
                {"IT001", "Rice 5kg", 100, 20, "OK"},
                {"IT002", "Sugar 1kg", 150, 30, "OK"},
                {"IT003", "Flour 1kg", 15, 20, "LOW"}
            });
        
        // Update Stock panel
        updateStockPanel = new JPanel(new BorderLayout());
        updateStockPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel updateLabel = new JLabel("Update Stock", JLabel.CENTER);
        updateLabel.setFont(new Font("Arial", Font.BOLD, 18));
        updateStockPanel.add(updateLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        formPanel.add(new JLabel("PO ID:"));
        JComboBox<String> poCombo = new JComboBox<>(new String[]{"PO001", "PO002"});
        formPanel.add(poCombo);
        
        formPanel.add(new JLabel("Received Quantity:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Notes:"));
        formPanel.add(new JTextField());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Update Stock"));
        buttonPanel.add(new JButton("Cancel"));
        
        updateStockPanel.add(formPanel, BorderLayout.CENTER);
        updateStockPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Low Stock Alerts panel
        lowStockPanel = createSimplePanel("Low Stock Alerts", 
            new String[]{"Item Code", "Item Name", "Current Stock", "Min Stock Level", "Status"},
            new Object[][]{
                {"IT003", "Flour 1kg", 15, 20, "LOW"},
                {"IT005", "Cooking Oil 2L", 8, 10, "LOW"},
                {"IT008", "Salt 500g", 5, 10, "CRITICAL"}
            });
        
        // Stock Reports panel
        stockReportsPanel = new JPanel(new BorderLayout());
        stockReportsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel reportsLabel = new JLabel("Stock Reports", JLabel.CENTER);
        reportsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        stockReportsPanel.add(reportsLabel, BorderLayout.NORTH);
        
        JPanel reportsButtonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        reportsButtonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        reportsButtonPanel.add(new JButton("Current Stock Report"));
        reportsButtonPanel.add(new JButton("Low Stock Report"));
        reportsButtonPanel.add(new JButton("Stock Movement Report"));
        reportsButtonPanel.add(new JButton("Inventory Valuation Report"));
        
        stockReportsPanel.add(reportsButtonPanel, BorderLayout.CENTER);
        
        // View Purchase Orders panel (for receiving)
        viewPOPanel = createSimplePanel("View Purchase Orders", 
            new String[]{"PO ID", "Date", "Item Code", "Quantity", "Status", "Received"},
            new Object[][]{
                {"PO001", "2025-03-26", "IT001", 50, "APPROVED", "No"},
                {"PO002", "2025-03-27", "IT002", 100, "APPROVED", "No"}
            });
    }
    
    /**
     * Create a simple panel with a table
     * @param title Panel title
     * @param columnNames Column names for table
     * @param data Table data
     * @return Panel with table
     */
    private JPanel createSimplePanel(String title, String[] columnNames, Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(label, BorderLayout.NORTH);
        
        JTable table = new JTable(data, columnNames);
        table.setDefaultEditor(Object.class, null); // Make the table non-editable
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Methods to show different panels
    private void showViewItemsPanel() {
        setContent(viewItemsPanel);
        setStatus("Viewing Items");
    }
    
    private void showUpdateStockPanel() {
        setContent(updateStockPanel);
        setStatus("Updating Stock");
    }
    
    private void showLowStockPanel() {
        setContent(lowStockPanel);
        setStatus("Low Stock Alerts");
    }
    
    private void showStockReportsPanel() {
        setContent(stockReportsPanel);
        setStatus("Stock Reports");
    }
    
    private void showViewPOPanel() {
        setContent(viewPOPanel);
        setStatus("Viewing Purchase Orders");
    }
}