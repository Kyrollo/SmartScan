package com.SmartScan.Tables;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.ByteArrayOutputStream;

@Entity(tableName = "item")
public class Item {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "item_id")
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

    // To store image validation
    @Ignore
    public Bitmap image;

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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemID() {
        return ItemID;
    }

    public void setItemID(int itemID) {
        ItemID = itemID;
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

    public String getItemSN() {
        return ItemSN;
    }

    public void setItemSN(String itemSN) {
        ItemSN = itemSN;
    }

    public int getItemId() {
        return id;
    }

    public void setItemId(int itemId) {
        this.id = itemId;
    }

    public String getItemBarCode() {
        return itemBarCode;
    }

    public void setItemBarCode(String itemBarCode) {
        this.itemBarCode = itemBarCode;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
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

    public void setOpt3(String opt3) {
        this.opt3 = opt3;
    }

    public int getItemBarcodeAbb() {
        return ItemBarcodeAbb;
    }

    public void setItemBarcodeAbb(int itemBarcodeAbb) {
        ItemBarcodeAbb = itemBarcodeAbb;
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
