package it.turin.hermesserver.network;

import it.turin.hermesserver.model.ServerModel;
import it.turin.hermesserver.service.RequestService;
import it.turin.hermesserver.tasks.RequestHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server socket TCP dell'applicazione Hermes.
 *
 * <p>La classe apre una {@link ServerSocket} sulla porta configurata, accetta
 * connessioni client e affida ogni richiesta a un {@link RequestHandler}
 * eseguito da un pool di thread.</p>
 */
public class HermesServerSocket implements Runnable {
    private final int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running = false;
    private final boolean serviceAlive;
    private final ServerModel serverModel;
    private final RequestService requestService;

    /**
     * Crea il server socket applicativo.
     *
     * @param port porta TCP su cui mettersi in ascolto
     * @param serverModel modello usato per registrare i log del server
     * @param requestService servizio richieste condiviso dai gestori client
     * @param serviceAlive indica se il servizio mailbox e' disponibile
     */
    public HermesServerSocket(int port, ServerModel serverModel, RequestService requestService, boolean serviceAlive) {
        this.port = port;
        this.serverModel = serverModel;
        this.requestService = requestService;
        this.serviceAlive = serviceAlive;
    }

    /**
     * Inizializza il pool di thread, apre la socket server e avvia il thread di ascolto.
     *
     * <p>In caso di errore di apertura della porta, l'eccezione viene registrata
     * nel modello e il ciclo di ascolto non viene avviato.</p>
     */
    public void init () {
        serverModel.addLog(Thread.currentThread().getName() + " - Initializing server socket");
        threadPool = Executors.newFixedThreadPool(9);
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            new Thread(this, "SERVER-SOCKET").start();
        } catch (IOException e) {
            serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
        }
    }

    /**
     * Esegue il ciclo di accettazione delle connessioni client.
     *
     * <p>Per ogni connessione accettata crea un nuovo {@link RequestHandler} e
     * lo sottomette al pool di thread.</p>
     */
    @Override
    public void run() {
        serverModel.addLog(Thread.currentThread().getName() + " started, listening on : " + serverSocket.getLocalPort());
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                serverModel.addLog(Thread.currentThread().getName() + " - connection from: " + socket.getInetAddress() + ":" + socket.getPort());
                threadPool.execute(new RequestHandler(socket, this.serverModel, this.requestService, serviceAlive));
            } catch (IOException e) {
                serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
            }
        }
    }

    /**
     * Arresta il server socket.
     *
     * <p>Il metodo interrompe il ciclo di ascolto, avvia lo shutdown del pool
     * di thread e chiude la {@link ServerSocket} se presente.</p>
     */
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
