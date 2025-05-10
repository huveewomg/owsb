package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.ItemController;
import com.owsb.controller.SupplierController;
// import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.User;
import com.owsb.model.user.SalesManager;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for Sales Managers
 * Demonstrates MVC pattern by separating UI from business logic
 */
public class SalesManagerDashboard extends BaseDashboard {
    
    // Dedicated management panels
    private ItemManagementPanel itemPanel;
    private SupplierManagementPanel supplierPanel;
    private JPanel salesPanel;
    private JPanel requisitionPanel;
    
    // Controllers - using composition to implement functionality
    private final ItemController itemController;
    private final SupplierController supplierController;
    // private final PurchaseRequisitionController prController;
    
    /**
     * Constructor for SalesManagerDashboard
     * @param user Current logged in user (should be SalesManager)
     * @param authController Authentication controller
     */
    public SalesManagerDashboard(User user, AuthController authController) {
        super("OWSB - Sales Manager Dashboard", user, authController);
        
        // Enforce type safety - polymorphism principle in action
        if (!(user instanceof SalesManager)) {
            throw new IllegalArgumentException("User must be a Sales Manager");
        }
        
        // Initialize controllers
        this.itemController = new ItemController();
        this.itemController.setCurrentUser(user);
        this.supplierController = new SupplierController();
        // this.prController = new PurchaseRequisitionController();
        
        // Initialize UI components
        initPanels();
        
        // Add menu buttons for Sales Manager functions
        addMenuButton("Manage Items", e -> showItemPanel());
        addMenuButton("Manage Suppliers", e -> showSupplierPanel());
        addMenuButton("Daily Sales Entry", e -> showSalesPanel());
        addMenuButton("Purchase Requisitions", e -> showRequisitionPanel());
        addMenuButton("View Purchase Orders", e -> viewPurchaseOrders());
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
     * Initialize role-specific components
     * Implementation of the Template Method pattern from BaseDashboard
     */
    @Override
    protected void initRoleComponents() {
        // Components initialized in constructor
    }
    
    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // Create dedicated panels using the new classes
        itemPanel = new ItemManagementPanel(itemController);
        supplierPanel = new SupplierManagementPanel(supplierController);
        
        // Sales entry panel placeholder
        salesPanel = new JPanel(new BorderLayout());
        JLabel salesLabel = new JLabel("Daily Sales Entry", JLabel.CENTER);
        salesLabel.setFont(new Font("Arial", Font.BOLD, 18));
        salesPanel.add(salesLabel, BorderLayout.NORTH);
        
        JLabel salesPlaceholder = new JLabel("Sales entry functionality coming soon...", JLabel.CENTER);
        salesPlaceholder.setFont(new Font("Arial", Font.ITALIC, 14));
        salesPanel.add(salesPlaceholder, BorderLayout.CENTER);
        
        // Purchase requisition panel placeholder
        requisitionPanel = new JPanel(new BorderLayout());
        JLabel requisitionLabel = new JLabel("Purchase Requisitions", JLabel.CENTER);
        requisitionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        requisitionPanel.add(requisitionLabel, BorderLayout.NORTH);
        
        JLabel requisitionPlaceholder = new JLabel("Purchase requisition functionality coming soon...", JLabel.CENTER);
        requisitionPlaceholder.setFont(new Font("Arial", Font.ITALIC, 14));
        requisitionPanel.add(requisitionPlaceholder, BorderLayout.CENTER);
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
        
        JLabel placeholder = new JLabel("Purchase order functionality coming soon...", JLabel.CENTER);
        placeholder.setFont(new Font("Arial", Font.ITALIC, 14));
        panel.add(placeholder, BorderLayout.CENTER);
        
        setContent(panel);
        setStatus("Viewing Purchase Orders");
    }
}