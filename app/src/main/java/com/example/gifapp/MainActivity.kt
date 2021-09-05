package com.example.gifapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gifapp.databinding.ActivityMainBinding
import com.example.gifapp.fragment.GifPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val pagerAdapter = GifPagerAdapter(this)

        val viewPager = binding.viewPager.apply {
            adapter = pagerAdapter
        }

        val tabs: TabLayout = binding.tabLayout

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}