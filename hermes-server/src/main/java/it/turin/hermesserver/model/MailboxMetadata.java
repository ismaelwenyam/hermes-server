package it.turin.hermesserver.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

/**
 * Metadati serializzabili associati a una mailbox.
 *
 * <p>La classe mantiene l'identificativo progressivo delle email, l'ultimo
 * identificativo noto al client, il numero di email da consegnare per pagina e
 * il conteggio totale. I metodi pubblici sono sincronizzati per proteggere le
 * modifiche allo stato interno.</p>
 */
public class MailboxMetadata implements Serializable {
    private static final long serialVersionUID = -5684418935487L;

    private long emailGId;
    private long lastKnownId = 0L;
    private int nrEmailsDelivering = 10;
    private long emailCount = 0L;

    public boolean newMessage() {
        return newMessage;
    }

    public void setNewMessage(boolean newMessage) {
        this.newMessage = newMessage;
    }

    private boolean newMessage;
    private final Date creationDate;
    private Date updateDate;

    /**
     * Crea i metadati iniziali della mailbox.
     *
     * @param emailGId identificativo progressivo iniziale da assegnare alle email
     */
    public MailboxMetadata(long emailGId) {
        this.emailGId = emailGId;
        this.creationDate = Date.from(Instant.now());
    }

    /**
     * Restituisce l'ultimo identificativo noto.
     *
     * @return ultimo identificativo registrato come noto
     */
    public synchronized long getLastKnownId() {return lastKnownId;}

    /**
     * Restituisce il numero di email da consegnare per pagina.
     *
     * @return dimensione della pagina di consegna
     */
    public synchronized int getNrEmailsDelivering() {return nrEmailsDelivering;}

    /**
     * Restituisce il prossimo identificativo progressivo da assegnare.
     *
     * @return identificativo progressivo corrente
     */
    public synchronized long getEmailGId() {return emailGId;}

    /**
     * Restituisce il conteggio totale delle email nella mailbox.
     *
     * @return numero di email presenti
     */
    public synchronized long getEmailCount() {return emailCount;}

    /**
     * Aggiorna l'ultimo identificativo noto e la data di aggiornamento.
     *
     * @param id identificativo da registrare come ultimo noto
     */
    public synchronized void setLastKnownId(long id) {
        lastKnownId = id;
        updateDate = Date.from(Instant.now());
    }

    /**
     * Incrementa l'identificativo progressivo e aggiorna la data di modifica.
     */
    public synchronized void incrementEmailGId(){
        emailGId++;
        updateDate = Date.from(Instant.now());
    }

    /**
     * Imposta il numero di email da consegnare per pagina.
     *
     * @param nr nuova dimensione della pagina di consegna
     */
    public synchronized void setnrEmailsDelivering(int nr) {
        nrEmailsDelivering = nr;
        updateDate = Date.from(Instant.now());
    }

    /**
     * Incrementa il conteggio delle email e aggiorna la data di modifica.
     */
    public synchronized void incrementEmailCount() {
        emailCount++;
        updateDate = Date.from(Instant.now());
    }

    /**
     * Decrementa il conteggio delle email e aggiorna la data di modifica.
     */
    public synchronized void decrementEmailCount() {
        emailCount--;
        updateDate = Date.from(Instant.now());
    }
}
