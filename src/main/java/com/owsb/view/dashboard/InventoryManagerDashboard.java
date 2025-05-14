package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.PurchaseOrderController;
import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.User;
import com.owsb.model.user.InventoryManager;
import com.owsb.view.inventory.StockUpdatePanel;
import com.owsb.view.order.PurchaseOrderPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for Inventory Managers
 * Extends BaseDashboard and adds role-specific functionality
 */
public class InventoryManagerDashboard extends BaseDashboard {
    
    // Controllers
    private final PurchaseRequisitionController prController;
    private final PurchaseOrderController poController;
    
    // Specific panels for Inventory Manager
    private JPanel viewItemsPanel;
    private StockUpdatePanel updateStockPanel;
    private JPanel lowStockPanel;
    private JPanel stockReportsPanel;
    private PurchaseOrderPanel viewPOPanel;
    
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
        
        // Initialize controllers
        this.prController = new PurchaseRequisitionController();
        this.prController.setCurrentUser(user);
        
        this.poController = new PurchaseOrderController();
        this.poController.setCurrentUser(user);
        
        // Initialize panels
        initPanels();
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
        
        updateStockPanel = new StockUpdatePanel(poController);
        
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
        
        viewPOPanel = new PurchaseOrderPanel(poController, currentUser);
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