/*
 * Copyright 2014 XOR TECH LTD 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.xortech.email;

import java.util.Date; 
import java.util.Properties; 
import javax.activation.CommandMap; 
import javax.activation.DataHandler; 
import javax.activation.DataSource; 
import javax.activation.FileDataSource; 
import javax.activation.MailcapCommandMap; 
import javax.mail.Authenticator;
import javax.mail.BodyPart; 
import javax.mail.MessagingException;
import javax.mail.Multipart; 
import javax.mail.PasswordAuthentication; 
import javax.mail.Session; 
import javax.mail.Transport; 
import javax.mail.internet.InternetAddress; 
import javax.mail.internet.MimeBodyPart; 
import javax.mail.internet.MimeMessage; 
import javax.mail.internet.MimeMultipart;  

import android.os.AsyncTask;
 
public class Mail extends Authenticator { 
	
	private String _user; 
	private String _pass; 	 
	private String[] _to; 
	private String _from; 
	private String _port; 
	private String _sport; 	 
	private String _host; 	 
	private String _subject; 
	private String _body; 	 
	private boolean _auth; 	   
	private boolean _debuggable; 	 
	private Multipart _multipart; 
	  
	/**
	 * METHOD TO SET UP THE EMAIL PARAMETERS
	 */
	public Mail() { 
		_host = "smtp.gmail.com"; // DEFAULT SMTP SERVER
		_port = "465"; // DEFAULT SMTP PORT 
		_sport = "465"; // DEFAULT SOCKETFACTORY PORT
	 
	    _user = ""; // USERNAME
	    _pass = ""; // PASSWORD
	    _from = ""; // EMAIL SENT FROM:
	    _subject = ""; // EMAIL SUBJECT 
	    _body = ""; // EMAIL BODY
	 
	    _debuggable = false; // DEBUG MODE ON OR OFF - DEFAULT OFF
	    _auth = true; // SMTP AUTHENTICATION - DEFAULT ON
	 
	    _multipart = new MimeMultipart(); 
	    // FIX FOR MAILCAP, JAVAMAIL CANNOT FIND A HANDLER FOR THE MULTIPART/MIXED PART
	    MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
	    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
	    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
	    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
	    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
	    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822"); 
	    CommandMap.setDefaultCommandMap(mc); 
	} 
	  
	/**
	 * METHOD TO HANDLE THE USERNAME AND PASSWORD FOR AUTHENTICATION
	 * @param user
	 * @param pass
	 */
	public Mail(String user, String pass) { 
		this();  
	    this._user = user; 
	    this._pass = pass; 
	} 
	  
	/**
	 * METHOD TO SEND THE EMAIL
	 * @return
	 * @throws Exception
	 */
	public boolean send() throws Exception { 
		Properties props = _setProperties(); 
	 
	    if(!_user.equals("") && !_pass.equals("") && _to.length > 0 && !_from.equals("") && !_subject.equals("") && !_body.equals("")) { 
	    	Session session = Session.getInstance(props, this); 
	 
	    	final MimeMessage msg = new MimeMessage(session); 
	 
	    	msg.setFrom(new InternetAddress(_from)); 
	       
	    	InternetAddress[] addressTo = new InternetAddress[_to.length]; 
	    	for (int i = 0; i < _to.length; i++) { 
	    		addressTo[i] = new InternetAddress(_to[i]); 
	    	} 
	        msg.setRecipients(MimeMessage.RecipientType.TO, addressTo); 
	 
	        msg.setSubject(_subject); 
	        msg.setSentDate(new Date()); 
	 
	        // SETUP THE MESSAGE BODY 
	        BodyPart messageBodyPart = new MimeBodyPart(); 
	        messageBodyPart.setContent(_body, "text/html; charset=ISO-8859-1"); 
	        _multipart.addBodyPart(messageBodyPart);
	 
	        // PLACE THE PARTS INTO THE MESSAGE 
	        msg.setContent(_multipart); 
	        
	        new AsyncTask<Void, Void, Void>(){
	    		@Override
	    		protected void onPreExecute() {
	    			super.onPreExecute();	    			
	    		}

	    		@Override
	    		protected Void doInBackground(Void... params) {
					
    			    try {
						Transport.send(msg);
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Failed");
						System.out.println(e);
					}
	    			  	
	    		    return null;
	    		}

		        @Override
		        protected void onPostExecute(Void res) {
		        	super.onPostExecute(res);	        	
		        }
	    	 }.execute();
	 
	        // SEND THE EMAIL
	        //Transport.send(msg); 
	 
	        return true; 
	    } else { 
	    	return false; 
	    } 
	} 
	  
	/**
	 * METHOD TO ADD ATTACHMENTS TO THE EMAIL - NOT USED
	 * @param filename
	 * @throws Exception
	 */
	public void addAttachment(String filename) throws Exception { 
		BodyPart messageBodyPart = new MimeBodyPart(); 
	    DataSource source = new FileDataSource(filename); 
	    messageBodyPart.setDataHandler(new DataHandler(source)); 
	    messageBodyPart.setFileName(filename); 
	 
	    _multipart.addBodyPart(messageBodyPart); 
	} 
	  
	/**
	 * METHOD TO HANDLE PASSWORD AUTHENTICATION
	 */
	@Override 
	public PasswordAuthentication getPasswordAuthentication() { 
		return new PasswordAuthentication(_user, _pass); 
	} 
	
	/**
	 * METHOD TO SET THE EMAIL PROPERTIES
	 * @return
	 */
	private Properties _setProperties() { 
		Properties props = new Properties(); 
		props.put("mail.smtp.host", _host); 
		if(_debuggable) { 
			props.put("mail.debug", "true"); 
		} 
	 
		if(_auth) { 
			props.put("mail.smtp.auth", "true"); 
		} 
	 
		props.put("mail.smtp.port", _port); 
		props.put("mail.smtp.socketFactory.port", _sport); 
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
		props.put("mail.smtp.socketFactory.fallback", "false"); 
	 
		return props; 
	} 
	
	/**
	 * METHOD TO GET THE MESSAGE BODY
	 * @return
	 */
	public String getBody() { 
		return _body; 
	} 
	
	/**
	 * METHOD TO SET THE MESSAGE BODY
	 * @param _body
	 */
	public void setBody(String _body) { 
		this._body = _body; 
	} 
	
	/**
	 * METHOD TO SET THE "TO" STRING ARRAY
	 * @param toArr
	 */
	public void setTo(String[] toArr) {
		this._to = toArr;
	}
	
	/**
	 * METHOD TO GET THE "TO" STRING ARRAY
	 * @return
	 */
	public String[] getTo() {
		return _to;
	} 
	
	/**
	 * METHOD TO SET THE "FROM" FIELD FOR THE MESSAGE
	 * @param string
	 */
	public void setFrom(String string) {
		this._from = string;
	}
	
	/**
	 * METHOD TO GET THE "FROM" FIELD FOR THE MESSAGE
	 * @return
	 */
	public String getFrom() {
		return _from;
	} 
	
	/**
	 * METHOD TO SET THE MESSAGE SUBJECT
	 * @param string
	 */
	public void setSubject(String string) {
		this._subject = string;
	} 
	
	/**
	 * METHOD TO GET THE MESSAGE SUBJECT
	 * @return
	 */
	public String getSubject() {
		return _subject;
	} 
	
	/**
	 * METHOD TO DETERMINE AUTHENTICATION
	 * @return
	 */
	public boolean is_auth() {
	    return _auth;
	}
	
	/**
	 * METHOD TO SET AUTHENTICATION
	 * @param _auth
	 */
	public void set_auth(boolean _auth) {
	    this._auth = _auth;
	}
	
	/**
	 * METHOD TO DETERMINE IF DEBUGGABLE
	 * @return
	 */
	public boolean is_debuggable() {
	    return _debuggable;
	}
	
	/**
	 * METHOD TO SET DEBUGGABLE
	 * @param _debuggable
	 */
	public void set_debuggable(boolean _debuggable) {
	    this._debuggable = _debuggable;
	}
	
	/**
	 * METHOD TO GET MULTIPART
	 * @return
	 */
	public Multipart get_multipart() {
	    return _multipart;
	}
	
	/**
	 * METHOD TO SET MULTIPART
	 * @param _multipart
	 */
	public void set_multipart(Multipart _multipart) {
	    this._multipart = _multipart;
	}
} 

