package com.owsb.view.supplier;

import com.owsb.controller.SupplierController;
import com.owsb.model.supplier.Supplier;
import com.owsb.model.user.User;
import com.owsb.view.PanelHeaderUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for viewing suppliers (similar to ItemListPanel)
 */
public class SupplierListPanel extends JPanel {
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton viewButton;
    private final SupplierController supplierController;
    private final User currentUser;
    private List<Supplier> suppliers;

    public SupplierListPanel(SupplierController supplierController, User currentUser) {
        this.supplierController = supplierController;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        addListeners();
        loadSuppliers();
    }

    private void initComponents() {
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Table model
        tableModel = new DefaultTableModel(
                new Object[]{"Supplier Code", "Name", "Contact Person", "Phone", "# Items"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        supplierTable = new JTable(tableModel);
        supplierTable.getTableHeader().setReorderingAllowed(false);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {100, 180, 180, 180, 100};
        for (int i = 0; i < widths.length; i++) {
            supplierTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane tableScrollPane = new JScrollPane(supplierTable);
        tableScrollPane.setPreferredSize(new Dimension(700, 350));
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
        JLabel titleLabel = PanelHeaderUtils.createHeaderLabel("Suppliers");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh");
        buttonPanel.add(refreshButton);
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        return panel;
    }

    private void addListeners() {
        refreshButton.addActionListener(e -> loadSuppliers());
        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            viewButton.setEnabled(supplierTable.getSelectedRow() != -1);
        });
        viewButton.addActionListener(e -> viewSupplier());
    }

    /**
     * Loads the list of suppliers into the table.
     * <p>
     * This method clears the current table data, fetches the list of suppliers
     * from the {@link SupplierController}, and populates the table with the
     * supplier details. It also disables the "View" button after loading.
     */
    public void loadSuppliers() {
        tableModel.setRowCount(0);
        suppliers = supplierController.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            int itemCount = supplier.getItemIDs() != null ? supplier.getItemIDs().size() : 0;
            tableModel.addRow(new Object[]{
                    supplier.getSupplierID(),
                    supplier.getName(),
                    supplier.getContactPerson(),
                    supplier.getPhone(),
                    itemCount
            });
        }
        viewButton.setEnabled(false);
    }

    private void viewSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) return;
        String supplierId = (String) tableModel.getValueAt(selectedRow, 0);
        Supplier supplier = suppliers.stream().filter(s -> s.getSupplierID().equals(supplierId)).findFirst().orElse(null);
        if (supplier == null) {
            JOptionPane.showMessageDialog(this, "Supplier not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Show supplier details dialog
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "Supplier Details: " + supplierId, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
        addLabelField(detailsPanel, "Supplier Code:", supplier.getSupplierID());
        addLabelField(detailsPanel, "Name:", supplier.getName());
        addLabelField(detailsPanel, "Contact Person:", supplier.getContactPerson());
        addLabelField(detailsPanel, "Phone:", supplier.getPhone());
        addLabelField(detailsPanel, "Email:", supplier.getEmail() != null ? supplier.getEmail() : "-");
        addLabelField(detailsPanel, "Address:", supplier.getAddress() != null ? supplier.getAddress() : "-");
        addLabelField(detailsPanel, "# Items:", String.valueOf(supplier.getItemIDs() != null ? supplier.getItemIDs().size() : 0));
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
