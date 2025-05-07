package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.ItemController;
import com.owsb.controller.SupplierController;
import com.owsb.model.User;
import com.owsb.model.user.PurchaseManager;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for Purchase Managers Extends BaseDashboard and adds role-specific
 * functionality
 */
public class PurchaseManagerDashboard extends BaseDashboard {

    // Specific panels for Purchase Manager
    private JPanel viewItemsPanel;
    private JPanel viewSuppliersPanel;
    private JPanel viewRequisitionsPanel;
    private JPanel generatePOPanel;
    private JPanel viewPOPanel;

    private SupplierController supplierController;
    private ItemController itemController;

    /**
     * Constructor for PurchaseManagerDashboard
     *
     * @param user Current logged in user (should be PurchaseManager)
     * @param authController Authentication controller
     */
    public PurchaseManagerDashboard(User user, AuthController authController) {
        super("OWSB - Purchase Manager Dashboard", user, authController);

        // Check if user is a PurchaseManager
        if (!(user instanceof PurchaseManager)) {
            throw new IllegalArgumentException("User must be a Purchase Manager");
        }
    }

    /**
     * Override role-specific greeting
     *
     * @return Purchase Manager specific greeting
     */
    @Override
    protected String getRoleSpecificGreeting() {
        PurchaseManager purchaseManager = (PurchaseManager) currentUser;
        return purchaseManager.getPurchaseGreeting();
    }

    /**
     * Initialize Purchase Manager specific components
     */
    @Override
    protected void initRoleComponents() {
        // Initialize panel placeholders
        initPanels();

        // Add menu buttons for Purchase Manager functions
        addMenuButton("View Items", e -> showViewItemsPanel());
        addMenuButton("View Suppliers", e -> showViewSuppliersPanel());
        addMenuButton("View Requisitions", e -> showViewRequisitionsPanel());
        addMenuButton("Generate Purchase Orders", e -> showGeneratePOPanel());
        addMenuButton("View Purchase Orders", e -> showViewPOPanel());
    }

    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // View Items panel
        itemController = new ItemController();
        Object[][] itemData = itemController.getItemData();
        viewItemsPanel = createSimplePanel("View Items",
                new String[]{"Item Code", "Item Name", "Current Stock"},
                itemData);

        // View Suppliers panel
        supplierController = new SupplierController();
        Object[][] supplierData = supplierController.getSupplierData();
        viewSuppliersPanel = createSimplePanel("View Suppliers",
                new String[]{"Supplier Code", "Supplier Name", "Contact"},
                supplierData);

        // View Requisitions panel
        viewRequisitionsPanel = createSimplePanel("View Purchase Requisitions",
                new String[]{"PR ID", "Date", "Item Code", "Quantity", "Required Date", "Status"},
                new Object[][]{
                    {"PR001", "2025-03-25", "IT001", 50, "2025-04-05", "NEW"},
                    {"PR002", "2025-03-26", "IT002", 100, "2025-04-10", "NEW"},
                    {"PR003", "2025-03-27", "IT003", 30, "2025-04-15", "NEW"}
                });

        // Generate Purchase Order panel
        generatePOPanel = new JPanel(new BorderLayout());
        generatePOPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel poLabel = new JLabel("Generate Purchase Order", JLabel.CENTER);
        poLabel.setFont(new Font("Arial", Font.BOLD, 18));
        generatePOPanel.add(poLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        formPanel.add(new JLabel("PR ID:"));
        JComboBox<String> prCombo = new JComboBox<>(new String[]{"PR001", "PR002", "PR003"});
        formPanel.add(prCombo);

        formPanel.add(new JLabel("Supplier:"));
        JComboBox<String> supplierCombo = new JComboBox<>(new String[]{"SUP001", "SUP002"});
        formPanel.add(supplierCombo);

        formPanel.add(new JLabel("Expected Delivery Date:"));
        formPanel.add(new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(
                new java.util.Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000)))); // 2 weeks from now

        formPanel.add(new JLabel("Notes:"));
        formPanel.add(new JTextField());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Generate PO"));
        buttonPanel.add(new JButton("Cancel"));

        generatePOPanel.add(formPanel, BorderLayout.CENTER);
        generatePOPanel.add(buttonPanel, BorderLayout.SOUTH);

        // View Purchase Orders panel
        viewPOPanel = createSimplePanel("View Purchase Orders",
                new String[]{"PO ID", "Date", "PR ID", "Item Code", "Quantity", "Status"},
                new Object[][]{
                    {"PO001", "2025-03-26", "PR001", "IT001", 50, "PENDING"},
                    {"PO002", "2025-03-27", "PR002", "IT002", 100, "PENDING"}
                });
    }

    /**
     * Create a simple panel with a table
     *
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
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Methods to show different panels
    private void showViewItemsPanel() {
        setContent(viewItemsPanel);
        setStatus("Viewing Items");
    }

    private void showViewSuppliersPanel() {
        setContent(viewSuppliersPanel);
        setStatus("Viewing Suppliers");
    }

    private void showViewRequisitionsPanel() {
        setContent(viewRequisitionsPanel);
        setStatus("Viewing Purchase Requisitions");
    }

    private void showGeneratePOPanel() {
        setContent(generatePOPanel);
        setStatus("Generating Purchase Order");
    }

    private void showViewPOPanel() {
        setContent(viewPOPanel);
        setStatus("Viewing Purchase Orders");
    }
}
