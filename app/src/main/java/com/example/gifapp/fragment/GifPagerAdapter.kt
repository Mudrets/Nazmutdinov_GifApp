package com.example.gifapp.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gifapp.utils.Constants.Companion.PAGES

/**
 * Адаптер под ViewPager2.
 *
 * @param activity - фрагмент активити, в котором находится ViewPager2.
 */
class GifPagerAdapter(private val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    /**
     * Инициализируем фрагменты
     */
    private val fragments = PAGES.map {
        GifFragment.newInstance(it)
    }

    override fun getItemCount(): Int = fragments.count()

    override fun createFragment(position: Int): Fragment = fragments[position]

    /**
     * Полулучаем название страницы с помощью фабрики страниц [PAGES]
     */
    fun getPageTitle(position: Int) : CharSequence {
        return activity.resources.getString(PAGES.elementAt(position).resourceId)
    }
}