package it.turin.hermesserver.tasks;

import it.turin.hermesserver.dto.Response;
import it.turin.hermesserver.model.ServerModel;
import it.turin.hermesserver.service.RequestDispatcher;
import it.turin.hermesserver.service.RequestService;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RequestHandler implements Runnable {

    private static final Gson gson = new Gson();

    private final Socket socket;
    private final ServerModel serverModel;
    private final RequestService requestService;

    public RequestHandler(Socket socket, ServerModel serverModel, RequestService requestService) {
        this.socket = socket;
        this.serverModel = serverModel;
        this.requestService = requestService;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        serverModel.addLog(threadName + " started");
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true)) {
            String jsonRequest = in.readLine();
            serverModel.addLog(threadName + " received request: " + jsonRequest);
            if (jsonRequest == null) {
                serverModel.addLog(threadName + " - client closed connection");
                return;
            }

            if (jsonRequest.trim().isEmpty()) {
                serverModel.addLog(threadName + " - empty request");
                String errorResponse = gson.toJson(new Response<>(400, "Empty request"));
                out.println(errorResponse);
                return;
            }
            String jsonResponse = RequestDispatcher.handleRequest(jsonRequest, requestService, serverModel);

            serverModel.addLog(threadName + " sending response...");
            out.println(jsonResponse);
            serverModel.addLog(threadName + " response sent: " + jsonResponse);
        } catch (IOException e) {
            serverModel.addLog(threadName + " - [IO ERROR] - " + e.getMessage());
        } catch (Exception e) {
            serverModel.addLog(threadName + " - [UNEXPECTED ERROR] - " + e.getMessage());
        } finally {
            closeSocket(threadName);
            serverModel.addLog(threadName + " end execution");
        }
    }

    private void closeSocket(String threadName) {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            serverModel.addLog(threadName + " - [SOCKET CLOSE ERROR] - " + e.getMessage());
        }
    }
}