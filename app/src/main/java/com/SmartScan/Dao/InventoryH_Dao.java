package com.SmartScan.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.SmartScan.Tables.InventoryH;

import java.util.List;

@Dao
public interface InventoryH_Dao {
    @Insert
    void insert(InventoryH inventoryH);

    @Query("SELECT inventory_id, inventory_name, start_date, end_date, closed, id FROM inventoryh WHERE inventory_id = :inventoryID")
    InventoryH getInventoryHByID(String inventoryID);

    @Query("SELECT * FROM inventoryh")
    List<InventoryH> getAllInventoryHs();

    @Query("DELETE FROM inventoryh")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'inventoryh'")
    void resetPrimaryKey();
}
