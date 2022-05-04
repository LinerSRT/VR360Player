package ru.liner.vr360server.recycler.model;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 04.05.2022, среда
 **/
public class DownloadProgress {
    public int downloadedBytes;
    public int totalBytes;
    public int downloadProgress;
    public int downloadSpeed;

    @Override
    public String toString() {
        return "DownloadProgress{" +
                "downloadedBytes=" + downloadedBytes +
                ", totalBytes=" + totalBytes +
                ", downloadProgress=" + downloadProgress +
                ", downloadSpeed=" + downloadSpeed +
                '}';
    }
}
