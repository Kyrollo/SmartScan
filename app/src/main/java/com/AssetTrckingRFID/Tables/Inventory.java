package com.AssetTrckingRFID.Tables;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.ByteArrayOutputStream;

@Entity(tableName = "inventory")
public class Inventory {

    public Inventory() {
    }

    @ColumnInfo(name = "inventory_id")
    public int InventoryID;

    @ColumnInfo(name = "inventory_date")
    public String InventoryDate;

    @ColumnInfo(name = "user_id")
    public int UserID;

    @PrimaryKey(autoGenerate = false)
    public int ItemID;

    @ColumnInfo(name = "item_barcode")
    public String ItemBarcode;

    @ColumnInfo(name = "remark")
    public String Remark;

    @ColumnInfo(name = "category_id")
    public String CategoryId;

    @ColumnInfo(name = "category_desc")
    public String CategoryDesc;

    @ColumnInfo(name = "status_id")
    public int StatusID;

    @ColumnInfo(name = "location_id")
    public String LocationID;

    @ColumnInfo(name = "location_desc")
    public String LocationDesc;

    @ColumnInfo(name = "full_location_desc")
    public String FullLocationDesc;

    @ColumnInfo(name = "scanned")
    public boolean Scanned;

    @ColumnInfo(name = "missing")
    public boolean Missing;

    @ColumnInfo(name = "manual")
    public boolean Manual;

    @ColumnInfo(name = "reallocated")
    public boolean Reallocated;

    @ColumnInfo(name = "old_location_id")
    public String OldLocationID;

    @ColumnInfo(name = "old_location_desc")
    public String OldLocationDesc;

    @ColumnInfo(name = "old_full_location_desc")
    public String OldFullLocationDesc;

    @ColumnInfo(name = "status_updated")
    public boolean StatusUpdated;

    @ColumnInfo(name = "reallocated_applied")
    public boolean ReallocatedApplied;

    @ColumnInfo(name = "status_applied")
    public boolean StatusApplied;

    @ColumnInfo(name = "missing_applied")
    public boolean MissingApplied;

    @ColumnInfo(name = "checked")
    public boolean IsChecked;

    @ColumnInfo(name = "registered")
    public boolean Registered;

    @ColumnInfo(name = "created_by")
    public int CreatedBy;

    @ColumnInfo(name = "creation_date")
    public String CreationDate;

    @ColumnInfo(name = "modified_by")
    public int ModifiedBy;

    @ColumnInfo(name = "modification_date")
    public String ModificationDate;

    @ColumnInfo(name = "reason_id")
    public int ReasonID;

    @ColumnInfo(name = "tag_id")
    public String TagId;

    @Ignore
    public Bitmap image;

