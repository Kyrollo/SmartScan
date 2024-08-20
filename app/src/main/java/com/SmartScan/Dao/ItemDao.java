package com.SmartScan.Dao;

import android.graphics.Bitmap;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.SmartScan.Tables.Item;

import java.util.List;

@Dao
public interface ItemDao {
    @Insert
    void insert(Item item);

    @Query("SELECT itemId, item_bar_code, item_desc, remark FROM item WHERE opt3 = :barcode")
    Item getItemsByOPT3(String barcode);

    @Query("UPDATE item SET status = 'Found' WHERE opt3 = :opt3")
    void updateItemStatusToFound(String opt3);

    @Query("UPDATE item SET image_data = :imageData WHERE item_bar_code = :barcode")
    void updateItemImage(byte[] imageData, String barcode);

    @Query("SELECT * FROM item")
    List<Item> getAllItems();

    @Query("DELETE FROM item")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'item'")
    void resetPrimaryKey();
}
