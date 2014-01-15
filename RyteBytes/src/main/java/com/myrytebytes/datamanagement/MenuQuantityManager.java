package com.myrytebytes.datamanagement;

import com.myrytebytes.datamodel.LocationItem;
import com.myrytebytes.datamodel.MenuItem;

import java.util.List;

public class MenuQuantityManager {
    public static List<LocationItem> mLocationItems;

    public static void setLocationItems(List<LocationItem> locationItems) {
        mLocationItems = locationItems;
    }

    public static int getAvailableQuantity(MenuItem menuItem) {
        if (mLocationItems == null) {
            return 20;
        } else {
            int quantity = 0;
            for (LocationItem locationItem : mLocationItems) {
                if (menuItem.objectId.equals(locationItem.menuItem.objectId)) {
                    quantity = locationItem.quantity;
                    break;
                }
            }
            if (quantity > 20) {
                quantity = 20;
            } else if (quantity < 0) {
                quantity = 0;
            }
            return quantity;
        }
    }
}