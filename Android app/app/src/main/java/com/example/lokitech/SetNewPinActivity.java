package com.example.lokitech;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SetNewPinActivity extends AppCompatActivity {

    public static final String API_URL = "https://shacks.pythonanywhere.com/";
    public static final String PREFERENCES_LABEL = "global_vars";
    public static final String PREFERENCES_USER_ID_TAG = "user_id";
    public static final String PREFERENCES_DEVICE_HASH_TAG = "device_hash";
    public static final String PREFERENCES_DEVICE_ID_TAG = "device_id";
    public static final String PREFERENCES_DEVICE_PATTERN_TAG = "device_pattern";

    NumberPicker au_np;
    Button date_but, submit_but;
    int year_picked, month_picked, day_picked;
    Lock lock;

    Intent intent_rc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_pin);

        date_but = findViewById(R.id.set_pin_expiry_but);
        au_np = findViewById(R.id.set_pin_au_picker);
        submit_but = findViewById(R.id.set_pin_button);

        au_np.setMaxValue(50);
        au_np.setMinValue(1);

        intent_rc = getIntent();
        lock = new Lock(intent_rc.getIntExtra("lock_id", -1),
                intent_rc.getStringExtra("lock_name"),
                intent_rc.getBooleanExtra("lock_active", false)
        );

        if (lock.getLockId() == -1) {
            Intent intent = new Intent(SetNewPinActivity.this, LockListActivity.class);
            this.startActivity(intent);
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_LABEL, 0);
        int userId = sharedPreferences.getInt(PREFERENCES_USER_ID_TAG, -1);
        int deviceId = sharedPreferences.getInt(PREFERENCES_DEVICE_ID_TAG, -1);
        String DeviceHash = sharedPreferences.getString(PREFERENCES_DEVICE_HASH_TAG, "");
        String DevicePattern = sharedPreferences.getString(PREFERENCES_DEVICE_PATTERN_TAG, "");

        if (DeviceHash.equals("") || userId == -1 || deviceId == -1 || DevicePattern.equals("")) {
            Intent intent = new Intent(SetNewPinActivity.this, MainActivity.class);
            this.startActivity(intent);
            return;
        }

        date_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();

                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        SetNewPinActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                year_picked = year;
                                month_picked = monthOfYear;
                                day_picked = dayOfMonth;
                            }
                        },
                        year, month, day);

                datePickerDialog.show();
            }
        });

        submit_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (year_picked == 0 || month_picked == 0 || day_picked == 0)
                    return;

                if (au_np.getValue() < au_np.getMinValue() || au_np.getValue() > au_np.getMaxValue())
                    return;

                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                ServerAPI serverAPI = retrofit.create(ServerAPI.class);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year_picked);
                cal.set(Calendar.MONTH, month_picked);
                cal.set(Calendar.DAY_OF_MONTH, day_picked);
                Date dateRepresentation = cal.getTime();

                PinSetReqAPI pinSetReqAPI = new PinSetReqAPI(dateRepresentation, au_np.getValue(),
                        userId, deviceId, DeviceHash, lock.getLockId());

                Call<PinSetResAPI> call = serverAPI.pinSetMethod(pinSetReqAPI);

                call.enqueue(new Callback<PinSetResAPI>() {
                    @Override
                    public void onResponse(Call<PinSetResAPI> call, Response<PinSetResAPI> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(SetNewPinActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (response.code() == 200) {
                            PinSetResAPI pinSetResAPI = response.body();

                            if (pinSetResAPI == null){
                                Toast.makeText(SetNewPinActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (!pinSetResAPI.getPattern().equals(DevicePattern)){
                                Toast.makeText(SetNewPinActivity.this, "Setting pin security compromised", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Intent intent = new Intent(SetNewPinActivity.this, LockActivity.class);
                            intent.putExtra("lock_name", lock.getName());
                            intent.putExtra("lock_id", lock.getLockId());
                            intent.putExtra("lock_active", lock.isActive());
                            SetNewPinActivity.this.startActivity(intent);
                        }

                    }

                    @Override
                    public void onFailure(Call<PinSetResAPI> call, Throwable t) {
                        Toast.makeText(SetNewPinActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }
}