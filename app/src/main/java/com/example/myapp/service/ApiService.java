package com.example.myapp.service;

import com.example.myapp.entity.User;
import io.reactivex.Observable;
import jsc.kit.retrofit2.response.BaseResponse;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("user/{userId}")
    Observable<BaseResponse<User>> login(@Path("userId") String userId);

    @GET("JustinRoom/JSCKit/master/capture/output.json")
    Observable<String> getVersionInfo();

}
