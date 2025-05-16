package com.owsb.view.order;

import com.owsb.controller.PurchaseOrderController;
import com.owsb.model.user.User;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel for purchase order management
 * Acts as a container for PO generation and list panels
 */
public class PurchaseOrderPanel extends JPanel implements PropertyChangeListener {
    // Panels
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private PurchaseOrderGenerationPanel generationPanel;
    private PurchaseOrderListPanel listPanel;
    
    // Controller
    private final PurchaseOrderController poController;
    
    // Current user
    private final User currentUser;
    
    /**
     * Constructor for PurchaseOrderPanel
     * @param poController Purchase order controller
     * @param currentUser Current user
     */
    public PurchaseOrderPanel(PurchaseOrderController poController, User currentUser) {
        this.poController = poController;
        this.currentUser = currentUser;
        
        // Set up panel
        setLayout(new BorderLayout());
        
        // Initialize components
        initComponents();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Create card layout for switching between panels
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Create PO generation panel
        generationPanel = new PurchaseOrderGenerationPanel(poController);
        generationPanel.addPropertyChangeListener(this);
        
        // Create PO list panel
        listPanel = new PurchaseOrderListPanel(poController, currentUser);
        
        // Add panels to card panel
        cardPanel.add(listPanel, "list");
        cardPanel.add(generationPanel, "generation");
        
        // Show list panel initially
        cardLayout.show(cardPanel, "list");
        
        // Add panels to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create header panel with title and buttons
     * @return Header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create title label
        JLabel titleLabel = new JLabel("Purchase Orders");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton listButton = new JButton("View All Purchase Orders");
        
        listButton.addActionListener(e -> showListPanel());
        
        buttonPanel.add(listButton);
        
        // Add components to panel
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Show the PO generation panel
     * @param prId PR ID to create PO from
     */
    public void showGenerationPanel(String prId) {
        // Load the PR
        generationPanel.loadPurchaseRequisition(prId);
        
        cardLayout.show(cardPanel, "generation");
    }
    
    /**
     * Show the PO list panel
     */
    private void showListPanel() {
        // Reload POs
        listPanel.loadPurchaseOrders();
        
        cardLayout.show(cardPanel, "list");
    }
    
    /**
     * Handle property change events from child panels
     * @param evt Property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("poGenerated") || 
            evt.getPropertyName().equals("poCancelled")) {
            showListPanel();
        }
    }
}