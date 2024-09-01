package com.SmartScan.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory")

public class Inventory {
    @PrimaryKey(autoGenerate = false)
    private int ID;

    @ColumnInfo (name = "inventory_id")
    private int InventoryID;

    @ColumnInfo (name = "inventory_date")
    private String InventoryDate;

    @ColumnInfo (name = "user_id")
    private int UserID;

    @ColumnInfo (name = "item_id")
    private int ItemID;

    @ColumnInfo (name = "item_barcode")
    private String ItemBarcode;

    @ColumnInfo (name = "remark")
    private String Remark;

    @ColumnInfo (name = "category_id")
    private String CategoryId;

    @ColumnInfo (name = "category_desc")
    private String CategoryDesc;

    @ColumnInfo (name = "status_id")
    private int StatusID;

    @ColumnInfo (name = "status")
    private String Status;

    @ColumnInfo (name = "location_id")
    private String LocationID;

    @ColumnInfo (name = "location_desc")
    private String LocationDesc;

    @ColumnInfo (name = "full_location_desc")
    private String FullLocationDesc;

    @ColumnInfo (name = "scanned")
    private boolean Scanned;

    @ColumnInfo (name = "missing")
    private boolean Missing;

    @ColumnInfo (name = "manual")
    private boolean Manual;

    @ColumnInfo (name = "reallocated")
    private boolean Reallocated;

    @ColumnInfo (name = "old_location_id")
    private String OldLocationID;

    @ColumnInfo (name = "old_location_desc")
    private String OldLocationDesc;

    @ColumnInfo (name = "old_full_location_desc")
    private String OldFullLocationDesc;

    @ColumnInfo (name = "status_updated")
    private boolean StatusUpdated;

    @ColumnInfo (name = "reallocated_applied")
    private boolean ReallocatedApplied;

    @ColumnInfo (name = "status_applied")
    private boolean StatusApplied;

    @ColumnInfo (name = "missing_applied")
    private boolean MissingApplied;

    @ColumnInfo (name = "is_checked")
    private boolean IsChecked;

    @ColumnInfo (name = "registered")
    private boolean Registered;

    @ColumnInfo (name = "created_by")
    private int CreatedBy;

    @ColumnInfo (name = "creation_date")
    private String CreationDate;

    @ColumnInfo (name = "modified_by")
    private int ModifiedBy;

    @ColumnInfo (name = "modification_date")
    private String ModificationDate;

    @ColumnInfo (name = "reason_id")
    private int ReasonID;

    @ColumnInfo (name = "opt3")
    private String Opt3;

    public Inventory() {
    }

    public Inventory(int itemID, String itemBarcode, String remark, String categoryId, String categoryDesc, int statusID,
                     String locationID, String locationDesc, String fullLocationDesc, boolean scanned, boolean missing,
                     boolean reallocated, String oldLocationID, String oldLocationDesc, String oldFullLocationDesc,
                     boolean statusUpdated, boolean reallocatedApplied, boolean registered, String inventoryDate, int userID) {
        ItemID = itemID;
        ItemBarcode = itemBarcode;
        Remark = remark;
        CategoryId = categoryId;
        CategoryDesc = categoryDesc;
        StatusID = statusID;
        LocationID = locationID;
        LocationDesc = locationDesc;
        FullLocationDesc = fullLocationDesc;
        Scanned = scanned;
        Missing = missing;
        Reallocated = reallocated;
        OldLocationID = oldLocationID;
        OldLocationDesc = oldLocationDesc;
        OldFullLocationDesc = oldFullLocationDesc;
        StatusUpdated = statusUpdated;
        ReallocatedApplied = reallocatedApplied;
        Registered = registered;
        InventoryDate = inventoryDate;
        UserID = userID;
        InventoryID = -1;
        Manual = false;
        StatusApplied = false;
        MissingApplied = false;
        IsChecked = false;
        CreatedBy = -1;
        CreationDate = null;
        ModifiedBy = -1;
        ModificationDate = null;
        ReasonID = -1;
        Status = "Missing";
    }

    public Inventory(int ID, int inventoryID, String inventoryDate, int userID, int itemID, String itemBarcode, String remark,
                     String categoryId, String categoryDesc, int statusID, String locationID, String locationDesc, String fullLocationDesc,
                     boolean scanned, boolean missing, boolean manual, boolean reallocated, String oldLocationID, String oldLocationDesc,
                     String oldFullLocationDesc, boolean statusUpdated, boolean reallocatedApplied, boolean statusApplied,
                     boolean missingApplied, boolean isChecked, boolean registered, int createdBy, String creationDate,
                     int modifiedBy, String modificationDate, int reasonID) {
        this.ID = ID;
        InventoryID = inventoryID;
        InventoryDate = inventoryDate;
        UserID = userID;
        ItemID = itemID;
        ItemBarcode = itemBarcode;
        Remark = remark;
        CategoryId = categoryId;
        CategoryDesc = categoryDesc;
        StatusID = statusID;
        LocationID = locationID;
        LocationDesc = locationDesc;
        FullLocationDesc = fullLocationDesc;
        Scanned = scanned;
        Missing = missing;
        Manual = manual;
        Reallocated = reallocated;
        OldLocationID = oldLocationID;
        OldLocationDesc = oldLocationDesc;
        OldFullLocationDesc = oldFullLocationDesc;
        StatusUpdated = statusUpdated;
        ReallocatedApplied = reallocatedApplied;
        StatusApplied = statusApplied;
        MissingApplied = missingApplied;
        IsChecked = isChecked;
        Registered = registered;
        CreatedBy = createdBy;
        CreationDate = creationDate;
        ModifiedBy = modifiedBy;
        ModificationDate = modificationDate;
        ReasonID = reasonID;
        Status = "Missing";
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

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

    public boolean isChecked() {
        return IsChecked;
    }

    public void setChecked(boolean checked) {
        IsChecked = checked;
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

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
