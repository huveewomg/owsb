package com.owsb.controller;

import com.owsb.model.Item;
import com.owsb.util.FileUtils;
import com.owsb.util.Constants;
import java.lang.reflect.Type;
import java.io.IOException;
import java.util.List;

public class ItemController {

    public Object[][] getItemData() {
        try {
            // Use FileUtils to read the supplier data from the JSON file
            Type itemListType = FileUtils.getListType(Item.class);
            List<Item> items = FileUtils.readListFromJson(Constants.ITEM_FILE, itemListType);

            // Convert the list of suppliers into a 2D array
            Object[][] data = new Object[items.size()][3];
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                data[i][0] = item.getCode();
                data[i][1] = item.getName();
                data[i][2] = item.getStock();
            }

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new Object[0][0];  // Return an empty array if there's an error
        }
    }
}