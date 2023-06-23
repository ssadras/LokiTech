package com.example.lokitech;

import java.util.ArrayList;

public class LogListResAPI {
    private int Status;
    private ArrayList<Log> Statuses;

    public LogListResAPI(int status, ArrayList<Log> statuses) {
        Status = status;
        Statuses = statuses;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public ArrayList<Log> getStatuses() {
        return Statuses;
    }

    public void setStatuses(ArrayList<Log> statuses) {
        Statuses = statuses;
    }
}
