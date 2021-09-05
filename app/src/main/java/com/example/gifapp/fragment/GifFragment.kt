package com.example.gifapp.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.size.Scale
import com.example.gifapp.R
import com.example.gifapp.databinding.FragmentGifBinding
import com.example.gifapp.model.Gif
import com.example.gifapp.model.PageSection
import com.example.gifapp.repository.Repository
import com.example.gifapp.state.GifState
import com.example.gifapp.viewmodel.GifFromSectionViewModel
import com.example.gifapp.viewmodel.GifViewModel
import com.example.gifapp.viewmodel.RandomGifViewModel
import com.example.gifapp.viewmodel_factory.GifFromSectionViewModelFactory
import com.example.gifapp.viewmodel_factory.RandomGifViewModelFactory

/**
 * [Fragment] для отображения [Gif]-изображения и информации о нем
 */
class GifFragment : Fragment() {

    /**
     * ViewModel реализующая интефрфейс [GifViewModel]
     */
    private lateinit var viewModel: GifViewModel

    //Из документации
    private var _binding: FragmentGifBinding? = null
    private val binding
        get() = _binding!!

    /**
     * Информация о текщуей странице
     */
    var pageInfo = PageInfo(R.string.tab_random, PageSection.RANDOM)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGifBinding.inflate(inflater, container, false)

        setBtnClickListener()
        setObserver()
        setDefaultState()

