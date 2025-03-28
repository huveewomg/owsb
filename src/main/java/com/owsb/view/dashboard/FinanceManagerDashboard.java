package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.model.User;
import com.owsb.model.user.FinanceManager;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for Finance Managers
 * Extends BaseDashboard and adds role-specific functionality
 */
public class FinanceManagerDashboard extends BaseDashboard {
    
    // Specific panels for Finance Manager
    private JPanel approvePOPanel;
    private JPanel paymentsPanel;
    private JPanel financialReportsPanel;
    private JPanel viewPRPanel;
    private JPanel viewPOPanel;
    
    /**
     * Constructor for FinanceManagerDashboard
     * @param user Current logged in user (should be FinanceManager)
     * @param authController Authentication controller
     */
    public FinanceManagerDashboard(User user, AuthController authController) {
        super("OWSB - Finance Manager Dashboard", user, authController);
        
        // Check if user is a FinanceManager
        if (!(user instanceof FinanceManager)) {
            throw new IllegalArgumentException("User must be a Finance Manager");
        }
    }
    
    /**
     * Override role-specific greeting
     * @return Finance Manager specific greeting
     */
    @Override
    protected String getRoleSpecificGreeting() {
        FinanceManager financeManager = (FinanceManager) currentUser;
        return financeManager.getFinanceGreeting();
    }
    
    /**
     * Initialize Finance Manager specific components
     */
    @Override
    protected void initRoleComponents() {
        // Initialize panel placeholders
        initPanels();
        
        // Add menu buttons for Finance Manager functions
        addMenuButton("Approve Purchase Orders", e -> showApprovePOPanel());
        addMenuButton("Process Payments", e -> showPaymentsPanel());
        addMenuButton("Financial Reports", e -> showFinancialReportsPanel());
        addMenuButton("View Requisitions", e -> showViewPRPanel());
        addMenuButton("View Purchase Orders", e -> showViewPOPanel());
    }
    
    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // Approve Purchase Orders panel
        approvePOPanel = createSimplePanel("Approve Purchase Orders", 
            new String[]{"PO ID", "Date", "Item Code", "Quantity", "Supplier", "Total Amount", "Status"},
            new Object[][]{
                {"PO001", "2025-03-26", "IT001", 50, "SUP001", "$1,250.00", "PENDING"},
                {"PO002", "2025-03-27", "IT002", 100, "SUP002", "$580.00", "PENDING"}
            },
            new JButton[]{ new JButton("Approve"), new JButton("Reject") }
        );
        
        // Process Payments panel
        paymentsPanel = new JPanel(new BorderLayout());
        paymentsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel paymentsLabel = new JLabel("Process Payments", JLabel.CENTER);
        paymentsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        paymentsPanel.add(paymentsLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        
        formPanel.add(new JLabel("PO ID:"));
        JComboBox<String> poCombo = new JComboBox<>(new String[]{"PO001", "PO002"});
        formPanel.add(poCombo);
        
        formPanel.add(new JLabel("Supplier:"));
        formPanel.add(new JTextField("SUP001"));
        
        formPanel.add(new JLabel("Amount:"));
        formPanel.add(new JTextField("$1,250.00"));
        
        formPanel.add(new JLabel("Payment Method:"));
        JComboBox<String> methodCombo = new JComboBox<>(new String[]{"Bank Transfer", "Check", "Credit"});
        formPanel.add(methodCombo);
        
        formPanel.add(new JLabel("Payment Date:"));
        formPanel.add(new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date())));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Process Payment"));
        buttonPanel.add(new JButton("Cancel"));
        
        paymentsPanel.add(formPanel, BorderLayout.CENTER);
        paymentsPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Financial Reports panel
        financialReportsPanel = new JPanel(new BorderLayout());
        financialReportsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel reportsLabel = new JLabel("Financial Reports", JLabel.CENTER);
        reportsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        financialReportsPanel.add(reportsLabel, BorderLayout.NORTH);
        
        JPanel reportsButtonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        reportsButtonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        reportsButtonPanel.add(new JButton("Purchase Summary Report"));
        reportsButtonPanel.add(new JButton("Payment History Report"));
        reportsButtonPanel.add(new JButton("Supplier Payment Report"));
        reportsButtonPanel.add(new JButton("Budget vs. Actual Report"));
        
        financialReportsPanel.add(reportsButtonPanel, BorderLayout.CENTER);
        
        // View Purchase Requisitions panel
        viewPRPanel = createSimplePanel("View Purchase Requisitions", 
            new String[]{"PR ID", "Date", "Item Code", "Quantity", "Required Date", "Status"},
            new Object[][]{
                {"PR001", "2025-03-25", "IT001", 50, "2025-04-05", "NEW"},
                {"PR002", "2025-03-26", "IT002", 100, "2025-04-10", "NEW"},
                {"PR003", "2025-03-27", "IT003", 30, "2025-04-15", "NEW"}
            });
        
        // View Purchase Orders panel
        viewPOPanel = createSimplePanel("View Purchase Orders", 
            new String[]{"PO ID", "Date", "Item Code", "Supplier", "Amount", "Status"},
            new Object[][]{
                {"PO001", "2025-03-26", "IT001", "SUP001", "$1,250.00", "PENDING"},
                {"PO002", "2025-03-27", "IT002", "SUP002", "$580.00", "PENDING"},
                {"PO003", "2025-03-25", "IT003", "SUP001", "$210.00", "APPROVED"}
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
        return createSimplePanel(title, columnNames, data, null);
    }
    
    /**
     * Create a simple panel with a table and action buttons
     * @param title Panel title
     * @param columnNames Column names for table
     * @param data Table data
     * @param actionButtons Action buttons to add
     * @return Panel with table and buttons
     */
    private JPanel createSimplePanel(String title, String[] columnNames, Object[][] data, JButton[] actionButtons) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(label, BorderLayout.NORTH);
        
        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        if (actionButtons != null && actionButtons.length > 0) {
            JPanel buttonPanel = new JPanel();
            for (JButton button : actionButtons) {
                buttonPanel.add(button);
            }
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }
        
        return panel;
    }
    
    // Methods to show different panels
    private void showApprovePOPanel() {
        setContent(approvePOPanel);
        setStatus("Approving Purchase Orders");
    }
    
    private void showPaymentsPanel() {
        setContent(paymentsPanel);
        setStatus("Processing Payments");
    }
    
    private void showFinancialReportsPanel() {
        setContent(financialReportsPanel);
        setStatus("Financial Reports");
    }
    
    private void showViewPRPanel() {
        setContent(viewPRPanel);
        setStatus("Viewing Purchase Requisitions");
    }
    
    private void showViewPOPanel() {
        setContent(viewPOPanel);
        setStatus("Viewing Purchase Orders");
    }
}