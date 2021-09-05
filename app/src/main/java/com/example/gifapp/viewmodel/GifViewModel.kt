package com.example.gifapp.viewmodel

import androidx.lifecycle.LiveData
import com.example.gifapp.state.GifState

/**
 * интерфейс для ViewModel получающего [Gif]-изображения
 */
interface GifViewModel {
    /**
     * Неизменяемая LiveData для состояния загрузки
     */
    val state: LiveData<GifState>

    /**
     * Метод устанавливающий следующее GIF-изображение
     */
    fun nextGif()

    /**
     * Метод устанавливающий пердыдущее GIF-изображение
     */
    fun prevGif()

    /**
     * Инициализирует GIF изображение
     */
    fun initialize()

    /**
     * Повторяет попытку по получеию GIF-изображения
     */
    fun refresh()
}