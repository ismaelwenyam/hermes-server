package it.turin.hermesserver.persistence;

import it.turin.hermesserver.model.Email;
import it.turin.hermesserver.model.MailboxMetadata;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class used to store data to file system.
 * */
public class FilePersistenceManager {


    /**
     * Checks if a directory exists given a path
     * @param pathname
     * @return true if dir exists false otherwise
     * */
    public boolean directoryExists (String pathname){
        File file = new File(pathname);
        return file.isDirectory();
    }

    /**
     * @param pathname
     * @return true if the directory has been created, false otherwise
     * */
    public boolean createDirectory (String pathname) {
        File file = new File(pathname);
        boolean result;
        try {
            result = file.mkdirs();
            if (result)
                System.out.println(Thread.currentThread().getName() + " created directory: " + pathname);
        } catch (SecurityException ex) {
            //TODO log to monitor
            throw new SecurityException("no writing permission on: " + pathname);
        }
        return result;
    }

    /**
     *
     * @param data represents the email to be stored
     * @param fileId represents the id of data
     * @param pathname represents the folder where to store the data
     * @param extension file extension
     * @return true if data has been successfully stored, false otherwise
     *
     * */
    public boolean writeEmail(Email data, String fileId, String pathname, String extension) throws IOException {
        if (data == null) return false;
        String filename = fileId.concat(extension);
        File file = new File(pathname, filename);
        if (file.exists()) {
            return false;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
            out.flush();
        } catch (IOException ex) {
            //TODO log to monitor
            throw new IOException("storing data: " + data + " to: " + file.getCanonicalPath());
        }
        return true;
    }


    /**
     *
     * @param data represents the email to be stored
     * @param fileId represents the id of data
     * @param pathname represents the folder where to store the data
     * @param extension file extension
     * @param update if true updates metadata
     * @return true if data has been successfully stored, false otherwise
     *
     * */
    public boolean writeMetadata(MailboxMetadata data, String fileId, String pathname, String extension, boolean update) throws IOException {
        //TODO verify if update is necessary
        if (data == null) return false;
        String filename = fileId.concat(extension);
        File file = new File(pathname, filename);
        if (file.exists() && !update) {
            return false;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
            out.flush();
        } catch (IOException ex) {
            throw new IOException("storing data: " + data + " to: " + file.getCanonicalPath());
        }
        return true;
    }

    /**
     * @param filename represents the file to be deleted
     * @param pathname parent where resides the file to delete
     * @param extension file extension
     * @return true if files has been deleted, false otherwise
     * */
    public boolean removeEmail (String filename, String pathname, String extension) {
        File file = new File(pathname, filename.concat(extension));
        if (file.isDirectory() || !file.exists()) return false;
        return file.delete();
    }


    /**
     *
     * Retrieve nrElements emails within page
     *
     * @param pathname the directory path that contains the emails
     * @param nrElements the number of data files to retrieve
     * @param page the page to display
     * @return a list of T objects representing the datas present in parent,
     * if parent is a file or doesn't exists returns an empty collection
     * */
    public List<Email> readEmails(String pathname, int nrElements, int page) throws Exception {
        File dir = new File(pathname);
        if (dir.isFile() || !dir.exists()) return Collections.emptyList();
        String[] files = dir.list();
        if (files == null) return Collections.emptyList();
        Arrays.sort(files, (a, b) -> {
            int n1 = Integer.parseInt(a.replace(".bin", ""));
            int n2 = Integer.parseInt(b.replace(".bin", ""));
            // sort: desc order (n2, n1). asc order (n1, n2)
            return Integer.compare(n2, n1);
        });
        int from = page * nrElements;
        int to = Math.min((from + nrElements) * 2, files.length);
        List<String> filenames = new ArrayList<>();
        for (int i = from, j = 0; i < to; i++, j++) {
            filenames.add(files[i]);
        }
        return getEmails(pathname, filenames);
    }

    private static List<Email> getEmails(String pathname, List<String> filenames) throws Exception {
        List<Email> result = new ArrayList<>();
        for (String filename : filenames) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(pathname, filename)))) {
                Email data = (Email) in.readObject();
                result.add(data);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("reading email: " + e.getMessage());
                throw new Exception("error occured reading emails");
            }
        }
        return result;
    }

    /**
     * Reads the metadata from FS
     * @param pathname represents the path to the metadata file
     * @return MailboxMetadata
     * */
    public MailboxMetadata readMetadata(String pathname) throws Exception {
        File file = new File(pathname);
        if (file.isDirectory() || !file.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))){
            MailboxMetadata data = (MailboxMetadata) in.readObject();
            return data;
        } catch (IOException | ClassNotFoundException e) {
            throw new Exception("error occured reading metadata");
        }
    }
}
