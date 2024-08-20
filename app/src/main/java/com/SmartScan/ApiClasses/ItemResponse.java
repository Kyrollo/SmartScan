package com.SmartScan.ApiClasses;

public class ItemResponse {
    private String ItemBarCode;
    private String ItemDesc;
    private String Remark;
    private String OPT3;
    private String Status;

    public ItemResponse(String itemBarCode, String itemDesc, String remark, String OPT3) {
        this.ItemBarCode = itemBarCode;
        this.ItemDesc = itemDesc;
        this.Remark = remark;
        this.OPT3 = OPT3;
        this.Status = "Missing";
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
