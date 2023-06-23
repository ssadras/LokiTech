package com.example.lokitech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static final String API_URL = "https://shacks.pythonanywhere.com/";
    public static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_=@#$!";
    public static final String PREFERENCES_LABEL = "global_vars";
    public static final String PREFERENCES_USER_ID_TAG = "user_id";
    public static final String PREFERENCES_DEVICE_HASH_TAG = "device_hash";
    public static final String PREFERENCES_DEVICE_ID_TAG = "device_id";
    public static final String PREFERENCES_DEVICE_PATTERN_TAG = "device_pattern";

    EditText emailField, passField;
    Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailField = findViewById(R.id.register_email_field);
        passField = findViewById(R.id.register_pass_field);
        loginButton = findViewById(R.id.set_pin_button);
        registerButton = findViewById(R.id.login_register_button);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_LABEL, 0);
        int userId = sharedPreferences.getInt(PREFERENCES_USER_ID_TAG, -1);
        int deviceId = sharedPreferences.getInt(PREFERENCES_DEVICE_ID_TAG, -1);
        String DeviceHash = sharedPreferences.getString(PREFERENCES_DEVICE_HASH_TAG, "");
        String DevicePattern = sharedPreferences.getString(PREFERENCES_DEVICE_PATTERN_TAG, "");

        if (!DeviceHash.equals("") && userId != -1 && deviceId != -1){
            Intent intent = new Intent(MainActivity.this, LockListActivity.class);
            this.startActivity(intent);
            return;
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString();
                String pass = passField.getText().toString();
                String devicePattern = getRandomString(12);

                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                ServerAPI serverAPI = retrofit.create(ServerAPI.class);

                LoginUserReqAPI loginUserReqAPI = new LoginUserReqAPI(email, pass, devicePattern);

                Call<LoginUserResAPI> call = serverAPI.loginMethod(loginUserReqAPI);

                call.enqueue(new Callback<LoginUserResAPI>() {
                    @Override
                    public void onResponse(Call<LoginUserResAPI> call, Response<LoginUserResAPI> response) {
                        Log.w("MainActivity", "onResponse started");
                        if (!response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                            Log.w("MainActivity", "Not successful");
                            return;
                        }

                        if (response.code() == 200) {
                            Log.w("MainActivity", "Code 200 received");
                            LoginUserResAPI loginUserResAPI = response.body();

                            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_LABEL, 0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            assert loginUserResAPI != null;
                            editor.putInt(PREFERENCES_USER_ID_TAG, loginUserResAPI.getUserId());
                            editor.putString(PREFERENCES_DEVICE_HASH_TAG, loginUserResAPI.getLoginHash());
                            editor.putInt(PREFERENCES_DEVICE_ID_TAG, loginUserResAPI.getDeviceId());
                            editor.putString(PREFERENCES_DEVICE_PATTERN_TAG, devicePattern);

                            editor.apply();

                            Log.w("MainActivity", "Data added to SharedPrefs");

                            Intent intent = new Intent(MainActivity.this, LockListActivity.class);
                            MainActivity.this.startActivity(intent);
                            Log.w("MainActivity", "onResponse Exit");
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginUserResAPI> call, Throwable t) {
                        Log.w("MainActivity", "onFailure started");
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent = new Intent(MainActivity.this, RegisterActivity.class);

                String email = emailField.getText().toString();

                regIntent.putExtra("login_email_text", email);
                MainActivity.this.startActivity(regIntent);
            }
        });
    }

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}