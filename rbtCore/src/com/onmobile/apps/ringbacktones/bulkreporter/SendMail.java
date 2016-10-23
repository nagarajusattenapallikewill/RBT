package com.onmobile.apps.ringbacktones.bulkreporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 *  
 */
public class SendMail
{
	private static Logger logger = Logger.getLogger(SendMail.class);
	
	private String user = null;

	private String password = null;

	private String host = "smtp.gmail.com";

	private String port = "25";

	private String starttls = "true";

	private String auth = "true";

	private String debug = "true";

	private String from = null;

	private String[] to = null;

	private String[] cc = null;

	private String[] bcc = null;

	private String subject = null;

	private String bodyText = null;

	private String[] files = null;

	private String mimeType = "text/plain";

	public static void main(String[] args)
	{
		SendMail sendMail = new SendMail();
		try
		{
			String tmpTo = null;
			String tmpCC = null;
			String tmpBCC = null;
			String tmpFile = null;
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-u"))
					sendMail.user = args[++i].trim();
				else if (args[i].equals("-p"))
					sendMail.password = args[++i];
				else if (args[i].equals("-h"))
					sendMail.host = args[++i].trim();
				else if (args[i].equals("-t"))
					sendMail.port = args[++i].trim();
				else if (args[i].equals("-s"))
					sendMail.starttls = args[++i].trim();
				else if (args[i].equals("-a"))
					sendMail.auth = args[++i].trim();
				else if (args[i].equals("-d"))
					sendMail.debug = args[++i].trim();
				else if (args[i].equals("-F"))
					sendMail.from = args[++i].trim();
				else if (args[i].equals("-T"))
					tmpTo = args[++i].trim();
				else if (args[i].equals("-C"))
					tmpCC = args[++i].trim();
				else if (args[i].equals("-B"))
					tmpBCC = args[++i].trim();
				else if (args[i].equals("-S"))
					sendMail.subject = args[++i].trim();
				else if (args[i].equals("-A"))
					tmpFile = args[++i].trim();
				else if (args[i].equals("-BT"))
					sendMail.bodyText = args[++i].trim();
				else if (args[i].equals("-M"))
					sendMail.mimeType = args[++i].trim();
				else
				{
					System.out
					.println("Usage: SendMail [-u user_name] [-p password] [-h host] [-t port] [-s starttls] [-a authentication] [-d debug] [-f fallback] [-F from_address] [-T to_addresses seperated by semicolon] [-C cc_addresses seperated by semicolon] [-B bcc_addresses seperated by semicolon] [-S subject] [-A attachment_file]");
					System.exit(0);
				}
			}

			sendMail.to = sendMail.getAddresses(tmpTo);
			sendMail.cc = sendMail.getAddresses(tmpCC);
			sendMail.bcc = sendMail.getAddresses(tmpBCC);
			sendMail.files = sendMail.getAddresses(tmpFile);

			sendMail.validateParameters();
		}
		catch (Exception e)
		{
			System.out
			.println("Usage: SendMail [-u user_name] [-p password] [-h host] [-t port] [-s starttls] [-a authentication] [-d debug] [-f fallback] [-F from_address] [-T to_addresses seperated by semicolon] [-C cc_addresses seperated by semicolon] [-B bcc_addresses seperated by semicolon] [-S subject] [-A attachment_file]");
			e.printStackTrace();
			System.exit(0);
		}

		try
		{
			sendMail.sendMail();
		}
		catch (MessagingException e)
		{
			logger.error("", e);
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
	}

