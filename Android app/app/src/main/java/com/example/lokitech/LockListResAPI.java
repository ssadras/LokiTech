package com.example.lokitech;

import java.util.ArrayList;

public class LockListResAPI {
    private ArrayList<Lock> Locks;
    private int Status;

    public LockListResAPI(ArrayList<Lock> locks, int status) {
        Locks = locks;
        Status = status;
    }

    public ArrayList<Lock> getLocks() {
        return Locks;
    }

    public void setLocks(ArrayList<Lock> locks) {
        Locks = locks;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }
}
