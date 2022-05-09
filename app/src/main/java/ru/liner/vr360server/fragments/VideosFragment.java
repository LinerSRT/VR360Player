package ru.liner.vr360server.fragments;

import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.activity.MainActivity;
import ru.liner.vr360server.recycler.adapter.VideoAdapter;
import ru.liner.vr360server.server.Video;
import ru.liner.vr360server.utils.Files;
import ru.liner.vr360server.utils.Worker;
import ru.liner.vr360server.utils.hashing.Hash;
import ru.liner.vr360server.utils.hashing.HashAlgorithm;
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
    private SwipeButton syncAndPlayButton;


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
        syncAndPlayButton = find(R.id.syncAndPlayButton);
    }

    @Override
    public void onFragmentCreated() {
        videoAdapter = new VideoAdapter(server);
        videoAdapter.setCallback(new VideoAdapter.Callback() {
            @Override
            public void onSelected(Video video) {
                syncAndPlayButton.setEnabled(true);
            }
        });
        videoRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        videoRecycler.setAdapter(videoAdapter);
        syncAndPlayButton.setEnabled(false);
        syncAndPlayButton.setStateCallback(new SwipeButton.StateCallback() {
            @Override
            public void onStateChanged(SwipeButton swipeButton, boolean enabled, boolean fromUser) {
                if (enabled) {
                    syncAndPlayButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_red));
                } else {
                    syncAndPlayButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_green));
                }
                if (fromUser) {
                    if (!server.isServerRunning()) {
                        swipeButton.disableButton(false);
                        server.showNotification("Server not started!", "Start server and connect at least one client to start stream", R.color.red);
                    } else {

                    }
                }
            }
        });

        server.showNotification("Loading videos", "Loading video information from device", R.color.backgroundSecondaryColor, false, 0);
        Worker.runInBackground(new Runnable() {
            @Override
            public void run() {
                List<File> files = Files.getAllVideos(getContext(), new File(Environment.getExternalStorageDirectory(), "VRVideos"));
                for (int i = 0; i < files.size(); i++) {
                    server.updateProgress(false, Math.round(((float) i / files.size()) * 100f));
                    File file = files.get(i);
                    Video video = new Video(file);
                    video.thumb = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
                    video.path = file.getPath().trim();
                    video.name = file.getName().trim();
                    video.size = file.length();
                    video.hash = Hash.get(file, HashAlgorithm.MD5);
                    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                    metaRetriever.setDataSource(file.getAbsolutePath());
                    video.resolution = String.format("%sx%s", metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH), metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    metaRetriever.release();
                    parentView.post(() -> {
                        videoAdapter.add(video);
                        videoRecyclerEmpty.setVisibility(videoAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    });
                }
                server.showNotification("Video selected", "Start server and connect clients to stream", R.color.backgroundSecondaryColor);
            }
        });
    }

    @Override
    public int getLayoutRes() {
        return R.layout.video_fragment;
    }
}
