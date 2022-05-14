package ru.liner.vr360server.fragments;

import android.annotation.SuppressLint;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.activity.MainActivity;
import ru.liner.vr360server.recycler.adapter.VideoAdapter;
import ru.liner.vr360server.server.Video;
import ru.liner.vr360server.utils.Comparator;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Files;
import ru.liner.vr360server.utils.Lists;
import ru.liner.vr360server.utils.Utils;
import ru.liner.vr360server.utils.ViewUtils;
import ru.liner.vr360server.utils.background.Background;
import ru.liner.vr360server.utils.background.Function;
import ru.liner.vr360server.utils.hashing.Hash;
import ru.liner.vr360server.utils.hashing.HashAlgorithm;
import ru.liner.vr360server.views.ExtraPaddingLinearLayoutManager;
import ru.liner.vr360server.views.SwipeButton;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class VideosFragment extends BaseFragment {
    private VideoAdapter videoAdapter;
    private RecyclerView videoRecycler;
    private LinearLayout loadingVideosProgress;


    public VideosFragment(IServer server) {
        super(server);
    }

    public VideosFragment() {
        this.server = MainActivity.getServer();
    }

    @Override
    public void declareViews(View view) {
        videoRecycler = find(R.id.videoRecycler);
        loadingVideosProgress = find(R.id.loadingVideosProgress);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onFragmentCreated() {
        videoAdapter = new VideoAdapter(server);
        videoAdapter.setCallback(video -> {
            server.onVideoSelected(video);
        });
        videoRecycler.setLayoutManager(new ExtraPaddingLinearLayoutManager(getContext(), ViewUtils.dpToPx(16), ViewUtils.dpToPx(16)));
        videoRecycler.setAdapter(videoAdapter);
        server.runBackground(() -> {
            while (!server.hasLoadedVideos())
                Utils.sleep(16);
            for (Video video : server.getLoadedVideos())
                server.runOnUI(() -> {
                    loadingVideosProgress.setVisibility(View.GONE);
                    videoAdapter.add(video);
                });
        });
    }



    @Override
    public int getLayoutRes() {
        return R.layout.video_fragment;
    }
}
