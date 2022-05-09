package ru.liner.vr360server.utils;

import android.util.Log;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 01.05.2022, воскресенье
 **/
public abstract class Worker implements Runnable {
    protected boolean isRunning;
    private WorkerThread workerThread;

    public Worker() {
    }

    public void start() {
        if ((workerThread != null && workerThread.isAlive()) || isRunning)
            return;
        workerThread = new WorkerThread(this);
        workerThread.start();
        isRunning = true;
    }

    public void stop() {
        if (!isRunning)
            return;
        isRunning = false;
    }


    public abstract void execute();

    public abstract long delay();

    private static class WorkerThread extends Thread {
        public WorkerThread(Runnable runnable) {
            super(runnable);
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            execute();
            sleep(delay());
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {

        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static void runInBackground(Runnable runnable){
        new Thread(runnable).start();
    }
}
