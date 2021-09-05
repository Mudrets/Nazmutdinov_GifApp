package com.example.gifapp.api

import com.example.gifapp.model.Gif
import com.example.gifapp.model.ResponseWrapper
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Интерфейс для взаимодействия с сервером
 */
interface Api {

    /**
     * Метод получения случайного [Gif]-изображения
     */
    @GET("/random?json=true")
    fun getRandomGif(): Call<Gif>

    /**
     * Метод для получения [Gif]-изображений из категории
     * [category] на странице номер [page]
     *
     * @param category категория [Gif]-изображений
     * @param page номер страницы
     */
    @GET("/{category}/{page}?json=true")
    fun getGif(
        @Path("category") category: String,
        @Path("page") page: Int
    ): Call<ResponseWrapper>

}