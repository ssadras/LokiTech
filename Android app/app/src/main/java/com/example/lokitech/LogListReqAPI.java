package com.example.lokitech;

public class LogListReqAPI {
    private int UserId;
    private int DeviceId;
    private String LoginHash;
    private int LockId;

    public LogListReqAPI(int userId, int deviceId, String loginHash, int lockId) {
        UserId = userId;
        DeviceId = deviceId;
        LoginHash = loginHash;
        LockId = lockId;
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
