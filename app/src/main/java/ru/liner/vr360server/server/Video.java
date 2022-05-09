package ru.liner.vr360server.server;

import android.graphics.Bitmap;

import java.io.File;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 09.05.2022, понедельник
 **/
public class Video {
    private transient final File file;
    public transient Bitmap thumb;
    public String hash;
    public long size;
    public long duration;
    public String resolution;
    public String name;
    public String path;
    public boolean selected;

    public Video(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "Video{" +
                "hash='" + hash + '\'' +
                ", size=" + size +
                ", duration=" + duration +
                ", resolution='" + resolution + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
