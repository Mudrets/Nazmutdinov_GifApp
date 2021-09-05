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

        binding.nextBtn?.setOnClickListener {
            viewModel.nextGif()
        }

        binding.backBtn?.setOnClickListener {
            viewModel.prevGif()
        }

        binding.refreshBtn?.setOnClickListener {
            viewModel.refresh()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is GifState.SuccessState -> onSuccess(it.gif!!, it.hasPrev)
                is GifState.ErrorState<*> -> {
                    when (it.msg) {
                        is Int -> onError(it.msg)
                        is String -> onError(it.msg)
                    }
                }
                is GifState.LoadState -> onLoading()
            }
        }

        binding.progressBar?.visibility = View.GONE
        binding.refreshBtn?.visibility = View.GONE

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pageInfoArg = arguments?.getParcelable<PageInfo>(ARG_PAGE_INFO)
        if (pageInfoArg != null)
            pageInfo = pageInfoArg
        val repository = Repository()
        val viewModelFactory: ViewModelProvider.Factory
        viewModel = if (pageInfo.pageSection == PageSection.RANDOM) {
            viewModelFactory = RandomGifViewModelFactory(repository)
            ViewModelProvider(this, viewModelFactory).get(RandomGifViewModel::class.java)
        } else {
            viewModelFactory = GifFromSectionViewModelFactory(repository, pageInfo.pageSection)
            ViewModelProvider(this, viewModelFactory)
                .get(GifFromSectionViewModel::class.java)
        }

        viewModel.initialize()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun onSuccess(gif: Gif, hasPrev: Boolean) {
        binding.apply {
            progressBar?.visibility = View.VISIBLE
            description?.visibility = View.VISIBLE
            author?.visibility = View.VISIBLE
            warningMsg?.visibility = View.GONE
            warningIcon?.visibility = View.GONE
            refreshBtn?.visibility = View.GONE
            nextBtn?.isEnabled = true
            description?.text = gif.description
            author?.text = getString(R.string.author_name, gif.author)
            image?.load(gif.gifUrl) {
                crossfade(true)
                crossfade(300)
                scale(Scale.FILL)
                listener(onSuccess = { _, _ ->
                    progressBar?.visibility = View.GONE
                    image?.visibility = View.VISIBLE
                },
                    onError = { request, throwable ->
                        onError(throwable.message ?: "Проверьте свое подключение к интернету")
                    })
            }
            nextBtn?.isEnabled = true
            backBtn?.isEnabled = hasPrev
        }
    }

    private fun onError(msg: Int) {
        onError(activity?.resources?.getString(msg) ?: "")
    }

    private fun onError(msg: String) {
        binding.warningMsg?.visibility = View.VISIBLE
        binding.warningIcon?.visibility = View.VISIBLE
        binding.refreshBtn?.visibility = View.VISIBLE
        binding.progressBar?.visibility = View.GONE
        binding.description?.visibility = View.GONE
        binding.author?.visibility = View.GONE
        binding.warningMsg?.text = msg
    }

    private fun onLoading() {
        binding.apply {
            binding.refreshBtn?.visibility = View.GONE
            warningMsg?.visibility = View.GONE
            warningIcon?.visibility = View.GONE
            progressBar?.visibility = View.VISIBLE
            description?.visibility = View.GONE
            author?.visibility = View.GONE
            nextBtn?.isEnabled = false
            image?.visibility = View.GONE

        }
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