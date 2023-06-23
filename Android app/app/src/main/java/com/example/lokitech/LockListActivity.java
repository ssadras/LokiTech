package com.example.lokitech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LockListActivity extends AppCompatActivity {

    public static final String API_URL = "https://shacks.pythonanywhere.com/";
    public static final String PREFERENCES_LABEL = "global_vars";
    public static final String PREFERENCES_USER_ID_TAG = "user_id";
    public static final String PREFERENCES_DEVICE_HASH_TAG = "device_hash";
    public static final String PREFERENCES_DEVICE_ID_TAG = "device_id";
    public static final String PREFERENCES_DEVICE_PATTERN_TAG = "device_pattern";

    ArrayList<Lock> locks;
    LockListRecAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_list);

        recyclerView = findViewById(R.id.lock_list_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(LockListActivity.this));

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_LABEL, 0);
        int userId = sharedPreferences.getInt(PREFERENCES_USER_ID_TAG, -1);
        int deviceId = sharedPreferences.getInt(PREFERENCES_DEVICE_ID_TAG, -1);
        String DeviceHash = sharedPreferences.getString(PREFERENCES_DEVICE_HASH_TAG, "");
        String DevicePattern = sharedPreferences.getString(PREFERENCES_DEVICE_PATTERN_TAG, "");

        if (DeviceHash.equals("") || userId == -1 || deviceId == -1){
            Intent intent = new Intent(LockListActivity.this, MainActivity.class);
            LockListActivity.this.startActivity(intent);
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ServerAPI serverAPI = retrofit.create(ServerAPI.class);

        LockListReqAPI lockListReqAPI = new LockListReqAPI(deviceId, userId, DeviceHash);

        Call<LockListResAPI> call=serverAPI.lockListMethod(lockListReqAPI);

        call.enqueue(new Callback<LockListResAPI>() {
            @Override
            public void onResponse(Call<LockListResAPI> call, Response<LockListResAPI> response) {
                if (!response.isSuccessful()){
                    Toast.makeText(LockListActivity.this, "Not successful", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.code() == 200) {
                    LockListResAPI lockListResAPI = response.body();

                    if (lockListResAPI != null) {
                        locks = lockListResAPI.getLocks();

                        adapter = new LockListRecAdapter(locks, LockListActivity.this);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    Toast.makeText(LockListActivity.this, "Connection not successful!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LockListResAPI> call, Throwable t) {
                Toast.makeText(LockListActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.wtf("LockList",t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}