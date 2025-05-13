package com.owsb.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.owsb.dto.SupplierDTO;
import com.owsb.model.Supplier;
import com.owsb.util.Constants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SupplierRepository implements Repository<Supplier> {
    private final Gson gson;
    private final String filePath;

    public SupplierRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.filePath = Constants.SUPPLIER_FILE;
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        try {
            List<SupplierDTO> dtos = readSuppliersFromFile();
            for (SupplierDTO dto : dtos) {
                suppliers.add(convertToSupplier(dto));
            }
        } catch (IOException e) {
            System.err.println("Error reading suppliers: " + e.getMessage());
        }
        return suppliers;
    }

    @Override
    public Supplier findById(String id) {
        try {
            List<SupplierDTO> dtos = readSuppliersFromFile();
            for (SupplierDTO dto : dtos) {
                if (dto.supplierID.equals(id)) {
                    return convertToSupplier(dto);
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding supplier: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean save(Supplier supplier) {
        try {
            List<SupplierDTO> dtos = readSuppliersFromFile();
            for (SupplierDTO dto : dtos) {
                if (dto.supplierID.equals(supplier.getSupplierID())) {
                    return false;
                }
            }
            dtos.add(convertToDTO(supplier));
            writeSuppliersToFile(dtos);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving supplier: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Supplier supplier) {
        try {
            List<SupplierDTO> dtos = readSuppliersFromFile();
            boolean updated = false;
            for (int i = 0; i < dtos.size(); i++) {
                if (dtos.get(i).supplierID.equals(supplier.getSupplierID())) {
                    dtos.set(i, convertToDTO(supplier));
                    updated = true;
                    break;
                }
            }
            if (updated) {
                writeSuppliersToFile(dtos);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error updating supplier: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        try {
            List<SupplierDTO> dtos = readSuppliersFromFile();
            boolean removed = false;
            for (int i = 0; i < dtos.size(); i++) {
                if (dtos.get(i).supplierID.equals(id)) {
                    dtos.remove(i);
                    removed = true;
                    break;
                }
            }
            if (removed) {
                writeSuppliersToFile(dtos);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error deleting supplier: " + e.getMessage());
            return false;
        }
    }

    public String generateSupplierId() {
        List<Supplier> suppliers = findAll();
        int highestId = 0;
        for (Supplier supplier : suppliers) {
            String id = supplier.getSupplierID();
            if (id.startsWith("SUP")) {
                try {
                    int num = Integer.parseInt(id.substring(3));
                    if (num > highestId) {
                        highestId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("SUP%03d", highestId + 1);
    }

    private List<SupplierDTO> readSuppliersFromFile() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        FileReader reader = new FileReader(file);
        Type listType = new TypeToken<ArrayList<SupplierDTO>>(){}.getType();
        List<SupplierDTO> dtos = gson.fromJson(reader, listType);
        reader.close();
        if (dtos == null) {
            return new ArrayList<>();
        }
        return dtos;
    }

    private void writeSuppliersToFile(List<SupplierDTO> dtos) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        FileWriter writer = new FileWriter(file);
        gson.toJson(dtos, writer);
        writer.flush();
        writer.close();
    }

    private Supplier convertToSupplier(SupplierDTO dto) {
        Supplier supplier = new Supplier(dto.supplierID, dto.name, dto.contactPerson, dto.phone);
        supplier.setEmail(dto.email);
        supplier.setAddress(dto.address);
        supplier.setItemIDs(dto.itemIDs);
        return supplier;
    }

    private SupplierDTO convertToDTO(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.supplierID = supplier.getSupplierID();
        dto.name = supplier.getName();
        dto.contactPerson = supplier.getContactPerson();
        dto.phone = supplier.getPhone();
        dto.email = supplier.getEmail();
        dto.address = supplier.getAddress();
        dto.itemIDs = supplier.getItemIDs();
        return dto;
    }
}
