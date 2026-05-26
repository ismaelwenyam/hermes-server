package it.turin.hermesserver.service;

import it.turin.hermesserver.dto.EmailWrapper;
import it.turin.hermesserver.model.Email;
import it.turin.hermesserver.model.MailboxMetadata;
import it.turin.hermesserver.model.ServerModel;
import it.turin.hermesserver.persistence.FilePersistenceManager;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Thread-safe
 * Classe di gestione concorrente delle mailbox server.
 *
 * */
public class MailboxService {
    private static final String INBOXES_DIR = "C:\\Users\\arnau\\AppData\\Local\\hermes-server";
    private static final String METADATA_DIR = "metadata";
    private static final String MAILBOX_DIR = "mailbox";
    private static final String EXTENSION = ".bin";

    private Map<String, MailboxMetadata> mailboxesMetadata;
    private Map<String, ReentrantReadWriteLock> mailboxesLock;

    private AtomicBoolean mailboxMgmAlive = new AtomicBoolean(true);


    private final ServerModel serverModel;
    private final FilePersistenceManager persistenceManager;

    public MailboxService(ServerModel serverModel) {
        this.persistenceManager = new FilePersistenceManager();
        this.serverModel = serverModel;
    }


    /**
     * Initializes the email folders and creates metadata for the accounts when the server is started for the first time.
     * On subsequent starts, it reads the metadata for each account.
     * @param emails accounts predefiniti
     * */
    public void initMailBoxes (List<String> emails) {
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
        mailboxMgmAlive.set(positiveInit);
    }

    /**
     * Checks if this email has an account
     * @param email precompiled email
     * */
    public boolean accountExists (String email) {
        return persistenceManager.directoryExists(computePath(email));
    }

    /**
     * @param id mail id
     * @param account user account
     * */
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
     * Saves an email to an account
     * @param email
     * @param account
     * */
    public boolean saveEmail(Email email, String account) {
        System.out.println("saving mail for user: " + account);
        mailboxesLock.get(account).writeLock().lock();
        System.out.println("lock for user: " + account);
        try {
            long gId = mailboxesMetadata.get(account).getEmailGId();
            email.setID(gId);
            String id = String.valueOf(gId);
            boolean ok = persistenceManager.writeEmail(email, id, computePath(account, MAILBOX_DIR), EXTENSION);
            if (ok) {
                serverModel.addLog(Thread.currentThread().getName() + " - stored email from " + email.getSender() + " to: " + computePath(account));
                MailboxMetadata metadata = mailboxesMetadata.get(account);
                metadata.incrementEmailGId();
                metadata.incrementEmailCount();
                metadata.setLastKnownId(metadata.getEmailGId());
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

    /***/
    public EmailWrapper getEmails (String account, int page) {
        EmailWrapper emailWrapper = new EmailWrapper();
        List<Email> emails = new ArrayList<>();
        mailboxesLock.get(account).readLock().lock();
        try {
            emailWrapper.setEmailsCount(mailboxesMetadata.get(account).getEmailCount());
            int nrDelivering = mailboxesMetadata.get(account).getNrEmailsDelivering();
            emails = persistenceManager.readEmails(computePath(account, MAILBOX_DIR), nrDelivering, page);
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
     * returns the number of emails for the account
     * @param account email account
     * @return number of emails
     * */
    public long count(String account) {
        mailboxesLock.get(account).readLock().lock();
        long emailsCount = mailboxesMetadata.get(account).getEmailCount();
        mailboxesLock.get(account).readLock().unlock();
        return emailsCount;
    }


    private String computePath (String... paths) {
        return Paths.get(MailboxService.INBOXES_DIR, paths).toString();
    }
}
