package com.owsb.view.inventory;

import com.owsb.controller.ItemController;
import com.owsb.model.inventory.Item;
import com.owsb.model.user.User;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel for generating and displaying various stock reports
 */
public class StockReportsPanel extends JPanel {
    
    private final ItemController itemController;
    private final User currentUser;
    
    private JComboBox<String> reportTypeComboBox;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JPanel chartPanel;
    private JPanel filterPanel;
    private JComboBox<String> categoryFilter;
    
    /**
     * Constructor for StockReportsPanel
     * @param itemController Controller for item-related operations
     * @param currentUser Current logged-in user
     */
    public StockReportsPanel(ItemController itemController, User currentUser) {
        this.itemController = itemController;
        this.currentUser = currentUser;
        
        setLayout(new BorderLayout());
        initComponents();
        loadCurrentStockReport(); // Default report
    }
    
    /**
     * Initialize panel components
     */
    private void initComponents() {
        // Title label
        JLabel titleLabel = new JLabel("Stock Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        // Top control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Report selection
        JPanel reportSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportSelectionPanel.add(new JLabel("Report Type:"));
        
        reportTypeComboBox = new JComboBox<>(new String[] {
            "Current Stock Report", 
            "Low Stock Report",
            "Stock Valuation Report"
        });
        
        reportTypeComboBox.addActionListener(e -> {
            String selectedReport = (String) reportTypeComboBox.getSelectedItem();
            if (selectedReport != null) {
                switch (selectedReport) {
                    case "Current Stock Report":
                        loadCurrentStockReport();
                        break;
                    case "Low Stock Report":
                        loadLowStockReport();
                        break;
                    case "Stock Valuation Report":
                        loadStockValuationReport();
                        break;
                }
            }
        });
        
        reportSelectionPanel.add(reportTypeComboBox);
        controlPanel.add(reportSelectionPanel, BorderLayout.WEST);
        
        // Filter panel
        filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.add(new JLabel("Filter by Category:"));
        
        // Get unique categories from items
        List<String> categories = itemController.getAllItems().stream()
                .map(Item::getCategory)
                .distinct()
                .collect(Collectors.toList());
        
        String[] categoryArray = new String[categories.size() + 1];
        categoryArray[0] = "All Categories";
        for (int i = 0; i < categories.size(); i++) {
            categoryArray[i + 1] = categories.get(i);
        }
        
        categoryFilter = new JComboBox<>(categoryArray);
        categoryFilter.addActionListener(e -> applyFilters());
        filterPanel.add(categoryFilter);
        
        JButton printButton = new JButton("Print Report");
        printButton.addActionListener(e -> printReport());
        filterPanel.add(printButton);
        
        controlPanel.add(filterPanel, BorderLayout.EAST);
        
        // Header panel with vertical layout for title and controls
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(controlPanel);
        add(headerPanel, BorderLayout.NORTH);

        // Center panel with table and chart
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table model with columns
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        reportTable = new JTable(tableModel);
        reportTable.setFillsViewportHeight(true);
        reportTable.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(reportTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(tablePanel);
        
        // Chart panel (placeholder initially)
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(chartPanel);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Load the Current Stock Report
     */
    private void loadCurrentStockReport() {
        // Setup table columns
        setupTableModel(new String[] {"Item ID", "Name", "Category", "Current Stock", "Min. Stock", "Max. Stock", "Status"});
        
        // Fetch all items
        List<Item> items = itemController.getAllItems();
        
        // Populate table
        for (Item item : items) {
            String status = getStockStatus(item);
            
            tableModel.addRow(new Object[] {
                item.getItemID(),
                item.getName(),
                item.getCategory(),
                item.getCurrentStock(),
                item.getMinimumStock(),
                item.getMaximumStock(),
                status
            });
        }
        
        // Update chart
        updateStockLevelChart(items);
        
        // Apply any active filters
        applyFilters();
    }
    
    /**
     * Load the Low Stock Report
     */
    private void loadLowStockReport() {
        // Setup table columns
        setupTableModel(new String[] {"Item ID", "Name", "Category", "Current Stock", "Min. Stock", "Required Qty", "Supplier ID"});
        
        // Fetch all items
        List<Item> items = itemController.getAllItems();
        
        // Filter for low stock items only
        List<Item> lowStockItems = items.stream()
                .filter(item -> item.getCurrentStock() <= item.getMinimumStock())
                .collect(Collectors.toList());
        
        // Populate table
        for (Item item : lowStockItems) {
            int requiredQty = item.getMaximumStock() - item.getCurrentStock();
            
            tableModel.addRow(new Object[] {
                item.getItemID(),
                item.getName(),
                item.getCategory(),
                item.getCurrentStock(),
                item.getMinimumStock(),
                requiredQty,
                item.getSupplierID()
            });
        }
        
        // Update chart
        updateLowStockChart(lowStockItems);
        
        // Apply any active filters
        applyFilters();
    }
    
    /**
     * Load the Stock Valuation Report
     */
    private void loadStockValuationReport() {
        // Setup table columns
        setupTableModel(new String[] {"Item ID", "Name", "Category", "Unit Price (RM)", "Current Stock", "Total Value (RM)"});
        
        // Fetch all items
        List<Item> items = itemController.getAllItems();
        
        // Populate table
        for (Item item : items) {
            double totalValue = item.getUnitPrice() * item.getCurrentStock();
            
            tableModel.addRow(new Object[] {
                item.getItemID(),
                item.getName(),
                item.getCategory(),
                String.format("%.2f", item.getUnitPrice()),
                item.getCurrentStock(),
                String.format("%.2f", totalValue)
            });
        }
        
        // Update chart
        updateValuationChart(items);
        
        // Apply any active filters
        applyFilters();
    }
    
    /**
     * Create and display a chart showing stock levels
     */
    private void updateStockLevelChart(List<Item> items) {
        // Create dataset for chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Limit to top 10 items for readability
        items.stream()
                .sorted((a, b) -> Integer.compare(b.getCurrentStock(), a.getCurrentStock()))
                .limit(10)
                .forEach(item -> dataset.addValue(item.getCurrentStock(), "Current Stock", item.getName()));
                
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 10 Items by Stock Level",
                "Item",
                "Quantity",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Create chart panel
        ChartPanel chartComponent = new ChartPanel(chart);
        chartComponent.setPreferredSize(new Dimension(400, 300));
        
        // Update chart panel
        chartPanel.removeAll();
        chartPanel.add(chartComponent, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    /**
     * Create and display a chart showing low stock items
     */
    private void updateLowStockChart(List<Item> items) {
        // Create dataset for chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add data for each item
        for (Item item : items) {
            dataset.addValue(item.getCurrentStock(), "Current Stock", item.getName());
            dataset.addValue(item.getMinimumStock(), "Minimum Stock", item.getName());
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Low Stock Items",
                "Item",
                "Quantity",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Create chart panel
        ChartPanel chartComponent = new ChartPanel(chart);
        chartComponent.setPreferredSize(new Dimension(400, 300));
        
        // Update chart panel
        chartPanel.removeAll();
        chartPanel.add(chartComponent, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    /**
     * Create and display a chart showing item valuations
     */
    private void updateValuationChart(List<Item> items) {
        // Create dataset for chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add top 10 items by value
        items.stream()
                .sorted((a, b) -> Double.compare(
                    b.getUnitPrice() * b.getCurrentStock(),
                    a.getUnitPrice() * a.getCurrentStock()))
                .limit(10)
                .forEach(item -> dataset.addValue(
                    item.getUnitPrice() * item.getCurrentStock(),
                    "Total Value (RM)",
                    item.getName()));
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 10 Items by Value",
                "Item",
                "Value (RM)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Create chart panel
        ChartPanel chartComponent = new ChartPanel(chart);
        chartComponent.setPreferredSize(new Dimension(400, 300));
        
        // Update chart panel
        chartPanel.removeAll();
        chartPanel.add(chartComponent, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    /**
     * Set up the table model with specified columns
     */
    private void setupTableModel(String[] columns) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        
        for (String column : columns) {
            tableModel.addColumn(column);
        }
    }
    
    /**
     * Apply filters to the table
     */
    private void applyFilters() {
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        
        if (selectedCategory == null || selectedCategory.equals("All Categories")) {
            // No category filter
            reportTable.setRowSorter(new TableRowSorter<>(tableModel));
        } else {
            // Apply category filter
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
            
            // Find category column index
            int categoryColumnIndex = -1;
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                if (tableModel.getColumnName(i).equals("Category")) {
                    categoryColumnIndex = i;
                    break;
                }
            }
            
            if (categoryColumnIndex != -1) {
                final int finalCategoryColumnIndex = categoryColumnIndex;
                sorter.setRowFilter(RowFilter.regexFilter("^" + selectedCategory + "$", finalCategoryColumnIndex));
                reportTable.setRowSorter(sorter);
            }
        }
    }
    
    /**
     * Get stock status based on current, minimum, and maximum stock levels
     */
    private String getStockStatus(Item item) {
        int currentStock = item.getCurrentStock();
        int minStock = item.getMinimumStock();
        int maxStock = item.getMaximumStock();
        
        if (currentStock <= minStock) {
            return "Low Stock";
        } else if (currentStock > maxStock) {
            return "Overstocked";
        } else {
            return "Normal";
        }
    }
    
    /**
     * Print the current report
     */
    private void printReport() {
        try {
            reportTable.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error printing report: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}