package com.example.lokitech;

import java.util.Date;

public class Pin {
    private int PinId;
    private String Pin;
    private int AvailableUses;
    private Date Expiry;

    public Pin(int PinId, String Pin, int AvailableUses, Date expiry) {
        this.PinId = PinId;
        this.Pin = Pin;
        this.AvailableUses = AvailableUses;
        this.Expiry = expiry;
    }

    public int getPinId() {
        return PinId;
    }

    public void setPinId(int pinId) {
        this.PinId = pinId;
    }

    public String getPin() {
        return Pin;
    }

    public void setPin(String pin) {
        this.Pin = pin;
    }

    public int getAvailableUses() {
        return AvailableUses;
    }

    public void setAvailableUses(int availableUses) {
        this.AvailableUses = availableUses;
    }

    public Date getExpiry() {
        return Expiry;
    }

    public void setExpiry(Date expiry) {
        this.Expiry = expiry;
    }
}
