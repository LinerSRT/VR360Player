package ru.liner.vr360server.recycler.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.server.Video;
import ru.liner.vr360server.utils.FileUtils;
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

    public VideoAdapter(IServer server) {
        this.server = server;
        this.videoList = new ArrayList<>();
    }

    public void add(Video video){
        videoList.add(video);
        notifyItemInserted(videoList.size()-1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.videofile_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Video video = videoList.get(position);
        holder.videoName.setText(video.name);
        holder.videoPath.setText(video.path);
        holder.videoSize.setText(String.format("Size: %s", FileUtils.humanReadableByteCount(video.size)));
        holder.videoResolution.setText(video.resolution);
        holder.videoSelection.setVisibility(video.selected ? View.VISIBLE : View.GONE);
        holder.videoThumb.setImageBitmap(video.thumb);
        holder.videoLayout.setOnClickListener(v -> {
            video.selected = !video.selected;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout videoLayout;
        private final MarqueeTextView videoName;
        private final TextView videoSize;
        private final MarqueeTextView videoPath;
        private final TextView videoResolution;
        private final RoundedImageView videoThumb;
        private final RoundedImageView videoSelection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoLayout = itemView.findViewById(R.id.videoLayout);
            videoName = itemView.findViewById(R.id.videoName);
            videoSize = itemView.findViewById(R.id.videoSize);
            videoPath = itemView.findViewById(R.id.videoPath);
            videoResolution = itemView.findViewById(R.id.videoResolution);
            videoThumb = itemView.findViewById(R.id.videoThumb);
            videoSelection = itemView.findViewById(R.id.videoSelection);
        }
    }
}
