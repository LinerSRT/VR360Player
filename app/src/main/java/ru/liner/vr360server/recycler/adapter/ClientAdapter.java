package ru.liner.vr360server.recycler.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.server.ClientStatus;
import ru.liner.vr360server.server.ConnectedClient;
import ru.liner.vr360server.server.DisconnectStatus;
import ru.liner.vr360server.utils.Comparator;
import ru.liner.vr360server.utils.FileUtils;
import ru.liner.vr360server.utils.Lists;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 02.05.2022, понедельник
 **/
public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ViewHolder> {
    private final IServer server;
    private final List<ConnectedClient> connectedClients;

    public ClientAdapter(IServer server) {
        this.server = server;
        this.connectedClients = new ArrayList<>();
    }


    public void add(Socket socket) {
        ConnectedClient client = new ConnectedClient(socket);
        client.hostname = socket.getInetAddress().getHostAddress();
        connectedClients.add(client);
        notifyItemInserted(connectedClients.size() - 1);
    }

    public void remove(Socket socket) {
        int index = Lists.indexOf(connectedClients, new Comparator<ConnectedClient, Socket>(socket) {
            @Override
            public boolean compare(ConnectedClient one, Socket other) {
                return one.hostname.equals(other.getInetAddress().getHostAddress());
            }
        });
        if (index != -1) {
            connectedClients.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Nullable
    public ConnectedClient get(int index) {
        if (index < 0 || index >= connectedClients.size())
            return null;
        return connectedClients.get(index);
    }

    @Nullable
    public ConnectedClient get(Socket socket) {
        int index = Lists.indexOf(connectedClients, new Comparator<ConnectedClient, Socket>(socket) {
            @Override
            public boolean compare(ConnectedClient one, Socket other) {
                return one.hostname.equals(other.getInetAddress().getHostAddress());
            }
        });
        return get(index);
    }

    public void update(ConnectedClient client) {
        int index = Lists.indexOf(connectedClients, new Comparator<ConnectedClient, ConnectedClient>(client) {
            @Override
            public boolean compare(ConnectedClient one, ConnectedClient other) {
                return one.hostname.equals(other.hostname);
            }
        });
        if (index != -1) {
            connectedClients.set(index, client);
            notifyItemChanged(index);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_adapter_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ConnectedClient client = connectedClients.get(position);
        holder.clientProgressBar.setVisibility(View.GONE);
        holder.clientProgressBarText.setVisibility(View.GONE);
        holder.clientHostname.setText(client.hostname);
        if (client.clientStatus != null) {
            if (client.clientStatus.waitingAction) {
                holder.clientStatus.setText("waiting for command");
            } else if (client.clientStatus.downloadingVideo) {
                holder.clientStatus.setText("downloading video");
                if(client.downloadingStatus != null) {
                    holder.clientProgressBar.setVisibility(View.VISIBLE);
                    holder.clientProgressBarText.setVisibility(View.VISIBLE);
                    holder.clientProgressBar.setProgress(client.downloadingStatus.downloadedProgress);
                    holder.clientProgressBarText.setText(
                            String.format("%s/%s (%s/s)",
                                    FileUtils.humanReadableByteCount(client.downloadingStatus.downloadedBytes),
                                    FileUtils.humanReadableByteCount(client.downloadingStatus.totalBytes),
                                    FileUtils.humanReadableByteCount(client.downloadingStatus.downloadingSpeed)
                            ));
                }
            } else if (client.clientStatus.playingVideo) {
                holder.clientStatus.setText("playing video");
            }
        } else {
            holder.clientStatus.setText("waiting for command");
        }
        holder.clientDisconnect.setOnClickListener(v -> {
            DisconnectStatus disconnectStatus = new DisconnectStatus();
            disconnectStatus.exitOnDisconnect = false;
            disconnectStatus.pauseOnDisconnect = true;
            disconnectStatus.shouldReconnect = false;
            server.send(client, disconnectStatus);
        });
    }

    @Override
    public int getItemCount() {
        return connectedClients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView clientHostname;
        private final TextView clientStatus;
        private final ProgressBar clientProgressBar;
        private final TextView clientProgressBarText;
        private final TextView clientDisconnect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clientHostname = itemView.findViewById(R.id.clientHostname);
            clientStatus = itemView.findViewById(R.id.clientStatus);
            clientProgressBar = itemView.findViewById(R.id.clientProgressBar);
            clientProgressBarText = itemView.findViewById(R.id.clientProgressBarText);
            clientDisconnect = itemView.findViewById(R.id.clientDisconnect);
        }
    }
}
