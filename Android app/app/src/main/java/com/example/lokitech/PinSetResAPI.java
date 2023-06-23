package com.example.lokitech;

public class PinSetResAPI {
    private String Pin;
    private String Pattern;

    public PinSetResAPI(String pin, String pattern) {
        Pin = pin;
        Pattern = pattern;
    }

    public String getPin() {
        return Pin;
    }

    public void setPin(String pin) {
        Pin = pin;
    }

    public String getPattern() {
        return Pattern;
    }

    public void setPattern(String pattern) {
        Pattern = pattern;
    }
}
