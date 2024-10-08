package com.SmartScan.Dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.SmartScan.ApiClasses.ItemResponse;
import com.SmartScan.Tables.Item;

import java.util.List;

@Dao
public interface ItemDao {
    @Insert
    void insert(Item item);

    @Query("SELECT * FROM item WHERE opt3 = :barcode")
    Item getItemsByOPT3(String barcode);

    @Query("SELECT * FROM item WHERE item_bar_code = :barcode")
    Item getItemByBarcode(String barcode);

    @Query("SELECT * FROM item WHERE item_id = :ItemID")
    Item getItemsByItemID(int ItemID);

    @Query("UPDATE item SET opt3 = :rfid WHERE item_bar_code = :barcode")
    void assignTag(String barcode, String rfid);

    @Query("UPDATE item SET opt3 = null WHERE item_bar_code = :barcode")
    void deleteOPT3(String barcode);

    @Query("UPDATE item SET image_data = :imageData WHERE item_id = :itemid")
    void SetItemImage(byte[] imageData, int itemid);

    @Query("SELECT * FROM item")
    List<Item> getAllItems();

    @Query("SELECT * FROM item WHERE opt3 is not null AND location_id = :locationID")
    List<Item> getAllItemsByParentID(String locationID);

    @Query("DELETE FROM item")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'item'")
    void resetPrimaryKey();
}
