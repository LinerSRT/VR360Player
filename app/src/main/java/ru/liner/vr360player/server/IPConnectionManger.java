package ru.liner.vr360player.server;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ru.liner.vr360player.asocket.ASocket;
import ru.liner.vr360player.asocket.ISocket;
import ru.liner.vr360player.asocket.udp.UDPServer;
import ru.liner.vr360player.server.packet.DeviceConnectPacket;
import ru.liner.vr360player.server.packet.DeviceDisconnectPacket;
import ru.liner.vr360player.server.packet.DevicePacket;
import ru.liner.vr360player.utils.Comparator;
import ru.liner.vr360player.utils.Constant;
import ru.liner.vr360player.utils.Lists;
import ru.liner.vr360player.utils.Networks;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class IPConnectionManger {
    private final ASocket socket;
    private final List<DeviceConnectPacket> connectedDevices;

    public IPConnectionManger(Callback callback) {
        this.connectedDevices = new ArrayList<>();
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
            if (stringData.contains(DeviceConnectPacket.class.getSimpleName())) {
                DeviceConnectPacket packet = new Gson().fromJson(stringData, DeviceConnectPacket.class);
                if (!isDeviceConnected(packet.host)) {
                    connectedDevices.add(packet);
                    callback.onDeviceConnected(packet);
                }
            }
            if (stringData.contains(DeviceDisconnectPacket.class.getSimpleName())) {
                DeviceDisconnectPacket disconnectPacket = new Gson().fromJson(stringData, DeviceDisconnectPacket.class);
                int index = indexOf(disconnectPacket.host);
                if (index != -1) {
                    connectedDevices.remove(index);
                    callback.onDeviceDisconnected(disconnectPacket);
                }
            }
            if (stringData.contains(DevicePacket.class.getSimpleName())) {
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

    public List<DeviceConnectPacket> getConnectedDevices() {
        return connectedDevices;
    }

    public ASocket getSocket() {
        return socket;
    }

    public boolean isDeviceConnected(@NonNull String host) {
        if(!Networks.isValidHost(host))
            return false;
        return Lists.contains(connectedDevices, new Comparator<DeviceConnectPacket, String>(host) {
            @Override
            public boolean compare(DeviceConnectPacket one, String other) {
                return one.host.equals(other);
            }
        });
    }

    public int indexOf(@NonNull String host) {
        if(!Networks.isValidHost(host))
            return -1;
        return Lists.indexOf(connectedDevices, new Comparator<DeviceConnectPacket, String>(host) {
            @Override
            public boolean compare(DeviceConnectPacket one, String other) {
                return one.host.equals(other);
            }
        });
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
