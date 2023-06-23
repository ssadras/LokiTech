package com.example.lokitech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {

    public static final String API_URL = "https://shacks.pythonanywhere.com/";
    public static final String PREFERENCES_LABEL = "global_vars";

    EditText fullNameField, emailField, passField, rePassField;
    Button submitBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullNameField = findViewById(R.id.register_name_field);
        emailField = findViewById(R.id.register_email_field);
        passField = findViewById(R.id.register_pass_field);
        rePassField = findViewById(R.id.register_repass_field);
        submitBut = findViewById(R.id.set_pin_button);

        submitBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = fullNameField.getText().toString();
                String pass = passField.getText().toString();
                String rePass = rePassField.getText().toString();
                String email = emailField.getText().toString();

                if (!pass.equals(rePass)){
                    rePassField.setError("The passwords do not match!");
                    return;
                }

                if (!email.contains("@")){
                    emailField.setError("The email is not valid!");
                    return;
                }

                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                ServerAPI serverAPI = retrofit.create(ServerAPI.class);

                RegisterUserReqAPI registerUserReqAPI = new RegisterUserReqAPI(fullName, email, pass);

                Call<RegisterUserResAPI> call=serverAPI.registerMethod(registerUserReqAPI);

                call.enqueue(new Callback<RegisterUserResAPI>() {
                    @Override
                    public void onResponse(Call<RegisterUserResAPI> call, Response<RegisterUserResAPI> response) {
                        if (!response.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (response.code() == 200) {
                            RegisterUserResAPI registerUserResAPI = response.body();

                            if (registerUserResAPI != null && registerUserResAPI.getStatus() == 1){
                                Toast.makeText(RegisterActivity.this, "Successfully registered. Please log in.", Toast.LENGTH_SHORT).show();

                                Intent logIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                logIntent.putExtra("login_email_text", email);
                                RegisterActivity.this.startActivity(logIntent);
                                return;
                            }

                            Toast.makeText(RegisterActivity.this, "Registration was not successful!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterUserResAPI> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }

    private String sha256String(String source) {
        byte[] hash = null;
        String hashCode = null;// w  ww  .  j  a va 2 s.c  o m
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(source.getBytes());
        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(RegisterActivity.this, "Hashing not succesful!", Toast.LENGTH_SHORT).show();
        }

        if (hash != null) {
            StringBuilder hashBuilder = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(b);
                if (hex.length() == 1) {
                    hashBuilder.append("0");
                    hashBuilder.append(hex.charAt(0));
                } else {
                    hashBuilder.append(hex.substring(hex.length() - 2));
                }
            }
            hashCode = hashBuilder.toString();
        }

        return hashCode;
    }
}