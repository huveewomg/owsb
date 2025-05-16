package com.owsb.view.item;

import com.owsb.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ItemCategoryManagementPanel extends JPanel {
    private static final String CATEGORY_FILE = "data/item_categories.txt";
    private DefaultListModel<String> categoryListModel;
    private JList<String> categoryList;
    private JTextField categoryField;

    public ItemCategoryManagementPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Manage Item Categories"));

        categoryListModel = new DefaultListModel<>();
        categoryList = new JList<>(categoryListModel);
        JScrollPane scrollPane = new JScrollPane(categoryList);

        loadCategories();

        JPanel inputPanel = new JPanel(new BorderLayout());
        categoryField = new JTextField();
        JButton addButton = new JButton("Add Category");
        addButton.addActionListener(this::addCategory);
        JButton deleteButton = new JButton("Delete Category");
        deleteButton.addActionListener(this::deleteCategory);
        inputPanel.add(categoryField, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        categoryListModel.clear();
        try {
            Type type = FileUtils.getListType(String.class);
            List<String> categories = FileUtils.readListFromJson(CATEGORY_FILE, type);
            for (String cat : categories) {
                categoryListModel.addElement(cat);
            }
        } catch (IOException e) {
            // Ignore, file may not exist yet
        }
    }

    private void addCategory(ActionEvent e) {
        String newCategory = categoryField.getText().trim();
        if (newCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name cannot be empty.");
            return;
        }
        if (categoryListModel.contains(newCategory)) {
            JOptionPane.showMessageDialog(this, "Category already exists.");
            return;
        }
        categoryListModel.addElement(newCategory);
        saveCategories();
        categoryField.setText("");
        JOptionPane.showMessageDialog(this, "Category added.");
        if (categoryChangeListener != null) categoryChangeListener.onCategoryChanged();
    }

    private void deleteCategory(ActionEvent e) {
        String selected = categoryList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete category '" + selected + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            categoryListModel.removeElement(selected);
            saveCategories();
            if (categoryChangeListener != null) categoryChangeListener.onCategoryChanged();
        }
    }

    private void saveCategories() {
        try {
            List<String> categories = java.util.Collections.list(categoryListModel.elements());
            FileUtils.writeListToJson(CATEGORY_FILE, categories);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save categories.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static List<String> getCategories() {
        try {
            Type type = FileUtils.getListType(String.class);
            return FileUtils.readListFromJson(CATEGORY_FILE, type);
        } catch (IOException e) {
            return java.util.Arrays.asList("Groceries", "Fresh Produce", "Essentials"); // fallback
        }
    }

    public interface CategoryChangeListener {
        void onCategoryChanged();
    }

    private CategoryChangeListener categoryChangeListener;

    public void setCategoryChangeListener(CategoryChangeListener listener) {
        this.categoryChangeListener = listener;
    }
}
