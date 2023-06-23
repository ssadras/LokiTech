package com.example.lokitech;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServerAPI {

    @POST("user_register")
    Call<RegisterUserResAPI> registerMethod(@Body RegisterUserReqAPI registerUserReqAPI);

    @POST("user_login")
    Call<LoginUserResAPI> loginMethod(@Body LoginUserReqAPI loginUserReqAPI);

    @POST("list_of_locks")
    Call<LockListResAPI> lockListMethod(@Body LockListReqAPI lockListReqAPI);

    @POST("get_last_pin")
    Call<PinGetResAPI> pinGetMethod(@Body PinGetReqAPI pinGetReqAPI);

    @POST("set_pin")
    Call<PinSetResAPI> pinSetMethod(@Body PinSetReqAPI pinSetReqAPI);

    @POST("get_proc_status")
    Call<LogListResAPI> logListMethod(@Body LogListReqAPI logListReqAPI);
}
