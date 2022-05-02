package ru.liner.vr360server.activity;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

import ru.liner.vr360server.R;
import ru.liner.vr360server.utils.FileUtils;
import ru.liner.vr360server.utils.VideoFile;
import ru.liner.vr360server.views.MarqueeTextView;

public class VideoPickerAdapter extends RecyclerView.Adapter<VideoPickerAdapter.ViewHolder> {
    private Context context;
    private List<VideoFile> videoFileList;
    private SelectionCallback selectionCallback;

    public VideoPickerAdapter(Context context, List<VideoFile> videoFileList) {
        this.context = context;
        this.videoFileList = videoFileList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.video_picker_holder, parent, false)
        );
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoFile videoFile = videoFileList.get(position);
        holder.videoName.setText(videoFile.getDisplayName());
        holder.videoSize.setText(FileUtils.humanReadableByteCount(videoFile.getSizeInBytes()));
        holder.videoSelection.setVisibility((videoFile.isSelected()) ? View.VISIBLE : View.GONE);
        holder.videoThumb.setImageBitmap(videoFile.getThumb());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (videoFile.isSelected()) {
                    holder.itemView.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .alpha(0.8f)
                            .setInterpolator(new OvershootInterpolator())
                            .setDuration(300)
                            .start();
                } else {
                    holder.itemView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setInterpolator(new OvershootInterpolator())
                            .setDuration(300)
                            .start();
                }
            }
        }, 100);
    }

    @Override
    public int getItemCount() {
        return videoFileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView videoThumb;
        private final ImageView videoSelection;
        private final MarqueeTextView videoName;
        private final TextView videoSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumb = itemView.findViewById(R.id.videoThumb);
            videoSelection = itemView.findViewById(R.id.videoSelection);
            videoName = itemView.findViewById(R.id.videoName);
            videoSize = itemView.findViewById(R.id.videoSize);
            itemView.setOnClickListener(view -> {
                if (selectionCallback != null) {
                    int position = getAdapterPosition();
                    for (VideoFile item : videoFileList)
                        item.setSelected(false);
                    videoFileList.get(position).setSelected(!videoFileList.get(position).isSelected());
                    notifyDataSetChanged();
                    selectionCallback.onSelected(position, videoFileList.get(position));
                }
            });
        }
    }

    public void setSelectionCallback(SelectionCallback selectionCallback) {
        this.selectionCallback = selectionCallback;
    }

    public interface SelectionCallback {
        void onSelected(int position, VideoFile model);
    }

    public boolean isEmpty() {
        return videoFileList.isEmpty();
    }
}
