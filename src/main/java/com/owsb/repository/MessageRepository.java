package com.owsb.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.owsb.model.message.Message;
import com.owsb.util.Constants;
import com.owsb.util.UserRole;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for managing system messages
 */
public class MessageRepository {
    private final Gson gson;
    private final String filePath;
    
    /**
     * Constructor
     */
    public MessageRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.filePath = Constants.MESSAGES_FILE;
        
        // Create empty file if it doesn't exist
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                file.createNewFile();
                // Write empty array to file
                FileWriter writer = new FileWriter(file);
                writer.write("[]");
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error creating messages file: " + e.getMessage());
        }
    }
    
    /**
     * Find all messages
     * @return List of all messages
     */
    public List<Message> findAll() {
        try {
            return readMessagesFromFile();
        } catch (IOException e) {
            System.err.println("Error reading messages: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Find messages by receiver role
     * @param role Receiver role
     * @return List of messages for that role
     */
    public List<Message> findByReceiverRole(UserRole role) {
        try {
            List<Message> messages = readMessagesFromFile();
            return messages.stream()
                    .filter(m -> m.getReceiverRole().equals(role.name()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error finding messages by role: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Find unread messages by receiver role
     * @param role Receiver role
     * @return List of unread messages for that role
     */
    public List<Message> findUnreadByReceiverRole(UserRole role) {
        try {
            List<Message> messages = readMessagesFromFile();
            return messages.stream()
                    .filter(m -> m.getReceiverRole().equals(role.name()) && !m.isRead())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error finding unread messages: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Save a new message
     * @param message Message to save
     * @return true if saved successfully
     */
    public boolean save(Message message) {
        try {
            List<Message> messages = readMessagesFromFile();
            
            // Check for duplicates
            boolean exists = messages.stream()
                    .anyMatch(m -> m.getMessageID().equals(message.getMessageID()));
            
            if (exists) {
                return false;
            }
            
            messages.add(message);
            writeMessagesToFile(messages);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update a message
     * @param message Message to update
     * @return true if updated successfully
     */
    public boolean update(Message message) {
        try {
            List<Message> messages = readMessagesFromFile();
            boolean updated = false;
            
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getMessageID().equals(message.getMessageID())) {
                    messages.set(i, message);
                    updated = true;
                    break;
                }
            }
            
            if (updated) {
                writeMessagesToFile(messages);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error updating message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark a message as read
     * @param messageID Message ID
     * @return true if marked successfully
     */
    public boolean markAsRead(String messageID) {
        try {
            List<Message> messages = readMessagesFromFile();
            boolean updated = false;
            
            for (Message message : messages) {
                if (message.getMessageID().equals(messageID)) {
                    message.setRead(true);
                    updated = true;
                    break;
                }
            }
            
            if (updated) {
                writeMessagesToFile(messages);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error marking message as read: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a message
     * @param messageID Message ID
     * @return true if deleted successfully
     */
    public boolean delete(String messageID) {
        try {
            List<Message> messages = readMessagesFromFile();
            boolean removed = false;
            
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getMessageID().equals(messageID)) {
                    messages.remove(i);
                    removed = true;
                    break;
                }
            }
            
            if (removed) {
                writeMessagesToFile(messages);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a new message ID
     * @return New message ID
     */
    public String generateMessageId() {
        try {
            List<Message> messages = readMessagesFromFile();
            
            // Find the highest existing ID
            int highestId = 0;
            for (Message message : messages) {
                String id = message.getMessageID();
                if (id.startsWith("MSG")) {
                    try {
                        int num = Integer.parseInt(id.substring(3));
                        if (num > highestId) {
                            highestId = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            
            // Generate next ID
            return String.format("MSG%03d", highestId + 1);
        } catch (IOException e) {
            System.err.println("Error generating message ID: " + e.getMessage());
            return "MSG001"; // Default if error
        }
    }
    
    /**
     * Read messages from file
     * @return List of messages
     * @throws IOException If file cannot be read
     */
    private List<Message> readMessagesFromFile() throws IOException {
        File file = new File(filePath);
        
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        FileReader reader = new FileReader(file);
        Type listType = new TypeToken<ArrayList<Message>>(){}.getType();
        List<Message> messages = gson.fromJson(reader, listType);
        reader.close();
        
        // Handle null case when file is empty or invalid
        if (messages == null) {
            return new ArrayList<>();
        }
        
        return messages;
    }
    
    /**
     * Write messages to file
     * @param messages List of messages
     * @throws IOException If file cannot be written
     */
    private void writeMessagesToFile(List<Message> messages) throws IOException {
        File file = new File(filePath);
        
        // Create directory if it doesn't exist
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        FileWriter writer = new FileWriter(file);
        gson.toJson(messages, writer);
        writer.flush();
        writer.close();
    }

        /**
     * Find messages by sender ID
     * @param senderID Sender ID
     * @return List of messages from that sender
     */
    public List<Message> findBySenderId(String senderID) {
        try {
            List<Message> messages = readMessagesFromFile();
            return messages.stream()
                    .filter(m -> m.getSenderID().equals(senderID))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error finding messages by sender: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}