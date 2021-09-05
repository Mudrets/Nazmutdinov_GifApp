package com.example.gifapp.state

import com.example.gifapp.model.Gif

sealed class GifState {
    object LoadState : GifState()
    class SuccessState(val gif: Gif?, val hasPrev: Boolean) : GifState()
    class ErrorState<T>(val msg: T, val hasPrev: Boolean = true) : GifState()
}