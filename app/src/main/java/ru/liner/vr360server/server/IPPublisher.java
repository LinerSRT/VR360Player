package ru.liner.vr360server.server;


import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import ru.liner.vr360server.tcp.UDPMulticast;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Networks;
import ru.liner.vr360server.utils.Utils;


/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class IPPublisher extends Thread {
    private UDPMulticast udpMulticast;

    public IPPublisher() {
        try {
            udpMulticast = new UDPMulticast(Constant.SERVER_IP_PUBLISHER_HOST, Constant.SERVER_MULTICAST_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() {
        if (udpMulticast == null)
            return;
        super.start();
        udpMulticast.start();
    }

    @Override
    public void interrupt() {
        if (udpMulticast == null)
            return;
        super.interrupt();
        udpMulticast.stop();
    }

    @Override
    public void run() {
        while (!isInterrupted() && udpMulticast != null) {
            udpMulticast.writeString(Objects.requireNonNull(Networks.getLocalIpAddress()));
            Utils.sleep(TimeUnit.SECONDS.toMillis(1));
        }
    }
}
