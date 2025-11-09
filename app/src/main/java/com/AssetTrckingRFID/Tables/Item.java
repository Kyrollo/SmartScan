package com.AssetTrckingRFID.Tables;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "item")
public class Item {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    public int ItemID;

    @ColumnInfo(name = "item_bar_code")
    public String itemBarCode;

    @ColumnInfo(name = "item_barcode_abb")
    public int ItemBarcodeAbb;

    @ColumnInfo(name = "item_desc")
    public String itemDesc;

    @ColumnInfo(name = "item_sn")
    public String ItemSN;

    @ColumnInfo(name = "vendor_id")
    public int VendorID;

    @ColumnInfo(name = "insurer_id")
    public int InsurerID;

    @ColumnInfo(name = "purchase_date")
    public String PurchaseDate;

    @ColumnInfo(name = "warrenty_period")
    public int WarrentyPeriod;

    @ColumnInfo(name = "category_id")
    public String CategoryID;

    @ColumnInfo(name = "location_id")
    public String LocationID;

    @ColumnInfo(name = "status_id")
    public int StatusID;

    @ColumnInfo(name = "item_cost")
    public double ItemCost;

    @ColumnInfo(name = "item_price")
    public double ItemPrice;

    @ColumnInfo(name = "po_number")
    public String PONumber;

    @ColumnInfo(name = "item_life_time")
    public int ItemLifeTime;

    @ColumnInfo(name = "item_usage_period")
    public int ItemUsagePeriod;

    @ColumnInfo(name = "item_salvage")
    public int ItemSalvage;

    @ColumnInfo(name = "factor")
    public int Factor;

    @ColumnInfo(name = "item_first_inventory_date")
    public String ItemFirstInventoryDate;

    @ColumnInfo(name = "item_last_inventory_date")
    public String ItemLastInventoryDate;

    @ColumnInfo(name = "item_qty")
    public int ItemQty;

    @ColumnInfo(name = "remark")
    public String remark;

    @ColumnInfo(name = "opt1")
    public String opt1;

    @ColumnInfo(name = "opt2")
    public String opt2;

    @ColumnInfo(name = "opt3")
    public String opt3;

    @ColumnInfo(name = "image_data")
    public byte[] imageData;


    public Item() {
    }

    public Item(String itemBarCode, String itemDesc, String remark, String opt3, int itemID,
                String categoryID, String locationID, int statusID, String itemSN) {
        this.itemBarCode = itemBarCode;
        this.itemDesc = itemDesc;
        this.remark = remark;
        this.opt3 = opt3;
        ItemID = itemID;
        CategoryID = categoryID;
        LocationID = locationID;
        StatusID = statusID;
        ItemSN = itemSN;

    }

    // Getters and Setters
    public int getItemID() {
        return ItemID;
    }

    public String getCategoryID() {
        return CategoryID;
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

    public String getItemSN() {
        return ItemSN;
    }

    public String getItemBarCode() {
        return itemBarCode;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOpt3() {
        return opt3;
    }

    public int getItemBarcodeAbb() {
        return ItemBarcodeAbb;
    }

    public int getVendorID() {
        return VendorID;
    }

    public int getInsurerID() {
        return InsurerID;
    }

    public String getPurchaseDate() {
        return PurchaseDate;
    }

    public int getWarrentyPeriod() {
        return WarrentyPeriod;
    }

    public double getItemCost() {
        return ItemCost;
    }

    public double getItemPrice() {
        return ItemPrice;
    }

    public String getPONumber() {
        return PONumber;
    }

    public int getItemLifeTime() {
        return ItemLifeTime;
    }

    public int getItemUsagePeriod() {
        return ItemUsagePeriod;
    }

    public int getItemSalvage() {
        return ItemSalvage;
    }

    public int getFactor() {
        return Factor;
    }

    public String getItemFirstInventoryDate() {
        return ItemFirstInventoryDate;
    }

    public String getItemLastInventoryDate() {
        return ItemLastInventoryDate;
    }

    public int getItemQty() {
        return ItemQty;
    }

    public String getOpt1() {
        return opt1;
    }

    public String getOpt2() {
        return opt2;
    }

}
