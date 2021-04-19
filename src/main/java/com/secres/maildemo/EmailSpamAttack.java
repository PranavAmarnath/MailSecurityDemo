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
 * A demo for Programming Club to demonstrate the reality of a spam attack. This
 * program sends approximately 74 spam mails until GMail recognizes it and
 * temporarily shuts down the connection. Restarting won't do any good.
 * <P>
 * This technique requires
 * <a href="https://myaccount.google.com/lesssecureapps">less secure access</a>
 * to be enabled on the vulnerable account.
 * <P>
 * As said before, this is a demo. This requires a previous step: less secure
 * access for apps.
 * 
 * @author Pranav Amarnath
 * @version April 18, 2021
 *
 */
public class EmailSpamAttack {

	private String USERNAME;
	private String PASSWORD;
	private final String PATH = "/credentials.txt";
	private Session emailSession;
	private Transport emailTransport;

	/**
	 * Constructor that uses Jakarta Mail to access emails.
	 */
	public EmailSpamAttack() {
		try {
			readMail();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the mail and sets up SMTP.
	 * 
	 * @throws MessagingException
	 */
	private void readMail() throws MessagingException {
		readFile();

		// create properties field
		Properties properties = System.getProperties();
		properties.setProperty("mail.user", USERNAME);
		properties.setProperty("mail.password", PASSWORD);
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.smtp.host", "smtp.gmail.com");
		properties.setProperty("mail.smtp.port", "465");
		properties.setProperty("mail.smtp.auth", "true");

		emailSession = Session.getDefaultInstance(properties);

		emailTransport = emailSession.getTransport();
		emailTransport.connect(USERNAME, PASSWORD);

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
						USERNAME = line;
					}
					else {
						PASSWORD = line;
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
		System.out.println("[LOG] Started IDLE loop.");
		new Thread(() -> {
			while(true) {
				try {
					System.out.println("[LOG] Sending mail...");
					// Send a mail with random 12 letters.
					sendEmail(System.getProperties(), "pranny2k@gmail.com", "Get started with your new Gmail account", String.join(" ", generateRandomWords(12)));
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				System.out.println("[LOG] Sent mail!");
				try {
					Thread.sleep(200); // Wait time
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Generates an array of random letters in words.
	 * 
	 * @param numberOfWords  the number of words to return
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

		msg.setFrom(new InternetAddress(USERNAME));
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
