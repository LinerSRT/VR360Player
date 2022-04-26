package ru.liner.vr360player.server;

import android.media.AudioManager;
import android.media.ToneGenerator;

import androidx.core.app.NotificationCompat;


import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import ru.liner.vr360player.asocket.ASocket;
import ru.liner.vr360player.asocket.udp.UDPMulticast;
import ru.liner.vr360player.utils.Constant;
import ru.liner.vr360player.utils.Networks;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class IPPublisher extends Thread{
    private ASocket socket;
    private UDPMulticast udpMulticast;
    private ToneGenerator toneGenerator;

    public IPPublisher() {
        udpMulticast = new UDPMulticast(Constant.IP_REQUEST, Constant.MULTICAST_PORT);
        socket = new ASocket(udpMulticast);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 50);
    }

    @Override
    public synchronized void start() {
        super.start();
        socket.start();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        socket.close();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            byte[] deviceIp = Objects.requireNonNull(Networks.getLocalIpAddress()).getBytes(StandardCharsets.UTF_8);
            socket.write(deviceIp);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
        }
    }
}
