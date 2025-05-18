package com.owsb.view.finance;

import com.owsb.controller.PurchaseOrderController;
import com.owsb.controller.SalesController;
import com.owsb.model.finance.Payment;
import com.owsb.model.procurement.POItem;
import com.owsb.model.procurement.PurchaseOrder;
import com.owsb.model.sales.Sale;
import com.owsb.model.sales.SaleItem;
import com.owsb.model.user.User;
import com.owsb.repository.PaymentRepository;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel for generating and displaying various financial reports
 */
public class FinancialReportsPanel extends JPanel {

    private final PurchaseOrderController poController;
    private final SalesController salesController;
    private final PaymentRepository paymentRepository;
    private final User currentUser;
    
    // UI Components
    private JComboBox<String> reportTypeComboBox;
    private JPanel reportContentPanel;
    private JPanel chartPanel;
    private JPanel tablePanel;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    
    // Date range filter components
    private JPanel filterPanel;
    private JComboBox<String> periodComboBox;
    private JButton generateButton;
    
    // Formatters
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    
    // Report types
    private static final String PURCHASE_SUMMARY = "Purchase Summary Report";
    private static final String PROFIT_LOSS = "Profit and Loss Report";
    private static final String SUPPLIER_PAYMENT = "Supplier Payment Report";
    
    /**
     * Constructor for FinancialReportsPanel
     * @param poController Purchase Order controller
     * @param salesController Sales controller
     * @param currentUser Current logged-in user
     */
    public FinancialReportsPanel(PurchaseOrderController poController, SalesController salesController, User currentUser) {
        this.poController = poController;
        this.salesController = salesController;
        this.paymentRepository = new PaymentRepository();
        this.currentUser = currentUser;
        
        // Set up panel
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Initialize components
        initComponents();
        
        // Add listeners
        addListeners();
        
        // Load initial report
        generatePurchaseSummaryReport();
    }
    
    /**
     * Initialize panel components
     */
    private void initComponents() {
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Financial Reports", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // Control panel - Report selection and filters
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Report type selection panel
        JPanel reportTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypePanel.add(new JLabel("Report Type:"));
        
        reportTypeComboBox = new JComboBox<>(new String[]{
            PURCHASE_SUMMARY,
            PROFIT_LOSS,
            SUPPLIER_PAYMENT
        });
        reportTypePanel.add(reportTypeComboBox);
        
        // Filter panel
        filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("Period:"));
        
        periodComboBox = new JComboBox<>(new String[]{
            "Last Month",
            "Last Quarter",
            "Year to Date",
            "All Time"
        });
        filterPanel.add(periodComboBox);
        
        generateButton = new JButton("Generate Report");
        filterPanel.add(generateButton);
        
        // Add report type and filter panels to control panel
        controlPanel.add(reportTypePanel, BorderLayout.WEST);
        controlPanel.add(filterPanel, BorderLayout.EAST);
        
        // Add title and control panels to top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);
        
        // Report content panel
        reportContentPanel = new JPanel(new BorderLayout());
        
        // Chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Report Chart"));
        
        // Table panel
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Report Details"));
        
        // Initialize table
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(tableModel);
        reportTable.setFillsViewportHeight(true);
        JScrollPane tableScrollPane = new JScrollPane(reportTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Add chart and table panels to report content panel
        reportContentPanel.add(chartPanel, BorderLayout.NORTH);
        reportContentPanel.add(tablePanel, BorderLayout.CENTER);
        
        add(reportContentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Add action listeners to components
     */
    private void addListeners() {
        // Report type combo box listener
        reportTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSelectedReport();
            }
        });
        