    @ColumnInfo(name = "image_data")
    public byte[] imageData;

//    public Inventory(int inventoryID, String inventoryDate, int userID, int itemID, String itemBarcode, String remark, String categoryId, String categoryDesc, int statusID, String locationID, String locationDesc, String fullLocationDesc, Boolean scanned, Boolean missing, Boolean manual, Boolean reallocated, String oldLocationID, String oldLocationDesc, String oldFullLocationDesc, Boolean statusUpdated, Boolean isChecked, Boolean registered, int createdBy, String creationDate, int modifiedBy, String modificationDate, int reasonID) {
//        InventoryID = inventoryID;
//        InventoryDate = inventoryDate;
//        UserID = userID;
//        ItemID = itemID;
//        ItemBarcode = itemBarcode;
//        Remark = remark;
//        CategoryId = categoryId;
//        CategoryDesc = categoryDesc;
//        StatusID = statusID;
//        LocationID = locationID;
//        LocationDesc = locationDesc;
//        FullLocationDesc = fullLocationDesc;
//        Scanned = scanned;
//        Missing = missing;
//        Manual = manual;
//        Reallocated = reallocated;
//        OldLocationID = oldLocationID;
//        OldLocationDesc = oldLocationDesc;
//        OldFullLocationDesc = oldFullLocationDesc;
//        StatusUpdated = statusUpdated;
//        IsChecked = isChecked;
//        Registered = registered;
//        CreatedBy = createdBy;
//        CreationDate = creationDate;
//        ModifiedBy = modifiedBy;
//        ModificationDate = modificationDate;
//        ReasonID = reasonID;
//    }

//    // general constructor
//    public Inventory(int inventoryID, String inventoryDate, int userID, int itemID, String itemBarcode, String remark, String categoryId,
//                     String categoryDesc, int statusID, String locationID, String locationDesc, String fullLocationDesc, Boolean scanned,
//                     Boolean missing, Boolean manual, int createdBy) {
//        InventoryID = inventoryID;
//        InventoryDate = inventoryDate;
//        UserID = userID;
//        ItemID = itemID;
//        ItemBarcode = itemBarcode;
//        Remark = remark;
//        CategoryId = categoryId;
//        CategoryDesc = categoryDesc;
//        StatusID = statusID;
//        LocationID = locationID;
//        LocationDesc = locationDesc;
//        FullLocationDesc = fullLocationDesc;
//        Scanned = scanned;
//        Missing = missing;
//        Manual = manual;
//        CreatedBy = createdBy;
//
//    }
    // Constructor for the insert an item in Inventory Table
    public Inventory(int inventoryID, String inventoryDate, int userID, int itemID, String itemBarcode, String tagId, String remark, String categoryId,
                     String categoryDesc, int statusID, String locationID, String locationDesc, String fullLocationDesc) {
        InventoryID = inventoryID;              // From Inventory_H
        InventoryDate = inventoryDate;          // From Inventory_H
        UserID = userID;                        // From Users
        ItemID = itemID;                        // From Item
        ItemBarcode = itemBarcode;                     // From Item
        TagId = tagId;
        Remark = remark;                        // From Item
        CategoryId = categoryId;                // From Item
        CategoryDesc = categoryDesc;            // From Category
        StatusID = statusID;                    // From Item
        LocationID = locationID;                // From Item
        LocationDesc = locationDesc;            // From Location
        FullLocationDesc = fullLocationDesc;    // From Location
        Scanned = false;            // The item is not scanned yet
        Missing = true;             // The item is still missing
        Manual = false;             // The item is not manually added
        Reallocated = false;         // The item is not reallocated
      //  Registered = true;           // The item is not registered
        CreatedBy = userID;         // The item is created by the user
    }

//    // Constructor for the reallocation of an item in Inventory Table
    public Inventory(int inventoryID, String inventoryDate, int userID, int itemID, String itemBarcode, String tagId, String remark, String categoryId,
                     String categoryDesc, int statusID, String locationID, String locationDesc, String fullLocationDesc,
                     String oldLocationID, String oldLocationDesc,  String oldFullLocationDesc) {
        InventoryID = inventoryID;              // From Inventory_H
        InventoryDate = inventoryDate;          // From Inventory_H
        UserID = userID;                        // From Users
        ItemID = itemID;                        // From Item
        ItemBarcode = itemBarcode;                     // From Item
        TagId = tagId;
        Remark = remark;                        // From Item
        CategoryId = categoryId;                // From Item
        CategoryDesc = categoryDesc;            // From Category
        StatusID = statusID;                    // From Item
        LocationID = locationID;                // From the new Location
        LocationDesc = locationDesc;            // From the new Location
        FullLocationDesc = fullLocationDesc;    // From the new Location
        Scanned = true;                // The item is scanned
        Missing = false;               // The item is not missing
        Manual = false;                // The item is not manually added
        Reallocated = true;            // The item is reallocated
        Registered = false;             // The item is not registered
        IsChecked = true;
        OldLocationID = oldLocationID;              // The item is reallocated, the locationID is the old from Item
        OldLocationDesc = oldLocationDesc;          // The item is reallocated, the locationDesc is not old
        OldFullLocationDesc = oldFullLocationDesc;  // The item is reallocated, the fullLocationDesc is not old
        CreatedBy = userID;         // The item is created by the user
    }


    // Getter and Setter methods

    public int getInventoryID() {
        return InventoryID;
    }

    public void setInventoryID(int inventoryID) {
        InventoryID = inventoryID;
    }

    public String getInventoryDate() {
        return InventoryDate;
    }

