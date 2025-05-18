package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.ItemController;
import com.owsb.controller.PurchaseOrderController;
import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.controller.SupplierController;
import com.owsb.model.user.PurchaseManager;
import com.owsb.model.user.User;
import com.owsb.view.item.ItemListPanel;
import com.owsb.view.order.PurchaseOrderPanel;
import com.owsb.view.requisition.PurchaseRequisitionListPanel;
import com.owsb.view.supplier.SupplierListPanel;

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
    private final ItemController itemController;
    private final SupplierController supplierController;
    
    // Panels
    private JPanel viewItemsPanel;
    private SupplierListPanel viewSuppliersPanel;
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
        
        this.itemController = new ItemController();
        this.itemController.setCurrentUser(user);
        
        this.supplierController = new SupplierController();
        this.supplierController.setCurrentUser(user);
        
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
        // View Items panel - Use ItemListPanel
        viewItemsPanel = new ItemListPanel(itemController, currentUser);
        
        // View Suppliers panel - Use SupplierListPanel
        viewSuppliersPanel = new SupplierListPanel(supplierController, currentUser);
        
        // Purchase Requisition List Panel - Only for viewing
        viewRequisitionsPanel = new PurchaseRequisitionListPanel(prController, currentUser, true);
        viewRequisitionsPanel.addPropertyChangeListener(this);
        
        // Purchase Order Panel
        purchaseOrderPanel = new PurchaseOrderPanel(poController, currentUser);
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