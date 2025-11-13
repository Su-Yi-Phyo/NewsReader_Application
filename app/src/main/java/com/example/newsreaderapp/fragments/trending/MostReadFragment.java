package com.example.newsreaderapp.fragments.trending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsreaderapp.R;
import com.example.newsreaderapp.adapters.TrendingNewsAdapter;
import com.example.newsreaderapp.viewmodel.NewsViewModel;

public class MostReadFragment extends Fragment {

    private RecyclerView recyclerView;
    private TrendingNewsAdapter adapter;

    private int currentPage = 1;
    private final int pageSize = 20;

    private NewsViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // assumes res/layout/mostread.xml has RecyclerView id @+id/recyclerMostRead
        return inflater.inflate(R.layout.most_read_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewMostRead);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TrendingNewsAdapter();
        recyclerView.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(NewsViewModel.class);

        // observe data
        vm.articles.observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) adapter.setItems(articles);
        });

        vm.loading.observe(getViewLifecycleOwner(), isLoading -> {
            // keep scroll listener logic simple; no UI spinner for now
        });

        vm.error.observe(getViewLifecycleOwner(), err -> {
            if (err != null && isAdded()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });

        // initial load
        currentPage = 1;
        vm.clear();
        vm.fetchMostRead(currentPage, pageSize, false);

        // pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;

                int visible = lm.getChildCount();
                int total   = lm.getItemCount();
                int first   = lm.findFirstVisibleItemPosition();

                boolean nearEnd = (visible + first) >= (total - 4);
                Boolean loading = vm.loading.getValue();
                if (nearEnd && (loading == null || !loading)) {
                    currentPage++;
                    vm.fetchMostRead(currentPage, pageSize, true);
                }
            }
        });
    }
}