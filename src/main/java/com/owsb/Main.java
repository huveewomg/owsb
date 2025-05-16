package com.owsb;

import com.owsb.controller.AuthController;
import com.owsb.model.user.User;
import com.owsb.view.dashboard.AdminDashboard;
import com.owsb.view.dashboard.FinanceManagerDashboard;
import com.owsb.view.dashboard.InventoryManagerDashboard;
import com.owsb.view.dashboard.PurchaseManagerDashboard;
import com.owsb.view.dashboard.SalesManagerDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main entry point for the OWSB System
 * This class creates the login window and handles authentication
 */
public class Main extends JFrame {
    
    // UI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private JLabel statusLabel;
    
    // Controller
    private final AuthController authController;
    
    /**
     * Constructor - initializes the login window
     */
    public Main() {
        // Initialize controller
        authController = new AuthController();
        
        // Set up window properties
        setTitle("OWSB System - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setResizable(false);
        
        // Initialize UI components
        initComponents();
        
        // Add window listener to handle close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    
    /**
     * Initialize and arrange UI components
     */
    private void initComponents() {
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create logo/title panel at top
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Omega Wholesale Sdn Bhd");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        
        // Create form panel in center
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        
        // Status label for error messages
        statusLabel = new JLabel("");
        statusLabel.setForeground(Color.RED);
        
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(new JLabel("")); // Empty label for spacing
        formPanel.add(statusLabel);
        
        // Create button panel at bottom
        JPanel buttonPanel = new JPanel();
        loginButton = new JButton("Login");
        exitButton = new JButton("Exit");
        
        // Set preferred button size
        Dimension buttonSize = new Dimension(100, 30);
        loginButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);
        
        // Add action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        // Add buttons to panel
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0))); // Add space between buttons
        buttonPanel.add(exitButton);
        
        // Add all panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Set default button (press Enter to login)
        getRootPane().setDefaultButton(loginButton);
    }
    
    /**
     * Handle login button action
     */
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required");
            return;
        }
        
        // Attempt login
        boolean success = authController.login(username, password);
        
        if (success) {
            // Get current user
            User currentUser = authController.getCurrentUser();
            
            // Hide login window
            setVisible(false);
            
            // Open appropriate dashboard based on user role
            openDashboard(currentUser);
        } else {
            statusLabel.setText("Invalid username or password");
            passwordField.setText("");
        }
    }
    
    /**
     * Open the appropriate dashboard based on user role
     */
    private void openDashboard(User user) {
        // Using polymorphism to determine user type and open appropriate dashboard
        JFrame dashboard = null;
        
        switch (user.getRole()) {
            case ADMIN:
                dashboard = new AdminDashboard(user, authController);
                break;
            case SALES_MANAGER:
                dashboard = new SalesManagerDashboard(user, authController);
                break;
            case PURCHASE_MANAGER:
                dashboard = new PurchaseManagerDashboard(user, authController);
                break;
            case INVENTORY_MANAGER:
                dashboard = new InventoryManagerDashboard(user, authController);
                break;
            case FINANCE_MANAGER:
                dashboard = new FinanceManagerDashboard(user, authController);
                break;
            default:
                JOptionPane.showMessageDialog(
                    this,
                    "Unknown user role",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                ); 
                setVisible(true); // Show login window again
                return;
        }
        
        // Show dashboard
        if (dashboard != null) {
            dashboard.setVisible(true);
            
            // When dashboard closes, show login window again
            dashboard.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // Clear fields
                    usernameField.setText("");
                    passwordField.setText("");
                    statusLabel.setText("");
                    
                    // Log out user
                    authController.logout();
                    
                    // Show login window again
                    setVisible(true);
                }
            });
        }
    }
    
    /**
     * Main method - application entry point
     */
    public static void main(String[] args) {
        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show login window
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
}
