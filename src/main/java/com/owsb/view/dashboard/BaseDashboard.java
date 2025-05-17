package com.owsb.view.dashboard;

import com.owsb.controller.AuthController;
import com.owsb.model.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base dashboard class that all role-specific dashboards extend
 * Provides common dashboard functionality
 */
public abstract class BaseDashboard extends JFrame {
    
    // Common components
    protected JPanel mainPanel;
    protected JPanel menuPanel;
    protected JPanel contentPanel;
    protected JLabel statusBar;
    
    // Controller and user
    protected final AuthController authController;
    protected final User currentUser;
    
    /**
     * Constructor for BaseDashboard
     * @param title Dashboard title
     * @param user Current logged in user
     * @param authController Authentication controller
     */
    public BaseDashboard(String title, User user, AuthController authController) {
        this.currentUser = user;
        this.authController = authController;
        
        // Set up window properties
        setTitle(title);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        
        // Initialize common components
        initComponents();
        
        // Initialize role-specific components
        initRoleComponents();
    }
    
    /**
     * Initialize common dashboard components
     */
    private void initComponents() {
        // Create main panel with BorderLayout
        mainPanel = new JPanel(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        
        // Create menu panel
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(50, 50, 50));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        menuPanel.setPreferredSize(new Dimension(200, 600));
        
        // Create content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create welcome panel in content
        JPanel welcomePanel = createWelcomePanel();
        contentPanel.add(welcomePanel, BorderLayout.CENTER);
        
        // Create status bar
        statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Combine panels
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        // Add logout button to menu
        addMenuButton("Logout", e -> logout());
        
        // Add to frame
        add(mainPanel);
    }
    
    /**
     * Create header panel with title and user info
     * @return Header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create title
        JLabel titleLabel = new JLabel("OWSB System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        // Create user info
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel(currentUser.getName() + " (" + currentUser.getRole().getDisplayName() + ")");
        userLabel.setForeground(Color.WHITE);
        
        // Format current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
        JLabel dateLabel = new JLabel(dateFormat.format(new Date()));
        dateLabel.setForeground(Color.WHITE);
        
        userInfoPanel.add(dateLabel);
        userInfoPanel.add(Box.createHorizontalStrut(20));
        userInfoPanel.add(userLabel);
        
        // Add to header panel
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Create welcome panel shown when dashboard first opens
     * @return Welcome panel
     */
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to the OWSB System!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        // Role-specific greeting
        JLabel roleLabel = new JLabel(getRoleSpecificGreeting());
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        
        // Instructions
        JTextArea instructionsArea = new JTextArea();
        instructionsArea.setText("Use the menu on the left to navigate through the system.");
        instructionsArea.setEditable(false);
        instructionsArea.setBackground(welcomePanel.getBackground());
        instructionsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        
        // Add to panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(welcomeLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(roleLabel);
        textPanel.add(Box.createVerticalStrut(20));
        textPanel.add(instructionsArea);
        
        welcomePanel.add(textPanel, BorderLayout.NORTH);
        
        return welcomePanel;
    }
    
    /**
     * Get role-specific greeting based on user type
     * @return Greeting message
     */
    protected String getRoleSpecificGreeting() {
        return "You are logged in as " + currentUser.getRole().getDisplayName() + ".";
    }
    
    /**
     * Add button to menu panel
     * @param label Button label
     * @param listener Action listener
     */
    protected void addMenuButton(String label, ActionListener listener) {
        JButton button = new JButton(label);
        button.setMaximumSize(new Dimension(180, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(listener);
        
        // Add some spacing
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(button);
    }
    
    /**
     * Set the content panel
     * @param panel Panel to set as content
     */
    protected void setContent(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Update status bar text
     * @param message Message to display
     */
    protected void setStatus(String message) {
        statusBar.setText(message);
    }
    
    /**
     * Logout and close dashboard
     */
    protected void logout() {
        authController.logout();
        dispose();
    }
    
    /**
     * Abstract method to initialize role-specific components
     * Each dashboard implementation must provide this
     */
    protected abstract void initRoleComponents();
    
    /**
     * Navigate to the Purchase Requisition panel
     * This is a placeholder that should be overridden by dashboards that have a PR panel
     */
    public void navigateToPRPanel() {
        // Default implementation does nothing
        // This should be overridden by dashboards that have a PR panel
        JOptionPane.showMessageDialog(this, 
            "Navigation to Purchase Requisition panel is not implemented for this dashboard.",
            "Navigation Error",
            JOptionPane.WARNING_MESSAGE);
    }
}