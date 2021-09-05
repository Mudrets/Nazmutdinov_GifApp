package com.example.gifapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gifapp.R
import com.example.gifapp.model.PageSection
import com.example.gifapp.model.Gif
import com.example.gifapp.model.ResponseWrapper
import com.example.gifapp.repository.Repository
import com.example.gifapp.state.GifState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel реализацющий интефейс [GifViewModel]. Получает [Gif]-изображения из какой-то
 * конкретной категорией. [Gif]-изображения хранятся в моделе [Gif]
 *
 * @param repository репозиторий для получения GIF-изображений
 * @param pageSection выбранная секция для загрузки
 */
class GifFromSectionViewModel(
    private val repository: Repository,
    private val pageSection: PageSection
) : ViewModel(), GifViewModel {

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
     * Номер следующей страницы с [Gif]-изображениями, которую
     * необходимо загрузить
     */
    private var nextPage = 0

    /**
     * Состояние загрузки [GifState]
     */
    private val _state = MutableLiveData<GifState>()

    /**
     * Если следующее [Gif]-изображение уже загружено, то устанавливает
     * в переменную [currGif] следующее [Gif]-изображение. Если же следующего
     * [Gif]-изображения еще нет, то загружает страницу с сервера (5 [Gif]-изображений)
     * и сохраняет первое [Gif]-изображение со страницы в [currGif]
     */
    override fun nextGif() {
        if (!hasNext())
            loadGifs()
        else {
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
     * Повторно пытается получить [Gif]-изображение с сайта если [currGif] == null.
     * Иначе устанавливает нормальное состояние.
     */
    override fun refresh() {
        if (currGif == null)
            loadGifs()
        else
            setNormalState()
    }

    /**
     * Обработка успешного получения [Gif]-изображения
     */
    private fun successLoad(responseWrapper: ResponseWrapper) {
        prevGifs.addAll(responseWrapper.result)
        lastWasFail = false
        currGif = prevGifs[++index]
        nextPage++
        setNormalState()
    }

    /**
     * Проверяет, что ответ оказался успешным
     */
    private fun isSuccessResponse(
        response: Response<ResponseWrapper>,
        responseWrapper: ResponseWrapper?
    ) = response.code() == 200 && responseWrapper != null &&
                !responseWrapper.result.isNullOrEmpty()

    /**
     * Обрабатывает получение ответа
     */
    private fun onResponse(response: Response<ResponseWrapper>) {
        val responseWrapper = response.body()
        if (isSuccessResponse(response, responseWrapper))
            successLoad(responseWrapper!!)
        else if (responseWrapper != null && responseWrapper.result.isNullOrEmpty())
            onFail(R.string.collection_of_gifs_is_null)
        else
            onFail(R.string.connection_error)
    }

    /**
     * Обрабатывает получение ошибки с сервера
     */
    private fun onFailure() {
        currGif = null
        lastWasFail = true
        onFail(R.string.connection_error)
    }

    /**
     * Отправляет запрос на сервер для получения [Gif]-изображений
     */
    private fun loadGifs() {
        _state.postValue(GifState.LoadState)
        val response = repository.getGif(pageSection.value, nextPage)

        response.enqueue(object : Callback<ResponseWrapper> {
            override fun onResponse(
                call: Call<ResponseWrapper>,
                response: Response<ResponseWrapper>
            ) = onResponse(response)

            override fun onFailure(call: Call<ResponseWrapper>, t: Throwable) = onFailure()
        })
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
     * Проверяет есить ли предыдущее [Gif]-изображение
     */
    private fun hasPrev() = index > 0 && prevGifs.isNotEmpty()

    /**
     * Проверяет есть ли следующее [Gif]-изображение
     */
    private fun hasNext() = index < prevGifs.size - 1
}
