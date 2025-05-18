package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.ItemController;
import com.owsb.controller.MessageController;
import com.owsb.controller.PurchaseOrderController;
import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.controller.SalesController;
import com.owsb.controller.SupplierController;
import com.owsb.model.user.Administrator;
import com.owsb.model.user.User;
import com.owsb.view.finance.FinancialReportsPanel;
import com.owsb.view.finance.PaymentHistoryPanel;
import com.owsb.view.finance.PaymentPanel;
import com.owsb.view.inventory.LowStockAlertsPanel;
import com.owsb.view.inventory.StockReportsPanel;
import com.owsb.view.inventory.StockUpdatePanel;
import com.owsb.view.item.ItemCategoryManagementPanel;
import com.owsb.view.item.ItemListPanel;
import com.owsb.view.item.ItemManagementPanel;
import com.owsb.view.message.MessagePanel;
import com.owsb.view.order.PurchaseOrderGenerationPanel;
import com.owsb.view.order.PurchaseOrderListPanel;
import com.owsb.view.order.PurchaseOrderPanel;
import com.owsb.view.requisition.PurchaseRequisitionCreationPanel;
import com.owsb.view.requisition.PurchaseRequisitionListPanel;
import com.owsb.view.requisition.PurchaseRequisitionPanel;
import com.owsb.view.sales.SalesEntryPanel;
import com.owsb.view.supplier.SupplierListPanel;
import com.owsb.view.supplier.SupplierManagementPanel;
import com.owsb.view.user.UserManagementPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Enhanced Dashboard for Administrators
 * Extends BaseDashboard and adds admin-specific functionality
 * Administrators can access all functionalities of other roles
 */
public class AdminDashboard extends BaseDashboard {
    
    // Admin-specific components
    private UserManagementPanel userManagementPanel;
    private JPanel systemConfigPanel;
    
    // Controllers for all functionalities
    private ItemController itemController;
    private SupplierController supplierController;
    private SalesController salesController;
    private PurchaseRequisitionController prController;
    private PurchaseOrderController poController;
    private MessageController messageController;
    
    // Sales Manager panels
    private ItemManagementPanel itemManagementPanel;
    private SupplierManagementPanel supplierManagementPanel;
    private SalesEntryPanel salesEntryPanel;
    private PurchaseRequisitionPanel purchaseRequisitionPanel;
    private MessagePanel messagePanel;
    
    // Purchase Manager panels
    private ItemListPanel itemListPanel;
    private SupplierListPanel supplierListPanel;
    private PurchaseRequisitionListPanel prListPanel;
    private PurchaseOrderPanel purchaseOrderPanel;
    
    // Inventory Manager panels
    private LowStockAlertsPanel lowStockAlertsPanel;
    private StockUpdatePanel stockUpdatePanel;
    private StockReportsPanel stockReportsPanel;
    
    // Finance Manager panels
    private PaymentPanel paymentPanel;
    private PaymentHistoryPanel paymentHistoryPanel;
    private FinancialReportsPanel financialReportsPanel;
    
    // Additional specialized panels
    private ItemCategoryManagementPanel itemCategoryPanel;
    
    /**
     * Constructor for AdminDashboard
     * @param user Current logged in user (should be Administrator)
     * @param authController Authentication controller
     */
    public AdminDashboard(User user, AuthController authController) {
        super("OWSB - Administrator Dashboard", user, authController);
        
        // Check if user is an Administrator
        if (!(user instanceof Administrator)) {
            throw new IllegalArgumentException("User must be an Administrator");
        }
        
        // Initialize controllers
        initControllers();
        
        // Initialize all panels
        initAllPanels();
    }
    
    /**
     * Initialize all controllers
     */
    private void initControllers() {
        // Initialize all required controllers
        this.itemController = new ItemController();
        this.itemController.setCurrentUser(currentUser);
        
        this.supplierController = new SupplierController();
        this.supplierController.setCurrentUser(currentUser);
        
        this.salesController = new SalesController();
        this.salesController.setCurrentUser(currentUser);
        
        this.prController = new PurchaseRequisitionController();
        this.prController.setCurrentUser(currentUser);
        
        this.poController = new PurchaseOrderController();
        this.poController.setCurrentUser(currentUser);
        
        this.messageController = new MessageController();
        this.messageController.setCurrentUser(currentUser);
    }
    
