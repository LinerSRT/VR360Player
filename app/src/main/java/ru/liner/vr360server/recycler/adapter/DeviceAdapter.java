package ru.liner.vr360server.recycler.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.activity.IVideoStream;
import ru.liner.vr360server.recycler.model.ConnectedDevice;
import ru.liner.vr360server.utils.FileUtils;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private IServer server;
    private IVideoStream videoStream;
    private List<ConnectedDevice> connectedDeviceList;

    public DeviceAdapter(IServer server, IVideoStream videoStream) {
        this.server = server;
        this.videoStream = videoStream;
        this.connectedDeviceList = new ArrayList<>();
    }

    public void addDevice(ConnectedDevice connectedDevice) {
        connectedDeviceList.add(connectedDevice);
        notifyItemInserted(connectedDeviceList.size() - 1);
    }

    public void removeDevice(ConnectedDevice connectedDevice) {
        int index = connectedDeviceList.indexOf(connectedDevice);
        connectedDeviceList.remove(index);
        notifyItemRemoved(index);
    }

    public boolean containDevice(ConnectedDevice connectedDevice) {
        if (connectedDeviceList.isEmpty())
            return false;
        for (ConnectedDevice device : connectedDeviceList)
            if (
                    device.getSocket().getInetAddress().getHostAddress().equals(connectedDevice.getSocket().getInetAddress().getHostAddress()) ||
                            device.getSocket().getInetAddress().getHostName().equals(connectedDevice.getSocket().getInetAddress().getHostName()) ||
                            device.getSocket().getInetAddress().getCanonicalHostName().equals(connectedDevice.getSocket().getInetAddress().getCanonicalHostName()) ||
                            device.getSocket().getPort() == connectedDevice.getSocket().getPort()
            )
                return true;
        return false;
    }

    public int getDeviceIndex(ConnectedDevice connectedDevice) {
        for (int i = 0; i < connectedDeviceList.size(); i++)
            if (connectedDeviceList.get(i).getSocket().equals(connectedDevice.getSocket()))
                return i;
        return -1;
    }

    public ConnectedDevice getConnectedDevice(int index) {
        return connectedDeviceList.get(index);
    }

    public void updateDevice(ConnectedDevice connectedDevice) {
        int index = getDeviceIndex(connectedDevice);
        if (index != -1) {
            connectedDeviceList.set(index, connectedDevice);
            notifyItemChanged(index);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_binder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ConnectedDevice connectedDevice = connectedDeviceList.get(position);
        holder.deviceAllowPlay.setChecked(connectedDevice.isAllowPlay());
        holder.deviceAllowPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedDevice.setAllowPlay(holder.deviceAllowPlay.isChecked());
                notifyItemChanged(position);
            }
        });
//        holder.diconnectDevice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                server.sendToSocket(connectedDevice, "disconnect");
//            }
//        });
//        holder.devicePlayPause.setClickCallback(new LImageButton.Callback() {
//            @Override
//            public void onClick(LImageButton button) {
//                if (videoStream.isVideoPaused(connectedDevice)) {
//                    videoStream.playVideo(connectedDevice);
//                    holder.devicePlayPause.setImageDrawable(ContextCompat.getDrawable(button.getContext(), R.drawable.ic_baseline_play_arrow_24));
//                } else {
//                    videoStream.stopVideo(connectedDevice);
//                    holder.devicePlayPause.setImageDrawable(ContextCompat.getDrawable(button.getContext(), R.drawable.ic_baseline_pause_24));
//                }
//            }
//        });
//        holder.deviceVolumeDown.setClickCallback(new LImageButton.Callback() {
//            @Override
//            public void onClick(LImageButton button) {
//                videoStream.decreaseVolume(connectedDevice);
//            }
//        });
//        holder.deviceVolumeUp.setClickCallback(new LImageButton.Callback() {
//            @Override
//            public void onClick(LImageButton button) {
//                videoStream.increaseVolume(connectedDevice);
//            }
//        });
        holder.deviceStatusProgressBar.setVisibility(View.GONE);
        holder.deviceProgressBarData.setVisibility(View.GONE);
        holder.deviceStatusText.setVisibility(View.GONE);
        holder.mediaControlLayout.setVisibility(View.GONE);
        holder.lockView.setVisibility(View.GONE);
        holder.deviceHostName.setText(String.format("%s (%s)", connectedDevice.getSocket().getInetAddress().getCanonicalHostName(), connectedDevice.getSocket().getInetAddress().getHostAddress()));
        if (connectedDevice.isFetchingStream()) {
            holder.deviceStatusText.setVisibility(View.VISIBLE);
            holder.deviceProgressBarData.setVisibility(View.VISIBLE);
            holder.deviceStatusProgressBar.setVisibility(View.VISIBLE);
            holder.deviceStatusText.setText("Receiving video");
            holder.deviceStatusProgressBar.setProgress(connectedDevice.getDownloadProgress().downloadProgress);
            holder.deviceProgressBarData.setText(String.format("%s/%s", FileUtils.humanReadableByteCount(connectedDevice.getDownloadProgress().downloadedBytes), FileUtils.humanReadableByteCount(connectedDevice.getDownloadProgress().totalBytes)));
            holder.devicePing.setText(FileUtils.humanReadableByteCount(connectedDevice.getDownloadProgress().downloadSpeed) + "/s");
        } else if (connectedDevice.isReadyToPlay()) {
            holder.deviceStatusText.setVisibility(View.VISIBLE);
            holder.deviceStatusText.setText("Ready for play");
            holder.mediaControlLayout.setVisibility(View.VISIBLE);
            holder.lockView.setVisibility(View.VISIBLE);
        } else {
            holder.deviceStatusText.setVisibility(View.VISIBLE);
            holder.deviceStatusText.setText("Waiting for action");
        }
        if (!connectedDevice.isFetchingStream())
            holder.devicePing.setText(connectedDevice.getPingMs() + "ms");
    }

    @Override
    public int getItemCount() {
        return connectedDeviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceHostName;
        private TextView deviceStatusText;
        private ProgressBar deviceStatusProgressBar;
        private TextView deviceProgressBarData;
        private TextView devicePing;
        private LinearLayout mediaControlLayout;
        private Button diconnectDevice;
        private Button devicePlayPause;
        private ImageView lockView;
        private Button deviceVolumeDown;
        private Button deviceVolumeUp;
        private Switch deviceAllowPlay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lockView = itemView.findViewById(R.id.lockView);
            devicePlayPause = itemView.findViewById(R.id.devicePlayPause);
            deviceVolumeDown = itemView.findViewById(R.id.deviceVolumeDown);
            deviceVolumeUp = itemView.findViewById(R.id.deviceVolumeUp);
            deviceHostName = itemView.findViewById(R.id.deviceHostName);
            deviceStatusText = itemView.findViewById(R.id.deviceStatusText);
            deviceStatusProgressBar = itemView.findViewById(R.id.clientProgressBar);
            deviceProgressBarData = itemView.findViewById(R.id.clientProgressBarText);
            devicePing = itemView.findViewById(R.id.devicePing);
            mediaControlLayout = itemView.findViewById(R.id.mediaControlLayout);
            deviceAllowPlay = itemView.findViewById(R.id.deviceAllowPlay);
            diconnectDevice = itemView.findViewById(R.id.diconnectDevice);
//            new Timer().scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    if (!connectedDeviceList.isEmpty())
//                        server.sendPing(getConnectedDevice(Math.max(0, Math.min(getAdapterPosition(), connectedDeviceList.size()))));
//                }
//            }, 0, 5000);
        }
    }
}
