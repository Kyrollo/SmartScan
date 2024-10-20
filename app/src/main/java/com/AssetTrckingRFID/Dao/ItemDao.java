package com.AssetTrckingRFID.Dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import com.AssetTrckingRFID.Tables.Inventory;
import com.AssetTrckingRFID.Tables.Item;

import java.util.List;

@Dao
public interface ItemDao {
    @Insert
    void insert(Item item);

    @Query("SELECT * FROM item WHERE opt3 = :barcode")
    Item getItemsByOPT3(String barcode);

    @Query("SELECT * FROM item WHERE item_bar_code = :barcode")
    Item getItemByBarcode(String barcode);

    @Query("SELECT * FROM item WHERE ItemID = :ItemID")
    Item getItemsByItemID(int ItemID);

    @Query("UPDATE item SET opt3 = :rfid WHERE item_bar_code = :barcode")
    void assignTag(String barcode, String rfid);

    @Query("UPDATE item SET opt3 = null WHERE item_bar_code = :barcode")
    void deleteOPT3(String barcode);

    @Query("UPDATE item SET image_data = :imageData WHERE ItemID = :itemid")
    void SetItemImage(byte[] imageData, int itemid);

    @Query("SELECT * FROM item")
    List<Item> getAllItems();

    @Query("SELECT * FROM item Where image_data != null ")
    List<Item> getAllItems2();

    @Query("SELECT * FROM item WHERE /*opt3 is not null AND*/ location_id = :locationID")
    List<Item> getAllItemsByParentID(String locationID);

    @Query("DELETE FROM item")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'item'")
    void resetPrimaryKey();

//    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
//    @Query("select  :inventoryID as InventoryID,:inventoryDate as InventoryDate ,:userID as UserID, ItemID,opt3 as ItemBarcode" +
//            ",I.remark,I.category_id as CategoryId,category_desc,status_id,I.location_id,location_desc, full_location_desc" +
//            ",false as Scanned, true as Missing, false as Manual, :userID as CreatedBy, null as ModifiedBy, 0 as ReasonID from Item as I " +
//            "inner join Category as C on I.category_id = C.CategoryID " +
//            "inner join Location as L on I.location_id = L.LocationID where L.LocationID = :locationId ")
//    List<Inventory> getItemsInLocation_Inventory(String locationId, int inventoryID, String inventoryDate, int userID);

}
