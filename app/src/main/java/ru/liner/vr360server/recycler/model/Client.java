package ru.liner.vr360server.recycler.model;

import androidx.annotation.NonNull;

import java.net.InetAddress;

import ru.liner.vr360server.utils.Networks;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 28.04.2022, четверг
 **/
public class Client {
    @NonNull
    private final String host;
    @NonNull
    private final String name;
    @ClientStatus
    private int status;

    public Client(@NonNull String host, @NonNull String name, @ClientStatus int status) {
        this.host = host;
        this.name = name;
        this.status = status;
    }

    @NonNull
    public String getHost() {
        return host;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @ClientStatus
    public int getStatus() {
        return status;
    }

    public void setStatus(@ClientStatus int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Client{" +
                "host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    public static Client from(InetAddress inetAddress){
        return new Client(Networks.getHost(inetAddress), inetAddress.getHostName(), ClientStatus.WAITING);
    }
}
