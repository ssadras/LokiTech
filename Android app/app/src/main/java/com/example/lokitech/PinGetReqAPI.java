package com.example.lokitech;

public class PinGetReqAPI {
    private int LockId;
    private int UserId;
    private int DeviceId;
    private String LoginHash;

    public PinGetReqAPI(int lockId, int userId, int deviceId, String loginHash) {
        LockId = lockId;
        UserId = userId;
        DeviceId = deviceId;
        LoginHash = loginHash;
    }

    public int getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(int deviceId) {
        DeviceId = deviceId;
    }

    public int getLockId() {
        return LockId;
    }

    public void setLockId(int lockId) {
        LockId = lockId;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public String getLoginHash() {
        return LoginHash;
    }

    public void setLoginHash(String loginHash) {
        LoginHash = loginHash;
    }
}
