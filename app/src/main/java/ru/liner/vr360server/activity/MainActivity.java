package ru.liner.vr360server.activity;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import ru.liner.vr360server.CoreActivity;
import ru.liner.vr360server.R;
import ru.liner.vr360server.recycler.adapter.DeviceAdapter;
import ru.liner.vr360server.recycler.model.ConnectedDevice;
import ru.liner.vr360server.recycler.model.DownloadProgress;
import ru.liner.vr360server.server.IPPublisher;
import ru.liner.vr360server.server.MediaStreamingServer;
import ru.liner.vr360server.tcp.TCPServer;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Networks;
import ru.liner.vr360server.utils.Utils;
import ru.liner.vr360server.utils.VideoFile;
import ru.liner.vr360server.views.BaseDialog;
import ru.liner.vr360server.views.BaseDialogBuilder;
import ru.liner.vr360server.views.LImageButton;
import ru.liner.vr360server.views.MarqueeTextView;


@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("WrongConstant")
public class MainActivity extends CoreActivity implements IServer, IVideoStream {
    private boolean mediaServerRunning;
    private boolean isIPPublisherRunning;
    private boolean syncVideoStarted;

    public static TCPServer tcpServer;
    private IPPublisher ipPublisher;
    private MediaStreamingServer mediaStreamingServer;
    private DeviceAdapter deviceAdapter;

    private RecyclerView deviceRecycler;
    private TextView emptyDevices;
    private LinearLayout streamingInfoLayout;
    private TextView streamingHost;
    private TextView connectedDeviceCount;
    private MarqueeTextView streamingPath;
    private TextView applicationStatusText;
    private LImageButton playStopButton;
    private LImageButton selectVideoButton;
    private LImageButton startIPPublisher;


    private VideoFile videoFile;


    private void findViews() {
        deviceRecycler = findViewById(R.id.deviceRecycler);
        emptyDevices = findViewById(R.id.emptyDevices);
        streamingInfoLayout = findViewById(R.id.streamingInfoLayout);
        streamingHost = findViewById(R.id.streamingHost);
        connectedDeviceCount = findViewById(R.id.connectedDeviceCount);
        streamingPath = findViewById(R.id.streamingPath);
        applicationStatusText = findViewById(R.id.applicationStatusText);
        playStopButton = findViewById(R.id.playStopButton);
        selectVideoButton = findViewById(R.id.selectVideoButton);
        startIPPublisher = findViewById(R.id.startIPPublisher);
    }

    private void setApplicationStatus(@NonNull String text) {
        runOnUiThread(() -> applicationStatusText.setText(text));
    }

