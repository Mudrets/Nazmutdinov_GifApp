package com.example.gifapp.viewmodel

import androidx.lifecycle.LiveData

interface GifViewModel {
    val state: LiveData<GifState>
    fun nextGif()
    fun prevGif()
    fun initialize()
    fun refresh()
}