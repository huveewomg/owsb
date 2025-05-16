package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.ItemController;
import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.controller.SalesController;
import com.owsb.controller.SupplierController;
import com.owsb.controller.PurchaseOrderController;
import com.owsb.model.user.SalesManager;
import com.owsb.model.user.User;
import com.owsb.view.item.ItemManagementPanel;
import com.owsb.view.requisition.PurchaseRequisitionPanel;
import com.owsb.view.sales.SalesEntryPanel;
import com.owsb.view.supplier.SupplierManagementPanel;
import com.owsb.view.order.PurchaseOrderPanel;

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
    private SalesEntryPanel salesPanel;
    private PurchaseRequisitionPanel requisitionPanel;
    private PurchaseOrderPanel purchaseOrderPanel;
    
    // Controllers - using composition to implement functionality
    private final ItemController itemController;
    private final SupplierController supplierController;
    private final SalesController salesController;
    private final PurchaseRequisitionController prController;
    private final PurchaseOrderController poController;
    
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
        this.salesController = new SalesController();
        this.salesController.setCurrentUser(user);
        this.prController = new PurchaseRequisitionController();
        this.prController.setCurrentUser(user);
        this.poController = new PurchaseOrderController();
        this.poController.setCurrentUser(user);
        
        // Initialize UI components
        initPanels();
        
        // Add menu buttons for Sales Manager functions
        addMenuButton("Manage Items", e -> showItemPanel());
        addMenuButton("Manage Suppliers", e -> showSupplierPanel());
        addMenuButton("Daily Sales Entry", e -> showSalesPanel());
        addMenuButton("Purchase Requisitions", e -> showRequisitionPanel());
        addMenuButton("View Purchase Orders", e -> showPurchaseOrdersPanel());
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
        supplierPanel = new SupplierManagementPanel(supplierController, itemController);
        salesPanel = new SalesEntryPanel(salesController);
        requisitionPanel = new PurchaseRequisitionPanel(prController, currentUser);
        purchaseOrderPanel = new PurchaseOrderPanel(poController, currentUser);
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
     * Show purchase orders panel
     */
    private void showPurchaseOrdersPanel() {
        setContent(purchaseOrderPanel);
        setStatus("Viewing Purchase Orders");
    }
}