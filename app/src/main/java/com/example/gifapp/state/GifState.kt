package com.example.gifapp.state

import com.example.gifapp.model.Gif

/**
 * Состояние загрузки [Gif]-изображения
 */
sealed class GifState {
    /**
     * Состояние загрузки
     */
    object LoadState : GifState()

    /**
     * Состояние успешного получения [Gif]-изображения
     *
     * @param gif возвращаемое [Gif]-изображение
     * @param hasPrev есть ли предыдущее [Gif]-изображение
     */
    class SuccessState(val gif: Gif?, val hasPrev: Boolean) : GifState()

    /**
     * Состояние ошибки
     *
     * @param msg сообщение об ошибке
     * @param hasPrev есть ли предыдущее [Gif]-изображение
     */
    class ErrorState<T>(val msg: T, val hasPrev: Boolean = true) : GifState()
}