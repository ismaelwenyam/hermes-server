package it.turin.hermesserver.service;

import com.google.gson.Gson;
import it.turin.hermesserver.dto.EmailWrapper;
import it.turin.hermesserver.dto.Response;
import it.turin.hermesserver.model.Email;
import it.turin.hermesserver.model.ServerModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Servizio applicativo che espone le operazioni richieste dai client.
 *
 * <p>La classe traduce le chiamate del dispatcher in operazioni su
 * {@link MailboxService} e restituisce sempre risposte serializzate in JSON
 * tramite {@link Response}.</p>
 */
public class RequestService {
    private static final Gson gson = new Gson();
    private final MailboxService mailboxService;
    private final ServerModel serverModel;

    /**
     * Crea un servizio richieste.
     *
     * @param mailboxService servizio che gestisce mailbox e persistenza
     * @param serverModel modello usato per registrare i log del server
     */
    public RequestService(MailboxService mailboxService, ServerModel serverModel) {
        this.mailboxService = mailboxService;
        this.serverModel = serverModel;
    }

    /**
     * Verifica l'esistenza di un account.
     *
     * @param account indirizzo email dell'account da cercare
     * @return JSON di una {@link Response} con stato {@code 200} se l'account
     *         esiste, {@code 404} altrimenti
     */
    public String getUser(String account) {
        boolean result = mailboxService.accountExists(account);
        serverModel.addLog(Thread.currentThread().getName() + " - user exists: " + result);
        Response<Void> response = new Response<>(result ? 200 : 404, null);
        return gson.toJson(response);
    }

    /**
     * Elimina un'email da una mailbox.
     *
     * @param emailId identificativo dell'email da eliminare
     * @param account account proprietario della mailbox
     * @return JSON di una {@link Response} con stato {@code 200} se
     *         l'eliminazione riesce, {@code 404} altrimenti
     */
    public String deleteEmail(String emailId, String account) {
        boolean result = mailboxService.deleteEmail(emailId, account);
        serverModel.addLog(Thread.currentThread().getName() + " - deleted email id: " + emailId + " : " + result);
        Response<Void> response = new Response<>(result ? 200 : 404, null);
        return gson.toJson(response);
    }

    /**
     * Salva un'email nelle mailbox di tutti i destinatari validi.
     *
     * <p>Se almeno un destinatario non esiste, l'operazione termina subito e
     * restituisce una risposta {@code 404} contenente i destinatari non validi
     * individuati fino a quel momento.</p>
     *
     * @param email email da recapitare
     * @return JSON di una {@link Response} con stato {@code 200} se il
     *         salvataggio riesce per tutti i destinatari, {@code 404} altrimenti
     */
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

    /**
     * Restituisce una pagina di email per l'account indicato.
     *
     * @param account account proprietario della mailbox
     * @param page indice della pagina da leggere, a partire da {@code 0}
     * @return JSON di una {@link Response} con corpo {@link EmailWrapper};
     *         lo stato e' {@code 200} se sono presenti email, {@code 404}
     *         se la pagina risulta vuota
     */
    public String getEmails(String account, int page) {
        EmailWrapper emailWrapper = mailboxService.getEmails(account, page);
        Response<EmailWrapper> response = new Response<>(emailWrapper.getEmails().isEmpty() ? 404 : 200, emailWrapper);
        return gson.toJson(response);
    }

    /**
     * Restituisce il numero di email presenti nella mailbox dell'account.
     *
     * @param account account da interrogare
     * @return JSON di una {@link Response} con stato {@code 200} e corpo pari
     *         al conteggio corrente
     */
    public String getCount(String account) {
        long count = mailboxService.count(account);
        serverModel.addLog(Thread.currentThread().getName() + " - account: " + account + " - email count: " + count);
        Response<Long> response = new Response<>(200, count);
        return gson.toJson(response);
    }

}
