package ru.liner.vr360server.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
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
    private LImageButton selectVideo;
    private LImageButton syncDevicesButton;

    private List<Client> clientList;

    private GenericAdapter adapter;
    private RecyclerView recyclerView;
    private TextView noDevicesWarn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        tcpServer = new TCPServer(Constant.SERVER_TCP_CONNECTION_PORT);
        tcpServer.setITCPCallback(this);
        tcpServer.start();
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.deviceRecycler);
        noDevicesWarn = findViewById(R.id.noDevicesWarn);
        syncDevicesButton = findViewById(R.id.syncDevicesButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GenericAdapter(recyclerView);
        adapter.register(R.layout.device_list_binder, DeviceBinder.class, Client.class);
        recyclerView.setAdapter(adapter);
        clientList = new ArrayList<>();
        ipPublisher = new IPPublisher();
        ipPublisher.start();
        selectVideo = findViewById(R.id.selectVideo);
        selectVideo.setClickCallback(button -> {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choose video to stream"), Constant.CHOOSE_VIDEO_REQUEST_CORE);
        });
        syncDevicesButton.setClickCallback(new LImageButton.Callback() {
            @Override
            public void onClick(LImageButton button) {
                clientList.clear();
                for(TCPDevice client:tcpServer.getClients()){
                    clientList.add(new Client(Networks.getHost(client.getInetAddress()), client.getInetAddress().getHostName(), ClientStatus.WAITING));
                }
                runOnUiThread(() -> {
                    adapter.set(clientList);
                    noDevicesWarn.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.CHOOSE_VIDEO_REQUEST_CORE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (mediaStreamingServer != null) {
                        mediaStreamingServer.closeAllConnections();
                        mediaStreamingServer.stop();
                    }
                    mediaStreamingServer = new MediaStreamingServer(Files.getRealPathFromURI(this, uri), Constant.SERVER_STREAM_VIDEO_PORT);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            tcpServer.sendToAll("play");
                        }
                    }, 1000);
                    try {
                        mediaStreamingServer.start(500);
                        Toast.makeText(this, "Server started: " + mediaStreamingServer.getServerUrl(Networks.getLocalIpAddress()), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    public void onConnected(TCPDevice device) {
        clientList.clear();
        for(TCPDevice client:tcpServer.getClients()){
            clientList.add(new Client(Networks.getHost(client.getInetAddress()), client.getInetAddress().getHostName(), ClientStatus.WAITING));
        }
        runOnUiThread(() -> {
            adapter.set(clientList);
            noDevicesWarn.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDisconnected(InetAddress inetAddress) {
        clientList.clear();
        for(TCPDevice client:tcpServer.getClients()){
            clientList.add(new Client(Networks.getHost(client.getInetAddress()), client.getInetAddress().getHostName(), ClientStatus.WAITING));
        }
        runOnUiThread(() -> {
            adapter.set(clientList);
            noDevicesWarn.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onConnectionFailed(InetAddress inetAddress) {

    }

    @Override
    public void onReceived(InetAddress inetAddress, byte[] data) {

    }

    @Override
    public void onReceived(InetAddress inetAddress, String data) {
        switch (data){
            case "disconnect":
                tcpServer.disconnectClient(Networks.getHost(inetAddress));
                break;
        }
    }
}