    private void setApplicationStatus(@NonNull String text, int timeout) {
        String ret = applicationStatusText.getText().toString();
        runOnUiThread(() -> applicationStatusText.setText(text));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                setApplicationStatus(ret);
            }
        }, timeout);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        findViews();
        deviceRecycler.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new DeviceAdapter(this, this);
        deviceRecycler.setAdapter(deviceAdapter);
        selectVideoButton.setEnabled(false);
        playStopButton.setEnabled(false);
        startIPPublisher.setClickCallback(button -> {
            startIPPublisher.setEnabled(false);
            if (!isServerRunning()) {
                startIPPublisher();
                startServer();
                selectVideoButton.setEnabled(true);
                playStopButton.setEnabled(false);
                setApplicationStatus("Waiting for clients");
            } else {
                stopIPPublisher();
                stopServer();
                videoFile = null;
                selectVideoButton.setEnabled(false);
                playStopButton.setEnabled(false);
                streamingInfoLayout.setVisibility(View.GONE);
                setApplicationStatus("Connections disabled");
            }
            startIPPublisher.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, isServerRunning() ? R.color.red : R.color.green)));
        });


        selectVideoButton.setClickCallback(button -> {
            if (!isVideoSelected()) {
                selectVideo();
            } else {
                sendToAll("stop_stream");
                stopMediaServer();
                setApplicationStatus("Ready for start streaming");
            }
            playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, isVideoSelected() ? R.drawable.ic_baseline_stop_24 : R.drawable.ic_baseline_play_arrow_24));
            playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, isVideoSelected() ? R.color.red : R.color.green)));
            playStopButton.setEnabled(false);
        });


        playStopButton.setClickCallback(button -> {
            if (videoFile == null) {
                setApplicationStatus("No selected video, aborting", 2000);
            } else {
                if (!isVideoPlaying()) {
                    playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_stop_24));
                    playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.red)));
                    setApplicationStatus("Streaming video");
                    sendToAll("play_stream");
                } else {
                    sendToAll("pause_stream");
                    //stopMediaServer();
                    playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_play_arrow_24));
                    playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.green)));
                    setApplicationStatus("Ready to stream");
                }
            }
        });
    }

    @Override
    public void startServer() {
        if (tcpServer == null)
            tcpServer = new TCPServer(Constant.SERVER_TCP_CONNECTION_PORT);
        tcpServer.start(new TCPServer.Callback() {
            @Override
            public void onStarted(TCPServer tcpServer) {
                TCPServer.Callback.super.onStarted(tcpServer);
                startIPPublisher.setEnabled(true);
            }

            @Override
            public void onConnected(Socket socket) {
                TCPServer.Callback.super.onConnected(socket);
                ConnectedDevice connectedDevice = new ConnectedDevice(socket);
                if (!deviceAdapter.containDevice(connectedDevice))
                    runOnUiThread(() -> onDeviceConnect(connectedDevice));
            }

            @Override
            public void onDisconnected(Socket socket) {
                TCPServer.Callback.super.onDisconnected(socket);
                ConnectedDevice connectedDevice = new ConnectedDevice(socket);
                int index = deviceAdapter.getDeviceIndex(connectedDevice);
                if (index != -1) {
                    runOnUiThread(() -> {
                        MainActivity.this.onDeviceDisconnected(deviceAdapter.getConnectedDevice(index));
                    });
                }
            }

            @Override
            public void onStopped(TCPServer tcpServer) {
                TCPServer.Callback.super.onStopped(tcpServer);
                startIPPublisher.setEnabled(true);
            }


            @Override
            public void onReceived(Socket device, String data) {
                TCPServer.Callback.super.onReceived(device, data);
                ConnectedDevice connectedDevice = new ConnectedDevice(device);
                int index = deviceAdapter.getDeviceIndex(connectedDevice);
                if (index != -1) {
                    MainActivity.this.onReceived(deviceAdapter.getConnectedDevice(index), data);
                }
            }
        });
    }

    @Override
    public void stopServer() {
        if (tcpServer != null)
            tcpServer.stop();
    }

    @Override
    public boolean isServerRunning() {
        return tcpServer != null && tcpServer.isRunning();
    }

    @Override
    public void startMediaServer(VideoFile videoFile) {
        mediaStreamingServer = new MediaStreamingServer(videoFile.getFullPath(), Constant.SERVER_STREAM_VIDEO_PORT);
        try {
            mediaStreamingServer.start();
            mediaServerRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopMediaServer() {
        if (mediaStreamingServer != null) {
            mediaStreamingServer.closeAllConnections();
            mediaStreamingServer.stop();
            mediaServerRunning = false;
        }
    }

    @Override
    public boolean isMediaServerRunning() {
        return mediaServerRunning;
    }


    @Override
    public void startIPPublisher() {
        if (ipPublisher == null)
            ipPublisher = new IPPublisher();
        ipPublisher.start();
        isIPPublisherRunning = true;
    }

    @Override
    public void stopIPPublisher() {
        if (ipPublisher != null) {
            ipPublisher.interrupt();
            ipPublisher = null;
            isIPPublisherRunning = false;
        }
    }

    @Override
    public boolean isIPPublisherRunning() {
        return isIPPublisherRunning;
    }

    @Override
    public void onDeviceConnect(ConnectedDevice connectedDevice) {
        deviceAdapter.addDevice(connectedDevice);
        emptyDevices.setVisibility(deviceAdapter.getItemCount() != 0 ? View.GONE : View.VISIBLE);
        connectedDeviceCount.setText(String.valueOf(deviceAdapter.getItemCount()));
        setApplicationStatus("New device connected " + Networks.getHost(connectedDevice.getSocket().getInetAddress()), 2000);
    }

    @Override
    public void onDeviceDisconnected(ConnectedDevice connectedDevice) {
        deviceAdapter.removeDevice(connectedDevice);
        emptyDevices.setVisibility(deviceAdapter.getItemCount() != 0 ? View.GONE : View.VISIBLE);
        connectedDeviceCount.setText(String.valueOf(deviceAdapter.getItemCount()));
        setApplicationStatus("Device disconnected " + Networks.getHost(connectedDevice.getSocket().getInetAddress()), 2000);
    }

    @Override
    public void onReceived(ConnectedDevice connectedDevice, String command) {
        if (command.contains("preparing_started")) {
            connectedDevice.setFetchingStream(true);
            playStopButton.setEnabled(false);
            selectVideoButton.setEnabled(false);
        } else if (command.contains("preparing_progress")) {
            String[] dataset = command.split("@");
            System.out.println(Arrays.toString(dataset));
            if(dataset.length >= 4) {
                connectedDevice.setFetchingStream(true);
                DownloadProgress downloadProgress = connectedDevice.getDownloadProgress();
                if (downloadProgress == null)
                    downloadProgress = new DownloadProgress();
                if (Utils.isNumeric(dataset[1]))
                    downloadProgress.downloadProgress = (int) Long.parseLong(dataset[1]);
                if (Utils.isNumeric(dataset[2]))
                    downloadProgress.downloadedBytes = (int) Long.parseLong(dataset[2]);
                if (Utils.isNumeric(dataset[3]))
                    downloadProgress.totalBytes = (int) Long.parseLong(dataset[3]);
                if (Utils.isNumeric(dataset[4]))
                    downloadProgress.downloadSpeed = (int) Long.parseLong(dataset[4]);
                connectedDevice.setDownloadProgress(downloadProgress);
            }
        } else if (command.contains("preparing_finished")) {
            connectedDevice.setReadyToPlay(true);
            connectedDevice.setFetchingStream(false);
        } else if (command.contains("answer_ping")) {
            connectedDevice.setPingMs(System.currentTimeMillis() - pingStart);
        }
        deviceAdapter.updateDevice(connectedDevice);
        if (isSyncStarted() && isSyncFinished()) {
            selectVideoButton.setEnabled(true);
            playStopButton.setEnabled(true);
        }
    }

    @Override
    public void sendToAll(String command) {
        if (isServerRunning())
            tcpServer.sendToAll(command);
    }

    @Override
    public void sendTo(ConnectedDevice connectedDevice, String command) {
        if (isServerRunning())
            tcpServer.sendTo(connectedDevice.getSocket(), command);
    }

    private long pingStart;

    @Override
    public void sendPing(ConnectedDevice connectedDevice) {
        pingStart = System.currentTimeMillis();
        sendTo(connectedDevice, "check_ping");

    }


    @Override
    public void selectVideo() {
        BaseDialog baseDialog = new BaseDialogBuilder(MainActivity.this)
                .setDialogTitle("Choose video")
                .setDialogText("Select video for stream")
                .setDialogType(BaseDialogBuilder.Type.VIDEO_CHOOSE)
                .setVideoPickListener(videoModel -> {
                    videoFile = videoModel;
                    stopMediaServer();
                    startMediaServer(videoModel);
                    if (!tcpServer.getClientSocketList().isEmpty()) {
                        playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, isVideoPlaying() ? R.drawable.ic_baseline_stop_24 : R.drawable.ic_baseline_play_arrow_24));
                        playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, isVideoPlaying() ? R.color.red : R.color.green)));
                        selectVideoButton.setEnabled(false);
                        playStopButton.setEnabled(false);
                        syncVideo();
                    }
                    streamingInfoLayout.setVisibility(View.VISIBLE);
                    streamingHost.setText(String.format("http://%s:%s", Networks.getLocalIpAddress(), Constant.SERVER_STREAM_VIDEO_PORT));
                    connectedDeviceCount.setText(String.valueOf(tcpServer.getClientSocketList().size()));
                    streamingPath.setText(videoModel.getDisplayName());
                }).build();
        baseDialog.showDialog();
    }


    @Override
    public void syncVideo(ConnectedDevice connectedDevice) {
        sendTo(connectedDevice, "prepare_stream@http://" + Networks.getLocalIpAddress() + ":" + Constant.SERVER_STREAM_VIDEO_PORT + "@" + videoFile.getDisplayName());
        setApplicationStatus("Synchronizing stream with clients");
        syncVideoStarted = true;
    }
    @Override
    public void syncVideo() {
        sendToAll("prepare_stream@http://" + Networks.getLocalIpAddress() + ":" + Constant.SERVER_STREAM_VIDEO_PORT + "@" + videoFile.getDisplayName());
        setApplicationStatus("Synchronizing stream with clients");
        syncVideoStarted = true;
    }

    @Override
    public void playVideo(ConnectedDevice connectedDevice) {
        sendTo(connectedDevice, "play_stream");
    }

    @Override
    public void playVideo() {
        sendToAll("play_stream");
    }

    @Override
    public void pauseVideo(ConnectedDevice connectedDevice) {
        sendTo(connectedDevice, "pause_stream");
    }

    @Override
    public void pauseVideo() {
        sendToAll("pause_stream");
    }

    @Override
    public void stopVideo(ConnectedDevice connectedDevice) {
        sendTo(connectedDevice, "stop_stream");

    }

    @Override
    public void stopVideo() {
        sendToAll("stop_stream");
    }

    @Override
    public void seekVideo(ConnectedDevice connectedDevice, int position) {
        sendTo(connectedDevice, "seek_stream@"+position);

    }

    @Override
    public void seekVideo(int position) {
        sendToAll( "seek_stream@"+position);
    }

    @Override
    public void increaseVolume(ConnectedDevice connectedDevice) {
        sendTo(connectedDevice, "volume_up");

    }

    @Override
    public void increaseVolume() {
        sendToAll("volume_up");
    }

    @Override
    public void decreaseVolume(ConnectedDevice connectedDevice) {
        sendTo(connectedDevice,"volume_down");
    }

    @Override
    public void decreaseVolume() {
        sendToAll("volume_down");

    }

    @Override
    public boolean isVideoSelected() {
        return videoFile != null;
    }

    @Override
    public boolean isSyncStarted() {
        return syncVideoStarted;
    }

    @Override
    public boolean isSyncFinished(ConnectedDevice connectedDevice) {
        return false;
    }

    @Override
    public boolean isSyncFinished() {
        for (int i = 0; i < deviceAdapter.getItemCount(); i++) {
            ConnectedDevice client = deviceAdapter.getConnectedDevice(i);
            if (!client.isReadyToPlay())
                return false;
        }
        syncVideoStarted = false;
        return true;
    }

    @Override
    public boolean isVideoPlaying(ConnectedDevice connectedDevice) {
        return false;
    }

    @Override
    public boolean isVideoPlaying() {
        return false;
    }

    @Override
    public boolean isVideoPaused(ConnectedDevice connectedDevice) {
        return false;
    }

    @Override
    public boolean isVideoPaused() {
        return false;
    }

    @Override
    public boolean isVideoStopped(ConnectedDevice connectedDevice) {
        return false;
    }

    @Override
    public boolean isVideoStopped() {
        return false;
    }

    @Override
    public int getVideoPosition(ConnectedDevice connectedDevice) {
        return 0;
    }

    @Override
    public int getVideoPosition() {
        return 0;
    }

    @Override
    public VideoFile getVideoFile() {
        return videoFile;
    }

    @Override
    public void deleteSyncedVideo(ConnectedDevice connectedDevice) {

    }

    @Override
    public void deleteSyncedVideo() {

    }

    @Override
    public void resetCamera(ConnectedDevice connectedDevice) {
        sendTo(connectedDevice, "reset_camera");
    }

    @Override
    public void resetCamera() {
        sendToAll( "reset_camera");
    }
}
