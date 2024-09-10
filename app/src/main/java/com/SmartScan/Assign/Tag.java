package com.SmartScan.Assign;

public class Tag {
    private String itemBarcode;
    private String tagId;

    public Tag(String itemBarcode, String tagId) {
        this.itemBarcode = itemBarcode;
        this.tagId = tagId;
    }

    public String getItemBarcode() {
        return itemBarcode;
    }

    public String getTagId() {
        return tagId;
    }
}
