package ru.liner.vr360player.server;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.nio.charset.StandardCharsets;

import ru.liner.vr360player.asocket.ASocket;
import ru.liner.vr360player.asocket.ISocket;
import ru.liner.vr360player.asocket.udp.UDPServer;
import ru.liner.vr360player.server.packet.DeviceConnectPacket;
import ru.liner.vr360player.server.packet.DeviceDisconnectPacket;
import ru.liner.vr360player.server.packet.DevicePacket;
import ru.liner.vr360player.server.packet.Packet;
import ru.liner.vr360player.utils.Constant;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class IPConnectionManger {
    private final ASocket socket;
    private final Callback callback;

    public IPConnectionManger(Callback callback) {
        this.callback = callback;
        socket = new ASocket(new UDPServer(Constant.TCP_CONNECTION_PORT));
        socket.setOnSocketStateListener(new ISocket.OnSocketStateListener() {
            @Override
            public void onStarted() {
                callback.onConnectionCreated();
            }

            @Override
            public void onClosed() {
                callback.onConnectionClosed();
            }

            @Override
            public void onException(Exception e) {
                callback.onException(e);
            }
        });
        socket.setOnMessageReceivedListener(data -> {
            String stringData = new String(data, StandardCharsets.UTF_8);
            if(stringData.contains(DeviceConnectPacket.class.getSimpleName())){
                callback.onDeviceConnected(new Gson().fromJson(stringData, DeviceConnectPacket.class));
            }
            if(stringData.contains(DeviceDisconnectPacket.class.getSimpleName())){
                callback.onDeviceDisconnected(new Gson().fromJson(stringData, DeviceDisconnectPacket.class));
            }
            if(stringData.contains(DevicePacket.class.getSimpleName())){
                callback.onDeviceChanged(new Gson().fromJson(stringData, DevicePacket.class));
            }
            callback.onRawDataReceived(data);
        });
    }

    public void start() {
        socket.start();
    }

    public void stop() {
        socket.closeAndQuit();
    }


    public interface Callback {
        void onConnectionCreated();

        void onConnectionClosed();

        void onException(Exception e);

        void onDeviceConnected(DeviceConnectPacket packet);

        void onDeviceDisconnected(DeviceDisconnectPacket packet);

        void onDeviceChanged(DevicePacket packet);

        void onRawDataReceived(byte[] bytes);

        String getHostAddress();
    }
}
