package com.example.gifapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gifapp.fragment.PageInfo
import com.example.gifapp.fragment.PageSection
import com.example.gifapp.model.Gif
import com.example.gifapp.model.ResponseWrapper
import com.example.gifapp.repository.Repository
import com.example.gifapp.state.GifState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GifFromSectionViewModel(
    private val repository: Repository,
    private val pageSection: PageSection
) : ViewModel(), GifViewModel {

    override val state: LiveData<GifState>
        get() = _state

    private var lastWasFail = false
    private var currGif: Gif? = null
    private val prevGifs = mutableListOf<Gif>()
    private var index = -1
    private var nextPage = 0
    private val _state = MutableLiveData<GifState>()

    override fun nextGif() {
        if (!hasNext())
            loadGifs()
        else {
            currGif = prevGifs[++index]
            setNormalState()
        }
    }

    override fun prevGif() {
        if (hasPrev()) {
            val i = if(lastWasFail) index else --index
            currGif = prevGifs[i]
            lastWasFail = false
            setNormalState()
        }
    }

    override fun initialize() {
        if (currGif != null)
            _state.postValue(GifState.SuccessState(currGif, hasPrev()))
        else
            nextGif()
    }

    override fun refresh() {
        if (currGif == null)
            loadGifs()
        else
            setNormalState()
    }

    private fun loadGifs() {
        _state.postValue(GifState.LoadState)
        val response = repository.getGif(pageSection.value, nextPage)

        response.enqueue(object : Callback<ResponseWrapper> {
            override fun onResponse(
                call: Call<ResponseWrapper>,
                response: Response<ResponseWrapper>
            ) {
                val responseWrapper = response.body()
                if (response.code() == 200 && responseWrapper != null &&
                    !responseWrapper.result.isNullOrEmpty()
                ) {
                    prevGifs.addAll(responseWrapper.result)
                    lastWasFail = false
                    currGif = prevGifs[++index]
                    nextPage++
                    setNormalState()
                } else if(responseWrapper != null && responseWrapper.result.isNullOrEmpty()) {
                    onFail("Gif изображений в этой категории нет")
                } else
                    onFail()
            }

            override fun onFailure(call: Call<ResponseWrapper>, t: Throwable) {
                currGif = null
                lastWasFail = true
                onFail(t.message ?: "Возникла ошибка")
            }
        })
    }

    private fun onFail(msg: String = "Возникла ошибка") {
        _state.postValue(GifState.ErrorState(msg))
    }

    private fun setNormalState() = when {
        index >= 0 -> _state.postValue(GifState.SuccessState(currGif, hasPrev()))
        else -> _state.postValue(GifState.ErrorState("Какая-то проблема"))
    }

    private fun hasPrev() = index > 0 && prevGifs.isNotEmpty()

    private fun hasNext() = index < prevGifs.size - 1
}
