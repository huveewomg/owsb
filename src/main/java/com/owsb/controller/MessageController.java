package com.owsb.controller;

import com.owsb.model.message.Message;
import com.owsb.model.user.User;
import com.owsb.repository.MessageRepository;
import com.owsb.util.UserRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for message-related operations
 */
public class MessageController {
    private final MessageRepository messageRepository;
    private User currentUser;
    
    /**
     * Constructor
     */
    public MessageController() {
        this.messageRepository = new MessageRepository();
    }
    
    /**
     * Set current user
     * @param user Current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Send a message to a role
     * @param receiverRole Role to receive the message
     * @param subject Message subject
     * @param content Message content
     * @param relatedItemID Optional related item ID
     * @return true if sent successfully
     */
    public boolean sendMessage(UserRole receiverRole, String subject, String content, String relatedItemID) {
        // Validate user is logged in
        if (currentUser == null) {
            return false;
        }
        
        // Create message
        String messageID = messageRepository.generateMessageId();
        Message message;
        if (relatedItemID != null && !relatedItemID.isEmpty()) {
            message = new Message(
                messageID,
                currentUser.getUserId(),
                currentUser.getName(),
                receiverRole.name(),
                subject,
                content,
                relatedItemID
            );
        } else {
            message = new Message(
                messageID,
                currentUser.getUserId(),
                currentUser.getName(),
                receiverRole.name(),
                subject,
                content
            );
        }
        
        // Save message
        return messageRepository.save(message);
    }
    
    /**
     * Get all messages for the current user's role or sent by the current user
     * @return List of messages
     */
    public List<Message> getMessagesForCurrentUser() {
        if (currentUser == null) {
            return List.of();
        }
        
        List<Message> roleMessages = messageRepository.findByReceiverRole(currentUser.getRole());
        List<Message> sentMessages = messageRepository.findBySenderId(currentUser.getUserId());
        
        // Combine and sort by timestamp (most recent first)
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(roleMessages);
        allMessages.addAll(sentMessages);
        
        // Remove duplicates (messages that might be both sent and received by the same role)
        List<Message> uniqueMessages = new ArrayList<>();
        for (Message message : allMessages) {
            boolean isDuplicate = false;
            for (Message uniqueMessage : uniqueMessages) {
                if (message.getMessageID().equals(uniqueMessage.getMessageID())) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                uniqueMessages.add(message);
            }
        }
        
        // Sort by timestamp (most recent first)
        uniqueMessages.sort((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));
        
        return uniqueMessages;
    }
    
    /**
     * Get unread messages for the current user's role
     * @return List of unread messages
     */
    public List<Message> getUnreadMessagesForCurrentUser() {
        if (currentUser == null) {
            return List.of();
        }
        
        return messageRepository.findUnreadByReceiverRole(currentUser.getRole());
    }
    
    /**
     * Mark a message as read
     * @param messageID Message ID
     * @return true if marked successfully
     */
    public boolean markMessageAsRead(String messageID) {
        return messageRepository.markAsRead(messageID);
    }
    
    /**
     * Mark all messages as read for current user's role
     * @return true if all marked successfully
     */
    public boolean markAllMessagesAsRead() {
        List<Message> unreadMessages = getUnreadMessagesForCurrentUser();
        boolean success = true;
        for (Message message : unreadMessages) {
            success = messageRepository.markAsRead(message.getMessageID()) && success;
        }
        return success;
    }
    
    /**
     * Delete a message
     * @param messageID Message ID
     * @return true if deleted successfully
     */
    public boolean deleteMessage(String messageID) {
        return messageRepository.delete(messageID);
    }
    
    /**
     * Get count of unread messages for current user
     * @return Number of unread messages
     */
    public int getUnreadMessageCount() {
        return getUnreadMessagesForCurrentUser().size();
    }
}