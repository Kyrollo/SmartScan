package com.SmartScan.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.SmartScan.Tables.Inventory;

import java.util.List;

@Dao
public interface InventoryDao {
    @Insert
    void insert(Inventory inventory);

    @Insert
    void insertAll(List<Inventory> inventory);

    @Query("SELECT * FROM inventory")
    List<Inventory> getAllInventories();

    @Query("SELECT * FROM inventory WHERE item_barcode = :barcode")
    Inventory getInventoryByOPT3(String barcode);

    @Query("SELECT * FROM inventory WHERE item_barcode = :barcode AND location_id = :locationid")
    Inventory getInventoryByOPT3AndLocationID(String barcode, String locationid);

    @Query("SELECT * FROM inventory WHERE location_id = :LocationId AND missing = :missing")
    List<Inventory> getInventoriesByMissingStatus(String LocationId, boolean missing);

    @Query("UPDATE inventory SET missing = false WHERE item_id = :itemID")
    void updateItemStatusToFound(int itemID);

    @Query("SELECT * FROM inventory WHERE location_id = :LocationId")
    List<Inventory> getAllInventoriesByLocationId(String LocationId);

    @Query("UPDATE inventory SET image_data = :imageData WHERE item_barcode = :barcode")
    void updateItemImage(byte[] imageData, String barcode);

    @Query("DELETE FROM inventory")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'inventory'")
    void resetPrimaryKey();
}