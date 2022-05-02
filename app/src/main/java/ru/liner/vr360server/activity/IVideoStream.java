package ru.liner.vr360server.activity;

import ru.liner.vr360server.utils.VideoFile;

public interface IVideoStream {
    void selectVideo();
    void syncVideo();
    void playVideo();
    void stopVideo();
    void seekVideo(int position);
    void increaseVolume();
    void decreaseVolume();
    boolean isVideoSelected();
    boolean isSyncStarted();
    boolean isSyncFinished();
    boolean isVideoPlaying();
    boolean isVideoPaused();
    boolean isVideoStopped();
    int getVideoPosition();
    VideoFile getVideoFile();
    void deleteSyncedVideo();
    void resetCamera();
}
