package com.owsb.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for file operations with JSON data
 * Encapsulates common file I/O operations
 */
public class FileUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Private constructor to prevent instantiation
     */
    private FileUtils() {
        // Utility class should not be instantiated
    }
    
    /**
     * Read a JSON file and convert to a list of objects
     * @param fileName Name of the file to read
     * @param type Type of objects in the list
     * @return List of objects from file
     * @throws IOException If there's an error reading the file
     */
    public static <T> List<T> readListFromJson(String fileName, Type type) throws IOException {
        try (Reader reader = new FileReader(fileName)) {
            List<T> list = gson.fromJson(reader, type);
            
            // Handle empty file or null result
            if (list == null) {
                return new ArrayList<>();
            }
            
            return list;
        } catch (FileNotFoundException e) {
            // If file doesn't exist, return empty list
            return new ArrayList<>();
        }
    }
    
    /**
     * Write a list of objects to a JSON file
     * @param fileName Name of the file to write
     * @param list List of objects to write
     * @throws IOException If there's an error writing the file
     */
    public static <T> void writeListToJson(String fileName, List<T> list) throws IOException {
        // Create directory if it doesn't exist
        File file = new File(fileName);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(list, writer);
        }
    }
    
    /**
     * Read a single object from a JSON file
     * @param fileName Name of the file to read
     * @param classOfT Class of the object
     * @return Object from file or null if file doesn't exist
     * @throws IOException If there's an error reading the file
     */
    public static <T> T readObjectFromJson(String fileName, Class<T> classOfT) throws IOException {
        try (Reader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, classOfT);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Write a single object to a JSON file
     * @param fileName Name of the file to write
     * @param object Object to write
     * @throws IOException If there's an error writing the file
     */
    public static <T> void writeObjectToJson(String fileName, T object) throws IOException {
        // Create directory if it doesn't exist
        File file = new File(fileName);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(object, writer);
        }
    }
    
    /**
     * Check if a file exists
     * @param fileName Name of the file to check
     * @return True if file exists, false otherwise
     */
    public static boolean fileExists(String fileName) {
        return new File(fileName).exists();
    }
    
    /**
     * Create a backup of a file
     * @param fileName Name of the file to backup
     * @throws IOException If there's an error creating the backup
     */
    public static void createBackup(String fileName) throws IOException {
        if (!fileExists(fileName)) {
            return;
        }
        
        String backupFileName = fileName + "." + System.currentTimeMillis() + ".bak";
        
        try (InputStream in = new FileInputStream(fileName);
             OutputStream out = new FileOutputStream(backupFileName)) {
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
    
    /**
     * Create a TypeToken for a list of a specific class
     * @param clazz Class of objects in the list
     * @return TypeToken for the list
     */
    public static <T> Type getListType(Class<T> clazz) {
        return TypeToken.getParameterized(ArrayList.class, clazz).getType();
    }
}