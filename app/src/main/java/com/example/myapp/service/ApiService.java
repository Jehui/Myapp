package com.example.myapp.service;


import io.reactivex.Observable;
import retrofit2.http.GET;
public interface ApiService {

    @GET("wangbuer1/Myapp/master/app/release/output.json")
    Observable<String> getVersionInfo();

}
