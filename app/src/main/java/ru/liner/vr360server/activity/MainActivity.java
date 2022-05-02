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
import java.util.Timer;
import java.util.TimerTask;

import ru.liner.vr360server.CoreActivity;
import ru.liner.vr360server.R;
import ru.liner.vr360server.recycler.binder.DeviceBinder;
import ru.liner.vr360server.recycler.genericadapter.GenericAdapter;
import ru.liner.vr360server.recycler.model.ConnectedDevice;
import ru.liner.vr360server.server.IPPublisher;
import ru.liner.vr360server.server.MediaStreamingServer;
import ru.liner.vr360server.tcp.TCPServer;
import ru.liner.vr360server.utils.Comparator;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Lists;
import ru.liner.vr360server.utils.Networks;
import ru.liner.vr360server.utils.VideoFile;
import ru.liner.vr360server.views.BaseDialog;
import ru.liner.vr360server.views.BaseDialogBuilder;
import ru.liner.vr360server.views.LImageButton;


@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("WrongConstant")
public class MainActivity extends CoreActivity implements TCPServer.Callback {
    public static TCPServer tcpServer;
    private IPPublisher ipPublisher;
    private MediaStreamingServer mediaStreamingServer;
    private GenericAdapter adapter;

    private RecyclerView deviceRecycler;
    private TextView emptyDevices;
    private LinearLayout streamingInfoLayout;
    private TextView streamingHost;
    private TextView connectedDeviceCount;
    private TextView streamingPath;
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


    private boolean networkingStarted;
    private boolean streamingStarted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        findViews();
        ipPublisher = new IPPublisher();
        tcpServer = new TCPServer(Constant.SERVER_TCP_CONNECTION_PORT);
        deviceRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GenericAdapter(deviceRecycler);
        adapter.register(R.layout.device_list_binder, DeviceBinder.class, ConnectedDevice.class);
        deviceRecycler.setAdapter(adapter);
        selectVideoButton.setEnabled(false);
        playStopButton.setEnabled(false);


