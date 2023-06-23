package com.example.lokitech;

public class RegisterUserReqAPI {
    private String Username;
    private String Email;
    private String Pass;

    public RegisterUserReqAPI(String Username, String email, String Pass) {
        this.Username = Username;
        this.Email = email;
        this.Pass = Pass;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        this.Username = username;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getPass() {
        return Pass;
    }

    public void setPass(String pass) {
        this.Pass = pass;
    }
}

