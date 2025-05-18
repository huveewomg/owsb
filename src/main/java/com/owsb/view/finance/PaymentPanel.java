package com.owsb.view.finance;

import com.owsb.controller.PurchaseOrderController;
import com.owsb.model.finance.Payment;
import com.owsb.model.procurement.POItem;
import com.owsb.model.procurement.PurchaseOrder;
import com.owsb.repository.PaymentRepository;
import com.owsb.util.Constants;
import com.owsb.view.PanelHeaderUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Panel for processing payments for approved purchase orders
 * Demonstrates MVC pattern and payment processing UI
 */
public class PaymentPanel extends JPanel {
    // UI components
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    private JLabel titleLabel;
    
    private JComboBox<String> poComboBox;
    private JButton loadButton;
    
    private JLabel supplierLabel;
    private JTextField supplierField;
    
    private JLabel amountLabel;
    private JTextField amountField;
    
    private JLabel paymentMethodLabel;
    private JComboBox<String> paymentMethodComboBox;
    
    private JLabel paymentDateLabel;
    private JSpinner paymentDateSpinner;
    
    private JLabel notesLabel;
    private JTextArea notesArea;
    private JScrollPane notesScrollPane;
    
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    
    private JButton processButton;
    private JButton cancelButton;
    
    // Controllers and repositories
    private final PurchaseOrderController poController;
    private final PaymentRepository paymentRepository;
    