        startIPPublisher.setClickCallback(button -> {
            startIPPublisher.setEnabled(false);
            if (!networkingStarted) {
                networkingStarted = true;
                if (ipPublisher == null)
                    ipPublisher = new IPPublisher();
                ipPublisher.start();
                tcpServer.start(this);
                selectVideoButton.setEnabled(true);
                playStopButton.setEnabled(false);
                setApplicationStatus("Waiting for clients");
            } else {
                networkingStarted = false;
                ipPublisher.interrupt();
                ipPublisher = null;
                tcpServer.stop();
                adapter.clear();
                selectVideoButton.setEnabled(false);
                playStopButton.setEnabled(false);
                streamingInfoLayout.setVisibility(View.GONE);
                setApplicationStatus("Connections disabled");
            }
            startIPPublisher.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, networkingStarted ? R.color.red : R.color.green)));
        });


        selectVideoButton.setClickCallback(button -> {
            if (!streamingStarted) {
               BaseDialog baseDialog =  new BaseDialogBuilder(MainActivity.this)
                        .setDialogTitle("Choose video")
                        .setDialogText("Select video for stream")
                        .setDialogType(BaseDialogBuilder.Type.VIDEO_CHOOSE)
                        .setVideoPickListener(new BaseDialog.BaseDialogVideoPickListener() {
                            @Override
                            public void onVideoSelected(VideoFile videoModel) {
                                MainActivity.this.videoFile = videoModel;
                                stopMediaServer();
                                startMediaServer();
                                if (!tcpServer.getClientSocketList().isEmpty()) {
                                    selectVideoButton.setEnabled(false);
                                    playStopButton.setEnabled(false);
                                    tcpServer.sendToAll("prepare_stream@http://" + Networks.getLocalIpAddress() + ":" + Constant.SERVER_STREAM_VIDEO_PORT + "@" + videoModel.getDisplayName());
                                    setApplicationStatus("Synchronizing stream with clients");
                                }
                                streamingInfoLayout.setVisibility(View.VISIBLE);
                                streamingHost.setText(String.format("http://%s:%s", Networks.getLocalIpAddress(), Constant.SERVER_STREAM_VIDEO_PORT));
                                connectedDeviceCount.setText(String.valueOf(tcpServer.getClientSocketList().size()));
                                streamingPath.setText(videoModel.getDisplayName());
                            }
                        }).build();
               baseDialog.showDialog();

            } else {
                if (tcpServer != null)
                    tcpServer.sendToAll("stop_stream");
                stopMediaServer();
                streamingStarted = false;
                setApplicationStatus("Ready for start streaming");
            }
            playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, streamingStarted ? R.drawable.ic_baseline_stop_24 : R.drawable.ic_baseline_play_arrow_24));
            playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, streamingStarted ? R.color.red : R.color.green)));
        });


        playStopButton.setClickCallback(button -> {
            if (videoFile == null) {
                setApplicationStatus("No selected video, aborting", 1000);
            } else {
                if (!streamingStarted) {
                    playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_stop_24));
                    playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.red)));
                    setApplicationStatus("Streaming video");
                    if (tcpServer != null)
                        tcpServer.sendToAll("play_stream");
                    streamingStarted = true;
                } else {
                    if (tcpServer != null)
                        tcpServer.sendToAll("pause_stream");
                    //stopMediaServer();
                    streamingStarted = false;
                    playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_play_arrow_24));
                    playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.green)));
                    setApplicationStatus("Ready to stream");
                }
            }
        });
    }

    private void stopMediaServer() {
        if (mediaStreamingServer != null) {
            mediaStreamingServer.closeAllConnections();
            mediaStreamingServer.stop();
        }
    }

    private void startMediaServer() {
        mediaStreamingServer = new MediaStreamingServer(videoFile.getFullPath(), Constant.SERVER_STREAM_VIDEO_PORT);
        try {
            mediaStreamingServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStarted(TCPServer tcpServer) {
        TCPServer.Callback.super.onStarted(tcpServer);
        startIPPublisher.setEnabled(true);
    }

    @Override
    public void onConnected(Socket socket) {
        TCPServer.Callback.super.onConnected(socket);
        ConnectedDevice connectedDevice = new ConnectedDevice(socket);
        if (!Lists.contains(adapter.getItemsList(), new Comparator<ConnectedDevice, ConnectedDevice>(connectedDevice) {
            @Override
            public boolean compare(ConnectedDevice one, ConnectedDevice other) {
                return one.getSocket().equals(other.getSocket());
            }
        })) {
            runOnUiThread(() -> {
                adapter.add(connectedDevice);
                emptyDevices.setVisibility(adapter.getItemCount() != 0 ? View.GONE : View.VISIBLE);
                connectedDeviceCount.setText(String.valueOf(adapter.getItemCount()));
            });
            setApplicationStatus("New device connected " + Networks.getHost(socket.getInetAddress()), 500);
        }
    }


    @Override
    public void onDisconnected(Socket socket) {
        TCPServer.Callback.super.onDisconnected(socket);
        if (socket == null)
            return;

        ConnectedDevice connectedDevice = new ConnectedDevice(socket);
        int index = Lists.indexOf(adapter.getItemsList(), new Comparator<ConnectedDevice, ConnectedDevice>(connectedDevice) {
            @Override
            public boolean compare(ConnectedDevice one, ConnectedDevice other) {
                return one.getSocket().equals(other.getSocket());
            }
        });
        if (index != -1) {
            runOnUiThread(() -> {
                if (index < adapter.getItemCount()) {
                    adapter.remove(index);
                    emptyDevices.setVisibility(adapter.getItemCount() != 0 ? View.GONE : View.VISIBLE);
                    connectedDeviceCount.setText(String.valueOf(adapter.getItemCount()));
                }
            });
            setApplicationStatus("Device disconnected " + Networks.getHost(socket.getInetAddress()), 500);
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
        ConnectedDevice ret = new ConnectedDevice(device);
        int index = Lists.indexOf(adapter.getItemsList(), new Comparator<ConnectedDevice, ConnectedDevice>(ret) {
            @Override
            public boolean compare(ConnectedDevice one, ConnectedDevice other) {
                return one.getSocket().equals(other.getSocket());
            }
        });
        if (index != -1) {
            ConnectedDevice client = adapter.get(index);
            if (data.contains("preparing_started")) {
                client.setFetchingStream(true);
                playStopButton.setEnabled(false);
                selectVideoButton.setEnabled(false);
            } else if (data.contains("preparing_progress")) {
                int progress = Integer.parseInt(data.split("@")[1]);
                int downloadedBytes = Integer.parseInt(data.split("@")[2]);
                int totalBytes = Integer.parseInt(data.split("@")[2]);
                client.setFetchingStream(true);
                client.setFetchProgress(progress);
                client.setFetchDownloaded(downloadedBytes);
                client.setFetchTotal(totalBytes);
            } else if (data.contains("preparing_finished")) {
                client.setReadyToPlay(true);
                client.setFetchingStream(false);
            }
            adapter.set(index, client);
            if (allClientsReceivedVideo()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        selectVideoButton.setEnabled(true);
                        playStopButton.setEnabled(true);
                    }
                });
            }
        }
    }

    private boolean allClientsReceivedVideo() {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ConnectedDevice client = adapter.get(i);
            if (!client.isReadyToPlay())
                return false;
        }
        return true;
    }
}
