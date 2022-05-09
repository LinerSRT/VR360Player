package ru.liner.vr360server.fragments;

import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.net.Socket;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.activity.MainActivity;
import ru.liner.vr360server.recycler.adapter.ClientAdapter;
import ru.liner.vr360server.server.Client;
import ru.liner.vr360server.views.SwipeButton;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class DevicesFragment extends BaseFragment {
    private ClientAdapter clientAdapter;
    private RecyclerView socketRecycler;
    private SwipeButton startServerButton;
    private TextView socketRecyclerEmpty;

    public DevicesFragment(IServer server) {
        super(server);
    }

    public DevicesFragment() {
        this.server = MainActivity.getServer();
    }

    @Override
    public void declareViews(View view) {
        socketRecycler = find(R.id.socketRecycler);
        startServerButton = find(R.id.startServerButton);
        socketRecyclerEmpty = find(R.id.socketRecyclerEmpty);
    }

    @Override
    public void onFragmentCreated() {
        clientAdapter = new ClientAdapter(server);
        socketRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        socketRecycler.setAdapter(clientAdapter);
        startServerButton.setStateCallback((swipeButton, enabled, fromUser) -> {
            if (enabled) {
                startServerButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_red));
                server.showNotification("Server started!", "Waiting for client connection", R.color.primaryColor);
                server.startServer();

            } else {
                startServerButton.setButtonBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_rounded_primary));
                server.showNotification("Server stopped!", "All connections has been closed", R.color.red);
                server.stopServer();
            }
        });
    }

    @Override
    public int getLayoutRes() {
        return R.layout.devices_fragment;
    }

    @Override
    public void onSocketConnected(Socket socket, int position) {
        super.onSocketConnected(socket, position);
        clientAdapter.add(socket);
        socketRecyclerEmpty.setVisibility(clientAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSocketDisconnected(Socket socket, int position) {
        super.onSocketDisconnected(socket, position);
        clientAdapter.remove(socket);
        socketRecyclerEmpty.setVisibility(clientAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void onClientChanged(Socket socket, Client client) {
        client.socket = socket;
        clientAdapter.update(client);
    }


    @Override
    public void onReceived(Socket socket, String command) {
        super.onReceived(socket, command);
        if (command.contains("@")) {
            String[] data = command.split("@");
            if (data.length == 2 && data[0].equals("Client"))
                onClientChanged(socket, new Gson().fromJson(data[1], Client.class));
        }
    }

}
