package com.example.gifapp.api

import com.example.gifapp.model.Gif
import com.example.gifapp.model.ResponseWrapper
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {

    @GET("/random?json=true")
    fun getRandomGif(): Call<Gif>

    @GET("/{category}/{page}?json=true")
    fun getGif(
        @Path("category") category: String,
        @Path("page") page: Int
    ): Call<ResponseWrapper>

}