    /**
     * Initialize all panels from all roles
     */
    private void initAllPanels() {
        // Admin-specific panels
        userManagementPanel = new UserManagementPanel(authController);
        initSystemConfigPanel();
        
        // Sales Manager panels
        itemManagementPanel = new ItemManagementPanel(itemController);
        supplierManagementPanel = new SupplierManagementPanel(supplierController, itemController);
        salesEntryPanel = new SalesEntryPanel(salesController);
        purchaseRequisitionPanel = new PurchaseRequisitionPanel(prController, currentUser);
        messagePanel = new MessagePanel(messageController, prController, currentUser);
        
        // Purchase Manager panels
        itemListPanel = new ItemListPanel(itemController, currentUser);
        supplierListPanel = new SupplierListPanel(supplierController, currentUser);
        prListPanel = new PurchaseRequisitionListPanel(prController, currentUser);
        purchaseOrderPanel = new PurchaseOrderPanel(poController, currentUser);
        
        // Inventory Manager panels
        lowStockAlertsPanel = new LowStockAlertsPanel(itemController, messageController, currentUser);
        stockUpdatePanel = new StockUpdatePanel(poController, itemController);
        stockReportsPanel = new StockReportsPanel(itemController, currentUser);
        
        // Finance Manager panels
        paymentPanel = new PaymentPanel(poController);
        paymentHistoryPanel = new PaymentHistoryPanel();
        financialReportsPanel = new FinancialReportsPanel(poController, salesController, currentUser);
        
        // Additional specialized panels
        itemCategoryPanel = new ItemCategoryManagementPanel();
    }
    
    /**
     * Initialize system configuration panel
     */
    private void initSystemConfigPanel() {
        systemConfigPanel = new JPanel(new BorderLayout());
        JLabel configLabel = new JLabel("System Configuration", JLabel.CENTER);
        configLabel.setFont(new Font("Arial", Font.BOLD, 18));
        systemConfigPanel.add(configLabel, BorderLayout.NORTH);
        
        JPanel configContent = createSystemConfigContent();
        systemConfigPanel.add(configContent, BorderLayout.CENTER);
    }
    
    /**
     * Override role-specific greeting
     * @return Admin specific greeting
     */
    @Override
    protected String getRoleSpecificGreeting() {
        return "Welcome, Administrator " + currentUser.getName() + "! You have full system access.";
    }
    
    /**
     * Initialize Administrator specific components
     */
    @Override
    protected void initRoleComponents() {
        // Add menu buttons for Admin functions
        addMenuButton("User Management", e -> showUserManagementPanel());
        addMenuButton("System Configuration", e -> showSystemConfigPanel());
        
        // Add menu separator
        addMenuSeparator();
        
        // Add access to all other functions grouped by category
        
        // Items & Suppliers section
        addMenuButton("Item Management", e -> showItemManagementPanel());
        addMenuButton("Supplier Management", e -> showSupplierManagementPanel());
        
        addMenuSeparator();
        
        // Sales & Inventory section
        addMenuButton("Sales Entry", e -> showSalesEntryPanel());
        addMenuButton("Low Stock Alerts", e -> showLowStockAlertsPanel());
        addMenuButton("Stock Update", e -> showStockUpdatePanel());
        addMenuButton("Stock Reports", e -> showStockReportsPanel());
        
        addMenuSeparator();
        
        // Procurement section
        addMenuButton("Create Requisition", e -> showCreateRequisitionPanel());
        addMenuButton("Purchase Orders", e -> showPurchaseOrdersPanel());
        
        addMenuSeparator();
        
        // Finance section
        addMenuButton("Process Payments", e -> showPaymentsPanel());
        addMenuButton("Payment History", e -> showPaymentHistoryPanel());
        addMenuButton("Financial Reports", e -> showFinancialReportsPanel());
        
        
    }
    
