package com.example.gifapp.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.size.Scale
import com.example.gifapp.R
import com.example.gifapp.databinding.FragmentGifBinding
import com.example.gifapp.model.Gif
import com.example.gifapp.repository.Repository
import com.example.gifapp.state.GifState
import com.example.gifapp.viewmodel.GifFromSectionViewModel
import com.example.gifapp.viewmodel.GifViewModel
import com.example.gifapp.viewmodel.RandomGifViewModel
import com.example.gifapp.viewmodel_factory.GifFromSectionViewModelFactory
import com.example.gifapp.viewmodel_factory.RandomGifViewModelFactory

class GifFragment : Fragment() {

    private lateinit var viewModel: GifViewModel
    private var _binding: FragmentGifBinding? = null
    private val binding
        get() = _binding!!
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

    private fun setLoadingState() {
        binding.apply {
            setDefaultState()
            setVisibilityToContentAndImage(View.GONE)
            setVisibilityToWarning(View.GONE)
            progressBar?.visibility = View.VISIBLE
            nextBtn?.isEnabled = false
        }
    }

    private fun setVisibilityToWarning(visibility: Int) = binding.apply {
        warningIcon?.visibility = visibility
        warningMsg?.visibility = visibility
    }

    private fun setVisibilityToContent(visibility: Int) = binding.apply {
        author?.visibility = visibility
        description?.visibility = visibility
    }

    private fun setVisibilityToContentAndImage(visibility: Int) = binding.apply {
        setVisibilityToContent(visibility)
        image?.visibility = visibility
    }

    private fun setErrorState() = binding.apply {
        setVisibilityToWarning(View.VISIBLE)
        setVisibilityToContentAndImage(View.GONE)
        refreshBtn?.visibility = View.VISIBLE
        progressBar?.visibility = View.GONE
    }

    private fun setGifLoadingErrorState() = binding.apply {
        setVisibilityToWarning(View.VISIBLE)
        progressBar?.visibility = View.GONE
        warningMsg?.text = getString(R.string.gif_error)
        refreshBtn?.visibility = View.VISIBLE
    }

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

    private fun setBtnClickListener() {
        binding.apply {
            nextBtn?.setOnClickListener { viewModel.nextGif() }
            backBtn?.setOnClickListener { viewModel.prevGif() }
            refreshBtn?.setOnClickListener { viewModel.refresh() }
        }
    }

    private fun setPageInfo() {
        val pageInfoArg = arguments?.getParcelable<PageInfo>(ARG_PAGE_INFO)
        if (pageInfoArg != null)
            pageInfo = pageInfoArg
    }

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

    private fun setContent(author: String, content: String) = binding.apply {
        setVisibilityToContent(View.VISIBLE)
        nextBtn?.isEnabled = true
        description?.text = content
        this.author?.text = getString(R.string.author_name, author)
    }

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

    private fun onSuccess(gif: Gif, hasPrev: Boolean) {
        binding.apply {
            progressBar?.visibility = View.VISIBLE
            setContent(gif.author, gif.description)
            setVisibilityToWarning(View.GONE)
            loadGif(gif.gifUrl)
            backBtn?.isEnabled = hasPrev
        }
    }

    private fun onError(msg: Int, hasPrev: Boolean) {
        onError(
            activity?.resources?.getString(msg) ?: getString(R.string.unknown_error),
            hasPrev
        )
    }

    private fun onError(msg: String, hasPrev: Boolean) = binding.apply {
        setErrorState()
        warningMsg?.text = msg
        backBtn?.isEnabled = hasPrev
    }

    private fun onLoading() {
        setLoadingState()
    }

    companion object {
        private const val ARG_PAGE_INFO = "page_type"

        fun newInstance(
            pageInfo: Parcelable
        ): GifFragment {
            return GifFragment().apply {
                arguments = bundleOf(ARG_PAGE_INFO to pageInfo)
            }
        }
    }
}