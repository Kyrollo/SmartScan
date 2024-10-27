package com.AssetTrckingRFID.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.AssetTrckingRFID.Tables.Inventory;

import java.util.List;

@Dao
public interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Inventory inventory);

    @Insert
    void insertAll(List<Inventory> inventory);

    @Query("SELECT * FROM inventory")
    List<Inventory> getAllInventories();

    @Query("SELECT * FROM inventory WHERE tag_id = :barcode")
    Inventory getInventoryByOPT3(String barcode);

    @Query("SELECT * FROM inventory WHERE tag_id = :barcode AND location_id = :locationid")
    Inventory getInventoryByOPT3AndLocationID(String barcode, String locationid);

    @Query("SELECT * FROM inventory WHERE location_id = :LocationId AND missing = :missing")
    List<Inventory> getInventoriesByMissingStatus(String LocationId, boolean missing);

    @Query("SELECT * FROM inventory WHERE location_id = :LocationId AND registered = true")
    List<Inventory> getRegistered(String LocationId);

    @Query("SELECT * FROM inventory WHERE location_id = :LocationId AND reallocated = true")
    List<Inventory> getUnRegistered(String LocationId);

    @Query("SELECT * FROM inventory WHERE location_id = :LocationId AND checked = true")
    List<Inventory> getAll(String LocationId);

    @Query("UPDATE inventory SET missing = false, registered = true, scanned = true, checked = true, manual= false WHERE item_barcode = :itemBarcode")
    void updateItemStatusToFound(String itemBarcode);

    @Query("SELECT * FROM inventory WHERE location_id = :LocationId")
    List<Inventory> getAllInventoriesByLocationId(String LocationId);

    @Query("UPDATE inventory SET image_data = :imageData WHERE item_barcode = :barcode")
    void updateItemImage(byte[] imageData, String barcode);

    @Query("DELETE FROM inventory")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'inventory'")
    void resetPrimaryKey();

    @Query("SELECT * FROM Inventory where location_id = :locationID")
    List<Inventory> getAllInLocation(String locationID);

}