	public void validateParameters() throws IOException
	{
		System.out.println("Please enter required details: ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		if (user == null)
		{
			System.out.print("User: ");
			System.out.flush();
			user = in.readLine();
		}

		if (password == null)
		{
			System.out.print("Password: ");
			System.out.flush();
			password = in.readLine();
		}

		if (from != null && from.equalsIgnoreCase(""))
			from = null;
		while (from == null)
		{
			System.out.print("From: ");
			System.out.flush();
			from = in.readLine();
			from = from.trim();
			if (from.equalsIgnoreCase(""))
			{
				from = null;
				System.out.println("Please enter valid address");
			}
		}

		while (to == null)
		{
			String tmpTo = null;
			System.out.print("To(Semicolon separated): ");
			System.out.flush();
			tmpTo = in.readLine();
			to = getAddresses(tmpTo);
			if (to == null)
			{
				System.out.println("Please enter valid address");
				System.out.flush();
			}
		}
		/*
		 * if(cc == null) { String tmpCC = null; System.out.print("CC(Semicolon
		 * separated): "); System.out.flush(); tmpCC = in.readLine(); cc =
		 * getAddresses(tmpCC); }
		 * 
		 * if(bcc == null) { String tmpBCC = null;
		 * System.out.print("BCC(Semicolon separated): "); System.out.flush();
		 * tmpBCC = in.readLine(); bcc = getAddresses(tmpBCC); }
		 */
		if (subject == null)
		{
			System.out.print("Subject: ");
			System.out.flush();
			subject = in.readLine();
		}

		if (files == null)
		{
			String tmpFile = null;
			System.out.print("Attachments(Semicolon separated File Names): ");
			System.out.flush();
			tmpFile = in.readLine();
			files = getAddresses(tmpFile);
		}

		if (bodyText == null)
		{
			System.out.println("Mail Body Text: ");
			System.out.flush();
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null)
			{
				sb.append(line);
				sb.append("\n");
			}
			bodyText = sb.toString();
		}
	}

	public String[] getAddresses(String address)
	{
		if (address == null)
			return null;

		String[] addresses = null;

		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer addressTokens = new StringTokenizer(address, ";");
		for (int t = 0; addressTokens.hasMoreTokens(); t++)
		{
			String token = addressTokens.nextToken();
			token = token.trim();
			if (!token.equalsIgnoreCase(""))
				list.add(token);
		}
		if (list.size() > 0)
			addresses = list.toArray(new String[0]);

		return addresses;
	}

	public void sendMail() throws MessagingException, IOException
	{
		Properties properties = new Properties();
		properties.put("mail.smtp.user", user);
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.starttls.enable", starttls);
		properties.put("mail.smtp.auth", auth);
		properties.put("mail.smtp.debug", debug);

		Authenticator authenticator = new SMTPAuthenticator();
		Session session = Session.getInstance(properties, authenticator);
		session.setDebug(Boolean.valueOf(debug).booleanValue());
		Message message = new MimeMessage(session);

		MimeBodyPart mimeBodyPartText = new MimeBodyPart();
		mimeBodyPartText.setContent(bodyText, mimeType);

		File[] attachFiles = null;
		MimeBodyPart[] mimeBodyPartAttachments = null;
		if (files != null)
		{
			attachFiles = new File[files.length];
			mimeBodyPartAttachments = new MimeBodyPart[files.length];
			for (int i = 0; i < files.length; i++)
			{
				attachFiles[i] = new File(files[i]);
				if (attachFiles[i].exists())
				{
					mimeBodyPartAttachments[i] = new MimeBodyPart();
					mimeBodyPartAttachments[i].attachFile(attachFiles[i]);
				}
			}
		}

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(mimeBodyPartText);
		if (mimeBodyPartAttachments != null)
		{
			for (int i = 0; i < mimeBodyPartAttachments.length; i++)
			{
				multipart.addBodyPart(mimeBodyPartAttachments[i]);
			}
		}
		message.setContent(multipart);

		InternetAddress fromAddress = new InternetAddress(from);
		InternetAddress[] toAddress = new InternetAddress[to.length];
		for (int i = 0; i < to.length; i++)
		{
			toAddress[i] = new InternetAddress(to[i]);
		}
		InternetAddress[] ccAddress = null;
		if (cc != null)
		{
			ccAddress = new InternetAddress[cc.length];
			for (int i = 0; i < cc.length; i++)
			{
				ccAddress[i] = new InternetAddress(cc[i]);
			}
		}
		InternetAddress[] bccAddress = null;
		if (bcc != null)
		{
			bccAddress = new InternetAddress[bcc.length];
			for (int i = 0; i < bcc.length; i++)
			{
				bccAddress[i] = new InternetAddress(bcc[i]);
			}
		}
		message.setSubject(subject);
		message.setFrom(fromAddress);
		message.addRecipients(Message.RecipientType.TO, toAddress);
		if (ccAddress != null)
			message.addRecipients(Message.RecipientType.CC, ccAddress);
		if (bccAddress != null)
			message.addRecipients(Message.RecipientType.BCC, bccAddress);

		Transport.send(message);
	}

	private class SMTPAuthenticator extends javax.mail.Authenticator
	{
		/* (non-Javadoc)
		 * @see javax.mail.Authenticator#getPasswordAuthentication()
		 */
		@Override
		public PasswordAuthentication getPasswordAuthentication()
		{
			return new PasswordAuthentication(user, password);
		}
	}

}