package ru.liner.vr360server.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.net.InetAddress;

import ru.liner.vr360server.CoreActivity;
import ru.liner.vr360server.R;
import ru.liner.vr360server.server.IPPublisher;
import ru.liner.vr360server.server.MediaStreamingServer;
import ru.liner.vr360server.tcp.ITCPCallback;
import ru.liner.vr360server.tcp.TCPDevice;
import ru.liner.vr360server.tcp.TCPServer;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Files;
import ru.liner.vr360server.utils.Networks;
import ru.liner.vr360server.views.LImageButton;


@SuppressWarnings("FieldCanBeLocal")
@SuppressLint("WrongConstant")
public class MainActivity extends CoreActivity implements ITCPCallback {
    private TCPServer tcpServer;
    private IPPublisher ipPublisher;
    private MediaStreamingServer mediaStreamingServer;


    private LImageButton selectVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tcpServer = new TCPServer(Constant.SERVER_TCP_CONNECTION_PORT);
        tcpServer.setITCPCallback(this);
        ipPublisher = new IPPublisher();
        mediaStreamingServer = new MediaStreamingServer(Constant.SERVER_STREAM_VIDEO_PORT);
        tcpServer.start();
        ipPublisher.start();
        selectVideo = findViewById(R.id.selectVideo);
        selectVideo.setClickCallback(button -> {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choose video to stream"), Constant.CHOOSE_VIDEO_REQUEST_CORE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.CHOOSE_VIDEO_REQUEST_CORE) {
                if (data != null) {
                    Uri uri = data.getData();
                    mediaStreamingServer.setFilePath(Files.getRealPathFromURI(this, uri));
                    Toast.makeText(this, "Server started: " + mediaStreamingServer.getServerUrl(Networks.getLocalIpAddress()), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onConnected(TCPDevice device) {

    }

    @Override
    public void onDisconnected(InetAddress inetAddress) {

    }

    @Override
    public void onConnectionFailed(InetAddress inetAddress) {

    }

    @Override
    public void onReceived(InetAddress inetAddress, byte[] data) {

    }

    @Override
    public void onReceived(InetAddress inetAddress, String data) {

    }
}
