package com.example.gifapp.utils

import com.example.gifapp.R
import com.example.gifapp.fragment.PageInfo
import com.example.gifapp.model.PageSection

/**
 * Константы использующиеся в приложении
 */
class Constants {
    companion object {
        const val BASE_URL = "https://developerslife.ru/"
        val PAGES: Set<PageInfo> = setOf(
            PageInfo(R.string.tab_random, PageSection.RANDOM),
            PageInfo(R.string.tab_top, PageSection.TOP),
            PageInfo(R.string.tab_latest, PageSection.LATEST),
            PageInfo(R.string.tab_hot, PageSection.HOT)
        )
    }
}