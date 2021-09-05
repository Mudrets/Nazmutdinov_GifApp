package com.example.gifapp.api

import com.example.gifapp.model.Gif
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface Api {

    @GET("/random?json=true")
    fun getRandomGif(): Call<Gif>

    @GET("/{category}/{page}?json=true")
    suspend fun getGif(category: String, page: Int): Call<Gif>

}