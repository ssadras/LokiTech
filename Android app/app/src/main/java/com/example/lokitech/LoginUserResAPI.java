package com.example.lokitech;

public class LoginUserResAPI {
    private int UserId;
    private String LoginHash;
    private int DeviceId;

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
        this.LoginHash = loginHash;
    }
}
