package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.model.User;
import com.owsb.model.user.SalesManager;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for Sales Managers
 * Extends BaseDashboard and adds role-specific functionality
 */
public class SalesManagerDashboard extends BaseDashboard {
    
    // Specific components for SalesManager
    private JPanel itemPanel;
    private JPanel supplierPanel;
    private JPanel salesPanel;
    private JPanel requisitionPanel;
    
    /**
     * Constructor for SalesManagerDashboard
     * @param user Current logged in user (should be SalesManager)
     * @param authController Authentication controller
     */
    public SalesManagerDashboard(User user, AuthController authController) {
        super("OWSB - Sales Manager Dashboard", user, authController);
        
        // Check if user is a SalesManager
        if (!(user instanceof SalesManager)) {
            throw new IllegalArgumentException("User must be a Sales Manager");
        }
    }
    
    /**
     * Override role-specific greeting
     * @return Sales Manager specific greeting
     */
    @Override
    protected String getRoleSpecificGreeting() {
        SalesManager salesManager = (SalesManager) currentUser;
        return salesManager.getSalesGreeting();
    }
    
    /**
     * Initialize Sales Manager specific components
     */
    @Override
    protected void initRoleComponents() {
        // Initialize panel placeholders
        initPanels();
        
        // Add menu buttons for Sales Manager functions
        addMenuButton("Manage Items", e -> showItemPanel());
        addMenuButton("Manage Suppliers", e -> showSupplierPanel());
        addMenuButton("Daily Sales Entry", e -> showSalesPanel());
        addMenuButton("Purchase Requisitions", e -> showRequisitionPanel());
        addMenuButton("View Purchase Orders", e -> viewPurchaseOrders());
    }
    
    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // Item management panel
        itemPanel = new JPanel(new BorderLayout());
        JLabel itemLabel = new JLabel("Item Management", JLabel.CENTER);
        itemLabel.setFont(new Font("Arial", Font.BOLD, 18));
        itemPanel.add(itemLabel, BorderLayout.NORTH);
        
        JPanel itemContent = createItemContent();
        itemPanel.add(itemContent, BorderLayout.CENTER);
        
        // Supplier management panel
        supplierPanel = new JPanel(new BorderLayout());
        JLabel supplierLabel = new JLabel("Supplier Management", JLabel.CENTER);
        supplierLabel.setFont(new Font("Arial", Font.BOLD, 18));
        supplierPanel.add(supplierLabel, BorderLayout.NORTH);
        
        JPanel supplierContent = createSupplierContent();
        supplierPanel.add(supplierContent, BorderLayout.CENTER);
        
        // Sales entry panel
        salesPanel = new JPanel(new BorderLayout());
        JLabel salesLabel = new JLabel("Daily Sales Entry", JLabel.CENTER);
        salesLabel.setFont(new Font("Arial", Font.BOLD, 18));
        salesPanel.add(salesLabel, BorderLayout.NORTH);
        
        JPanel salesContent = createSalesContent();
        salesPanel.add(salesContent, BorderLayout.CENTER);
        
        // Purchase requisition panel
        requisitionPanel = new JPanel(new BorderLayout());
        JLabel requisitionLabel = new JLabel("Purchase Requisitions", JLabel.CENTER);
        requisitionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        requisitionPanel.add(requisitionLabel, BorderLayout.NORTH);
        
