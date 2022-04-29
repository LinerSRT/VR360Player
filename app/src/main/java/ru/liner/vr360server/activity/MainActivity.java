package ru.liner.vr360server.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import ru.liner.vr360server.CoreActivity;
import ru.liner.vr360server.R;
import ru.liner.vr360server.recycler.binder.DeviceBinder;
import ru.liner.vr360server.recycler.genericadapter.GenericAdapter;
import ru.liner.vr360server.recycler.model.Client;
import ru.liner.vr360server.recycler.model.ClientStatus;
import ru.liner.vr360server.server.IPPublisher;
import ru.liner.vr360server.server.MediaStreamingServer;
import ru.liner.vr360server.tcp.ITCPCallback;
import ru.liner.vr360server.tcp.TCPDevice;
import ru.liner.vr360server.tcp.TCPServer;
import ru.liner.vr360server.utils.Comparator;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Files;
import ru.liner.vr360server.utils.Lists;
import ru.liner.vr360server.utils.Networks;
import ru.liner.vr360server.views.LImageButton;


@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("WrongConstant")
public class MainActivity extends CoreActivity implements ITCPCallback {
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
    private LImageButton openStreamButton;
    private LImageButton playStopButton;
    private LImageButton selectVideoButton;


    private String selectedVideoPath;
    private boolean streamingStarted;

    private void createSockets() {
        tcpServer = new TCPServer(Constant.SERVER_TCP_CONNECTION_PORT);
        tcpServer.setITCPCallback(this);
        ipPublisher = new IPPublisher();
    }

