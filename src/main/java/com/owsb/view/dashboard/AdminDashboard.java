package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.model.User;
import com.owsb.model.user.Administrator;
import com.owsb.util.UserRole;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for Administrators
 * Extends BaseDashboard and adds admin-specific functionality
 */
public class AdminDashboard extends BaseDashboard {
    
    // Admin-specific components
    private JPanel userManagementPanel;
    private JPanel systemConfigPanel;
    
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
        // Initialize panel placeholders
        initPanels();
        
        // Add menu buttons for Admin functions
        addMenuButton("User Management", e -> showUserManagementPanel());
        addMenuButton("System Configuration", e -> showSystemConfigPanel());
        
        // Add menu separator
        menuPanel.add(Box.createVerticalStrut(20));
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(180, 1));
        menuPanel.add(separator);
        menuPanel.add(Box.createVerticalStrut(20));
        
        // Add access to all other functions
        addMenuButton("Items & Suppliers", e -> showItemsAndSuppliers());
        addMenuButton("Sales Records", e -> showSalesRecords());
        addMenuButton("Purchase Requisitions", e -> showPurchaseRequisitions());
        addMenuButton("Purchase Orders", e -> showPurchaseOrders());
        addMenuButton("Inventory Management", e -> showInventoryManagement());
        addMenuButton("Financial Reports", e -> showFinancialReports());
    }
    
    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // User management panel
        userManagementPanel = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel("User Management", JLabel.CENTER);
        userLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userManagementPanel.add(userLabel, BorderLayout.NORTH);
        
        JPanel userContent = createUserManagementContent();
        userManagementPanel.add(userContent, BorderLayout.CENTER);
        
        // System configuration panel
        systemConfigPanel = new JPanel(new BorderLayout());
        JLabel configLabel = new JLabel("System Configuration", JLabel.CENTER);
        configLabel.setFont(new Font("Arial", Font.BOLD, 18));
        systemConfigPanel.add(configLabel, BorderLayout.NORTH);
        
        JPanel configContent = createSystemConfigContent();
        systemConfigPanel.add(configContent, BorderLayout.CENTER);
    }
    
    /**
     * Create user management content
     * @return User management panel
     */
    private JPanel createUserManagementContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // User creation form
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        
        formPanel.add(new JLabel("User ID:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Username:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Password:"));
        formPanel.add(new JPasswordField());
        
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Email:"));
        formPanel.add(new JTextField());
        
        formPanel.add(new JLabel("Role:"));
        JComboBox<String> roleCombo = new JComboBox<>();
        for (UserRole role : UserRole.values()) {
            roleCombo.addItem(role.getDisplayName());
        }
        formPanel.add(roleCombo);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Create User"));
        buttonPanel.add(new JButton("Update User"));
        buttonPanel.add(new JButton("Delete User"));
        buttonPanel.add(new JButton("Reset Password"));
        
        // Mock user table
        String[] columnNames = {"User ID", "Username", "Name", "Role", "Email"};
        Object[][] data = {
            {"U001", "john_doe", "John Doe", "Sales Manager", "john@example.com"},
            {"U002", "jane_smith", "Jane Smith", "Purchase Manager", "jane@example.com"},
            {"U003", "admin", "Admin User", "Administrator", "admin@example.com"}
        };
        
        JTable userTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(userTable);
        
        // Combine components
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
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
    
    /**
     * Show user management panel
     */
    private void showUserManagementPanel() {
        setContent(userManagementPanel);
        setStatus("User Management");
    }
    
    /**
     * Show system configuration panel
     */
    private void showSystemConfigPanel() {
        setContent(systemConfigPanel);
        setStatus("System Configuration");
    }
    
    // The following methods would show placeholders for other functionality
    // that Administrators can access
    
    private void showItemsAndSuppliers() {
        JPanel panel = createPlaceholderPanel("Items & Suppliers Management");
        setContent(panel);
        setStatus("Items & Suppliers Management");
    }
    
    private void showSalesRecords() {
        JPanel panel = createPlaceholderPanel("Sales Records");
        setContent(panel);
        setStatus("Sales Records");
    }
    
    private void showPurchaseRequisitions() {
        JPanel panel = createPlaceholderPanel("Purchase Requisitions");
        setContent(panel);
        setStatus("Purchase Requisitions");
    }
    
    private void showPurchaseOrders() {
        JPanel panel = createPlaceholderPanel("Purchase Orders");
        setContent(panel);
        setStatus("Purchase Orders");
    }
    
    private void showInventoryManagement() {
        JPanel panel = createPlaceholderPanel("Inventory Management");
        setContent(panel);
        setStatus("Inventory Management");
    }
    
    private void showFinancialReports() {
        JPanel panel = createPlaceholderPanel("Financial Reports");
        setContent(panel);
        setStatus("Financial Reports");
    }
    
    /**
     * Create a placeholder panel with a title
     * @param title Panel title
     * @return Placeholder panel
     */
    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(label, BorderLayout.NORTH);
        
        JLabel placeholder = new JLabel("This is a placeholder for " + title, JLabel.CENTER);
        placeholder.setFont(new Font("Arial", Font.ITALIC, 14));
        panel.add(placeholder, BorderLayout.CENTER);
        
        return panel;
    }
}