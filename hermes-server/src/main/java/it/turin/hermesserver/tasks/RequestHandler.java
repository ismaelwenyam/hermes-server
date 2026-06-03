package it.turin.hermesserver.tasks;

import it.turin.hermesserver.dto.Response;
import it.turin.hermesserver.model.ServerModel;
import it.turin.hermesserver.service.RequestDispatcher;
import it.turin.hermesserver.service.RequestService;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Task eseguito dal pool del server per gestire una singola connessione client.
 *
 * <p>Il gestore legge una richiesta JSON dalla socket, valida i casi base
 * della connessione e delega l'elaborazione a {@link RequestDispatcher}. La
 * risposta viene inviata al client come singola riga JSON codificata in UTF-8.</p>
 */
public class RequestHandler implements Runnable {

    private static final Gson gson = new Gson();

    private final Socket socket;
    private final ServerModel serverModel;
    private final RequestService requestService;
    private final boolean mailboxServiceAlive;

    /**
     * Crea un gestore per una connessione client.
     *
     * @param socket socket collegata al client
     * @param serverModel modello usato per registrare i log del server
     * @param requestService servizio applicativo usato dal dispatcher
     * @param mailboxServiceAlive indica se il servizio mailbox e' stato
     *                            inizializzato correttamente
     */
    public RequestHandler(Socket socket, ServerModel serverModel, RequestService requestService, boolean mailboxServiceAlive) {
        this.socket = socket;
        this.serverModel = serverModel;
        this.requestService = requestService;
        this.mailboxServiceAlive = mailboxServiceAlive;
    }

    /**
     * Esegue il ciclo di gestione della richiesta.
     *
     * <p>Il metodo legge una sola riga dalla socket, restituisce errori JSON
     * per richieste vuote o servizio mailbox non disponibile, quindi chiude la
     * socket nel blocco {@code finally}.</p>
     */
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName().toUpperCase();
        serverModel.addLog(threadName + " started");
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true)) {
            String jsonRequest = in.readLine();
            if (jsonRequest == null) {
                serverModel.addLog(threadName + " - client closed connection");
                return;
            }
            if (!mailboxServiceAlive) {
                serverModel.addLog("mailbox service not alive, returning 500");
                String errorResponse = gson.toJson(new Response<>(500, "Internal Server Error"));
                out.println(errorResponse);
                return;
            }

            if (jsonRequest.trim().isEmpty()) {
                serverModel.addLog(threadName + " - empty request");
                String errorResponse = gson.toJson(new Response<>(400, "Empty request"));
                out.println(errorResponse);
                return;
            }
            serverModel.addLog(threadName + " received request: " + jsonRequest);
            String jsonResponse = RequestDispatcher.handleRequest(jsonRequest, requestService, serverModel);

            serverModel.addLog(threadName + " sending response...");
            out.println(jsonResponse);
            serverModel.addLog(threadName + " response sent: " + jsonResponse);
        } catch (IOException e) {
            serverModel.addLog(threadName + " - [io error] - " + e.getMessage());
        } catch (Exception e) {
            serverModel.addLog(threadName + " - [unexpected error] - " + e.getMessage());
        } finally {
            closeSocket(threadName);
            serverModel.addLog(threadName + " end execution");
        }
    }

    /**
     * Chiude la socket associata al client, registrando eventuali errori.
     *
     * @param threadName nome del thread usato nei messaggi di log
     */
    private void closeSocket(String threadName) {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            serverModel.addLog(threadName + " - [socket close error] - " + e.getMessage());
        }
    }
}
