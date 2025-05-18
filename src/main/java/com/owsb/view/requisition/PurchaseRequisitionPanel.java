package com.owsb.view.requisition;

import com.owsb.controller.PurchaseRequisitionController;
import com.owsb.model.user.User;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel for purchase requisition management
 * Acts as a container for PR creation and list panels
 */
public class PurchaseRequisitionPanel extends JPanel implements PropertyChangeListener {
    // Panels
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private PurchaseRequisitionCreationPanel creationPanel;
    private PurchaseRequisitionListPanel listPanel;
    
    // Controller
    private final PurchaseRequisitionController prController;
    
    // Current user
    private final User currentUser;
    
    /**
     * Constructor for PurchaseRequisitionPanel
     * @param prController Purchase requisition controller
     * @param currentUser Current user
     */
    public PurchaseRequisitionPanel(PurchaseRequisitionController prController, User currentUser) {
        this.prController = prController;
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
        
        // Create PR creation panel
        creationPanel = new PurchaseRequisitionCreationPanel(prController);
        creationPanel.addPropertyChangeListener(this);
        
        // Create PR list panel
        listPanel = new PurchaseRequisitionListPanel(prController, currentUser, false);
        listPanel.addPropertyChangeListener(this);
        
        // Add panels to card panel
        cardPanel.add(listPanel, "list");
        cardPanel.add(creationPanel, "creation");
        
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
        JLabel titleLabel = new JLabel("Purchase Requisitions");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton newButton = new JButton("New Requisition");
        JButton listButton = new JButton("View All Requisitions");
        
        newButton.addActionListener(e -> showCreationPanel(null));
        listButton.addActionListener(e -> showListPanel());
        
        buttonPanel.add(newButton);
        buttonPanel.add(listButton);
        
        // Add components to panel
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Show the PR creation panel
     * @param prId PR ID to edit, or null for new PR
     */
    private void showCreationPanel(String prId) {
        if (prId != null) {
            // Set up for editing
            creationPanel.setupForEdit(prId);
        }
        
        cardLayout.show(cardPanel, "creation");
    }
    
    /**
     * Show the PR list panel
     */
    private void showListPanel() {
        // Reload PRs
        listPanel.loadPurchaseRequisitions();
        
        cardLayout.show(cardPanel, "list");
    }
    
    /**
     * Handle property change events from child panels
     * @param evt Property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "prSaved", "prSubmitted", "prCancelled" -> showListPanel();
            case "editPR" -> showCreationPanel((String) evt.getNewValue());
            case "createPO" -> firePropertyChange("createPO", null, evt.getNewValue());
        }
    }
}