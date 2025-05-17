package com.owsb.view.item;

import com.owsb.controller.ItemController;
import com.owsb.model.inventory.Item;
import com.owsb.model.user.User;
import com.owsb.util.SupplierUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

/**
 * Panel for viewing items (similar to PurchaseOrderListPanel)
 */
public class ItemListPanel extends JPanel {
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton viewButton;
    private final ItemController itemController;
    private final User currentUser;
    private List<Item> items;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    public ItemListPanel(ItemController itemController, User currentUser) {
        this.itemController = itemController;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        addListeners();
        loadItems();
    }

    private void initComponents() {
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table model
        tableModel = new DefaultTableModel(
                new Object[]{"Item Code", "Name", "Description", "Price", "Category", "Supplier", "Stock", "Min", "Max", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemTable = new JTable(tableModel);
        itemTable.getTableHeader().setReorderingAllowed(false);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Set column widths
        int[] widths = {80, 120, 180, 80, 100, 120, 60, 50, 50, 80};
        for (int i = 0; i < widths.length; i++) {
            itemTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // Price column right align
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        itemTable.getColumnModel().getColumn(3).setCellRenderer(rightAlign);
        itemTable.getColumnModel().getColumn(6).setCellRenderer(rightAlign);
        itemTable.getColumnModel().getColumn(7).setCellRenderer(rightAlign);
        itemTable.getColumnModel().getColumn(8).setCellRenderer(rightAlign);
        JScrollPane tableScrollPane = new JScrollPane(itemTable);
        tableScrollPane.setPreferredSize(new Dimension(900, 350));
        add(tableScrollPane, BorderLayout.CENTER);

        // Bottom panel with view button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewButton = new JButton("View");
        viewButton.setEnabled(false);
        bottomPanel.add(viewButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Create header panel with title and refresh button
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JLabel titleLabel = new JLabel("Items");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        buttonPanel.add(refreshButton);
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private void addListeners() {
        refreshButton.addActionListener(e -> loadItems());
        itemTable.getSelectionModel().addListSelectionListener(e -> {
            viewButton.setEnabled(itemTable.getSelectedRow() != -1);
        });
        viewButton.addActionListener(e -> viewItem());
    }

    public void loadItems() {
        tableModel.setRowCount(0);
        items = itemController.getAllItems();
        Map<String, String> supplierMap = SupplierUtils.getSupplierIdToNameMap();
        for (Item item : items) {
            String supplierName = supplierMap.getOrDefault(item.getSupplierID(), "Unknown");
            tableModel.addRow(new Object[]{
                    item.getItemID(),
                    item.getName(),
                    item.getDescription(),
                    currencyFormat.format(item.getUnitPrice()),
                    item.getCategory(),
                    supplierName,
                    item.getCurrentStock(),
                    item.getMinimumStock(),
                    item.getMaximumStock(),
                    item.getStockStatus()
            });
        }
        viewButton.setEnabled(false);
    }

    private void viewItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) return;
        String itemId = (String) tableModel.getValueAt(selectedRow, 0);
        Item item = items.stream().filter(i -> i.getItemID().equals(itemId)).findFirst().orElse(null);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Show item details dialog
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "Item Details: " + itemId, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Item Details"));
        addLabelField(detailsPanel, "Item Code:", item.getItemID());
        addLabelField(detailsPanel, "Name:", item.getName());
        addLabelField(detailsPanel, "Description:", item.getDescription());
        addLabelField(detailsPanel, "Category:", item.getCategory());
        addLabelField(detailsPanel, "Supplier:", SupplierUtils.getSupplierIdToNameMap().getOrDefault(item.getSupplierID(), "Unknown"));
        addLabelField(detailsPanel, "Unit Price:", currencyFormat.format(item.getUnitPrice()));
        addLabelField(detailsPanel, "Current Stock:", String.valueOf(item.getCurrentStock()));
        addLabelField(detailsPanel, "Minimum Stock:", String.valueOf(item.getMinimumStock()));
        addLabelField(detailsPanel, "Maximum Stock:", String.valueOf(item.getMaximumStock()));
        addLabelField(detailsPanel, "Date Added:", item.getDateAdded());
        addLabelField(detailsPanel, "Last Updated:", item.getLastUpdated());
        addLabelField(detailsPanel, "Stock Status:", item.getStockStatus());
        dialog.add(detailsPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addLabelField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
        JLabel valueComponent = new JLabel(value);
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
}
