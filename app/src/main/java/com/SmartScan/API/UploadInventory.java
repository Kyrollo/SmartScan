package com.SmartScan.API;

public class UploadInventory {
    private int InventoryID;
    private String InventoryDate;
    private int UserID;
    private int ItemID;
    private String ItemBarcode;
    private String Remark;
    private String CategoryId;
    private String CategoryDesc;
    private int StatusID;
    private String LocationID;
    private String LocationDesc;
    private String FullLocationDesc;
    private boolean Scanned;
    private boolean Missing;
    private boolean Manual;
    private boolean Reallocated;
    private String OldLocationID;
    private String OldLocationDesc;
    private String OldFullLocationDesc;
    private boolean StatusUpdated;
    private boolean ReallocatedApplied;
    private boolean StatusApplied;
    private boolean MissingApplied;
    private boolean IsChecked;
    private boolean Registered;
    private int CreatedBy;
    private String CreationDate;
    private int ModifiedBy;
    private String ModificationDate;
    private int ReasonID;

    public UploadInventory() {
    }

    public UploadInventory(int inventoryID, String inventoryDate, int userID, int itemID, String itemBarcode, String remark,
                           String categoryId, String categoryDesc, int statusID, String locationID, String locationDesc,
                           String fullLocationDesc, boolean scanned, boolean missing, boolean manual, boolean reallocated,
                           String oldLocationID, String oldLocationDesc, String oldFullLocationDesc, boolean statusUpdated,
                           boolean reallocatedApplied, boolean statusApplied, boolean missingApplied, boolean isChecked,
                           boolean registered, int createdBy, String creationDate, int modifiedBy, String modificationDate, int reasonID) {
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
    }

    // Getters and Setters
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

    public int getReasonID() {
        return ReasonID;
    }

    public void setReasonID(int reasonID) {
        ReasonID = reasonID;
    }

    public String getModificationDate() {
        return ModificationDate;
    }

    public void setModificationDate(String modificationDate) {
        ModificationDate = modificationDate;
    }
}
