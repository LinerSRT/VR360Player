package ru.liner.vr360server.recycler.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.server.Video;
import ru.liner.vr360server.utils.FileUtils;
import ru.liner.vr360server.utils.Utils;
import ru.liner.vr360server.utils.ViewUtils;
import ru.liner.vr360server.views.MarqueeTextView;
import ru.liner.vr360server.views.RoundedImageView;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private final IServer server;
    private final List<Video> videoList;
    private Callback callback;

    public VideoAdapter(IServer server) {
        this.server = server;
        this.videoList = new ArrayList<>();
    }

    public void add(Video video) {
        videoList.add(video);
        notifyItemInserted(videoList.size() - 1);
    }
    public void update(Video video) {
        videoList.set(videoList.lastIndexOf(video), video);
        notifyItemInserted(videoList.lastIndexOf(video));
    }

    public List<Video> getVideoList() {
        return videoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.videofile_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Video video = videoList.get(position);
        holder.videoThumb.animate()
                .scaleY(video.selected ? 0.95f : 1f)
                .scaleX(video.selected ? 0.95f : 1f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator()).start();
        holder.videoSelection.animate()
                .scaleY(video.selected ? 0.95f : 1f)
                .scaleX(video.selected ? 0.95f : 1f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator()).start();
        ViewUtils.setVisibility(holder.videoSelection, video.selected ? View.VISIBLE : View.GONE);
        holder.videoName.setText(video.name);
        holder.videoPath.setText(video.path);
        holder.videoSize.setText(String.format("Size: %s", FileUtils.humanReadableByteCount(video.size)));
        holder.videoResolution.setText(video.resolution);

        holder.videoThumb.setImageBitmap(video.thumb == null ? Utils.toBitmap(Objects.requireNonNull(ContextCompat.getDrawable(holder.videoThumb.getContext(), R.drawable.video_thumb))) : video.thumb);
        holder.videoDuration.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(video.duration),
                TimeUnit.MILLISECONDS.toMinutes(video.duration) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(video.duration) % TimeUnit.MINUTES.toSeconds(1))
        );
        holder.videoLayout.setOnClickListener(v -> {
            for(Video otherVideos:videoList)
                otherVideos.selected = false;
            video.selected = true;
            notifyDataSetChanged();
            if(callback != null)
                callback.onSelected(video);
        });
    }

    @Nullable
    public Video getSelectedVideo(){
        for(Video video:videoList)
            if(video.selected)
                return video;
            return null;
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout videoLayout;
        private final MarqueeTextView videoName;
        private final TextView videoDuration;
        private final TextView videoSize;
        private final MarqueeTextView videoPath;
        private final TextView videoResolution;
        private final RoundedImageView videoThumb;
        private final RoundedImageView videoSelection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoLayout = itemView.findViewById(R.id.videoLayout);
            videoDuration = itemView.findViewById(R.id.videoDuration);
            videoName = itemView.findViewById(R.id.videoName);
            videoSize = itemView.findViewById(R.id.videoSize);
            videoPath = itemView.findViewById(R.id.videoPath);
            videoResolution = itemView.findViewById(R.id.videoResolution);
            videoThumb = itemView.findViewById(R.id.videoThumb);
            videoSelection = itemView.findViewById(R.id.videoSelection);
        }
    }

    public interface Callback {
        void onSelected(Video video);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
