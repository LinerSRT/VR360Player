package ru.liner.vr360server.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.github.florent37.shapeofview.shapes.RoundRectView;

import ru.liner.vr360server.utils.FileUtils;
import ru.liner.vr360server.R;
import ru.liner.vr360server.utils.VideoFile;

public class VideoPickerFrament extends Fragment {
    private TextView noVideos;
    private RecyclerView recyclerView;
    private VideoPickerAdapter videoPickerAdapter;
    private RoundRectView progress;
    private VideoFile selectedVideo;


    public VideoPickerFrament() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_picker_layout, container, false);
        noVideos = view.findViewById(R.id.mediaNoItems);
        recyclerView = view.findViewById(R.id.mediaFileRecycler);
        progress = view.findViewById(R.id.mediaFileProgress);
        progress.setVisibility(View.VISIBLE);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false));
        videoPickerAdapter = new VideoPickerAdapter(container.getContext(), FileUtils.getVideoFromDevice(container.getContext()));
        videoPickerAdapter.setSelectionCallback(new VideoPickerAdapter.SelectionCallback() {
            @Override
            public void onSelected(int position, VideoFile model) {
                processClick(position, model);
            }
        });
        recyclerView.setAdapter(videoPickerAdapter);
        progress.setVisibility(View.GONE);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        if (videoPickerAdapter.isEmpty())
            noVideos.setVisibility(View.VISIBLE);
        else
            noVideos.setVisibility(View.GONE);
        return view;
    }

    private void processClick(int position, VideoFile model) {
       this.selectedVideo = model;

    }

    @Nullable
    public VideoFile getSelectedVideo() {
        return selectedVideo;
    }
}
