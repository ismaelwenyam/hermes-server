package it.turin.hermesserver.dto;

import it.turin.hermesserver.model.Email;

import java.util.List;

public class EmailWrapper {
    private long emailsCount;
    private List<Email> emails;

    public long getEmailsCount() {
        return emailsCount;
    }

    public void setEmailsCount(long emailsCount) {
        this.emailsCount = emailsCount;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }
}
