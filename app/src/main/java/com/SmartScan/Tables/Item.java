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

    @ColumnInfo(name = "item_bar_code")
    public String itemBarCode;

    @ColumnInfo(name = "item_desc")
    public String itemDesc;

    @ColumnInfo(name = "remark")
    public String remark;

    @ColumnInfo(name = "opt3")
    public String opt3;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "item_id")
    public int ItemID;

    @ColumnInfo(name = "category_id")
    public String CategoryID;

    @ColumnInfo(name = "location_id")
    public String LocationID;

    @ColumnInfo(name = "status_id")
    public int StatusID;

    @ColumnInfo(name = "item_sn")
    public String ItemSN;

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
        this.status = "Missing";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
