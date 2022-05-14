package ru.liner.vr360server.activity;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.Socket;
import java.util.List;

import ru.liner.vr360server.server.Client;
import ru.liner.vr360server.server.Video;

public interface IServer {
    void runBackground(Runnable runnable);
    void runOnUI(Runnable runnable);

    void registerDataReceiver(IDataReceiver dataReceiver);
    void unregisterDataReceiver(IDataReceiver dataReceiver);
    void startTCPServer();
    void stopTCPServer();
    boolean isTCPServerRunning();
    void startMediaServer(Video video);
    void stopMediaServer();
    String getHost();

    void onClientConnected(Socket socket);
    void onClientDisconnected(Socket socket);
    void sendData(Socket socket, @NonNull String data);
    void sendData(Socket socket, @NonNull Object data);
    void sendData(@NonNull String data);
    void sendData(@NonNull Object data);
    boolean isClientConnected(Socket socket);
    void disconnectClient(Socket socket);
    void onClientDataReceived(Socket socket, @NonNull String data);
    String serializeObject(@NonNull Object object);
    @Nullable
    Object deserializeObject(@NonNull String object);


    int connectedClientsCount();
    boolean hasConnectedClients();
    boolean hasActiveSyncSessions();
    boolean hasLoadedVideos();
    boolean hasSelectVideo();
    Video getSelectedVideo();
    List<Video> getLoadedVideos();
    void requestSync(Socket socket, @NonNull Video video);
    void requestSync(@NonNull Video video);
    void onVideoSelected(Video video);
}
