package com.example.lokitech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LockActivity extends AppCompatActivity {

    public static final String API_URL = "https://shacks.pythonanywhere.com/";
    public static final String PREFERENCES_LABEL = "global_vars";
    public static final String PREFERENCES_USER_ID_TAG = "user_id";
    public static final String PREFERENCES_DEVICE_HASH_TAG = "device_hash";
    public static final String PREFERENCES_DEVICE_ID_TAG = "device_id";

    private TextView pin_tx, expiry_tx, au_tx, active_tx, title_tx;
    private Button newPin_bu, log_bu;
    private Lock lock;
    private Pin pin;
    Intent intent_rc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        pin_tx = findViewById(R.id.lock_pin_pin);
        expiry_tx = findViewById(R.id.set_pin_expiry_but);
        au_tx = findViewById(R.id.set_pin_au);
        active_tx = findViewById(R.id.lock_active);
        title_tx = findViewById(R.id.set_pin_title);
        newPin_bu = findViewById(R.id.set_pin_button);
        log_bu = findViewById(R.id.lock_log_button);

        intent_rc = getIntent();
        lock = new Lock(intent_rc.getIntExtra("lock_id", -1),
                intent_rc.getStringExtra("lock_name"),
                intent_rc.getBooleanExtra("lock_active", false)
        );

        String active = "Not active";
        int color = R.color.red;
        if (lock.isActive()) {
            active = "Active";
            color = R.color.green;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_LABEL, 0);
        int userId = sharedPreferences.getInt(PREFERENCES_USER_ID_TAG, -1);
        int deviceId = sharedPreferences.getInt(PREFERENCES_DEVICE_ID_TAG, -1);
        String DeviceHash = sharedPreferences.getString(PREFERENCES_DEVICE_HASH_TAG, "");
        Log.wtf("Lock", DeviceHash);

        if (DeviceHash.equals("") || userId == -1 || deviceId == -1) {
            Intent intent = new Intent(LockActivity.this, MainActivity.class);
            this.startActivity(intent);
            return;
        }

        active_tx.setText(active);
        active_tx.setTextColor(ContextCompat.getColor(LockActivity.this, color));
        title_tx.setText(lock.getName());

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ServerAPI serverAPI = retrofit.create(ServerAPI.class);

        PinGetReqAPI pinGetReqAPI = new PinGetReqAPI(lock.getLockId(), userId, deviceId, DeviceHash);

        Call<PinGetResAPI> call = serverAPI.pinGetMethod(pinGetReqAPI);

        call.enqueue(new Callback<PinGetResAPI>() {
            @Override
            public void onResponse(Call<PinGetResAPI> call, Response<PinGetResAPI> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(LockActivity.this, "Not successful or No last pin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.code() == 200) {
                    PinGetResAPI pinGetResAPI = response.body();

                    if (pinGetResAPI != null) {
                        String pattern = "MMM dd, yyyy";
                        DateFormat dateFormat = new SimpleDateFormat(pattern);

                        pin = new Pin(pinGetResAPI.getPinId(), pinGetResAPI.getPin(), pinGetResAPI.getAU(), pinGetResAPI.getExpiry());

                        pin_tx.setText(pin.getPin());
                        expiry_tx.setText(dateFormat.format(pin.getExpiry()));
                        au_tx.setText(String.valueOf(pin.getAvailableUses()));
                    }
                } else {
                    Toast.makeText(LockActivity.this, "Connection not successful!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PinGetResAPI> call, Throwable t) {
                Toast.makeText(LockActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        String pattern = "MMM dd, yyyy";
        DateFormat dateFormat = new SimpleDateFormat(pattern);

        if (pin == null) {
            pin_tx.setText(R.string.lock_no_history);
            expiry_tx.setText(R.string.lock_no_history);
            au_tx.setText(R.string.lock_no_history);
        } else {
            pin_tx.setText(pin.getPin());
            expiry_tx.setText(dateFormat.format(pin.getExpiry()));
            au_tx.setText(String.valueOf(pin.getAvailableUses()));
        }

        newPin_bu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LockActivity.this, SetNewPinActivity.class);
                intent.putExtra("lock_name", lock.getName());
                intent.putExtra("lock_id", lock.getLockId());
                intent.putExtra("lock_active", lock.isActive());
                LockActivity.this.startActivity(intent);
            }
        });

        log_bu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LockActivity.this, LockLogActivity.class);
                intent.putExtra("lock_name", lock.getName());
                intent.putExtra("lock_id", lock.getLockId());
                intent.putExtra("lock_active", lock.isActive());
                LockActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LockActivity.this, LockListActivity.class));
        finish();
    }
}