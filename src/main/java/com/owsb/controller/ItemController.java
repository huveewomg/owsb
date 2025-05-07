package com.owsb.controller;

import com.owsb.model.Item;
import com.owsb.util.FileUtils;
import com.owsb.util.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ItemController {

    public Object[][] getItemData() {
        try {
            Type itemListType = FileUtils.getListType(Item.class);
            List<Item> items = FileUtils.readListFromJson(Constants.ITEM_FILE, itemListType);

            Object[][] data = new Object[items.size()][6];

            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                data[i][0] = item.getItemID();
                data[i][1] = item.getName();
                data[i][2] = item.getUnitPrice();
                data[i][3] = item.getCategory();
                data[i][4] = item.getSupplierID();
                data[i][5] = item.getDateAdded();
            }

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new Object[0][0];  // return empty if there's an error
        }
    }
}