        JPanel requisitionContent = createRequisitionContent();
        requisitionPanel.add(requisitionContent, BorderLayout.CENTER);
    }
    
    /**
     * Create item management content
     * @return Item management panel
     */
    private JPanel createItemContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Simple form for item entry
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        formPanel.add(new JLabel("Item Code:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Item Name:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Current Stock:"));
        formPanel.add(new JTextField());
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Add Item"));
        buttonPanel.add(new JButton("Update Item"));
        buttonPanel.add(new JButton("Delete Item"));
        
        // Mock item table
        String[] columnNames = {"Item Code", "Item Name", "Current Stock"};
        Object[][] data = {
            {"IT001", "Rice 5kg", 100},
            {"IT002", "Sugar 1kg", 150},
            {"IT003", "Flour 1kg", 80}
        };
        
        JTable itemTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(itemTable);
        
        // Combine components
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create supplier management content
     * @return Supplier management panel
     */
    private JPanel createSupplierContent() {
        // Similar structure to item panel but with supplier fields
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        formPanel.add(new JLabel("Supplier Code:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Supplier Name:"));
        formPanel.add(new JTextField());
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Add Supplier"));
        buttonPanel.add(new JButton("Update Supplier"));
        buttonPanel.add(new JButton("Delete Supplier"));
        
        // Mock supplier table
        String[] columnNames = {"Supplier Code", "Supplier Name", "Contact"};
        Object[][] data = {
            {"SUP001", "ABC Groceries", "John (123-456-7890)"},
            {"SUP002", "XYZ Distributors", "Mary (098-765-4321)"}
        };
        
        JTable supplierTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        
        // Combine components
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create sales entry content
     * @return Sales entry panel
     */
    private JPanel createSalesContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form for daily sales entry
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        formPanel.add(new JLabel("Date:"));
        formPanel.add(new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())));
        
        formPanel.add(new JLabel("Item Code:"));
        JComboBox<String> itemCombo = new JComboBox<>(new String[]{"IT001", "IT002", "IT003"});
        formPanel.add(itemCombo);
        
        formPanel.add(new JLabel("Quantity Sold:"));
        formPanel.add(new JTextField());
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Record Sale"));
        buttonPanel.add(new JButton("View Sales Report"));
        
        // Mock sales table
        String[] columnNames = {"Date", "Item Code", "Item Name", "Quantity Sold"};
        Object[][] data = {
            {"2025-03-28", "IT001", "Rice 5kg", 10},
            {"2025-03-28", "IT002", "Sugar 1kg", 15},
            {"2025-03-27", "IT003", "Flour 1kg", 8}
        };
        
        JTable salesTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(salesTable);
        
        // Combine components
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create purchase requisition content
     * @return Purchase requisition panel
     */
    private JPanel createRequisitionContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form for creating PRs
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        
        formPanel.add(new JLabel("Item Code:"));
        JComboBox<String> itemCombo = new JComboBox<>(new String[]{"IT001", "IT002", "IT003"});
        formPanel.add(itemCombo);
        
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Required Date:"));
        formPanel.add(new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(
            new java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)))); // 1 week from now
        
        formPanel.add(new JLabel("Supplier:"));
        JComboBox<String> supplierCombo = new JComboBox<>(new String[]{"SUP001", "SUP002"});
        formPanel.add(supplierCombo);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Create Requisition"));
        buttonPanel.add(new JButton("View Requisitions"));
        
        // Mock PR table
        String[] columnNames = {"PR ID", "Date", "Item Code", "Quantity", "Required Date", "Status"};
        Object[][] data = {
            {"PR001", "2025-03-25", "IT001", 50, "2025-04-05", "NEW"},
            {"PR002", "2025-03-26", "IT002", 100, "2025-04-10", "PROCESSED"},
            {"PR003", "2025-03-27", "IT003", 30, "2025-04-15", "REJECTED"}
        };
        
        JTable prTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(prTable);
        
        // Combine components
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Show item management panel
     */
    private void showItemPanel() {
        setContent(itemPanel);
        setStatus("Item Management");
    }
    
    /**
     * Show supplier management panel
     */
    private void showSupplierPanel() {
        setContent(supplierPanel);
        setStatus("Supplier Management");
    }
    
    /**
     * Show sales entry panel
     */
    private void showSalesPanel() {
        setContent(salesPanel);
        setStatus("Daily Sales Entry");
    }
    
    /**
     * Show requisition panel
     */
    private void showRequisitionPanel() {
        setContent(requisitionPanel);
        setStatus("Purchase Requisitions");
    }
    
    /**
     * View purchase orders
     */
    private void viewPurchaseOrders() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Purchase Orders", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(label, BorderLayout.NORTH);
        
        // Mock PO table (view only for Sales Managers)
        String[] columnNames = {"PO ID", "Date", "PR ID", "Item Code", "Quantity", "Status"};
        Object[][] data = {
            {"PO001", "2025-03-26", "PR001", "IT001", 50, "PENDING"},
            {"PO002", "2025-03-27", "PR002", "IT002", 100, "APPROVED"},
            {"PO003", "2025-03-28", "PR003", "IT003", 30, "REJECTED"}
        };
        
        JTable poTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(poTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        setContent(panel);
        setStatus("Viewing Purchase Orders");
    }
}