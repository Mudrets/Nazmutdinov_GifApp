package com.example.gifapp.viewmodel

import androidx.lifecycle.LiveData
import com.example.gifapp.state.GifState

interface GifViewModel {
    val state: LiveData<GifState>
    fun nextGif()
    fun prevGif()
    fun initialize()
    fun refresh()
}