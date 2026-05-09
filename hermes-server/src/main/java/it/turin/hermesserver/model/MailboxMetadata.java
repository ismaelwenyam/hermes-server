package it.turin.hermesserver.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

public class MailboxMetadata implements Serializable {
    private static final long serialVersionUID = -5684418935487L;

    private long emailGId;
    private long lastKnownId = 0L;
    private int nrEmailsDelivering = 3;
    private long emailCount = 0L;
    private final Date creationDate;
    private Date updateDate;

    public MailboxMetadata(long emailGId) {
        this.emailGId = emailGId;
        this.creationDate = Date.from(Instant.now());
    }

    public synchronized long getLastKnownId() {return lastKnownId;}
    public synchronized int getNrEmailsDelivering() {return nrEmailsDelivering;}
    public synchronized long getEmailGId() {return emailGId;}
    public synchronized long getEmailCount() {return emailCount;}

    public synchronized void setLastKnownId(long id) {
        lastKnownId = id;
        updateDate = Date.from(Instant.now());
    }

    public synchronized void incrementEmailGId(){
        emailGId++;
        updateDate = Date.from(Instant.now());
    }

    public synchronized void setnrEmailsDelivering(int nr) {
        nrEmailsDelivering = nr;
        updateDate = Date.from(Instant.now());
    }

    public synchronized void incrementEmailCount() {
        emailCount++;
        updateDate = Date.from(Instant.now());
    }

    public synchronized void decrementEmailCount() {
        emailCount--;
        updateDate = Date.from(Instant.now());
    }
}
