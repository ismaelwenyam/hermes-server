package it.turin.hermesserver.service;

import it.turin.hermesserver.dto.EmailWrapper;
import it.turin.hermesserver.model.Email;
import it.turin.hermesserver.model.MailboxMetadata;
import it.turin.hermesserver.model.ServerModel;
import it.turin.hermesserver.persistence.FilePersistenceManager;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Servizio thread-safe per la gestione delle mailbox del server.
 *
 * <p>La classe mantiene in memoria i metadati delle mailbox inizializzate e
 * protegge le operazioni concorrenti con un {@link ReentrantReadWriteLock} per
 * account. La persistenza effettiva di email e metadati viene delegata a
 * {@link FilePersistenceManager}.</p>
 */
public class MailboxService {
    private static final String INBOXES_DIR = "C:\\Users\\arnau\\AppData\\Local\\hermes-server";
    private static final String METADATA_DIR = "metadata";
    private static final String MAILBOX_DIR = "mailbox";
    private static final String EXTENSION = ".bin";

    private Map<String, MailboxMetadata> mailboxesMetadata;
    private Map<String, ReentrantReadWriteLock> mailboxesLock;

    private final ServerModel serverModel;
    private final FilePersistenceManager persistenceManager;

    /**
     * Crea il servizio mailbox associandolo al modello del server.
     *
     * @param serverModel modello usato per registrare i log
     */
    public MailboxService(ServerModel serverModel) {
        this.persistenceManager = new FilePersistenceManager();
        this.serverModel = serverModel;
    }


