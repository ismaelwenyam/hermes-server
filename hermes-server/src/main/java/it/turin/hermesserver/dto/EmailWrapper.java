package it.turin.hermesserver.dto;

import it.turin.hermesserver.model.Email;

import java.util.List;

/**
 * DTO che raggruppa una pagina di email e il conteggio totale della mailbox.
 *
 * <p>Viene usato nelle risposte dell'endpoint di lettura email per fornire al
 * client sia i dati della pagina richiesta sia il numero complessivo di email
 * disponibili.</p>
 */
public class EmailWrapper {
    private long emailsCount;
    private boolean newMessage;
    private List<Email> emails;

    /**
     * Restituisce il conteggio totale delle email nella mailbox.
     *
     * @return numero totale di email
     */
    public long getEmailsCount() {
        return emailsCount;
    }

    /**
     * Imposta il conteggio totale delle email nella mailbox.
     *
     * @param emailsCount numero totale di email
     */
    public void setEmailsCount(long emailsCount) {
        this.emailsCount = emailsCount;
    }

    /**
     * Restituisce la lista di email della pagina corrente.
     *
     * @return email incluse nella risposta
     */
    public List<Email> getEmails() {
        return emails;
    }

    /**
     * Imposta la lista di email della pagina corrente.
     *
     * @param emails email da includere nella risposta
     */
    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public boolean isNewMessage() {
        return newMessage;
    }

    public void setNewMessage(boolean newMessage) {
        this.newMessage = newMessage;
    }
}
