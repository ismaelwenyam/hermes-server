package it.turin.hermesserver.network;

import it.turin.hermesserver.model.ServerModel;
import it.turin.hermesserver.service.RequestService;
import it.turin.hermesserver.tasks.RequestHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HermesServerSocket implements Runnable {
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running = false;
    private final ServerModel serverModel;
    private final RequestService requestService;

    public HermesServerSocket(int port, ServerModel serverModel, RequestService requestService) {
        this.port = port;
        this.serverModel = serverModel;
        this.requestService = requestService;
    }

    public void init () {
        serverModel.addLog(Thread.currentThread().getName() + " - Initializing server socket");
        threadPool = Executors.newFixedThreadPool(9);
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            new Thread(this, "server-socket").start();
        } catch (IOException e) {
            serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
        }
    }

    @Override
    public void run() {
        serverModel.addLog(Thread.currentThread().getName() + " started, listening on : " + serverSocket.getLocalPort());
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                serverModel.addLog(Thread.currentThread().getName() + " - connection from: " + socket.getInetAddress() + ":" + socket.getPort());
                threadPool.execute(new RequestHandler(socket, this.serverModel, this.requestService));
            } catch (IOException e) {
                serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
            }
        }
    }

    public void stop () {
        running = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
        }
        serverModel.addLog(Thread.currentThread().getName() + " stoped");
    }
}
