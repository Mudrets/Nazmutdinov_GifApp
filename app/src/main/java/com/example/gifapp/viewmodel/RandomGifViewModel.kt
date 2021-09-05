package com.example.gifapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gifapp.R
import com.example.gifapp.model.Gif
import com.example.gifapp.repository.Repository
import com.example.gifapp.state.GifState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RandomGifViewModel(private val repository: Repository) : ViewModel(), GifViewModel {

    override val state: LiveData<GifState>
        get() = _state

    private var lastWasFail = false
    private var currGif: Gif? = null
    private val prevGifs = mutableListOf<Gif>()
    private var index = -1
    private val _state = MutableLiveData<GifState>()

    private fun successLoad(response: Response<Gif>) {
        Log.d("Retrofit", response.toString())
        lastWasFail = false
        index++
        currGif = response.body()!!
        currGif?.let { prevGifs.add(it) }
        setNormalState()
    }

    private fun onResponse(response: Response<Gif>) {
        if (response.code() == 200)
            successLoad(response)
        else
            onFail(R.string.unknown_error)
    }

    private fun onFailure(t: Throwable) {
        Log.d("Retrofit", t.message ?: "Connection error")
        lastWasFail = true
        currGif = null
        onFail(R.string.connection_error)
    }

    private fun loadGif() {
        _state.postValue(GifState.LoadState)
        val response = repository.getGif()

        response.enqueue(object : Callback<Gif> {
            override fun onResponse(call: Call<Gif>, response: Response<Gif>) = onResponse(response)

            override fun onFailure(call: Call<Gif>, t: Throwable) = onFailure(t)
        })
    }

    override fun initialize() {
        if (currGif != null)
            _state.postValue(GifState.SuccessState(currGif, hasPrev()))
        else
            nextGif()
    }

    override fun nextGif() {
        if (!hasNext()) {
            loadGif()
        } else {
            currGif = prevGifs[++index]
            setNormalState()
        }
    }

    override fun prevGif() {
        if (hasPrev()) {
            val i = if (lastWasFail) index else --index
            currGif = prevGifs[i]
            lastWasFail = false
            setNormalState()
        }
    }

    override fun refresh() {
        if (currGif == null)
            loadGif()
        else
            setNormalState()
    }

    private fun hasPrev() = index > 0 && prevGifs.isNotEmpty()

    private fun hasNext() = index < prevGifs.size - 1

    private fun setNormalState() = when {
        index >= 0 -> _state.postValue(GifState.SuccessState(currGif, hasPrev()))
        else -> onFail(R.string.unknown_error)
    }

    private fun onFail(msg: Int) {
        _state.postValue(GifState.ErrorState(msg, hasPrev()))
    }

}