        return binding.root
    }

    /**
     * Устанавливает UI в состояние по умолчанию
     */
    private fun setDefaultState() {
        binding.apply {
            progressBar?.visibility = View.GONE
            refreshBtn?.visibility = View.GONE
            image?.visibility = View.VISIBLE
            nextBtn?.isEnabled = true
            setVisibilityToContent(View.VISIBLE)
            setVisibilityToWarning(View.GONE)
        }
    }

    /**
     * Устанавливает UI в состояние загрузки
     */
    private fun setLoadingState() {
        binding.apply {
            setDefaultState()
            setVisibilityToContentAndImage(View.GONE)
            setVisibilityToWarning(View.GONE)
            progressBar?.visibility = View.VISIBLE
            nextBtn?.isEnabled = false
        }
    }

    /**
     * Устанавливает значение [visibility] предупреждающей иконке и
     * сообщению об ошибке
     */
    private fun setVisibilityToWarning(visibility: Int) = binding.apply {
        warningIcon?.visibility = visibility
        warningMsg?.visibility = visibility
    }

    /**
     * Устанавливает значение [visibility] текстовым полям для
     * отображения никнейма автора и описания к [Gif]-изображению
     */
    private fun setVisibilityToContent(visibility: Int) = binding.apply {
        author?.visibility = visibility
        description?.visibility = visibility
    }

    /**
     * Устанавливает значение [visibility] текстовым полям для
     * отображения никнейма автора и описания к [Gif]-изображению и
     * самому [Gif]-изображению
     */
    private fun setVisibilityToContentAndImage(visibility: Int) = binding.apply {
        setVisibilityToContent(visibility)
        image?.visibility = visibility
    }

    /**
     * Устанавливает UI в состояние ошибки
     */
    private fun setErrorState() = binding.apply {
        setVisibilityToWarning(View.VISIBLE)
        setVisibilityToContentAndImage(View.GONE)
        refreshBtn?.visibility = View.VISIBLE
        progressBar?.visibility = View.GONE
    }

    /**
     * Устанавливает UI в состояние ошибки во время загрузки [Gif]-изображения
     */
    private fun setGifLoadingErrorState() = binding.apply {
        setVisibilityToWarning(View.VISIBLE)
        progressBar?.visibility = View.GONE
        warningMsg?.text = getString(R.string.gif_error)
        refreshBtn?.visibility = View.VISIBLE
    }

    /**
     * Устанавливает observer на [GifViewModel.state]
     */
    private fun setObserver() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is GifState.SuccessState -> onSuccess(it.gif!!, it.hasPrev)
                is GifState.ErrorState<*> -> {
                    when (it.msg) {
                        is Int -> onError(it.msg, it.hasPrev)
                        is String -> onError(it.msg, it.hasPrev)
                    }
                }
                is GifState.LoadState -> onLoading()
            }
        }
    }

    /**
     * Устанавливает обработчики нажатия на кнопки
     */
    private fun setBtnClickListener() {
        binding.apply {
            nextBtn?.setOnClickListener { viewModel.nextGif() }
            backBtn?.setOnClickListener { viewModel.prevGif() }
            refreshBtn?.setOnClickListener { viewModel.refresh() }
        }
    }

    /**
     * Устанавливает значение [pageInfo] переданное в качестве аргумента
     */
    private fun setPageInfo() {
        val pageInfoArg = arguments?.getParcelable<PageInfo>(ARG_PAGE_INFO)
        if (pageInfoArg != null)
            pageInfo = pageInfoArg
    }

    /**
     * Создает необходимую [GifViewModel] исходя из значения параметра [PageInfo.pageSection]
     * у [pageInfo]
     */
    private fun getGifViewModel(
        repository: Repository
    ): GifViewModel = if (pageInfo.pageSection == PageSection.RANDOM) {
        val viewModelFactory = RandomGifViewModelFactory(repository)
        ViewModelProvider(this, viewModelFactory).get(RandomGifViewModel::class.java)
    } else {
        val viewModelFactory = GifFromSectionViewModelFactory(repository, pageInfo.pageSection)
        ViewModelProvider(this, viewModelFactory)
            .get(GifFromSectionViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setPageInfo()
        viewModel = getGifViewModel(Repository())
        viewModel.initialize()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * Устанавливает имя автора и описание к [Gif]-изображению
     *
     * @param author никнейм автора
     * @param content описание к [Gif]-изображению
     */
    private fun setContent(author: String, content: String) = binding.apply {
        setVisibilityToContent(View.VISIBLE)
        nextBtn?.isEnabled = true
        description?.text = content
        this.author?.text = getString(R.string.author_name, author)
    }

    /**
     * Загружает [Gif]-изображения с сервера по ссылке
     *
     * @param gifUrl ссылка для загрузки [Gif]-изображения
     */
    private fun loadGif(gifUrl: String) = binding.apply {
        image?.load(gifUrl) {
            crossfade(true)
            crossfade(300)
            scale(Scale.FILL)
            listener(
                onSuccess = { _, _ -> setDefaultState() },
                onError = { _, _ -> setGifLoadingErrorState() }
            )
        }
    }

    /**
     * Обработчик успешного получения [Gif]-изображения с сервера
     *
     * @param gif [Gif]-изображение полученное из ViewModel
     * @param hasPrev есть ли предыдущее [Gif]-изображение
     */
    private fun onSuccess(gif: Gif, hasPrev: Boolean) {
        binding.apply {
            progressBar?.visibility = View.VISIBLE
            setContent(gif.author, gif.description)
            setVisibilityToWarning(View.GONE)
            loadGif(gif.gifUrl)
            backBtn?.isEnabled = hasPrev
        }
    }

    /**
     * Обработчик ошибки
     *
     * @param msg id [R.string] сообщения об ошибке
     * @param hasPrev есть ли предыдущее [Gif]-изображение
     */
    private fun onError(msg: Int, hasPrev: Boolean) {
        onError(
            activity?.resources?.getString(msg) ?: getString(R.string.unknown_error),
            hasPrev
        )
    }

    /**
     * Обработчик ошибки
     *
     * @param msg сообщение об ошибке
     * @param hasPrev есть ли предыдущее [Gif]-изображение
     */
    private fun onError(msg: String, hasPrev: Boolean) = binding.apply {
        setErrorState()
        warningMsg?.text = msg
        backBtn?.isEnabled = hasPrev
    }

    /**
     * Обработчик загрузки [Gif]-изображения
     */
    private fun onLoading() {
        setLoadingState()
    }

    companion object {
        /**
         * Аргумент для [Bundle] для хранения [PageInfo].
         */
        private const val ARG_PAGE_INFO = "page_type"

        /**
         * Создание экземпляра фрагемента с [Bundle],
         * в котором лежит [PageInfo] с аргументом [ARG_PAGE_INFO]
         *
         * @param pageInfo информация о странице.
         */
        fun newInstance(
            pageInfo: Parcelable
        ): GifFragment {
            return GifFragment().apply {
                arguments = bundleOf(ARG_PAGE_INFO to pageInfo)
            }
        }
    }
}