package ru.liner.vr360server.activity;

import ru.liner.vr360server.recycler.model.ConnectedDevice;
import ru.liner.vr360server.utils.VideoFile;

public interface IServer {
    void startServer();
    void stopServer();
    boolean isServerRunning();
    void startMediaServer(VideoFile videoFile);
    void stopMediaServer();
    boolean isMediaServerRunning();
    void startIPPublisher();
    void stopIPPublisher();
    boolean isIPPublisherRunning();
    void onDeviceConnect(ConnectedDevice connectedDevice);
    void onDeviceDisconnected(ConnectedDevice connectedDevice);
    void onReceived(ConnectedDevice connectedDevice, String command);
    void sendToAll(String command);
    void sendTo(ConnectedDevice connectedDevice);
}
