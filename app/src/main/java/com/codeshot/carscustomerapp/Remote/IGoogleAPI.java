package com.codeshot.carscustomerapp.Remote;

import com.codeshot.carscustomerapp.Models.MyPlaces;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleAPI {

    @GET
    Call<String> getPath(@Url String url);
    @GET
    Call<JsonObject> getPaths(@Url String url);

    @GET
    Call<MyPlaces> getPlaces(@Url String url);
}
