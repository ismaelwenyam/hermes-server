package it.turin.hermesserver.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Rappresenta un'email gestita dal server Hermes.
 *
 * <p>L'oggetto e' serializzabile per poter essere salvato su file system. Il
 * campo {@code ID} viene assegnato dal servizio mailbox al momento del
 * salvataggio.</p>
 */
public class Email implements Serializable {

    private static final long serialVersionUID = -21548911354897L;

    private long ID;
    private final String sender;
    private final List<String> recipients;
    private final String argument;
    private final String mailBody;
    private final Date sentDate;

    /**
     * Crea un'email con mittente, destinatari, oggetto, corpo e data di invio.
     *
     * @param sender mittente dell'email
     * @param recipients lista dei destinatari
     * @param argument oggetto dell'email
     * @param mailBody corpo testuale dell'email
     * @param sentDate data di invio
     */
    public Email(String sender, List<String> recipients, String argument, String mailBody, Date sentDate) {
        this.sender = sender;
        this.recipients = recipients;
        this.argument = argument;
        this.mailBody = mailBody;
        this.sentDate = sentDate;
    }

    /**
     * Restituisce l'identificativo assegnato all'email.
     *
     * @return identificativo dell'email
     */
    public long getID() {return this.ID;}

    /**
     * Imposta l'identificativo dell'email.
     *
     * @param id identificativo assegnato dal servizio mailbox
     */
    public void setID(long id){this.ID = id;}

    /**
     * Restituisce la data di invio.
     *
     * @return data di invio dell'email
     */
    public Date getSentDate() {return this.sentDate;}

    /**
     * Restituisce il mittente dell'email.
     *
     * @return indirizzo del mittente
     */
    public String getSender() {return this.sender;}

    /**
     * Restituisce una copia della lista dei destinatari.
     *
     * @return nuova lista contenente i destinatari dell'email
     */
    public List<String> getRecipients(){
        return new ArrayList<>(this.recipients);
    }

    /**
     * Restituisce una rappresentazione testuale dell'email.
     *
     * @return stringa descrittiva dell'email
     */
    @Override
    public String toString() {
        return "Email{" +
                "ID='" + ID + '\'' +
                ", sender='" + sender + '\'' +
                ", recipients=" + recipients +
                ", argument='" + argument + '\'' +
                ", mailBody='" + mailBody + '\'' +
                ", sentDate=" + sentDate +
                '}';
    }
}
