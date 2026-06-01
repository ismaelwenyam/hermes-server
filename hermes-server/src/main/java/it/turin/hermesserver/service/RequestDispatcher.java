package it.turin.hermesserver.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.turin.hermesserver.dto.Request;
import it.turin.hermesserver.dto.Response;
import it.turin.hermesserver.model.Email;
import it.turin.hermesserver.model.ServerModel;

/**
 * Dispatcher statico delle richieste JSON ricevute dal server.
 *
 * <p>La classe deserializza una {@link Request}, verifica endpoint e parametri
 * minimi richiesti, quindi invoca il metodo corrispondente di
 * {@link RequestService}. Tutte le risposte vengono restituite come JSON.</p>
 */
public class RequestDispatcher {

    private static final Gson gson = new Gson();

    /**
     * Impedisce l'istanziazione della classe di utilita'.
     */
    private RequestDispatcher() {}

    /**
     * Gestisce una richiesta JSON del client.
     *
     * @param jsonRequest payload JSON ricevuto dalla socket
     * @param service servizio applicativo da invocare
     * @param serverModel modello usato per registrare i log
     * @return JSON di risposta con stato applicativo e corpo coerenti con
     *         l'endpoint richiesto
     */
    public static String handleRequest(String jsonRequest, RequestService service, ServerModel serverModel) {
        try {
            Request<?> request = gson.fromJson(jsonRequest, Request.class);
            if (request == null) {
                return gson.toJson(new Response<>(400, "Request body is null"));
            }
            if (request.getEndpoint() == null) {
                return gson.toJson(new Response<>(400, "Endpoint is missing"));
            }
            if (!validRequestForOperation(request)) {
                return gson.toJson(new Response<>(400, "Invalid request"));
            }
            switch (request.getEndpoint()) {
                case PING: {
                    String account = request.getRequestParameter("account");
                    serverModel.addLog(Thread.currentThread().getName() + " - user: " + account + " ping");
                    return gson.toJson(new Response<Void>(200, null));
                }

                case GET_USER: {
                    String account = request.getRequestParameter("account");
                    return service.getUser(account);
                }

                case GET_EMAILS: {
                    String account = request.getRequestParameter("account");
                    String page = request.getRequestParameter("page");
                    String fetchNewMail = request.getRequestParameter("newMail");
                    return service.getEmails(account, Integer.parseInt(page), Boolean.parseBoolean(fetchNewMail));
                }

                case POST_EMAIL: {Email mail = gson.fromJson(gson.toJson(request.getBody()), Email.class);
                    return service.postEmail(mail);
                }

                case DELETE_EMAIL: {
                    String emailId = request.getRequestParameter("emailId");
                    String account = request.getRequestParameter("account");
                    return service.deleteEmail(emailId, account);
                }

                case COUNT : {
                    String account = request.getRequestParameter("account");
                    return service.getCount(account);
                }

                default:
                    return gson.toJson(new Response<>(400, "Unknown endpoint"));
            }

        } catch (JsonSyntaxException e) {
            return gson.toJson(new Response<>(400, "Malformed JSON request"));
        } catch (NumberFormatException e) {
            return gson.toJson(new Response<>(400, "Invalid numeric parameter"));
        } catch (Exception e) {
            return gson.toJson(new Response<String>(500, "Internal server error"));
        }
    }

    /**
     * Verifica che una richiesta contenga i campi minimi richiesti dal suo endpoint.
     *
     * @param request richiesta gia' deserializzata
     * @return {@code true} se la richiesta e' compatibile con l'operazione,
     *         {@code false} altrimenti
     */
    private static boolean validRequestForOperation(Request<?> request) {
        return switch (request.getEndpoint()) {
            case GET_USER, PING, COUNT ->
                    request.validParams("account");
            case GET_EMAILS ->
                    request.validParams("account", "page");
            case DELETE_EMAIL ->
                    request.validParams("emailId", "account");
            case POST_EMAIL ->
                    request.validObject();
        };
    }
}
