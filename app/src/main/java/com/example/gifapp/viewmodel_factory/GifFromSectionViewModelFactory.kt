package com.example.gifapp.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gifapp.fragment.PageInfo
import com.example.gifapp.fragment.PageSection
import com.example.gifapp.repository.Repository
import com.example.gifapp.viewmodel.GifFromSectionViewModel
import com.example.gifapp.viewmodel.RandomGifViewModel

class GifFromSectionViewModelFactory(
    private val repository: Repository,
    private val pageSection: PageSection
) : ViewModelProvider.Factory  {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GifFromSectionViewModel(repository, pageSection) as T
    }
}