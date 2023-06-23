package com.example.lokitech;

public class Lock {
    private int LockId;
    private String Name;
    private boolean Active;
    private Pin last_pin;

    public Lock(int LockId, String name, boolean active) {
        this.LockId = LockId;
        this.Name = name;
        this.Active = active;
        this.last_pin = null;
    }

    public int getLockId() {
        return LockId;
    }

    public void setLockId(int lockId) {
        this.LockId = lockId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        this.Active = active;
    }

    public Pin getLast_pin() {
        return last_pin;
    }

    public void setLast_pin(Pin last_pin) {
        this.last_pin = last_pin;
    }
}
