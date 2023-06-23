package com.example.lokitech;

import java.util.Date;

public class PinGetResAPI {
    private int PinId;
    private String Pin;
    private Date Expiry;
    private int AU;
    private int LockId;
    private int Status;

    public PinGetResAPI(int pinId, String pin, Date expiry, int AU, int lockId, int status) {
        PinId = pinId;
        Pin = pin;
        Expiry = expiry;
        this.AU = AU;
        LockId = lockId;
        Status = status;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getPinId() {
        return PinId;
    }

    public void setPinId(int pinId) {
        PinId = pinId;
    }

    public String getPin() {
        return Pin;
    }

    public void setPin(String pin) {
        Pin = pin;
    }

    public Date getExpiry() {
        return Expiry;
    }

    public void setExpiry(Date expiry) {
        this.Expiry = expiry;
    }

    public int getAU() {
        return AU;
    }

    public void setAU(int AU) {
        this.AU = AU;
    }

    public int getLockId() {
        return LockId;
    }

    public void setLockId(int lockId) {
        LockId = lockId;
    }
}
