package com.owsb.view.finance;

import com.owsb.model.finance.Payment;
import com.owsb.repository.PaymentRepository;
import com.owsb.view.PanelHeaderUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Panel for viewing payment history
 * Supports filtering by status, date range, etc.
 */
public class PaymentHistoryPanel extends JPanel {
    // UI components
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    private JLabel titleLabel;
    
    private JComboBox<StatusFilter> statusFilterComboBox;
    private JButton refreshButton;
    
    private JTable paymentsTable;
    private DefaultTableModel tableModel;
    
    private JButton viewButton;
    
    // Repository
    private final PaymentRepository paymentRepository;
    
    // Data
    private List<Payment> payments;
    
    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    /**
     * Constructor for PaymentHistoryPanel
     */
    public PaymentHistoryPanel() {
        this.paymentRepository = new PaymentRepository();
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize components
        initComponents();
        
        // Add listeners
        addListeners();
        
        // Load data
        loadPayments();
    }
    
    /**
     * Initialize components
     */
    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        JLabel titleLabel = PanelHeaderUtils.createHeaderLabel("Payment History");
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Top panel - Filters and refresh button
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JLabel statusFilterLabel = new JLabel("Status:");
        statusFilterComboBox = new JComboBox<>(getStatusFilters());
        
        refreshButton = new JButton("Refresh");
        
        topPanel.add(statusFilterLabel);
        topPanel.add(statusFilterComboBox);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(refreshButton);
        
        headerPanel.add(topPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        
        // Center panel - Payments table
        centerPanel = new JPanel(new BorderLayout());
        
        // Create table model
        tableModel = new DefaultTableModel(
                new Object[]{"Payment ID", "Date", "PO ID", "Supplier", "Amount", "Method", "Status", "Reference", "Notes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        paymentsTable = new JTable(tableModel);
        paymentsTable.getTableHeader().setReorderingAllowed(false);
        paymentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Create simple renderers
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        // Set column renderers
        paymentsTable.getColumnModel().getColumn(1).setCellRenderer(dateRenderer);
        paymentsTable.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        
        // Set column widths
        paymentsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        paymentsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        paymentsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        paymentsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        paymentsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        paymentsTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        paymentsTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        paymentsTable.getColumnModel().getColumn(7).setPreferredWidth(150);
        paymentsTable.getColumnModel().getColumn(8).setPreferredWidth(200);
        
        // Create scroll pane for table
        JScrollPane tableScrollPane = new JScrollPane(paymentsTable);
        tableScrollPane.setPreferredSize(new Dimension(900, 350));
        
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        viewButton = new JButton("View Details");
        viewButton.setEnabled(false);
        
        bottomPanel.add(viewButton);
        
        // Add panels to main panel
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Get status filters for combo box
     * @return Array of status filters
     */
    private StatusFilter[] getStatusFilters() {
        return new StatusFilter[]{
                new StatusFilter("All", null),
                new StatusFilter("Pending", Payment.Status.PENDING),
                new StatusFilter("Completed", Payment.Status.COMPLETED),
                new StatusFilter("Failed", Payment.Status.FAILED),
                new StatusFilter("Cancelled", Payment.Status.CANCELLED)
        };
    }
    
    /**
     * Add listeners to components
     */
    private void addListeners() {
        // Status filter combo box listener
        statusFilterComboBox.addActionListener(e -> filterPayments());
        
        // Refresh button listener
        refreshButton.addActionListener(e -> loadPayments());
        
        // Table selection listener
        paymentsTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = paymentsTable.getSelectedRow();
            boolean hasSelection = selectedRow != -1;
            
            viewButton.setEnabled(hasSelection);
        });
        
        // View button listener
        viewButton.addActionListener(e -> viewPayment());
    }
    
    /**
     * Load all payments
     */
    public void loadPayments() {
        // Get all payments
        payments = paymentRepository.findAll();
        
        // Apply filter
        filterPayments();
    }
    
    /**
     * Filter payments based on selected status
     */
    private void filterPayments() {
        // Clear table
        tableModel.setRowCount(0);
        
        // Get selected filter
        StatusFilter filter = (StatusFilter) statusFilterComboBox.getSelectedItem();
        
        if (filter == null) {
            return;
        }
        
        // Filter payments
        List<Payment> filteredPayments;
        
        if (filter.getStatus() == null) {
            // Show all
            filteredPayments = payments;
        } else {
            // Filter by status
            filteredPayments = paymentRepository.findByStatus(filter.getStatus());
        }
        
        // Add to table
        for (Payment payment : filteredPayments) {
            tableModel.addRow(new Object[]{
                    payment.getPaymentID(),
                    payment.getDate(),
                    payment.getPoID(),
                    payment.getSupplierID(),
                    payment.getAmount(),
                    payment.getPaymentMethod().getDisplayName(),
                    payment.getStatus().getDisplayName(),
                    payment.getReferenceNumber(),
                    payment.getNotes()
            });
        }
        
        // Reset button states
        viewButton.setEnabled(false);
    }
    
    /**
     * View selected payment details
     */
    private void viewPayment() {
        int selectedRow = paymentsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        // Get the payment ID
        String paymentID = (String) paymentsTable.getValueAt(selectedRow, 0);
        
        // Get the payment
        Payment payment = payments.stream()
                .filter(p -> p.getPaymentID().equals(paymentID))
                .findFirst()
                .orElse(null);
        
        if (payment == null) {
            JOptionPane.showMessageDialog(this, 
                    "Payment not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show payment details
        JOptionPane.showMessageDialog(this, 
                "Payment ID: " + payment.getPaymentID() + "\n" +
                "Date: " + dateFormat.format(payment.getDate()) + "\n" +
                "PO ID: " + payment.getPoID() + "\n" +
                "Supplier ID: " + payment.getSupplierID() + "\n" +
                "Amount: " + currencyFormat.format(payment.getAmount()) + "\n" +
                "Method: " + payment.getPaymentMethod().getDisplayName() + "\n" +
                "Reference: " + payment.getReferenceNumber() + "\n" +
                "Status: " + payment.getStatus().getDisplayName() + "\n" +
                "Notes: " + payment.getNotes() + "\n", 
                "Payment Details", 
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Status filter class for combo box
     */
    private static class StatusFilter {
        private final String displayName;
        private final Payment.Status status;
        
        public StatusFilter(String displayName, Payment.Status status) {
            this.displayName = displayName;
            this.status = status;
        }
        
        public Payment.Status getStatus() {
            return status;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}