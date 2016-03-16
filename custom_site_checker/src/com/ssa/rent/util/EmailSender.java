package com.ssa.rent.util;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class EmailSender {
    
    public final static String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public final static String CONTENT_TYPE_TEXT_HTML = "text/html";
    private static final String UTF_8 = "UTF-8";
    
    private String subject = "";
    private String body = "";
    private String contentType = CONTENT_TYPE_TEXT_HTML;
    
    public EmailSender(){
	super();
    }
    
    public void sendEmail(final String user, final String pass, String messageBody){
	Properties props = System.getProperties();
	
	props.put("mail.smtp.auth", "true");
	props.put("mail.smtp.starttls.enable", "true");
	props.put("mail.smtp.host", "smtp.gmail.com");
	props.put("mail.smtp.port", "587");
	
	Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, pass);
				}
			});

	//Session session = Session.getDefaultInstance(props, null);

	try {
	    //Transport transport = session.getTransport("smtp");
	    //transport.connect(host, user, pass);
	    Message msg = new MimeMessage(session);
	    msg.setFrom(new InternetAddress("sidorenko.s.a@gmail.com", "Sergey"));
	    msg.addRecipient(Message.RecipientType.TO,
		    new InternetAddress("sidorenko.s.a@gmail.com", "Sergey"));
	    msg.addRecipient(Message.RecipientType.TO,
		    new InternetAddress("vrnksunny@gmail.com", "Veronika"));
	    
	    msg.setSubject("***FLAT INFO***");
	    msg.setContent(messageBody, getContentType() + ";charset=" + UTF_8);
	    
	    Transport.send(msg);
	} catch (MessagingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (UnsupportedEncodingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public String getSubject(){
	return subject;
    }

    public void setSubject(String subject) {
	this.subject = subject;
    }

    public String getBody() {
	return body;
    }

    public void setBody(String body) {
	this.body = body;
    }

    public void clearBody(){
	this.body = "";
    }

    public void clearSubject(){
	this.subject = "";
    }
    
    public String getContentType() {
	return contentType;
    }
    
    public void setContentType(String contentType) {
	if(contentType.equals(CONTENT_TYPE_TEXT_HTML) || contentType.equals(CONTENT_TYPE_TEXT_PLAIN)){
	    this.contentType = contentType;
	}
	else{
	    this.contentType = CONTENT_TYPE_TEXT_PLAIN;
	}
    }
}
