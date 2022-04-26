package ru.liner.vr360player.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.liner.vr360player.CoreActivity;
import ru.liner.vr360player.R;
import ru.liner.vr360player.asocket.ASocket;
import ru.liner.vr360player.asocket.ISocket;
import ru.liner.vr360player.asocket.udp.UDPClient;
import ru.liner.vr360player.asocket.udp.UDPMulticast;
import ru.liner.vr360player.utils.Constant;
import ru.liner.vr360player.utils.Networks;
import ru.liner.vr360player.views.LImageButton;


@SuppressLint("WrongConstant")
public class ConnectionActivity extends CoreActivity {
    private ASocket tcpClient;
    private ASocket ipReceiver;
    private String connectionHost;
    private LImageButton reconnectDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        reconnectDevice = findViewById(R.id.reconnectDevice);
        reconnectDevice.setEnabled(false);
        ipReceiver = new ASocket(new UDPMulticast(Constant.IP_REQUEST, Constant.MULTICAST_PORT));
        ipReceiver.setOnMessageReceivedListener(data -> {
            String ipData = new String(data, StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ipData);
            if(matcher.find()){
                connectionHost = matcher.group(0);
                reconnectDevice.setEnabled(true);
            }
        });
        ipReceiver.start();
        reconnectDevice.setClickCallback(new LImageButton.Callback() {
            @Override
            public void onClick(LImageButton button) {
                reconnectDevice.setEnabled(false);
                tcpClient = new ASocket(new UDPClient(connectionHost, Constant.TCP_CONNECTION_PORT));
                tcpClient.setOnSocketStateListener(new ISocket.OnSocketStateListener() {
                    @Override
                    public void onStarted() {
                        //tcpClient.write(new Gson().toJson(new DeviceStatus(Networks.getLocalIpAddress(), true)).getBytes(StandardCharsets.UTF_8));
                        System.out.println("SADASDA");
                    }

                    @Override
                    public void onClosed() {

                    }

                    @Override
                    public void onException(Exception e) {

                    }
                });
                tcpClient.start();
            }
        });
    }
}
