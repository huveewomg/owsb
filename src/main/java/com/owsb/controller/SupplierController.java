package com.owsb.controller;

import com.owsb.model.Supplier;
import com.owsb.util.FileUtils;
import com.owsb.util.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class SupplierController {

    public Object[][] getSupplierData() {
        try {
            Type supplierListType = FileUtils.getListType(Supplier.class);
            List<Supplier> suppliers = FileUtils.readListFromJson(Constants.SUPPLIER_FILE, supplierListType);

            // Include: supplierID, name, contactPerson, phoneNumber
            Object[][] data = new Object[suppliers.size()][4];

            for (int i = 0; i < suppliers.size(); i++) {
                Supplier supplier = suppliers.get(i);
                data[i][0] = supplier.getSupplierID();
                data[i][1] = supplier.getName();
                data[i][2] = supplier.getContactPerson();
                data[i][3] = supplier.getPhoneNumber();
            }

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }
}