        // Generate button listener
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSelectedReport();
            }
        });
    }
    
    /**
     * Generate the currently selected report
     */
    private void generateSelectedReport() {
        String selectedReport = (String) reportTypeComboBox.getSelectedItem();
        
        if (selectedReport == null) {
            return;
        }
        
        switch (selectedReport) {
            case PURCHASE_SUMMARY:
                generatePurchaseSummaryReport();
                break;
            case PROFIT_LOSS:
                generateProfitAndLossReport();
                break;
            case SUPPLIER_PAYMENT:
                generateSupplierPaymentReport();
                break;
        }
    }
    
    /**
     * Generate Purchase Summary Report
     */
    private void generatePurchaseSummaryReport() {
        // Get purchase orders
        List<PurchaseOrder> purchaseOrders = poController.getAllPurchaseOrders();
        
        // Setup table columns
        setupTableModel(new String[]{
            "PO ID", "Date", "Supplier", "Status", "Total Value (RM)", "Items Count"
        });
        
        // Map to store supplier totals
        Map<String, Double> supplierTotals = new HashMap<>();
        
        // Total PO value
        double totalPOValue = 0;
        
        // Populate table with PO data
        for (PurchaseOrder po : purchaseOrders) {
            // Extract supplier name from first item
            String supplierName = po.getItems().isEmpty() ? "Unknown" : po.getItems().get(0).getSupplierName();
            
            // Count items
            int itemCount = po.getItems().size();
            
            // Format PO date
            String poDate = dateFormat.format(po.getDate());
            
            // Format status
            String status = po.getStatus().toString();
            
            // Format total value
            String totalValue = currencyFormat.format(po.getTotalValue());
            
            // Add to table
            tableModel.addRow(new Object[]{
                po.getPoID(),
                poDate,
                supplierName,
                status,
                totalValue,
                itemCount
            });
            
            // Update supplier totals
            supplierTotals.put(supplierName, 
                supplierTotals.getOrDefault(supplierName, 0.0) + po.getTotalValue());
            
            // Update total PO value
            totalPOValue += po.getTotalValue();
        }
        
        // Set up table sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        reportTable.setRowSorter(sorter);
        
        // Create supplier spending chart
        createSupplierSpendingChart(supplierTotals);
        
        // Create summary panel with key metrics
        JPanel summaryPanel = createSummaryPanel(
            new String[]{"Total Purchase Orders", "Total Spending", "Average PO Value"},
            new String[]{
                String.valueOf(purchaseOrders.size()),
                currencyFormat.format(totalPOValue),
                currencyFormat.format(purchaseOrders.isEmpty() ? 0 : totalPOValue / purchaseOrders.size())
            }
        );
        
        // Add summary to the top of the report content
        reportContentPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        // Update UI
        revalidate();
        repaint();
    }
    
    /**
     * Generate Profit and Loss Report
     */
    private void generateProfitAndLossReport() {
        // Get sales data
        List<Sale> sales = salesController.getAllSales();
        // Get purchase orders for loss calculation
        List<PurchaseOrder> purchaseOrders = poController.getAllPurchaseOrders();

        // Setup table columns
        setupTableModel(new String[]{
            "Sale ID", "Date", "Sales Manager", "Items Sold", "Total Sales (RM)"
        });

        // Calculate total sales amount
        double totalSalesAmount = 0;
        // Map to store sales by item category
        Map<String, Double> salesByCategory = new HashMap<>();

        // Populate table with sales data
        for (Sale sale : sales) {
            String saleDate = sale.getDate() != null ? dateFormat.format(sale.getDate()) : "N/A";
            int itemCount = sale.getItems().size();
            String totalAmount = currencyFormat.format(sale.getTotalAmount());
            tableModel.addRow(new Object[]{
                sale.getSaleID(),
                saleDate,
                sale.getSalesManagerID(),
                itemCount,
                totalAmount
            });
            totalSalesAmount += sale.getTotalAmount();
            for (SaleItem item : sale.getItems()) {
                String category = item.getItemName().split(" ")[0];
                salesByCategory.put(category,
                    salesByCategory.getOrDefault(category, 0.0) + item.getSubtotal());
            }
        }

        // Calculate total loss (all COMPLETED purchase orders)
        double totalLoss = purchaseOrders.stream()
            .filter(po -> "COMPLETED".equals(po.getStatus().toString()))
            .mapToDouble(PurchaseOrder::getTotalValue)
            .sum();

        // Calculate net profit
        double netProfit = totalSalesAmount - totalLoss;
        // Calculate profit margin
        double profitMarginPercentage = totalSalesAmount > 0 ? (netProfit / totalSalesAmount) * 100 : 0;

        // Create sales by category chart
        createSalesByCategoryChart(salesByCategory);

        // Create summary panel with key metrics
        JPanel summaryPanel = createSummaryPanel(
            new String[]{"Total Sales", "Total Loss", "Net Profit", "Profit Margin"},
            new String[]{
                currencyFormat.format(totalSalesAmount),
                currencyFormat.format(totalLoss),
                currencyFormat.format(netProfit),
                String.format("%.2f%%", profitMarginPercentage)
            }
        );

        // Add summary to the top of the report content
        reportContentPanel.add(summaryPanel, BorderLayout.SOUTH);

        // Update UI
        revalidate();
        repaint();
    }
    
    /**
     * Generate Supplier Payment Report
     */
    private void generateSupplierPaymentReport() {
        // Get payments
        List<Payment> payments = paymentRepository.findAll();
        
        // Group payments by supplier
        Map<String, List<Payment>> paymentsBySupplier = payments.stream()
            .collect(Collectors.groupingBy(Payment::getSupplierID));
        
        // Calculate total payment amount per supplier
        Map<String, Double> supplierPaymentTotals = new HashMap<>();
        
        for (Map.Entry<String, List<Payment>> entry : paymentsBySupplier.entrySet()) {
            double total = entry.getValue().stream()
                .mapToDouble(Payment::getAmount)
                .sum();
            supplierPaymentTotals.put(entry.getKey(), total);
        }
        
        // Setup table columns
        setupTableModel(new String[]{
            "Supplier ID", "Payment Count", "Total Paid (RM)", "Last Payment Date"
        });
        
        // Populate table
        for (Map.Entry<String, List<Payment>> entry : paymentsBySupplier.entrySet()) {
            String supplierID = entry.getKey();
            List<Payment> supplierPayments = entry.getValue();
            
            // Count payments
            int paymentCount = supplierPayments.size();
            
            // Calculate total
            double totalPaid = supplierPaymentTotals.get(supplierID);
            
            // Get last payment date
            Date lastPaymentDate = supplierPayments.stream()
                .map(Payment::getDate)
                .max(Date::compareTo)
                .orElse(null);
            
            String lastPaymentDateStr = lastPaymentDate != null ? 
                dateFormat.format(lastPaymentDate) : "N/A";
            
            // Add to table
            tableModel.addRow(new Object[]{
                supplierID,
                paymentCount,
                currencyFormat.format(totalPaid),
                lastPaymentDateStr
            });
        }
        
        // Create supplier payment chart
        createSupplierPaymentChart(supplierPaymentTotals);
        
        // Calculate total payments
        double totalPayments = payments.stream()
            .mapToDouble(Payment::getAmount)
            .sum();
        
        // Create summary panel with key metrics
        JPanel summaryPanel = createSummaryPanel(
            new String[]{"Total Suppliers Paid", "Total Payments", "Total Amount Paid"},
            new String[]{
                String.valueOf(paymentsBySupplier.size()),
                String.valueOf(payments.size()),
                currencyFormat.format(totalPayments)
            }
        );
        
        // Add summary to the top of the report content
        reportContentPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        // Update UI
        revalidate();
        repaint();
    }
    
    /**
     * Create a supplier spending pie chart
     */
    private void createSupplierSpendingChart(Map<String, Double> supplierTotals) {
        // Clear previous chart
        chartPanel.removeAll();
        
        // Create dataset
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        // Add top suppliers (to avoid too many slices)
        List<Map.Entry<String, Double>> topSuppliers = supplierTotals.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());
        
        for (Map.Entry<String, Double> entry : topSuppliers) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createPieChart(
            "Top 5 Suppliers by Spending",
            dataset,
            true,  // include legend
            true,  // tooltips
            false  // URLs
        );
        
        // Create panel
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(600, 300));
        
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.revalidate();
    }
    
    /**
     * Create a sales by category bar chart
     */
    private void createSalesByCategoryChart(Map<String, Double> salesByCategory) {
        // Clear previous chart
        chartPanel.removeAll();
        
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add categories
        for (Map.Entry<String, Double> entry : salesByCategory.entrySet()) {
            dataset.addValue(entry.getValue(), "Sales", entry.getKey());
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
            "Sales by Product Category",
            "Category",
            "Sales (RM)",
            dataset,
            PlotOrientation.VERTICAL,
            false,  // include legend
            true,   // tooltips
            false   // URLs
        );
        
        // Customize the chart
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        
        // Rotate category labels if there are many
        CategoryAxis domainAxis = plot.getDomainAxis();
        if (salesByCategory.size() > 5) {
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        }
        
        // Color bars
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 128, 0));
        
        // Create panel
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(600, 300));
        
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.revalidate();
    }
    
    /**
     * Create a supplier payment bar chart
     */
    private void createSupplierPaymentChart(Map<String, Double> supplierPaymentTotals) {
        // Clear previous chart
        chartPanel.removeAll();
        
        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add top suppliers (to avoid too many bars)
        List<Map.Entry<String, Double>> topSuppliers = supplierPaymentTotals.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());
        
        for (Map.Entry<String, Double> entry : topSuppliers) {
            dataset.addValue(entry.getValue(), "Payments", entry.getKey());
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
            "Top 5 Suppliers by Payment Amount",
            "Supplier",
            "Amount Paid (RM)",
            dataset,
            PlotOrientation.VERTICAL,
            false,  // include legend
            true,   // tooltips
            false   // URLs
        );
        
        // Customize the chart
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        
        // Rotate supplier labels if there are many
        CategoryAxis domainAxis = plot.getDomainAxis();
        if (topSuppliers.size() > 5) {
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        }
        
        // Color bars
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 0, 128));
        
        // Create panel
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(600, 300));
        
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.revalidate();
    }
    
    /**
     * Set up the table model with specified columns
     */
    private void setupTableModel(String[] columns) {
        // Clear existing table
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        // Add columns
        for (String column : columns) {
            tableModel.addColumn(column);
        }
        
        // Set up currency renderer for monetary columns
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer();
        currencyRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        // Apply currency renderer to columns containing monetary values
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].contains("(RM)") || columns[i].contains("Amount") || 
                columns[i].contains("Value") || columns[i].contains("Cost") || 
                columns[i].contains("Budget") || columns[i].contains("Actual") || 
                columns[i].contains("Variance")) {
                reportTable.getColumnModel().getColumn(i).setCellRenderer(currencyRenderer);
            }
        }
    }
    
    /**
     * Create a summary panel with key metrics
     */
    private JPanel createSummaryPanel(String[] metricLabels, String[] metricValues) {
        // Remove previous summary panel if it exists
        try {
            // Check if a component already exists at position 2
            if (reportContentPanel.getComponentCount() > 2) {
                Component existingSummary = reportContentPanel.getComponent(2);
                if (existingSummary != null) {
                    reportContentPanel.remove(existingSummary);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // No summary panel exists yet, which is fine
        }
        
        // Create summary panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        summaryPanel.setLayout(new GridLayout(1, metricLabels.length, 10, 5));
        
        // Add metrics
        for (int i = 0; i < metricLabels.length; i++) {
            JPanel metricPanel = new JPanel(new BorderLayout());
            
            JLabel valueLabel = new JLabel(metricValues[i], JLabel.CENTER);
            valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            JLabel descLabel = new JLabel(metricLabels[i], JLabel.CENTER);
            descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            metricPanel.add(valueLabel, BorderLayout.CENTER);
            metricPanel.add(descLabel, BorderLayout.SOUTH);
            
            summaryPanel.add(metricPanel);
        }
        
        return summaryPanel;
    }
    
    /**
     * Get month name from month index (0-based)
     */
    private String getMonthName(int month) {
        String[] monthNames = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        
        return monthNames[month];
    }
}