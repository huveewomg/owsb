package com.owsb.view;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for creating standardized panel headers.
 */
public class PanelHeaderUtils {
    /**
     * Creates a standardized header JLabel for panels.
     * @param title The header text
     * @return JLabel with consistent font, alignment, and border
     */
    public static JLabel createHeaderLabel(String title) {
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return label;
    }

    /**
     * Creates a JPanel containing the standardized header label (for flexible layout).
     * @param title The header text
     * @return JPanel with the header label centered
     */
    public static JPanel createHeaderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createHeaderLabel(title), BorderLayout.CENTER);
        return panel;
    }
}
