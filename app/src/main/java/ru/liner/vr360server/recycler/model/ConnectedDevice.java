package ru.liner.vr360server.recycler.model;

import java.net.Socket;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class ConnectedDevice {
    private final Socket socket;
    private boolean isFetchingStream;
    private int fetchProgress;
    private int fetchDownloaded;
    private int fetchTotal;
    private boolean isReadyToPlay;
    private boolean isPaused;

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

    public int getFetchProgress() {
        return fetchProgress;
    }

    public void setFetchProgress(int fetchProgress) {
        this.fetchProgress = fetchProgress;
    }

    public int getFetchDownloaded() {
        return fetchDownloaded;
    }

    public void setFetchDownloaded(int fetchDownloaded) {
        this.fetchDownloaded = fetchDownloaded;
    }

    public int getFetchTotal() {
        return fetchTotal;
    }

    public void setFetchTotal(int fetchTotal) {
        this.fetchTotal = fetchTotal;
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

    @Override
    public String toString() {
        return "ConnectedDevice{" +
                "socket=" + socket +
                ", isFetchingStream=" + isFetchingStream +
                ", fetchProgress=" + fetchProgress +
                ", fetchDownloaded=" + fetchDownloaded +
                ", fetchTotal=" + fetchTotal +
                ", isReadyToPlay=" + isReadyToPlay +
                ", isPaused=" + isPaused +
                '}';
    }
}
