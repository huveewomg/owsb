package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.controller.PurchaseOrderController;
import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.user.FinanceManager;
import com.owsb.model.user.User;
import com.owsb.view.finance.PaymentHistoryPanel;
import com.owsb.view.finance.PaymentPanel;
import com.owsb.view.order.PurchaseOrderPanel;
import com.owsb.view.requisition.PurchaseRequisitionListPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for Finance Managers
 * Extends BaseDashboard and adds role-specific functionality
 */
public class FinanceManagerDashboard extends BaseDashboard {
    
    // Controllers
    private final PurchaseRequisitionController prController;
    private final PurchaseOrderController poController;
    
    // Specific panels for Finance Manager
    private PurchaseOrderPanel approvePOPanel;
    private PaymentPanel paymentsPanel;
    private PaymentHistoryPanel paymentHistoryPanel;
    private JPanel financialReportsPanel;
    private PurchaseRequisitionListPanel viewPRPanel;
    
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
        // Add menu buttons for Finance Manager functions
        addMenuButton("Approve Purchase Orders", e -> showApprovePOPanel());
        addMenuButton("Process Payments", e -> showPaymentsPanel());
        addMenuButton("Payment History", e -> showPaymentHistoryPanel());
        addMenuButton("Financial Reports", e -> showFinancialReportsPanel());
        addMenuButton("View Requisitions", e -> showViewPRPanel());
    }
    
    /**
     * Initialize panel placeholders
     */
    private void initPanels() {
        // Approve Purchase Orders panel - Use PurchaseOrderPanel
        approvePOPanel = new PurchaseOrderPanel(poController, currentUser);
        
        // Process Payments panel - Use PaymentPanel
        paymentsPanel = new PaymentPanel(poController);
        
        // Payment History panel - Use PaymentHistoryPanel
        paymentHistoryPanel = new PaymentHistoryPanel();
        
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
        
        // View Purchase Requisitions panel - Use PurchaseRequisitionListPanel
        viewPRPanel = new PurchaseRequisitionListPanel(prController, currentUser);
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
    
    private void showPaymentHistoryPanel() {
        // Reload payment history
        paymentHistoryPanel.loadPayments();
        setContent(paymentHistoryPanel);
        setStatus("Viewing Payment History");
    }
    
    private void showFinancialReportsPanel() {
        setContent(financialReportsPanel);
        setStatus("Financial Reports");
    }
    
    private void showViewPRPanel() {
        // Reload requisitions
        viewPRPanel.loadPurchaseRequisitions();
        setContent(viewPRPanel);
        setStatus("Viewing Purchase Requisitions");
    }
}