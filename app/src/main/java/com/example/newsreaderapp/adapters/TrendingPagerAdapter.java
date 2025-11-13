package com.example.newsreaderapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.newsreaderapp.fragments.main.TrendingPageFragment;
import com.example.newsreaderapp.fragments.trending.MostReadFragment;

public class TrendingPagerAdapter extends FragmentStateAdapter {

    public TrendingPagerAdapter(@NonNull TrendingPageFragment host) {
        super(host);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // For now, only MostRead is implemented. Other tabs will be added later.
        // position: 0 = Most Read, 1 = Most Liked, 2 = Hot 24H
        return new MostReadFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}