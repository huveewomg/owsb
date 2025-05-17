package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.ItemController;
import com.owsb.controller.MessageController;
import com.owsb.controller.PurchaseOrderController;
import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.user.InventoryManager;
import com.owsb.model.user.User;
import com.owsb.view.inventory.LowStockAlertsPanel;
import com.owsb.view.inventory.StockReportsPanel;
import com.owsb.view.inventory.StockUpdatePanel;
import com.owsb.view.item.ItemListPanel;
import com.owsb.view.message.MessagePanel;
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
    private final ItemController itemController;
    private final MessageController messageController;
    
    // Specific panels for Inventory Manager
    private ItemListPanel viewItemsPanel;
    private LowStockAlertsPanel lowStockPanel;
    private StockUpdatePanel updateStockPanel;
    private StockReportsPanel stockReportsPanel;
    private PurchaseOrderPanel viewPOPanel;
    private MessagePanel messagePanel;
    
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
        
        this.itemController = new ItemController();
        this.itemController.setCurrentUser(user);
        
        this.messageController = new MessageController();
        this.messageController.setCurrentUser(user);
        
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
        addMenuButton("Messages", e -> showMessagesPanel());
    }
    
    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // View Items panel - use the ItemListPanel
        viewItemsPanel = new ItemListPanel(itemController, currentUser);
        
        // Update Stock panel
        updateStockPanel = new StockUpdatePanel(poController, itemController);
        
        // Low Stock Alerts panel - use the new LowStockAlertsPanel
        lowStockPanel = new LowStockAlertsPanel(itemController, currentUser);
        
        // Stock Reports panel
        stockReportsPanel = new StockReportsPanel(itemController, currentUser);
        
        // View Purchase Orders panel
        viewPOPanel = new PurchaseOrderPanel(poController, currentUser);
        
        // Messages panel
        messagePanel = new MessagePanel(messageController, prController, currentUser);
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
    
    private void showMessagesPanel() {
        setContent(messagePanel);
        setStatus("Messages");
    }
}