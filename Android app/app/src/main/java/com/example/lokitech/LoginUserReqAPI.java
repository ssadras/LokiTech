package com.example.lokitech;

public class LoginUserReqAPI {
    private String UserEmail;
    private String Pass;
    private String Pattern;

    public LoginUserReqAPI(String userEmail, String pass, String pattern) {
        UserEmail = userEmail;
        Pass = pass;
        Pattern = pattern;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public void setUserEmail(String userEmail) {
        this.UserEmail = userEmail;
    }

    public String getPass() {
        return Pass;
    }

    public void setPass(String pass) {
        this.Pass = pass;
    }

    public String getPattern() {
        return Pattern;
    }

    public void setPattern(String pattern) {
        Pattern = pattern;
    }
}
