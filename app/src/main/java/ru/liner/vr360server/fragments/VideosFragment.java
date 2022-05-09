package ru.liner.vr360server.fragments;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.activity.MainActivity;
import ru.liner.vr360server.recycler.adapter.VideoAdapter;
import ru.liner.vr360server.server.Video;
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
        videoAdapter.setCallback(new VideoAdapter.Callback() {
            @Override
            public void onSelected(Video video) {
                VideosFragment.this.video = video;
                syncAndPlayButton.setEnabled(true);
            }
        });
        videoRecycler.setLayoutManager(new ExtraPaddingLinearLayoutManager(getContext(), 0, ViewUtils.dpToPx(48)));
        videoRecycler.setAdapter(videoAdapter);
        syncAndPlayButton.setEnabled(false);
        syncAndPlayButton.setStateCallback(new SwipeButton.StateCallback() {
            @Override
            public void onStateChanged(SwipeButton swipeButton, boolean enabled, boolean fromUser) {
                if (enabled) {
                    if (fromUser) {
//                        if (!server.isServerRunning() || !server.hasConnectedClients()) {
//                            swipeButton.disableButton(false);
//                            server.showNotification("Server not started!", "Start server and connect at least one client to start stream", R.color.red);
//                        } else {
//                            if (video != null) {
//                                server.startMediaServer(video);
//                                server.send(String.format("download_video@%s@%s", "http://" + server.getHost() + ":" + Constant.SERVER_STREAM_VIDEO_PORT, new Gson().toJson(video)));
//                            }
//                        }
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
                    videoRecyclerEmpty.setVisibility(videoAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    videosFolder.setVisibility(videoAdapter.getItemCount() != 0 ? View.VISIBLE : View.GONE);
                });
        });
        //server.showNotification("Loading videos", "Loading video information from device", R.color.backgroundSecondaryColor, false, 0);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.video_fragment;
    }
}
