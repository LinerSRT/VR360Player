package ru.liner.vr360server.server;

import androidx.annotation.Nullable;

import java.net.Socket;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class Client {
    public transient Socket socket;
    public String hostname;
    public String videoHash = "none";
    public boolean waitingAction = true;
    public boolean playingVideo = false;
    public boolean downloadingVideo = false;
    public boolean downloadingFinished = false;
    public int downloadedBytes = 0;
    public int totalBytes = 0;
    public int downloadedProgress = 0;
    public int downloadingSpeed = 0;
    public int currentVolume = 100;
    public int currentVideoPosition = 0;
    public int videoLength = 0;
    public int videoSize = 0;
    public boolean shouldReconnect = true;
    public boolean exitOnDisconnect = false;
    public boolean pauseOnDisconnect = true;

    public Client(Socket socket, String hostname) {
        this.socket = socket;
        this.hostname = hostname;
    }
}
