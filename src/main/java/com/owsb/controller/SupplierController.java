package com.owsb.controller;

import com.owsb.model.supplier.Supplier;
import com.owsb.repository.SupplierRepository;
import java.util.List;

public class SupplierController {
    private final SupplierRepository supplierRepository;

    public SupplierController() {
        this.supplierRepository = new SupplierRepository();
    }

    public String generateNextSupplierId() {
        return supplierRepository.generateSupplierId();
    }

    public boolean addSupplier(String name, String contactPerson, String phone) {
        if (name == null || name.trim().isEmpty() ||
            contactPerson == null || contactPerson.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String supplierId = supplierRepository.generateSupplierId();
        Supplier supplier = new Supplier(supplierId, name, contactPerson, phone);
        return supplierRepository.save(supplier);
    }

    public boolean updateSupplier(String supplierId, String name, String contactPerson, String phone) {
        if (supplierId == null || supplierId.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            contactPerson == null || contactPerson.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            return false;
        }
        Supplier supplier = supplierRepository.findById(supplierId);
        if (supplier == null) {
            return false;
        }
        supplier.setName(name);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        return supplierRepository.update(supplier);
    }

    public boolean deleteSupplier(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return false;
        }
        return supplierRepository.delete(supplierId);
    }

    public Supplier getSupplierById(String supplierId) {
        if (supplierId == null || supplierId.trim().isEmpty()) {
            return null;
        }
        return supplierRepository.findById(supplierId);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }
}
