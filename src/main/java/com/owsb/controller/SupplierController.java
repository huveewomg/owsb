package com.owsb.controller;

import com.owsb.model.Supplier;
import com.owsb.util.Constants;
import com.owsb.util.FileUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class SupplierController {

    public Object[][] getSupplierData() {
        try {
            // Use FileUtils to read the supplier data from the JSON file
            Type supplierListType = FileUtils.getListType(Supplier.class);
            List<Supplier> suppliers = FileUtils.readListFromJson(Constants.SUPPLIER_FILE, supplierListType);

            // Convert the list of suppliers into a 2D array
            Object[][] data = new Object[suppliers.size()][3];
            for (int i = 0; i < suppliers.size(); i++) {
                Supplier supplier = suppliers.get(i);
                data[i][0] = supplier.getCode();
                data[i][1] = supplier.getName();
                data[i][2] = supplier.getContact();
            }

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new Object[0][0];  // Return an empty array if there's an error
        }
    }
}
