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

/**
 * ViewModel реализацющий интефейс [GifViewModel]. Получает рандомное [Gif]-изображения
 * [Gif]-изображения хранятся в моделе [Gif]
 *
 * @param repository репозиторий для получения GIF-изображений
 */
class RandomGifViewModel(private val repository: Repository) : ViewModel(), GifViewModel {

    /**
     * Неизменяемая LiveData для состояния загрузки
     */
    override val state: LiveData<GifState>
        get() = _state

    /**
     * Была ли последняя попытка загрузить GIF-изображение удачной
     */
    private var lastWasFail = false

    /**
     * Текщее [Gif]-изображение
     */
    private var currGif: Gif? = null

    /**
     * Список уже загруженных [Gif]-изображений
     */
    private val prevGifs = mutableListOf<Gif>()

    /**
     * Индекс текущего [Gif]-изображения
     */
    private var index = -1

    /**
     * Состояние загрузки [GifState]
     */
    private val _state = MutableLiveData<GifState>()

    /**
     * Обработка успешного получения [Gif]-изображения
     */
    private fun successLoad(response: Response<Gif>) {
        Log.d("Retrofit", response.toString())
        lastWasFail = false
        index++
        currGif = response.body()!!
        currGif?.let { prevGifs.add(it) }
        setNormalState()
    }

    /**
     * Обрабатывает получение ответа
     */
    private fun onResponse(response: Response<Gif>) {
        if (response.code() == 200)
            successLoad(response)
        else
            onFail(R.string.unknown_error)
    }

    /**
     * Обрабатывает получение ошибки с сервера
     */
    private fun onFailure(t: Throwable) {
        Log.d("Retrofit", t.message ?: "Connection error")
        lastWasFail = true
        currGif = null
        onFail(R.string.connection_error)
    }

    /**
     * Отправляет запрос на сервер для получения [Gif]-изображение
     */
    private fun loadGif() {
        _state.postValue(GifState.LoadState)
        val response = repository.getGif()

        response.enqueue(object : Callback<Gif> {
            override fun onResponse(call: Call<Gif>, response: Response<Gif>) = onResponse(response)

            override fun onFailure(call: Call<Gif>, t: Throwable) = onFailure(t)
        })
    }

    /**
     * Устанавливает в [_state] [GifState.SuccessState] передавая в нем [currGif].
     * Если [currGif] == null, то загружает [Gif]-изображение с сервера
     */
    override fun initialize() {
        if (currGif != null)
            _state.postValue(GifState.SuccessState(currGif, hasPrev()))
        else
            nextGif()
    }

    /**
     * Если следующее [Gif]-изображение уже загружено, то устанавливает
     * в переменную [currGif] следующее [Gif]-изображение. Если же следующего
     * [Gif]-изображения еще нет, то загружает новое [Gif]-изображение с сервера
     * и сохраняет его в [currGif]
     */
    override fun nextGif() {
        if (!hasNext()) {
            loadGif()
        } else {
            currGif = prevGifs[++index]
            setNormalState()
        }
    }

    /**
     * Если есть предыдущее [Gif]-изображение, то устанавливает его в [currGif]
     */
    override fun prevGif() {
        if (hasPrev()) {
            val i = if (lastWasFail) index else --index
            currGif = prevGifs[i]
            lastWasFail = false
            setNormalState()
        }
    }

    /**
     * Повторно пытается получить [Gif]-изображение с сайта если [currGif] == null.
     * Иначе устанавливает нормальное состояние.
     */
    override fun refresh() {
        if (currGif == null)
            loadGif()
        else
            setNormalState()
    }

    /**
     * Проверяет есить ли предыдущее [Gif]-изображение
     */
    private fun hasPrev() = index > 0 && prevGifs.isNotEmpty()

    /**
     * Проверяет есть ли следующее [Gif]-изображение
     */
    private fun hasNext() = index < prevGifs.size - 1

    /**
     * Устанавливает в [_state] [GifState.SuccessState] передавая в нем
     * [currGif] и [hasPrev]. Если [index] < 0, то устанавливает в [_state]
     * [GifState.ErrorState] c [R.string.unknown_error]
     */
    private fun setNormalState() = when {
        index >= 0 -> _state.postValue(GifState.SuccessState(currGif, hasPrev()))
        else -> onFail(R.string.unknown_error)
    }

    /**
     * Устанавливает в [_state] [GifState.ErrorState] передавая в нем
     * сообщение ресурс id которого равен [msg].
     *
     * @param msg id сообщения об ошибке
     */
    private fun onFail(msg: Int) {
        _state.postValue(GifState.ErrorState(msg, hasPrev()))
    }

}