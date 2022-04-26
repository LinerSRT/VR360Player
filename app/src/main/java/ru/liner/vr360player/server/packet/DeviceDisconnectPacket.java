package ru.liner.vr360player.server.packet;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class DeviceDisconnectPacket extends Packet{
    public String reason;
    public final String packetName = "DeviceDisconnectPacket";

    @Override
    public String toString() {
        return "DeviceDisconnectPacket{" +
                "host='" + host + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
