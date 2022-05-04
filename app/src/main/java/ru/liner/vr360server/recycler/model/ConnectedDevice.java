package ru.liner.vr360server.recycler.model;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class ConnectedDevice {
    private final Socket socket;
    private boolean isFetchingStream;
    private DownloadProgress downloadProgress;
    private boolean isReadyToPlay;
    private boolean isPaused;
    private boolean allowPlay = true;
    private long pingMs;

    public ConnectedDevice(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isFetchingStream() {
        return isFetchingStream;
    }

    public void setFetchingStream(boolean fetchingStream) {
        isFetchingStream = fetchingStream;
    }

    public void setDownloadProgress(DownloadProgress downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public DownloadProgress getDownloadProgress() {
        return downloadProgress;
    }

    public boolean isReadyToPlay() {
        return isReadyToPlay;
    }

    public void setReadyToPlay(boolean readyToPlay) {
        isReadyToPlay = readyToPlay;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public void setPingMs(long pingMs) {
        this.pingMs = pingMs;
    }

    public void setAllowPlay(boolean allowPlay) {
        this.allowPlay = allowPlay;
    }

    public boolean isAllowPlay() {
        return allowPlay;
    }

    public long getPingMs() {
        return pingMs;
    }

    @Override
    public String toString() {
        return "ConnectedDevice{" +
                "socket=" + socket +
                ", isFetchingStream=" + isFetchingStream +
                ", downloadProgress=" + downloadProgress.toString() +
                ", isReadyToPlay=" + isReadyToPlay +
                ", isPaused=" + isPaused +
                ", pingMs=" + pingMs +
                '}';
    }
}