    public void setInventoryDate(String inventoryDate) {
        InventoryDate = inventoryDate;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public int getItemID() {
        return ItemID;
    }

    public void setItemID(int itemID) {
        ItemID = itemID;
    }

    public String getItemBarcode() {
        return ItemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        ItemBarcode = itemBarcode;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(String categoryId) {
        CategoryId = categoryId;
    }

    public String getCategoryDesc() {
        return CategoryDesc;
    }

    public void setCategoryDesc(String categoryDesc) {
        CategoryDesc = categoryDesc;
    }

    public int getStatusID() {
        return StatusID;
    }

    public void setStatusID(int statusID) {
        StatusID = statusID;
    }

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

    public String getLocationDesc() {
        return LocationDesc;
    }

    public void setLocationDesc(String locationDesc) {
        LocationDesc = locationDesc;
    }

    public String getFullLocationDesc() {
        return FullLocationDesc;
    }

    public void setFullLocationDesc(String fullLocationDesc) {
        FullLocationDesc = fullLocationDesc;
    }

    public boolean isScanned() {
        return Scanned;
    }

    public void setScanned(boolean scanned) {
        Scanned = scanned;
    }

    public boolean isMissing() {
        return Missing;
    }

    public void setMissing(boolean missing) {
        Missing = missing;
    }

    public boolean isManual() {
        return Manual;
    }

    public void setManual(boolean manual) {
        Manual = manual;
    }

    public boolean isReallocated() {
        return Reallocated;
    }

    public void setReallocated(boolean reallocated) {
        Reallocated = reallocated;
    }

    public String getOldLocationID() {
        return OldLocationID;
    }

    public void setOldLocationID(String oldLocationID) {
        OldLocationID = oldLocationID;
    }

    public String getOldLocationDesc() {
        return OldLocationDesc;
    }

    public void setOldLocationDesc(String oldLocationDesc) {
        OldLocationDesc = oldLocationDesc;
    }

    public String getOldFullLocationDesc() {
        return OldFullLocationDesc;
    }

    public void setOldFullLocationDesc(String oldFullLocationDesc) {
        OldFullLocationDesc = oldFullLocationDesc;
    }

    public boolean isStatusUpdated() {
        return StatusUpdated;
    }

    public void setStatusUpdated(boolean statusUpdated) {
        StatusUpdated = statusUpdated;
    }

    public boolean isReallocatedApplied() {
        return ReallocatedApplied;
    }

    public void setReallocatedApplied(boolean reallocatedApplied) {
        ReallocatedApplied = reallocatedApplied;
    }

    public boolean isStatusApplied() {
        return StatusApplied;
    }

    public void setStatusApplied(boolean statusApplied) {
        StatusApplied = statusApplied;
    }

    public boolean isMissingApplied() {
        return MissingApplied;
    }

    public void setMissingApplied(boolean missingApplied) {
        MissingApplied = missingApplied;
    }

    public boolean IsChecked() {
        return IsChecked;
    }

    public void setIsChecked(boolean isChecked) {
        IsChecked = isChecked;
    }

    public boolean isRegistered() {
        return Registered;
    }

    public void setRegistered(boolean registered) {
        Registered = registered;
    }

    public int getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(int createdBy) {
        CreatedBy = createdBy;
    }

    public String getCreationDate() {
        return CreationDate;
    }

    public void setCreationDate(String creationDate) {
        CreationDate = creationDate;
    }

    public int getModifiedBy() {
        return ModifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        ModifiedBy = modifiedBy;
    }

    public String getModificationDate() {
        return ModificationDate;
    }

    public void setModificationDate(String modificationDate) {
        ModificationDate = modificationDate;
    }

    public int getReasonID() {
        return ReasonID;
    }

    public void setReasonID(int reasonID) {
        ReasonID = reasonID;
    }

    public String getTagId() {
        return TagId;
    }

    public void setTagId(String tagId) {
        TagId = tagId;
    }

    public Bitmap getImage() {
        return image = byteArrayToBitmap(imageData);
    }

    public void setImage(Bitmap image) {
        this.image = image;
        this.imageData = bitmapToByteArray(image);
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
        this.image = byteArrayToBitmap(imageData);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}