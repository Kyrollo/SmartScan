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

    @Query("SELECT * FROM inventory WHERE opt3 = :barcode")
    Inventory getInventoryByOPT3(String barcode);

    @Query("UPDATE inventory SET status = 'Found' WHERE item_id = :itemID")
    void updateItemStatusToFound(int itemID);

    @Query("SELECT * FROM inventory WHERE location_id = :LocationId")
    List<Inventory> getAllInventoriesByLocationId(String LocationId);

    @Query("DELETE FROM inventory")
    void deleteAll();
}