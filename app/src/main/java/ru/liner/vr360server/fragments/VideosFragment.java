package ru.liner.vr360server.fragments;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.net.Socket;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.activity.MainActivity;
import ru.liner.vr360server.recycler.adapter.VideoAdapter;
import ru.liner.vr360server.server.Video;
import ru.liner.vr360server.utils.Comparator;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Lists;
import ru.liner.vr360server.utils.Utils;
import ru.liner.vr360server.utils.ViewUtils;
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
    private TextView videoRecyclerEmpty;
    private TextView videosFolder;
    private SwipeButton syncAndPlayButton;
    private LinearLayout loadingVideosProgress;
    private Video video;


    public VideosFragment(IServer server) {
        super(server);
    }

    public VideosFragment() {
        this.server = MainActivity.getServer();
    }

    @Override
    public void declareViews(View view) {
        videoRecycler = find(R.id.videoRecycler);
        videoRecyclerEmpty = find(R.id.videoRecyclerEmpty);
        videosFolder = find(R.id.videosFolder);
        syncAndPlayButton = find(R.id.syncAndPlayButton);
        loadingVideosProgress = find(R.id.loadingVideosProgress);
    }

    @Override
    public void onFragmentCreated() {
        videoAdapter = new VideoAdapter(server);
        videoAdapter.setCallback(video -> {
            VideosFragment.this.video = video;
            syncAndPlayButton.disableButton(false);
        });
        videoRecycler.setLayoutManager(new ExtraPaddingLinearLayoutManager(getContext(), 0, ViewUtils.dpToPx(48)));
        videoRecycler.setAdapter(videoAdapter);
        syncAndPlayButton.setEnabled(false);
        syncAndPlayButton.setStateCallback(new SwipeButton.StateCallback() {
            @Override
            public void onStateChanged(SwipeButton swipeButton, boolean enabled, boolean fromUser) {
                if (enabled) {
                    if (fromUser) {
                        server.stopSyncSession();
                        server.stopMediaServer();
                        server.startMediaServer(video);
                        server.requestSync(video);
                    } else {
                        server.stopSyncSession();
                        server.stopMediaServer();
                    }
                    syncAndPlayButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_red));
                } else {
                    syncAndPlayButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_green));
                }
            }
        });
        server.runBackground(() -> {
            while (!server.allRetrievedLoaded())
                Utils.sleep(16);
            for (Video video : server.getVideoList())
                server.runOnUI(() -> {
                    loadingVideosProgress.setVisibility(View.GONE);
                    videoAdapter.add(video);
                });
            server.runOnUI(() -> {
                if(videoAdapter.getItemCount() != 0) {
                    Video video = videoAdapter.getVideoList().get(0);
                    VideosFragment.this.video = video;
                    video.selected = true;
                    videoAdapter.update(video);
                    syncAndPlayButton.setEnabled(server.isTCPServerRunning() || server.hasConnectedClients());
                    videoRecyclerEmpty.setVisibility(videoAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    videosFolder.setVisibility(videoAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
                    syncAndPlayButton.animate()
                            .setStartDelay(300)
                            .translationY(0)
                            .setDuration(300)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                }
            });
        });
    }

    @Override
    public void onClientConnected(Socket socket) {
        super.onClientConnected(socket);
        server.runOnUI(() -> {
            if(videoAdapter.getItemCount() != 0) {
                syncAndPlayButton.setEnabled(server.isTCPServerRunning() || server.hasConnectedClients());
                videoRecyclerEmpty.setVisibility(videoAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                videosFolder.setVisibility(videoAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
                syncAndPlayButton.animate()
                        .setStartDelay(300)
                        .translationY(0)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        });
    }

    @Override
    public void onClientDisconnected(Socket socket) {
        super.onClientDisconnected(socket);
        server.runOnUI(() -> {
            if(videoAdapter.getItemCount() != 0) {
                syncAndPlayButton.setEnabled(server.isTCPServerRunning() || server.hasConnectedClients());
                videoRecyclerEmpty.setVisibility(videoAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                videosFolder.setVisibility(videoAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
                syncAndPlayButton.animate()
                        .setStartDelay(300)
                        .translationY(0)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        });
    }

    @Override
    public int getLayoutRes() {
        return R.layout.video_fragment;
    }
}
