package ru.liner.vr360server.recycler.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import ru.liner.vr360server.server.Client;
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
    private final List<Client> clients;

    public ClientAdapter(IServer server) {
        this.server = server;
        this.clients = new ArrayList<>();
    }


    public void add(Socket socket) {
        Client client = new Client(socket, socket.getInetAddress().getHostAddress());
        clients.add(client);
        notifyItemInserted(clients.size() - 1);
    }

    public void remove(Socket socket) {
        int index = Lists.indexOf(clients, new Comparator<Client, Socket>(socket) {
            @Override
            public boolean compare(Client one, Socket other) {
                return one.hostname.equals(other.getInetAddress().getHostAddress());
            }
        });
        if (index != -1) {
            clients.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Nullable
    public Client get(int index) {
        if (index < 0 || index >= clients.size())
            return null;
        return clients.get(index);
    }

    @Nullable
    public Client get(Socket socket) {
        int index = Lists.indexOf(clients, new Comparator<Client, Socket>(socket) {
            @Override
            public boolean compare(Client one, Socket other) {
                return one.hostname.equals(other.getInetAddress().getHostAddress());
            }
        });
        return get(index);
    }

    public void update(Client client) {
        int index = Lists.indexOf(clients, new Comparator<Client, Client>(client) {
            @Override
            public boolean compare(Client one, Client other) {
                return one.hostname.equals(other.hostname);
            }
        });
        if (index != -1) {
            clients.set(index, client);
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
        Client client = clients.get(position);
        holder.clientProgressBar.setVisibility(View.GONE);
        holder.clientProgressBarText.setVisibility(View.GONE);
        holder.clientHostname.setText(client.hostname);

        if (client.waitingAction) {
            holder.clientStatus.setText("waiting for command");
        } else if (client.downloadingVideo && !client.downloadingFinished) {
            holder.clientStatus.setText("downloading video");
            holder.clientProgressBar.setVisibility(View.VISIBLE);
            holder.clientProgressBarText.setVisibility(View.VISIBLE);
            holder.clientProgressBar.setProgress(client.downloadedProgress);
            holder.clientProgressBarText.setText(
                    String.format("%s/%s (%s/s)",
                            FileUtils.humanReadableByteCount(client.downloadedBytes),
                            FileUtils.humanReadableByteCount(client.totalBytes),
                            FileUtils.humanReadableByteCount(client.downloadingSpeed)
                    ));

        } else if (client.playingVideo) {
            holder.clientStatus.setText("playing video");
        }
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView clientHostname;
        private final TextView clientStatus;
        private final ProgressBar clientProgressBar;
        private final TextView clientProgressBarText;
        private final ImageView clientDisconnect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clientHostname = itemView.findViewById(R.id.clientHostName);
            clientStatus = itemView.findViewById(R.id.clientStatus);
            clientProgressBar = itemView.findViewById(R.id.clientProgressBar);
            clientProgressBarText = itemView.findViewById(R.id.clientProgressBarText);
            clientDisconnect = itemView.findViewById(R.id.clientDisconnect);
        }
    }
}