    /**
     * Inizializza le mailbox degli account indicati.
     *
     * <p>Al primo avvio crea directory e file di metadati. Negli avvii
     * successivi rilegge i metadati esistenti e prepara i lock associati a ogni
     * account.</p>
     *
     * @param emails account predefiniti da inizializzare
     * @return {@code true} se l'inizializzazione e' completata senza errori,
     *         {@code false} se almeno un account produce errore
     */
    public boolean initMailBoxes (List<String> emails) {
        mailboxesMetadata = new HashMap<>(emails.size());
        mailboxesLock = new HashMap<>(emails.size());
        boolean positiveInit = true;
        for (String account : emails) {
            if (!accountExists(account)){
                try {
                    persistenceManager.createDirectory(computePath(account));
                    persistenceManager.createDirectory(computePath(account, METADATA_DIR));
                    persistenceManager.createDirectory(computePath(account, MAILBOX_DIR));
                    MailboxMetadata metadata = new MailboxMetadata(1);
                    persistenceManager.writeMetadata(metadata, String.valueOf(1), computePath(account, METADATA_DIR), EXTENSION, false);
                    mailboxesMetadata.put(account, metadata);
                    mailboxesLock.put(account, new ReentrantReadWriteLock());
                } catch (Exception ex) {
                    positiveInit = false;
                    serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + ex.getMessage());
                }
            } else {
                MailboxMetadata metadata = null;
                try {
                    metadata = persistenceManager.readMetadata(computePath(account, METADATA_DIR, String.valueOf(1)).concat(EXTENSION));
                    mailboxesMetadata.put(account, metadata);
                    mailboxesLock.put(account, new ReentrantReadWriteLock());
                    serverModel.addLog(Thread.currentThread().getName() + " - account " + account + " already exists");
                } catch (Exception e) {
                    serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
                }
            }
        }
        if (positiveInit) serverModel.addLog(Thread.currentThread().getName() + " - mailbox manager up and running");
        return positiveInit;
    }

    /**
     * Verifica se esiste una mailbox per l'indirizzo indicato.
     *
     * @param email indirizzo email dell'account
     * @return {@code true} se la directory dell'account esiste, {@code false}
     *         altrimenti
     */
    public boolean accountExists (String email) {
        boolean result = persistenceManager.directoryExists(computePath(email));
        if (!result) serverModel.addLog(Thread.currentThread().getName() + " - account: " + email + " not found");
        return result;
    }

    /**
     * Elimina un'email dalla mailbox di un account.
     *
     * <p>L'operazione acquisisce il lock di scrittura dell'account e, in caso
     * di successo, aggiorna anche il conteggio nei metadati persistiti.</p>
     *
     * @param id identificativo dell'email da eliminare
     * @param account account proprietario della mailbox
     * @return {@code true} se l'email e' stata eliminata e i metadati aggiornati,
     *         {@code false} altrimenti
     */
    public boolean deleteEmail(String id, String account) {
        boolean result = false;
        mailboxesLock.get(account).writeLock().lock();
        boolean ok = persistenceManager.removeEmail(id, computePath(account, MAILBOX_DIR), EXTENSION);
        if (ok) {
            serverModel.addLog(Thread.currentThread().getName() + " - email id: " + id + " successfully removed");
            MailboxMetadata metadata = mailboxesMetadata.get(account);
            metadata.decrementEmailCount();
            try {
                persistenceManager.writeMetadata(metadata, String.valueOf(1), computePath(account, METADATA_DIR), EXTENSION, true);
                result = true;
            } catch (IOException e) {
                serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
            }
        }
        else serverModel.addLog(Thread.currentThread().getName() +  " - email id: " + id + " not removed");
        mailboxesLock.get(account).writeLock().unlock();
        return result;
    }

    /**
     * Salva un'email nella mailbox di un account.
     *
     * <p>L'operazione assegna all'email l'identificativo progressivo della
     * mailbox, scrive il file e aggiorna i metadati dell'account.</p>
     *
     * @param email email da salvare
     * @param account account destinatario
     * @return {@code true} se il salvataggio e l'aggiornamento dei metadati
     *         riescono, {@code false} altrimenti
     */
    public boolean saveEmail(Email email, String account) {
        System.out.println("saving mail for user: " + account);
        mailboxesLock.get(account).writeLock().lock();
        System.out.println("lock for user: " + account);
        try {
            long gId = mailboxesMetadata.get(account).getEmailGId();
            Email mailCopy = copyEmail(email);
            mailCopy.setID(gId);
            String id = String.valueOf(gId);
            boolean ok = persistenceManager.writeEmail(mailCopy, id, computePath(account, MAILBOX_DIR), EXTENSION);
            if (ok) {
                serverModel.addLog(Thread.currentThread().getName() + " - stored email from " + mailCopy.getSender() + " to: " + computePath(account));
                MailboxMetadata metadata = mailboxesMetadata.get(account);
                metadata.incrementEmailGId();
                metadata.incrementEmailCount();
                metadata.setLastKnownId(metadata.getEmailGId());
                metadata.setNewMessage(true);
                persistenceManager.writeMetadata(metadata, String.valueOf(1), computePath(account, METADATA_DIR), EXTENSION, true);
                return true;
            }
        } catch (IOException exception) {
            serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + exception.getMessage());
        } finally {
            mailboxesLock.get(account).writeLock().unlock();
        }
        return false;
    }

    /**
     * Recupera una pagina di email dalla mailbox di un account.
     *
     * <p>La lettura viene eseguita con lock di lettura. Se vengono restituite
     * email, il metodo aggiorna il campo {@code lastKnownId} dei metadati con
     * l'identificativo dell'ultima email della pagina.</p>
     *
     * @param account account proprietario della mailbox
     * @param page indice della pagina da leggere, a partire da {@code 0}
     * @return wrapper contenente il conteggio totale e la lista di email lette
     */
    public EmailWrapper getEmails (String account, int page, boolean fetchNewMail) {
        EmailWrapper emailWrapper = new EmailWrapper();
        List<Email> emails = new ArrayList<>();
        mailboxesLock.get(account).readLock().lock();
        try {
            emailWrapper.setNewMessage(mailboxesMetadata.get(account).newMessage());
            emailWrapper.setEmailsCount(mailboxesMetadata.get(account).getEmailCount());
            int nrDelivering = mailboxesMetadata.get(account).getNrEmailsDelivering();
            if (fetchNewMail) emails = persistenceManager.readEmail(computePath(account, MAILBOX_DIR));
            else emails = persistenceManager.readEmails(computePath(account, MAILBOX_DIR), nrDelivering, page);
            emailWrapper.setEmails(emails);
        } catch (Exception e) {
            serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
        } finally {
            mailboxesLock.get(account).readLock().unlock();
        }
        if (!emails.isEmpty()) {
            mailboxesLock.get(account).writeLock().lock();
            try {
                MailboxMetadata metadata = mailboxesMetadata.get(account);
                metadata.setLastKnownId(emails.get(emails.size()-1).getID());
                //TODO consider removing
                metadata.setNewMessage(false);
                persistenceManager.writeMetadata(metadata, String.valueOf(1), computePath(account, METADATA_DIR), EXTENSION, true);
            } catch (IOException e) {
                serverModel.addLog(Thread.currentThread().getName() + " - [ERROR] - " + e.getMessage());
            } finally {
                mailboxesLock.get(account).writeLock().unlock();
            }
        }
        return emailWrapper;
    }

    /**
     * Restituisce il numero di email presenti nella mailbox dell'account e se c'è una nuova mail da leggere.
     *
     * @param account account da interrogare
     * @return conteggio corrente delle email e presenza nuova mail
     */
    public String count(String account) {
        mailboxesLock.get(account).readLock().lock();
        long emailsCount = mailboxesMetadata.get(account).getEmailCount();
        boolean newMessage = mailboxesMetadata.get(account).newMessage();
        mailboxesLock.get(account).readLock().unlock();
        mailboxesLock.get(account).writeLock().lock();
        mailboxesMetadata.get(account).setNewMessage(false);
        mailboxesLock.get(account).writeLock().unlock();
        return String.valueOf(emailsCount).concat(";").concat(String.valueOf(newMessage));
    }


    /**
     * Costruisce un percorso assoluto sotto la directory radice delle mailbox.
     *
     * @param paths segmenti del percorso da aggiungere alla directory radice
     * @return percorso composto tramite {@link Paths#get(String, String...)}
     */
    private String computePath (String... paths) {
        return Paths.get(MailboxService.INBOXES_DIR, paths).toString();
    }

    /**
     * Crea una copia indipendente dell'email da salvare.
     *
     * @param email email originale
     * @return copia dell'email con stato mutabile isolato
     */
    private Email copyEmail(Email email) {
        Date sentDate = email.getSentDate();
        Date copiedDate = sentDate == null ? null : new Date(sentDate.getTime());
        Email copy = new Email(email.getSender(), email.getRecipients(), email.getArgument(), email.getMailBody(), copiedDate);
        copy.setID(email.getID());
        return copy;
    }
}
