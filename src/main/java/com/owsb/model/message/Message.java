package com.owsb.model.message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message class for internal system communication
 */
public class Message {
    private String messageID;
    private String senderID;
    private String senderName;
    private String receiverRole;
    private String subject;
    private String content;
    private String timestamp;
    private boolean isRead;
    private String relatedItemID; // Optional - related item if relevant
    
    // Formatter for timestamps
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructor for creating a new message
     */
    public Message(String messageID, String senderID, String senderName, String receiverRole, 
                  String subject, String content) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.senderName = senderName;
        this.receiverRole = receiverRole;
        this.subject = subject;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(formatter);
        this.isRead = false;
        this.relatedItemID = null;
    }
    
    /**
     * Full constructor including relatedItemID
     */
    public Message(String messageID, String senderID, String senderName, String receiverRole, 
                  String subject, String content, String relatedItemID) {
        this(messageID, senderID, senderName, receiverRole, subject, content);
        this.relatedItemID = relatedItemID;
    }
    
    // Getters and setters
    public String getMessageID() {
        return messageID;
    }
    
    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }
    
    public String getSenderID() {
        return senderID;
    }
    
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getReceiverRole() {
        return receiverRole;
    }
    
    public void setReceiverRole(String receiverRole) {
        this.receiverRole = receiverRole;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public String getRelatedItemID() {
        return relatedItemID;
    }
    
    public void setRelatedItemID(String relatedItemID) {
        this.relatedItemID = relatedItemID;
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "subject='" + subject + '\'' +
                ", sender='" + senderName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", read=" + isRead +
                '}';
    }
}