    // Data
    private List<PurchaseOrder> approvedPOs;
    private PurchaseOrder selectedPO;
    
    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor for PaymentPanel
     * @param poController Purchase order controller
     */
    public PaymentPanel(PurchaseOrderController poController) {
        this.poController = poController;
        this.paymentRepository = new PaymentRepository();
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize components
        initComponents();
        
        // Add listeners
        addListeners();
        
        // Load data
        loadApprovedPOs();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        // Create a container for the title and payment form
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        titleLabel = new JLabel("Process Payments", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        
        // Top panel - PO selection and payment details
        topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // PO selection
        JLabel poLabel = new JLabel("Select Purchase Order:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(poLabel, gbc);
        
        poComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        topPanel.add(poComboBox, gbc);
        
        loadButton = new JButton("Load Details");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        topPanel.add(loadButton, gbc);
        
        // Supplier
        supplierLabel = new JLabel("Supplier:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(supplierLabel, gbc);
        
        supplierField = new JTextField(20);
        supplierField.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(supplierField, gbc);
        
        // Amount
        amountLabel = new JLabel("Payment Amount:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(amountLabel, gbc);
        
        amountField = new JTextField(20);
        amountField.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        topPanel.add(amountField, gbc);
        
        // Payment Method
        paymentMethodLabel = new JLabel("Payment Method:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        topPanel.add(paymentMethodLabel, gbc);
        
        paymentMethodComboBox = new JComboBox<>(new String[]{"Bank Transfer", "Check", "Credit Card"});
        gbc.gridx = 1;
        gbc.gridy = 3;
        topPanel.add(paymentMethodComboBox, gbc);
        
        // Payment Date
        paymentDateLabel = new JLabel("Payment Date:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        topPanel.add(paymentDateLabel, gbc);
        
        // Set default payment date to today
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        paymentDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(paymentDateSpinner, "yyyy-MM-dd");
        paymentDateSpinner.setEditor(dateEditor);
        gbc.gridx = 1;
        gbc.gridy = 4;
        topPanel.add(paymentDateSpinner, gbc);
        
        // Notes
        notesLabel = new JLabel("Payment Notes:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        topPanel.add(notesLabel, gbc);
        
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesScrollPane = new JScrollPane(notesArea);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        topPanel.add(notesScrollPane, gbc);
        
        headerPanel.add(topPanel, BorderLayout.CENTER);
        
        // Add the header panel to the main panel
        add(headerPanel, BorderLayout.NORTH);
        // Center panel - Items table
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Purchase Order Items"));
        
        // Create table model
        tableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Item Name", "Quantity", "Unit Price", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        itemsTable = new JTable(tableModel);
        itemsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set up currency renderer
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(currencyRenderer);
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        
        // Create scroll pane
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        processButton = new JButton("Process Payment");
        processButton.setEnabled(false);
        
        cancelButton = new JButton("Cancel");
        
        bottomPanel.add(cancelButton);
        bottomPanel.add(processButton);
        
        // Add panels to main panel
        // headerPanel is already added to NORTH position
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Add listeners to components
     */
    private void addListeners() {
        // Load button listener
        loadButton.addActionListener(e -> loadPODetails());
        
        // Process button listener
        processButton.addActionListener(e -> processPayment());
        
        // Cancel button listener
        cancelButton.addActionListener(e -> cancel());
    }
    
    /**
     * Load approved purchase orders
     */
    private void loadApprovedPOs() {
        // Get POs that are pending payment
        approvedPOs = poController.getPurchaseOrdersByStatus(Constants.PurchaseOrderStatus.PENDING_PAYMENT);
        
        // Clear combo box
        poComboBox.removeAllItems();
        
        // Add POs to combo box
        for (PurchaseOrder po : approvedPOs) {
            String supplierId = po.getItems().isEmpty() ? "Unknown" : po.getItems().get(0).getSupplierID();
            poComboBox.addItem(po.getPoID() + " - Supplier: " + supplierId + " - Date: " + dateFormat.format(po.getDate()));
        }
        
        // Reset fields
        clearFields();
    }
    
    /**
     * Load selected PO details
     */
    private void loadPODetails() {
        int selectedIndex = poComboBox.getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex >= approvedPOs.size()) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a purchase order.", 
                    "No PO Selected", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected PO
        selectedPO = approvedPOs.get(selectedIndex);
        
        // Fill supplier field
        String supplierId = selectedPO.getItems().isEmpty() ? "Unknown" : selectedPO.getItems().get(0).getSupplierID();
        String supplierName = selectedPO.getItems().isEmpty() ? "Unknown" : selectedPO.getItems().get(0).getSupplierName();
        supplierField.setText(supplierId + " - " + supplierName);
        
        // Fill amount field
        amountField.setText(currencyFormat.format(selectedPO.getTotalValue()));
        
        // Clear table
        tableModel.setRowCount(0);
        
        // Add items to table
        for (POItem poItem : selectedPO.getItems()) {
            tableModel.addRow(new Object[]{
                    poItem.getItemID(),
                    poItem.getItemName(),
                    poItem.getQuantity(),
                    poItem.getUnitPrice(),
                    poItem.getTotalCost()
            });
        }
        
        // Enable process button
        processButton.setEnabled(true);
    }
    
    /**
     * Process payment for selected PO
     */
    private void processPayment() {
        if (selectedPO == null) {
            return;
        }
        
        // Get payment method
        String paymentMethodStr = (String) paymentMethodComboBox.getSelectedItem();
        Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.fromString(paymentMethodStr);
        
        // Get payment date
        Date paymentDate = (Date) paymentDateSpinner.getValue();
        
        // Get notes
        String notes = notesArea.getText().trim();
        
        // Get supplier ID
        String supplierID = selectedPO.getItems().isEmpty() ? 
                "Unknown" : selectedPO.getItems().get(0).getSupplierID();
        
        // Confirm payment
        int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to process payment for " + 
                selectedPO.getPoID() + " in the amount of " + 
                currencyFormat.format(selectedPO.getTotalValue()) + "?", 
                "Confirm Payment", 
                JOptionPane.YES_NO_OPTION);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Generate payment reference number
        String referenceNumber = Payment.generateReferenceNumber(paymentMethod, paymentDate);
        
        // Generate payment ID
        String paymentID = paymentRepository.generateNewPaymentID();
        
        // Create payment record
        Payment payment = new Payment(
            paymentID,
            paymentDate,
            selectedPO.getPoID(),
            supplierID,
            selectedPO.getTotalValue(),
            paymentMethod,
            referenceNumber,
            poController.getCurrentUser().getUserId(),
            Payment.Status.COMPLETED,
            notes
        );
        
        // Save payment record
        boolean paymentSaved = paymentRepository.save(payment);
        
        // Complete the PO - mark it as fully paid
        boolean poCompleted = false;
        if (paymentSaved) {
            poCompleted = poController.completePurchaseOrder(selectedPO.getPoID());
        }
        
        if (paymentSaved && poCompleted) {
            JOptionPane.showMessageDialog(this, 
                    "Payment processed successfully!\n\n" +
                    "Payment ID: " + paymentID + "\n" +
                    "PO: " + selectedPO.getPoID() + "\n" +
                    "Amount: " + currencyFormat.format(selectedPO.getTotalValue()) + "\n" +
                    "Method: " + paymentMethod.getDisplayName() + "\n" +
                    "Reference: " + referenceNumber + "\n" +
                    "Date: " + dateFormat.format(paymentDate), 
                    "Payment Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Clear fields and reload POs
            clearFields();
            loadApprovedPOs();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "There was a problem processing the payment.", 
                    "Payment Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clear all fields
     */
    private void clearFields() {
        supplierField.setText("");
        amountField.setText("");
        paymentMethodComboBox.setSelectedIndex(0);
        paymentDateSpinner.setValue(new Date());
        notesArea.setText("");
        tableModel.setRowCount(0);
        processButton.setEnabled(false);
        selectedPO = null;
    }
    
    /**
     * Cancel payment processing
     */
    private void cancel() {
        clearFields();
    }
}