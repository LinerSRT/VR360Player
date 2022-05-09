package ru.liner.vr360server.fragments;

import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.recycler.adapter.VideoAdapter;
import ru.liner.vr360server.server.Video;
import ru.liner.vr360server.utils.Files;
import ru.liner.vr360server.utils.Utils;
import ru.liner.vr360server.utils.Worker;
import ru.liner.vr360server.utils.hashing.Hash;
import ru.liner.vr360server.utils.hashing.HashAlgorithm;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class VideosFragment extends BaseFragment {
    private VideoAdapter videoAdapter;
    private RecyclerView videoRecycler;
    private TextView videoRecyclerEmpty;


    public VideosFragment(IServer server) {
        super(server);
    }

    @Override
    public void declareViews(View view) {
        videoRecycler = find(R.id.videoRecycler);
        videoRecyclerEmpty = find(R.id.videoRecyclerEmpty);
    }

    @Override
    public void onFragmentCreated() {
        videoAdapter = new VideoAdapter(server);
        videoRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        videoRecycler.setAdapter(videoAdapter);
        server.showNotification("Loading videos", "Loading video information from device", R.color.green);

        Worker.runInBackground(new Runnable() {
            @Override
            public void run() {
                for(File file:Files.getAllVideos(getContext(), new File(Environment.getExternalStorageDirectory(), "VRVideos"))){
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
                server.showNotification("Server is ready", "Server application ready to work", R.color.backgroundSecondaryColor);
            }
        });
    }

    @Override
    public int getLayoutRes() {
        return R.layout.video_fragment;
    }
}
