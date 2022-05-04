package ru.liner.vr360server.activity;

import ru.liner.vr360server.recycler.model.ConnectedDevice;
import ru.liner.vr360server.utils.VideoFile;

public interface IVideoStream {
    void selectVideo();
    void syncVideo(ConnectedDevice connectedDevice);
    void syncVideo();
    void playVideo(ConnectedDevice connectedDevice);
    void playVideo();
    void pauseVideo(ConnectedDevice connectedDevice);
    void pauseVideo();
    void stopVideo(ConnectedDevice connectedDevice);
    void stopVideo();
    void seekVideo(ConnectedDevice connectedDevice, int position);
    void seekVideo(int position);
    void increaseVolume(ConnectedDevice connectedDevice);
    void increaseVolume();
    void decreaseVolume(ConnectedDevice connectedDevice);
    void decreaseVolume();
    boolean isVideoSelected();
    boolean isSyncStarted();
    boolean isSyncFinished(ConnectedDevice connectedDevice);
    boolean isSyncFinished();
    boolean isVideoPlaying(ConnectedDevice connectedDevice);
    boolean isVideoPlaying();
    boolean isVideoPaused(ConnectedDevice connectedDevice);
    boolean isVideoPaused();
    boolean isVideoStopped(ConnectedDevice connectedDevice);
    boolean isVideoStopped();
    int getVideoPosition(ConnectedDevice connectedDevice);
    int getVideoPosition();
    VideoFile getVideoFile();
    void deleteSyncedVideo(ConnectedDevice connectedDevice);
    void deleteSyncedVideo();
    void resetCamera(ConnectedDevice connectedDevice);
    void resetCamera();
}
