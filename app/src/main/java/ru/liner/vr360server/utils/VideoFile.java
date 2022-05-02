package ru.liner.vr360server.utils;

import android.graphics.Bitmap;
import android.net.Uri;

public class VideoFile {
    private final int id;
    private final String displayName;
    private final long sizeInBytes;
    private final String fullPath;
    private transient final Uri uri;
    private boolean isSelected;
    private transient final Bitmap thumb;

    public VideoFile(int id, String displayName, long sizeInBytes, String fullPath, Uri uri, Bitmap thumb) {
        this.id = id;
        this.displayName = displayName;
        this.sizeInBytes = sizeInBytes;
        this.fullPath = fullPath;
        this.uri = uri;
        this.thumb = thumb;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public String getFullPath() {
        return fullPath;
    }

    public Uri getUri() {
        return uri;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "VideoModel{" +
                "displayName='" + displayName + '\'' +
                ", sizeInBytes=" + sizeInBytes +
                ", fullPath='" + fullPath + '\'' +
                ", uri=" + uri.toString() +
                ", isSelected=" + isSelected +
                '}';
    }
}
