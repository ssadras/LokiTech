package com.example.lokitech;

import java.util.Date;

public class Log {
    private int Id;
    private String Status;
    private int LockId;
    private Date CreatedDate;

    public Log(int id, String status, int lockId, Date CreatedDate) {
        Id = id;
        Status = status;
        LockId = lockId;
        this.CreatedDate = CreatedDate;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public int getLockId() {
        return LockId;
    }

    public void setLockId(int lockId) {
        LockId = lockId;
    }

    public Date getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.CreatedDate = createdDate;
    }
}
