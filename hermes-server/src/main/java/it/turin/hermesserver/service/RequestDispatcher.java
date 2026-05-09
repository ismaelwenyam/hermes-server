package it.turin.hermesserver.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.turin.hermesserver.dto.Request;
import it.turin.hermesserver.dto.Response;
import it.turin.hermesserver.model.Email;
import it.turin.hermesserver.model.ServerModel;

public class RequestDispatcher {

    private static final Gson gson = new Gson();

    private RequestDispatcher() {}

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
                    return service.getEmails(account, Integer.parseInt(page));
                }

                case POST_EMAIL: {Email mail = gson.fromJson(gson.toJson(request.getBody()), Email.class);
                    return service.postEmail(mail);
                }

                case DELETE_EMAIL: {
                    String emailId = request.getRequestParameter("emailId");
                    String account = request.getRequestParameter("account");
                    return service.deleteEmail(emailId, account);
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

    private static boolean validRequestForOperation(Request<?> request) {
        return switch (request.getEndpoint()) {
            case GET_USER, PING ->
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