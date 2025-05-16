package com.owsb.view.user;

import com.owsb.controller.AuthController;
import com.owsb.model.user.User;
import com.owsb.util.UserRole;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for user management operations
 * Implements MVC pattern by separating UI from logic
 * Uses Repository pattern via the AuthController
 */
public class UserManagementPanel extends JPanel {
    // UI Components
    private JTextField idField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JTextField emailField;
    private JComboBox<String> roleCombo;
    private JButton createButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton resetPasswordButton;
    private JButton clearButton;
    
    private JTable userTable;
    private DefaultTableModel tableModel;
    
    // Controller reference
    private final AuthController authController;
    
    // Currently selected user
    private User selectedUser;
    
    /**
     * Constructor
     * @param authController Authentication controller
     */
    public UserManagementPanel(AuthController authController) {
        this.authController = authController;
        
        // Set up panel
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        
        // Add panels to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        
        // Initial data load
        loadUserData();
    }
    
    /**
     * Create form panel with input fields
     * @return Form panel
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("User Details"));
        
        panel.add(new JLabel("User ID:"));
        idField = new JTextField();
        idField.setEditable(false);
        panel.add(idField);
        
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);
        
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);
        
        panel.add(new JLabel("Full Name:"));
        nameField = new JTextField();
        panel.add(nameField);
        
        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);
        
        panel.add(new JLabel("Role:"));
        roleCombo = new JComboBox<>();
        for (UserRole role : UserRole.values()) {
            roleCombo.addItem(role.getDisplayName());
        }
        panel.add(roleCombo);
        
        return panel;
    }
    
    /**
     * Create button panel
     * @return Button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        createButton = new JButton("Create User");
        createButton.addActionListener(e -> createUser());
        
        updateButton = new JButton("Update User");
        updateButton.addActionListener(e -> updateUser());
        updateButton.setEnabled(false);
        
        deleteButton = new JButton("Delete User");
        deleteButton.addActionListener(e -> deleteUser());
        deleteButton.setEnabled(false);
        
        resetPasswordButton = new JButton("Reset Password");
        resetPasswordButton.addActionListener(e -> resetPassword());
        resetPasswordButton.setEnabled(false);
        
        clearButton = new JButton("Clear Form");
        clearButton.addActionListener(e -> clearForm());
        
        panel.add(createButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(resetPasswordButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * Create table panel with user list
     * @return Table panel
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("User List"));
        
        // Create table with non-editable model
        String[] columnNames = {"User ID", "Username", "Name", "Role", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add selection listener
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Get selected user ID
                    String userId = (String) tableModel.getValueAt(selectedRow, 0);
                    selectUser(userId);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadUserData());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Load user data from controller
     */
    private void loadUserData() {
        // Clear table
        tableModel.setRowCount(0);
        
        // Load users from controller
        List<User> users = authController.getAllUsers();
        
        // Add users to table
        for (User user : users) {
            Object[] row = {
                user.getUserId(),
                user.getUsername(),
                user.getName(),
                user.getRole().getDisplayName(),
                user.getEmail()
            };
            tableModel.addRow(row);
        }
        
        // Clear selection
        clearForm();
    }
    
    /**
     * Select a user by ID
     * @param userId User ID to select
     */
    private void selectUser(String userId) {
        // Find user by ID using controller
        User user = authController.getUserById(userId);
        
        if (user != null) {
            // Set selected user
            selectedUser = user;
            
            // Populate form
            idField.setText(user.getUserId());
            usernameField.setText(user.getUsername());
            passwordField.setText(""); // Don't show password
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            
            // Set role in combo box
            for (int i = 0; i < UserRole.values().length; i++) {
                if (UserRole.values()[i] == user.getRole()) {
                    roleCombo.setSelectedIndex(i);
                    break;
                }
            }
            
            // Enable/disable buttons
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
            resetPasswordButton.setEnabled(true);
            createButton.setEnabled(false);
        } else {
            // User not found
            clearForm();
        }
    }
    
    /**
     * Clear the form and selection
     */
    private void clearForm() {
        // Clear form fields
        idField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        nameField.setText("");
        emailField.setText("");
        roleCombo.setSelectedIndex(0);
        
        // Reset selection
        selectedUser = null;
        userTable.clearSelection();
        
        // Enable/disable buttons
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        resetPasswordButton.setEnabled(false);
        createButton.setEnabled(true);
    }
    
    /**
     * Create a new user
     */
    private void createUser() {
        // Validate form
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username, password, and name are required",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get selected role
        UserRole role = UserRole.values()[roleCombo.getSelectedIndex()];
        
        // Call controller to create user
        boolean success = authController.registerUser(username, password, name, email, role);
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "User created successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh table
            loadUserData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to create user. Username may already exist.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Update selected user
     */
    private void updateUser() {
        if (selectedUser == null) {
            return;
        }
        
        // Validate form
        String username = usernameField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (username.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username and name are required",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get selected role
        UserRole role = UserRole.values()[roleCombo.getSelectedIndex()];
        
        // Call controller to update user
        boolean success = authController.updateUser(
            selectedUser.getUserId(),
            username,
            name,
            email,
            role
        );
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "User updated successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh table
            loadUserData();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update user. Username may already exist.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Delete selected user
     */
    private void deleteUser() {
        if (selectedUser == null) {
            return;
        }
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this user?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Call controller to delete user
        boolean success = authController.deleteUser(selectedUser.getUserId());
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "User deleted successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh table
            loadUserData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to delete user",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Reset password for selected user
     */
    private void resetPassword() {
        if (selectedUser == null) {
            return;
        }
        
        // Get new password
        String password = new String(passwordField.getPassword());
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a new password",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Call controller to update password
        boolean success = authController.updatePassword(
            selectedUser.getUserId(),
            password
        );
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Password updated successfully",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Clear password field
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update password",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}