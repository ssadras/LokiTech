package com.example.lokitech;

import java.util.Date;

public class PinSetReqAPI {
    private Date ValidTime;
    private int Uses;
    private int UserId;
    private int DeviceId;
    private String LoginHash;
    private int LockId;

    public PinSetReqAPI(Date validTime, int uses, int userId, int deviceId, String loginHash, int lockId) {
        ValidTime = validTime;
        Uses = uses;
        UserId = userId;
        DeviceId = deviceId;
        LoginHash = loginHash;
        LockId = lockId;
    }

    public Date getValidTime() {
        return ValidTime;
    }

    public void setValidTime(Date validTime) {
        ValidTime = validTime;
    }

    public int getUses() {
        return Uses;
    }

    public void setUses(int uses) {
        Uses = uses;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public int getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(int deviceId) {
        DeviceId = deviceId;
    }

    public String getLoginHash() {
        return LoginHash;
    }

    public void setLoginHash(String loginHash) {
        LoginHash = loginHash;
    }

    public int getLockId() {
        return LockId;
    }

    public void setLockId(int lockId) {
        LockId = lockId;
    }
}