    private void findViews() {
        deviceRecycler = findViewById(R.id.deviceRecycler);
        emptyDevices = findViewById(R.id.emptyDevices);
        streamingInfoLayout = findViewById(R.id.streamingInfoLayout);
        streamingHost = findViewById(R.id.streamingHost);
        connectedDeviceCount = findViewById(R.id.connectedDeviceCount);
        streamingPath = findViewById(R.id.streamingPath);
        applicationStatusText = findViewById(R.id.applicationStatusText);
        openStreamButton = findViewById(R.id.openStreamButton);
        playStopButton = findViewById(R.id.playStopButton);
        selectVideoButton = findViewById(R.id.selectVideoButton);
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
        createSockets();
        ipPublisher.start();
        tcpServer.start();
        setApplicationStatus("Starting networking", 1500);
        deviceRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GenericAdapter(deviceRecycler);
        adapter.register(R.layout.device_list_binder, DeviceBinder.class, Client.class);
        deviceRecycler.setAdapter(adapter);
        selectVideoButton.setClickCallback(button -> {
            if(streamingStarted){
                tcpServer.sendToAll("force_stop");
                stopMediaServer();
                streamingStarted = false;
                playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_play_arrow_24));
                playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.green)));
                setApplicationStatus("Ready to stream");
            }
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choose video to stream"), Constant.CHOOSE_VIDEO_REQUEST_CORE);
        });
        playStopButton.setClickCallback(button -> {
            if (TextUtils.isEmpty(selectedVideoPath)) {
                setApplicationStatus("No selected video, aborting", 1000);
            } else {
                if (!streamingStarted) {
                    playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_stop_24));
                    playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.red)));
                    setApplicationStatus("Streaming video");
                    tcpServer.sendToAll("start_video");
                    streamingStarted = true;
                } else {
                    tcpServer.sendToAll("force_stop");
                    stopMediaServer();
                    streamingStarted = false;
                    playStopButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_play_arrow_24));
                    playStopButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.green)));
                    setApplicationStatus("Ready to stream");
                }
            }
        });
        openStreamButton.setClickCallback(button -> {
            if (streamingStarted) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(streamingHost.getText().toString())));
            } else if (!TextUtils.isEmpty(selectedVideoPath)) {
                setApplicationStatus("No streaming video, aborting", 2000);
            } else {
                setApplicationStatus("Select video to open stream", 2000);
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
        mediaStreamingServer = new MediaStreamingServer(selectedVideoPath, Constant.SERVER_STREAM_VIDEO_PORT);
        try {
            mediaStreamingServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.CHOOSE_VIDEO_REQUEST_CORE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    selectedVideoPath = Files.getRealPathFromURI(this, data.getData());
                    stopMediaServer();
                    startMediaServer();
                    tcpServer.sendToAll("start_download");
                    setApplicationStatus("Video selected, ready to play");
                    streamingInfoLayout.setVisibility(View.VISIBLE);
                    streamingHost.setText(String.format("http://%s:%s", Networks.getLocalIpAddress(), Constant.SERVER_STREAM_VIDEO_PORT));
                    connectedDeviceCount.setText(String.valueOf(adapter.getItemCount()));
                    streamingPath.setText(selectedVideoPath);
                }
            } else {
                setApplicationStatus("Failed video selecting, try again", 1000);
            }
        }
    }


    @Override
    public void onConnected(TCPDevice device) {
        Client client = new Client(Networks.getHost(device.getInetAddress()), device.getInetAddress().getCanonicalHostName(), ClientStatus.WAITING);
        if (!Lists.contains(adapter.getItemsList(), new Comparator<Client, Client>(client) {
            @Override
            public boolean compare(Client one, Client other) {
                return one.getHost().equals(other.getHost()) || one.getName().equals(other.getName());
            }
        })) {
            runOnUiThread(() -> {
                adapter.add(client);
                emptyDevices.setVisibility(adapter.getItemCount() != 0 ? View.GONE : View.VISIBLE);
                connectedDeviceCount.setText(String.valueOf(adapter.getItemCount()));
            });
            setApplicationStatus("New device connected " + Networks.getHost(device.getInetAddress()), 500);
        }
    }

    @Override
    public void onDisconnected(InetAddress inetAddress) {
        Client client = new Client(Networks.getHost(inetAddress), inetAddress.getCanonicalHostName(), ClientStatus.WAITING);
        int index = Lists.indexOf(adapter.getItemsList(), new Comparator<Client, Client>(client) {
            @Override
            public boolean compare(Client one, Client other) {
                return one.getHost().equals(other.getHost()) || one.getName().equals(other.getName());
            }
        });
        if (index != -1) {
            runOnUiThread(() -> {
                adapter.remove(index);
                emptyDevices.setVisibility(adapter.getItemCount() != 0 ? View.GONE : View.VISIBLE);
                connectedDeviceCount.setText(String.valueOf(adapter.getItemCount()));
            });
            setApplicationStatus("Device disconnected " + Networks.getHost(inetAddress), 500);
        }
    }

    @Override
    public void onConnectionFailed(InetAddress inetAddress) {
        setApplicationStatus("Failed to connect with " + Networks.getHost(inetAddress), 500);
    }

    @Override
    public void onReceived(InetAddress inetAddress, byte[] data) {

    }

    @Override
    public void onReceived(InetAddress inetAddress, String data) {
        if (data.equals("disconnect")) {
            tcpServer.disconnectClient(Networks.getHost(inetAddress));
        } else if (data.contains("download_update")) {
            Client client = new Client(Networks.getHost(inetAddress), inetAddress.getCanonicalHostName(), ClientStatus.WAITING);
            int index = Lists.indexOf(adapter.getItemsList(), new Comparator<Client, Client>(client) {
                @Override
                public boolean compare(Client one, Client other) {
                    return one.getHost().equals(other.getHost()) || one.getName().equals(other.getName());
                }
            });
            if (index != -1) {
                client = adapter.get(index);
                String[] dataSplit = data.split("@");
                if (dataSplit.length >= 4) {
                    if(TextUtils.isDigitsOnly(dataSplit[1])){
                        client.setDownloadProgress(Integer.parseInt(dataSplit[1]));
                    } else {
                        return;
                    }
                    if(TextUtils.isDigitsOnly(dataSplit[2])){
                        client.setDownloadedBytes(Integer.parseInt(dataSplit[2]));
                    } else {
                        return;
                    }
                    if(TextUtils.isDigitsOnly(dataSplit[3])){
                        client.setTotalBytes(Integer.parseInt(dataSplit[3]));
                    } else {
                        return;
                    }
                    client.setStatus(client.getDownloadProgress() >= 100 ? ClientStatus.READY : ClientStatus.DOWNLOADING_VIDEO);
                    Client finalClient = client;
                    runOnUiThread(() -> adapter.set(index, finalClient));
                }
            }
        } else if (data.equals("download_finished")) {
            Client client = new Client(Networks.getHost(inetAddress), inetAddress.getCanonicalHostName(), ClientStatus.WAITING);
            int index = Lists.indexOf(adapter.getItemsList(), new Comparator<Client, Client>(client) {
                @Override
                public boolean compare(Client one, Client other) {
                    return one.getHost().equals(other.getHost()) || one.getName().equals(other.getName());
                }
            });
            if (index != -1) {
                client = adapter.get(index);
                client.setStatus(ClientStatus.READY);
                Client finalClient = client;
                runOnUiThread(() -> {
                    adapter.set(index, finalClient);
                    playStopButton.setEnabled(allClientsReceivedVideo());
                });
                tcpServer.sendTo(inetAddress, "download_accepted");
            }
        }
    }

    private boolean allClientsReceivedVideo() {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Client client = adapter.get(i);
            if (client.getStatus() != ClientStatus.READY)
                return false;
        }
        return true;
    }
}
