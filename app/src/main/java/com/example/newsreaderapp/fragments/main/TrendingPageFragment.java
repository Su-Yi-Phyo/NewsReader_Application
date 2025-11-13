package com.example.newsreaderapp.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.adapters.TrendingPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class TrendingPageFragment extends Fragment {

    //variable declaration
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trending_page_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.TrendingtabLayout);
        viewPager = view.findViewById(R.id.TrendingNewsViewPager);

        viewPager.setAdapter(new TrendingPagerAdapter(this));
        viewPager.setOffscreenPageLimit(1);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                TabLayout.Tab t = tabLayout.getTabAt(position);
                if (t != null) t.select();
            }
        });

        // default to first tab
        TabLayout.Tab first = tabLayout.getTabAt(0);
        if (first != null) first.select();
        viewPager.setCurrentItem(0, false);
    }

}
