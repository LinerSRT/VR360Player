package ru.liner.vr360player.server;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class SyncPacket {
    private long packetTime;
    private long streamProgress;
    private long bytesStreamed;
    private long bytesTotal;

    public SyncPacket(long packetTime, long streamProgress, long bytesStreamed, long bytesTotal) {
        this.packetTime = packetTime;
        this.streamProgress = streamProgress;
        this.bytesStreamed = bytesStreamed;
        this.bytesTotal = bytesTotal;
    }

    public long getPacketTime() {
        return packetTime;
    }

    public void setPacketTime(long packetTime) {
        this.packetTime = packetTime;
    }

    public long getStreamProgress() {
        return streamProgress;
    }

    public void setStreamProgress(long streamProgress) {
        this.streamProgress = streamProgress;
    }

    public long getBytesStreamed() {
        return bytesStreamed;
    }

    public void setBytesStreamed(long bytesStreamed) {
        this.bytesStreamed = bytesStreamed;
    }

    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    @Override
    public String toString() {
        return "SyncPacket{" +
                "packetTime=" + packetTime +
                ", streamProgress=" + streamProgress +
                ", bytesStreamed=" + bytesStreamed +
                ", bytesTotal=" + bytesTotal +
                '}';
    }
}
