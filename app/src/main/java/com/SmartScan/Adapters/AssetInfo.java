package com.SmartScan.Adapters;

public class AssetInfo {
    private String itemBarCode;
    private String itemDesc;
    private String remark;
    private String opt3;

    public AssetInfo(String itemBarCode, String itemDesc, String remark, String opt3) {
        this.itemBarCode = itemBarCode;
        this.itemDesc = itemDesc;
        this.remark = remark;
        this.opt3 = opt3;
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
}