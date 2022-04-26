package ru.liner.vr360player.server.packet;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class DeviceConnectPacket extends Packet{
    public String deviceName;
    public long deviceLocalTime;
    public final String packetName = "DeviceConnectPacket";

    @Override
    public String toString() {
        return "DeviceConnectPacket{" +
                "host='" + host + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceLocalTime=" + deviceLocalTime +
                '}';
    }
}