    /**
     * Add a separator to the menu panel
     */
    private void addMenuSeparator() {
        menuPanel.add(Box.createVerticalStrut(5));
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(180, 1));
        menuPanel.add(separator);
        menuPanel.add(Box.createVerticalStrut(5));
    }
    
    /**
     * Create system configuration content
     * @return System configuration panel
     */
    private JPanel createSystemConfigContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // System settings form
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        
        formPanel.add(new JLabel("Company Name:"));
        formPanel.add(new JTextField("Omega Wholesale Sdn Bhd"));
        
        formPanel.add(new JLabel("Data Directory:"));
        formPanel.add(new JTextField("./data"));
        
        formPanel.add(new JLabel("Backup Directory:"));
        formPanel.add(new JTextField("./backup"));
        
        formPanel.add(new JLabel("Auto Backup:"));
        JComboBox<String> backupCombo = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Off"});
        formPanel.add(backupCombo);
        
        formPanel.add(new JLabel("Log Level:"));
        JComboBox<String> logCombo = new JComboBox<>(new String[]{"DEBUG", "INFO", "WARNING", "ERROR"});
        formPanel.add(logCombo);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Save Settings"));
        buttonPanel.add(new JButton("Backup Now"));
        buttonPanel.add(new JButton("View Logs"));
        
        // System status
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("System Status"));
        
        JTextArea statusArea = new JTextArea(10, 40);
        statusArea.setEditable(false);
        statusArea.setText(
            "System Version: 1.0.0\n" +
            "Database Status: Connected\n" +
            "Last Backup: 2025-03-27 08:00:00\n" +
            "User Count: 5\n" +
            "Item Count: 120\n" +
            "Supplier Count: 15\n" +
            "Active Sessions: 2\n" +
            "System Uptime: 3 days, 4 hours, 12 minutes"
        );
        
        JScrollPane scrollPane = new JScrollPane(statusArea);
        statusPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Combine components
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Methods to show Admin-specific panels
    private void showUserManagementPanel() {
        setContent(userManagementPanel);
        setStatus("User Management");
    }
    
    private void showSystemConfigPanel() {
        setContent(systemConfigPanel);
        setStatus("System Configuration");
    }
    
    // Methods to show Sales Manager panels
    private void showItemManagementPanel() {
        setContent(itemManagementPanel);
        setStatus("Item Management");
    }
    
    private void showItemCategoryPanel() {
        setContent(itemCategoryPanel);
        setStatus("Item Category Management");
    }
    
    private void showSupplierManagementPanel() {
        setContent(supplierManagementPanel);
        setStatus("Supplier Management");
    }
    
    private void showSalesEntryPanel() {
        setContent(salesEntryPanel);
        setStatus("Daily Sales Entry");
    }
    
    // Methods to show Purchase Manager panels
    private void showPurchaseRequisitionsPanel() {
        prListPanel.loadPurchaseRequisitions();
        setContent(prListPanel);
        setStatus("Purchase Requisitions");
    }
    
    private void showCreateRequisitionPanel() {
        setContent(purchaseRequisitionPanel);
        setStatus("Create Purchase Requisition");
    }
    
    private void showPurchaseOrdersPanel() {
        setContent(purchaseOrderPanel);
        setStatus("Purchase Orders");
    }
    
    // Methods to show Inventory Manager panels
    private void showLowStockAlertsPanel() {
        setContent(lowStockAlertsPanel);
        setStatus("Low Stock Alerts");
    }
    
    private void showStockUpdatePanel() {
        setContent(stockUpdatePanel);
        setStatus("Stock Update");
    }
    
    private void showStockReportsPanel() {
        setContent(stockReportsPanel);
        setStatus("Stock Reports");
    }
    
    // Methods to show Finance Manager panels
    private void showPaymentsPanel() {
        setContent(paymentPanel);
        setStatus("Process Payments");
    }
    
    private void showPaymentHistoryPanel() {
        paymentHistoryPanel.loadPayments();
        setContent(paymentHistoryPanel);
        setStatus("Payment History");
    }
    
    private void showFinancialReportsPanel() {
        setContent(financialReportsPanel);
        setStatus("Financial Reports");
    }
    
    // Communication
    private void showMessagesPanel() {
        messagePanel.loadMessages();
        setContent(messagePanel);
        setStatus("Messages");
    }
    
    /**
     * Override navigateToPRPanel to navigate to the PR panel
     */
    @Override
    public void navigateToPRPanel() {
        showPurchaseRequisitionsPanel();
    }
}