package com.example.gifapp.repository

import com.example.gifapp.api.RetrofitInstance
import com.example.gifapp.model.Gif
import retrofit2.Response

class Repository {

    fun getGif() = RetrofitInstance.api.getRandomGif()

    fun getGif(category: String, page: Int) =
        RetrofitInstance.api.getGif(category, page)
}