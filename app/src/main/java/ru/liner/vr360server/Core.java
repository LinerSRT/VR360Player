package ru.liner.vr360server;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.net.Socket;

import ru.liner.vr360server.tcp.TCPClient;
import ru.liner.vr360server.tcp.TCPServer;
import ru.liner.vr360server.utils.Constant;
import ru.liner.vr360server.utils.Networks;
import ru.liner.vr360server.utils.PM;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 22.04.2022, пятница
 **/
public class Core extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        PM.init(this);

    }

    public static Context getContext() {
        return context;
    }
}
