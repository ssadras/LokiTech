package com.example.lokitech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LockLogActivity extends AppCompatActivity {

    public static final String API_URL = "https://shacks.pythonanywhere.com/";
    public static final String PREFERENCES_LABEL = "global_vars";
    public static final String PREFERENCES_USER_ID_TAG = "user_id";
    public static final String PREFERENCES_DEVICE_HASH_TAG = "device_hash";
    public static final String PREFERENCES_DEVICE_ID_TAG = "device_id";
    public static final String PREFERENCES_DEVICE_PATTERN_TAG = "device_pattern";

    Lock lock;
    ArrayList<Log> logs;
    LogListRecAdapter adapter;
    RecyclerView recyclerView;
    Intent intent_rc;
    TextView subtitle_tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_log);

        recyclerView = findViewById(R.id.log_list_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(LockLogActivity.this));
        subtitle_tx = findViewById(R.id.log_list_subtitle);

        intent_rc = getIntent();
        lock = new Lock(intent_rc.getIntExtra("lock_id", -1),
                intent_rc.getStringExtra("lock_name"),
                intent_rc.getBooleanExtra("lock_active", false)
        );

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_LABEL, 0);
        int userId = sharedPreferences.getInt(PREFERENCES_USER_ID_TAG, -1);
        int deviceId = sharedPreferences.getInt(PREFERENCES_DEVICE_ID_TAG, -1);
        String DeviceHash = sharedPreferences.getString(PREFERENCES_DEVICE_HASH_TAG, "");
        String DevicePattern = sharedPreferences.getString(PREFERENCES_DEVICE_PATTERN_TAG, "");

        if (DeviceHash.equals("") || userId == -1 || deviceId == -1) {
            Intent intent = new Intent(LockLogActivity.this, MainActivity.class);
            this.startActivity(intent);
            return;
        }

        subtitle_tx.setText(lock.getName());

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ServerAPI serverAPI = retrofit.create(ServerAPI.class);

        LogListReqAPI logListReqAPI = new LogListReqAPI(userId,deviceId,DeviceHash,lock.getLockId());

        Call<LogListResAPI> call = serverAPI.logListMethod(logListReqAPI);

        call.enqueue(new Callback<LogListResAPI>() {
            @Override
            public void onResponse(Call<LogListResAPI> call, Response<LogListResAPI> response) {
                if (!response.isSuccessful()){
                    Toast.makeText(LockLogActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.code() == 200) {
                    LogListResAPI logListResAPI = response.body();

                    if (logListResAPI != null){
                        logs = logListResAPI.getStatuses();

                        adapter = new LogListRecAdapter(logs, LockLogActivity.this);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(LockLogActivity.this, "Connection not successful!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LogListResAPI> call, Throwable t) {
                Toast.makeText(LockLogActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}