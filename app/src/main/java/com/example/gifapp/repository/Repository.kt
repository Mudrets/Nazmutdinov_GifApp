package com.example.gifapp.repository

import com.example.gifapp.api.RetrofitInstance
import com.example.gifapp.model.Gif
import retrofit2.Response

/**
 * Репозиторий для получения [Gif]-изображений
 */
class Repository {

    /**
     * Возвращает рандомную [Gif]
     */
    fun getGif() = RetrofitInstance.api.getRandomGif()

    /**
     * Возвращает список [Gif] категории [category] со страницы [page]
     *
     * @param category название категории
     * @param page номер страницы
     */
    fun getGif(category: String, page: Int) =
        RetrofitInstance.api.getGif(category, page)
}