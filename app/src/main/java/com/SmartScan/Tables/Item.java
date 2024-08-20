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
    public int itemId;

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

    @Ignore
    public Bitmap image;

    @ColumnInfo(name = "image_data")
    public byte[] imageData;

    public Item() {
    }

    public Item(String itemBarCode, String itemDesc, String remark, String opt3) {
        this.itemBarCode = itemBarCode;
        this.itemDesc = itemDesc;
        this.remark = remark;
        this.opt3 = opt3;
        this.status = "Missing";
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
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
