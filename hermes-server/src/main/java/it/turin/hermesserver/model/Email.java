package it.turin.hermesserver.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Email implements Serializable {

    private static final long serialVersionUID = -21548911354897L;

    private long ID;
    private final String sender;
    private final List<String> recipients;
    private final String argument;
    private final String mailBody;
    private final Date sentDate;

    /**
     * @param sender the sender of this email
     * @param recipients a list of recipients
     * @param argument
     * @param mailBody
     * @param sentDate
     *
     * */
    public Email(String sender, List<String> recipients, String argument, String mailBody, Date sentDate) {
        this.sender = sender;
        this.recipients = recipients;
        this.argument = argument;
        this.mailBody = mailBody;
        this.sentDate = sentDate;
    }

    public long getID() {return this.ID;}
    public void setID(long id){this.ID = id;}
    public Date getSentDate() {return this.sentDate;}

    public String getSender() {return this.sender;}

    public List<String> getRecipients(){
        return new ArrayList<>(this.recipients);
    }

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
