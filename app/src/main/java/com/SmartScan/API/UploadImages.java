package com.SmartScan.API;

public class UploadImages {
    public int ItemID;
    public byte[] imageData;

    public UploadImages() {
    }

    public UploadImages(int itemID, byte[] imageData) {
        ItemID = itemID;
        this.imageData = imageData;
    }

    public int getItemID() {
        return ItemID;
    }

    public void setItemID(int itemID) {
        ItemID = itemID;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
