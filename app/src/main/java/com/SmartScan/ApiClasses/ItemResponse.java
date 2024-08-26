package com.SmartScan.ApiClasses;

public class ItemResponse {
    private int ItemID;
    private String ItemBarCode;
    private String ItemDesc;
    private String CategoryID;
    private String LocationID;
    private int StatusID;
    private String ItemSN;
    private String Remark;
    private String OPT3;
    private String Status;

    public ItemResponse() {
    }

    public ItemResponse(String categoryID, String locationID, int statusID, int itemID, String itemSN,
                        String itemBarCode, String itemDesc, String remark, String OPT3, String status) {
        CategoryID = categoryID;
        LocationID = locationID;
        StatusID = statusID;
        ItemID = itemID;
        ItemSN = itemSN;
        ItemBarCode = itemBarCode;
        ItemDesc = itemDesc;
        Remark = remark;
        this.OPT3 = OPT3;
        Status = status;
    }

    // Getters and Setters
    public String getCategoryID() {
        return CategoryID;
    }

    public void setCategoryID(String categoryID) {
        CategoryID = categoryID;
    }

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

    public int getStatusID() {
        return StatusID;
    }

    public void setStatusID(int statusID) {
        StatusID = statusID;
    }

    public int getItemID() {
        return ItemID;
    }

    public void setItemID(int itemID) {
        ItemID = itemID;
    }

    public String getItemSN() {
        return ItemSN;
    }

    public void setItemSN(String itemSN) {
        ItemSN = itemSN;
    }

    public String getItemBarCode() {
        return ItemBarCode;
    }

    public void setItemBarCode(String itemBarCode) {
        ItemBarCode = itemBarCode;
    }

    public String getItemDesc() {
        return ItemDesc;
    }

    public void setItemDesc(String itemDesc) {
        ItemDesc = itemDesc;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getOPT3() {
        return OPT3;
    }

    public void setOPT3(String OPT3) {
        this.OPT3 = OPT3;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
