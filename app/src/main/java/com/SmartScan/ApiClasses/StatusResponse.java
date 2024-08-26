package com.SmartScan.ApiClasses;

public class StatusResponse {
    private int StatusID;
    private String StatusDesc;

    public StatusResponse() {
    }

    public StatusResponse(int statusID, String statusDesc) {
        StatusID = statusID;
        StatusDesc = statusDesc;
    }

    // Getters and Setters
    public int getStatusID() {
        return StatusID;
    }

    public void setStatusID(int statusID) {
        StatusID = statusID;
    }

    public String getStatusDesc() {
        return StatusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        StatusDesc = statusDesc;
    }
}
