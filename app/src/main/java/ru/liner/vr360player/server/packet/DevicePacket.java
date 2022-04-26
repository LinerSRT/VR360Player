package ru.liner.vr360player.server.packet;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class DevicePacket extends Packet{
    private String deviceName;
    private String videoHash;
    private long playingProgress;
    public final String packetName = "DevicePacket";
    @Override
    public String toString() {
        return "DevicePacket{" +
                "deviceName='" + deviceName + '\'' +
                ", videoHash='" + videoHash + '\'' +
                ", playingProgress=" + playingProgress +
                ", host='" + host + '\'' +
                '}';
    }
}
