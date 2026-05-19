package it.turin.hermesserver.service;

import com.google.gson.Gson;
import it.turin.hermesserver.dto.EmailWrapper;
import it.turin.hermesserver.dto.Response;
import it.turin.hermesserver.model.Email;
import it.turin.hermesserver.model.ServerModel;

import java.util.ArrayList;
import java.util.List;

public class RequestService {
    private static final Gson gson = new Gson();
    private final MailboxService mailboxService;
    private final ServerModel serverModel;

    public RequestService(MailboxService mailboxService, ServerModel serverModel) {
        this.mailboxService = mailboxService;
        this.serverModel = serverModel;
    }

    public String getUser(String account) {
        boolean result = mailboxService.accountExists(account);
        serverModel.addLog(Thread.currentThread().getName() + " - user exists: " + result);
        Response<Void> response = new Response<>(result ? 200 : 404, null);
        return gson.toJson(response);
    }

    public String deleteEmail(String emailId, String account) {
        boolean result = mailboxService.deleteEmail(emailId, account);
        serverModel.addLog(Thread.currentThread().getName() + " - deleted email id: " + emailId + " : " + result);
        Response<Void> response = new Response<>(result ? 200 : 404, null);
        return gson.toJson(response);
    }

    public String postEmail(Email email) {
        List<String> recipients = email.getRecipients();
        List<String> unvalidRecipients = new ArrayList<>();
        boolean result = true;
        for (String m:
                recipients) {
            if (!mailboxService.accountExists(m)) {
                unvalidRecipients.add(m);
                Response<List<String>> response = new Response<>(404, unvalidRecipients);
                return gson.toJson(response);
            } else {
                boolean tempR = mailboxService.saveEmail(email, m);
                result = tempR && result;
            }
        }
        Response<List<String>> response = new Response<>(result ? 200 : 404, unvalidRecipients);
        return gson.toJson(response);
    }

    public String getEmails(String account, int page) {
        EmailWrapper emailWrapper = mailboxService.getEmails(account, page);
        Response<EmailWrapper> response = new Response<>(emailWrapper.getEmails().isEmpty() ? 404 : 200, emailWrapper);
        return gson.toJson(response);
    }

    public String getCount(String account) {
        long count = mailboxService.count(account);
        serverModel.addLog(Thread.currentThread().getName() + " - account: " + account + " - email count: " + count);
        Response<Long> response = new Response<>(200, count);
        return gson.toJson(response);
    }

}
