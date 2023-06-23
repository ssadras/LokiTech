package com.example.lokitech;

public class LockListReqAPI {
    private int DeviceId;
    private int UserId;
    private String LoginHash;

    public LockListReqAPI(int deviceId, int userId, String loginHash) {
        DeviceId = deviceId;
        UserId = userId;
        LoginHash = loginHash;
    }

    public int getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(int deviceId) {
        DeviceId = deviceId;
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
