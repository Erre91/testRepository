package francesco.workspace.homeapp;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.content.SharedPreferences;

public class EmailClient {
	private static EmailClient instance = null;
	private final Session session;
	private final String userName;
	private final String password;
	private SharedPreferences pref;

	protected EmailClient(String user, String psw) {
		this.userName = user;
		this.password = psw;
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtps.eurecom.fr");
		props.put("mail.smtp.port", "587");
		session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});
	}

	public static EmailClient getInstance(String user, String psw) {
		if (instance == null)
			instance = new EmailClient(user, psw);
		return instance;
	}

	public void sendEmail(String[] stringAddr, Event_App e) throws Exception {
		MimeMessage msg = new MimeMessage(session);
		InternetAddress[] addresses = new InternetAddress[stringAddr.length];
		for (int i = 0; i < stringAddr.length; i++)
			addresses[i] = new InternetAddress(stringAddr[i]);
		msg.setRecipients(Message.RecipientType.TO, addresses);
		msg.setFrom(new InternetAddress(userName));
		msg.setSubject("New Tap Event: " + e.getName());
		msg.setSentDate(new Date());
		String body = new String("Event created by " + userName + "\n"
				+ "Date: " + e.getData() + "\n" + "Location: " + e.getAddress()
				+ "\n" + "Hour: " + e.getHour() + "\n" + "Description: "
				+ e.getDescription());
		msg.setText(body);
		Transport.send(msg);
	}
}
