package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.PurchaseOrderController;
import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.User;
import com.owsb.model.user.PurchaseManager;
import com.owsb.view.order.PurchaseOrderPanel;
import com.owsb.view.requisition.PurchaseRequisitionListPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Dashboard for Purchase Managers
 * Extends BaseDashboard and adds role-specific functionality
 */
public class PurchaseManagerDashboard extends BaseDashboard implements PropertyChangeListener {
    
    // Controllers
    private final PurchaseRequisitionController prController;
    private final PurchaseOrderController poController;
    
    // Panels
    private JPanel viewItemsPanel;
    private JPanel viewSuppliersPanel;
    private PurchaseRequisitionListPanel viewRequisitionsPanel;
    private PurchaseOrderPanel purchaseOrderPanel;
    
    /**
     * Constructor for PurchaseManagerDashboard
     * @param user Current logged in user (should be PurchaseManager)
     * @param authController Authentication controller
     */
    public PurchaseManagerDashboard(User user, AuthController authController) {
        super("OWSB - Purchase Manager Dashboard", user, authController);
        
        // Check if user is a PurchaseManager
        if (!(user instanceof PurchaseManager)) {
            throw new IllegalArgumentException("User must be a Purchase Manager");
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
        // Add menu buttons for Purchase Manager functions
        addMenuButton("View Items", e -> showViewItemsPanel());
        addMenuButton("View Suppliers", e -> showViewSuppliersPanel());
        addMenuButton("View Requisitions/ Create PO", e -> showViewRequisitionsPanel());
        addMenuButton("Purchase Orders", e -> showPurchaseOrdersPanel());
    }
    
    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // View Items panel - Simple panel with table (replace with actual ItemPanel if available)
        viewItemsPanel = createSimplePanel("View Items", 
            new String[]{"Item Code", "Item Name", "Current Stock"},
            new Object[][]{
                {"IT001", "Rice 5kg", 100},
                {"IT002", "Sugar 1kg", 150},
                {"IT003", "Flour 1kg", 80}
            });
        
        // View Suppliers panel - Simple panel with table (replace with actual SupplierPanel if available)
        viewSuppliersPanel = createSimplePanel("View Suppliers", 
            new String[]{"Supplier Code", "Supplier Name", "Contact"},
            new Object[][]{
                {"SUP001", "ABC Groceries", "John (123-456-7890)"},
                {"SUP002", "XYZ Distributors", "Mary (098-765-4321)"}
            });
        
        // Purchase Requisition List Panel - Only for viewing
        viewRequisitionsPanel = new PurchaseRequisitionListPanel(prController, currentUser);
        viewRequisitionsPanel.addPropertyChangeListener(this);
        
        // Purchase Order Panel
        purchaseOrderPanel = new PurchaseOrderPanel(poController, currentUser);
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
    
    private void showViewSuppliersPanel() {
        setContent(viewSuppliersPanel);
        setStatus("Viewing Suppliers");
    }
    
    private void showViewRequisitionsPanel() {
        // Reload requisitions
        viewRequisitionsPanel.loadPurchaseRequisitions();
        
        setContent(viewRequisitionsPanel);
        setStatus("Viewing Purchase Requisitions");
    }
    
    private void showPurchaseOrdersPanel() {
        setContent(purchaseOrderPanel);
        setStatus("Managing Purchase Orders");
    }
    
    /**
     * Handle property change events from child panels
     * @param evt Property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("createPO")) {
            // Show purchase order creation panel with the selected PR
            String prId = (String) evt.getNewValue();
            purchaseOrderPanel.showGenerationPanel(prId);
            setContent(purchaseOrderPanel);
            setStatus("Creating Purchase Order");
        }
    }
}