package com.AssetTrckingRFID.API;

public class UploadItems {
    public int ItemID;
    public String itemBarCode;
    public int ItemBarcodeAbb;
    public String itemDesc;
    public String ItemSN;
    public int VendorID;
    public int InsurerID;
    public String PurchaseDate;
    public int WarrentyPeriod;
    public String CategoryID;
    public String LocationID;
    public int StatusID;
    public double ItemCost;
    public double ItemPrice;
    public String PONumber;
    public int ItemLifeTime;
    public int ItemUsagePeriod;
    public int ItemSalvage;
    public int Factor;
    public String ItemFirstInventoryDate;
    public String ItemLastInventoryDate;
    public int ItemQty;
    public String remark;
    public String opt1;
    public String opt2;
    public String opt3;

    public UploadItems() {
    }

    public UploadItems(int itemID, String itemBarCode, int itemBarcodeAbb, String itemDesc, String itemSN, int vendorID,
                       int insurerID, String purchaseDate, int warrentyPeriod, String categoryID, String locationID, int statusID,
                       double itemCost, double itemPrice, String PONumber, int itemLifeTime, int itemUsagePeriod, int itemSalvage,
                       int factor, String itemFirstInventoryDate, String itemLastInventoryDate, int itemQty, String remark,
                       String opt1, String opt2, String opt3) {
        ItemID = itemID;
        this.itemBarCode = itemBarCode;
        ItemBarcodeAbb = itemBarcodeAbb;
        this.itemDesc = itemDesc;
        ItemSN = itemSN;
        VendorID = vendorID;
        InsurerID = insurerID;
        PurchaseDate = purchaseDate;
        WarrentyPeriod = warrentyPeriod;
        CategoryID = categoryID;
        LocationID = locationID;
        StatusID = statusID;
        ItemCost = itemCost;
        ItemPrice = itemPrice;
        this.PONumber = PONumber;
        ItemLifeTime = itemLifeTime;
        ItemUsagePeriod = itemUsagePeriod;
        ItemSalvage = itemSalvage;
        Factor = factor;
        ItemFirstInventoryDate = itemFirstInventoryDate;
        ItemLastInventoryDate = itemLastInventoryDate;
        ItemQty = itemQty;
        this.remark = remark;
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.opt3 = opt3;
    }


    // Getters and Setters
    public int getItemID() {
        return ItemID;
    }

    public void setItemID(int itemID) {
        ItemID = itemID;
    }

    public String getItemBarCode() {
        return itemBarCode;
    }

    public void setItemBarCode(String itemBarCode) {
        this.itemBarCode = itemBarCode;
    }

    public int getItemBarcodeAbb() {
        return ItemBarcodeAbb;
    }

    public void setItemBarcodeAbb(int itemBarcodeAbb) {
        ItemBarcodeAbb = itemBarcodeAbb;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public String getItemSN() {
        return ItemSN;
    }

    public void setItemSN(String itemSN) {
        ItemSN = itemSN;
    }

    public int getVendorID() {
        return VendorID;
    }

    public void setVendorID(int vendorID) {
        VendorID = vendorID;
    }

    public int getInsurerID() {
        return InsurerID;
    }

    public void setInsurerID(int insurerID) {
        InsurerID = insurerID;
    }

    public String getPurchaseDate() {
        return PurchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        PurchaseDate = purchaseDate;
    }

    public int getWarrentyPeriod() {
        return WarrentyPeriod;
    }

    public void setWarrentyPeriod(int warrentyPeriod) {
        WarrentyPeriod = warrentyPeriod;
    }

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

    public double getItemCost() {
        return ItemCost;
    }

    public void setItemCost(double itemCost) {
        ItemCost = itemCost;
    }

    public double getItemPrice() {
        return ItemPrice;
    }

    public void setItemPrice(double itemPrice) {
        ItemPrice = itemPrice;
    }

    public String getPONumber() {
        return PONumber;
    }

    public void setPONumber(String PONumber) {
        this.PONumber = PONumber;
    }

    public int getItemLifeTime() {
        return ItemLifeTime;
    }

    public void setItemLifeTime(int itemLifeTime) {
        ItemLifeTime = itemLifeTime;
    }

    public int getItemUsagePeriod() {
        return ItemUsagePeriod;
    }

    public void setItemUsagePeriod(int itemUsagePeriod) {
        ItemUsagePeriod = itemUsagePeriod;
    }

    public int getItemSalvage() {
        return ItemSalvage;
    }

    public void setItemSalvage(int itemSalvage) {
        ItemSalvage = itemSalvage;
    }

    public int getFactor() {
        return Factor;
    }

    public void setFactor(int factor) {
        Factor = factor;
    }

    public String getItemFirstInventoryDate() {
        return ItemFirstInventoryDate;
    }

    public void setItemFirstInventoryDate(String itemFirstInventoryDate) {
        ItemFirstInventoryDate = itemFirstInventoryDate;
    }

    public String getItemLastInventoryDate() {
        return ItemLastInventoryDate;
    }

    public void setItemLastInventoryDate(String itemLastInventoryDate) {
        ItemLastInventoryDate = itemLastInventoryDate;
    }

    public int getItemQty() {
        return ItemQty;
    }

    public void setItemQty(int itemQty) {
        ItemQty = itemQty;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOpt1() {
        return opt1;
    }

    public void setOpt1(String opt1) {
        this.opt1 = opt1;
    }

    public String getOpt2() {
        return opt2;
    }

    public void setOpt2(String opt2) {
        this.opt2 = opt2;
    }

    public String getOpt3() {
        return opt3;
    }

    public void setOpt3(String opt3) {
        this.opt3 = opt3;
    }
}
