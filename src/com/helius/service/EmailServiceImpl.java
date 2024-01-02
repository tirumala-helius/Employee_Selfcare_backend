package com.helius.service;

import java.io.File;

import java.util.List;
import java.util.Properties;


import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.helius.utils.Utils;


@Service("emailService")
public class EmailServiceImpl implements EmailService {
    String awsCheck = Utils.awsCheckFlag();
	@Autowired
	private JavaMailSender mailSender;
   
	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(Utils.getProperty("emailHost"));
		mailSender.setPort(Integer.parseInt(Utils.getProperty("emailPort")));
		mailSender.setUsername(Utils.getProperty("emailUserName"));
		mailSender.setPassword(Utils.getProperty("emailPassword"));
		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", Utils.getProperty("mail.transport.protocol"));
		props.put("mail.smtp.auth", Utils.getProperty("mail.smtp.auth"));
		props.put("mail.smtp.starttls.enable", Utils.getProperty("mail.smtp.starttls.enable"));
		props.put("mail.debug", Utils.getProperty("mail.debug"));
		props.put("mail.smtp.ssl.enable", Utils.getProperty("mail.smtp.ssl.enable"));
		return mailSender;
	}
	
	@Async
	public void sendEmail(String to,String[] cc,String[] bcc, String subject, String text )throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		String check =	Utils.getHapProperty("hcm_testing");
		String from = (((JavaMailSenderImpl) mailSender).getUsername());
		helper.setSubject(subject);
		helper.setText(text.toString());
		helper.setFrom(new InternetAddress(from));
		if("yes".equalsIgnoreCase(check)){
    		to = "hap-testing@helius-tech.com";
    		if(bcc != null && bcc.length > 0){
    		 String[] testBCC = new String [] {"hap-testing@helius-tech.com"};
    		 bcc = testBCC;
    		}
    		if(cc != null && cc.length > 0){
          		 String[] testCC = new String [] {"hap-testing@helius-tech.com"};
          		 cc = testCC;
          		}
    		}
		helper.setTo(to);
		if(cc!=null){
		helper.setCc(cc);
		}
		if(bcc!=null){
		helper.setBcc(bcc);
		}
		mailSender.send(message);	
	}
	
	public void sendBulkEmail(String to,String[] cc,String[] bcc, String subject, String text)throws MessagingException {
    	MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		String check =	Utils.getHapProperty("hcm_testing");
		String from = (((JavaMailSenderImpl) mailSender).getUsername());
		helper.setSubject(subject);
		helper.setText(text.toString());
		helper.setFrom(new InternetAddress(from));
		if("yes".equalsIgnoreCase(check)){
    		to = "hap-testing@helius-tech.com";
    		if(bcc != null && bcc.length > 0){
    		 String[] testBCC = new String [] {"hap-testing@helius-tech.com"};
    		 bcc = testBCC;
    		}
    		if(cc != null && cc.length > 0){
       		 String[] testCC = new String [] {"hap-testing@helius-tech.com"};
       		 cc = testCC;
       		}
    		}
		helper.setTo(to);
		if(cc!=null){
		helper.setCc(cc);
		}
		if(bcc!=null){
		helper.setBcc(bcc);
		}
		mailSender.send(message);	
	}
	
	@Async
	public void sendEmailWithAttachment(String to,String[] cc,String[] bcc, String subject, String text,
			List<File> files) throws MessagingException {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			String from = (((JavaMailSenderImpl) mailSender).getUsername());
			helper.setSubject(subject);
			helper.setText(text.toString());
			helper.setFrom(new InternetAddress(from));
			String check =	Utils.getHapProperty("hcm_testing");
			if("yes".equalsIgnoreCase(check)){
	    		to = "hap-testing@helius-tech.com";
	    		if(bcc != null && bcc.length > 0){
	    		 String[] testBCC = new String [] {"hap-testing@helius-tech.com"};
	    		 bcc = testBCC;
	    		}
	    		if(cc != null && cc.length > 0){
	       		 String[] testCC = new String [] {"hap-testing@helius-tech.com"};
	       		 cc = testCC;
	       		}
	    		}
    		helper.setTo(to);
			if(cc!=null){
			helper.setCc(cc);
			}
			if(bcc!=null){
				helper.setCc(bcc);
				}
			if(!files.isEmpty() || files.size()!=0){
			for (File file : files) { 
			helper.addAttachment(file.getName(),file);
			}	
			}
			mailSender.send(message);
	}
	
	public void sendBulkEmailWithAttachment(String to,String[] cc,String[] bcc, String subject, String text,
			List<File> files) throws MessagingException {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			String from = (((JavaMailSenderImpl) mailSender).getUsername());
			helper.setSubject(subject);
			helper.setText(text.toString());
			helper.setFrom(new InternetAddress(from));
			String check =	Utils.getHapProperty("hcm_testing");
			if("yes".equalsIgnoreCase(check)){
	    		to = "hap-testing@helius-tech.com";
	    		if(bcc != null && bcc.length > 0){
	    		 String[] testBCC = new String [] {"hap-testing@helius-tech.com"};
	    		 bcc = testBCC;
	    		}
	    		if(cc != null && cc.length > 0){
	       		 String[] testCC = new String [] {"hap-testing@helius-tech.com"};
	       		 cc = testCC;
	       		}
	    		}
    		helper.setTo(to);
			if(cc!=null){
			helper.setCc(cc);
			}
			if(bcc!=null){
				helper.setCc(bcc);
				}
			if(!files.isEmpty() || files.size()!=0){
			for (File file : files) { 
			helper.addAttachment(file.getName(),file);
			}	
			}
			mailSender.send(message);
	}
	
	
	@Async
	public void sendMessageWithAttachment(String to,String[] cc, String subject, String text,
			List<String> pathToAttachment) throws MessagingException {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			String from = (((JavaMailSenderImpl) mailSender).getUsername());
			helper.setSubject(subject);
			helper.setText(text.toString());
			helper.setFrom(new InternetAddress(from));
			String check =	Utils.getHapProperty("hcm_testing");
			if ("yes".equalsIgnoreCase(check)) {
				to = "hap-testing@helius-tech.com";
				String[] testCC = new String[] { "hap-testing@helius-tech.com" };
				cc = testCC;
			}
			helper.setTo(to);
			if(cc!=null){
			helper.setCc(cc);
			}
			
			if((!pathToAttachment.isEmpty() || pathToAttachment.size()!=0)&&
					"no".equalsIgnoreCase(awsCheck)){
			for (String file : pathToAttachment) {
				FileSystemResource fr = new FileSystemResource(file);
				helper.addAttachment(fr.getFilename(), fr); 
			}
			}
			if ((!pathToAttachment.isEmpty() || pathToAttachment.size()!=0)&&
					"yes".equalsIgnoreCase(awsCheck)) {
	           	 MimeMultipart multipart = new MimeMultipart();
	   	         MimeBodyPart messageBodyPart = new MimeBodyPart();
	   	        // messageBodyPart.setContent(text, "text/html");
	   	         messageBodyPart.setText(text);
	   	         multipart.addBodyPart(messageBodyPart);
					for (String file : pathToAttachment) {
						try {
							File file1 = new File(file);
						     String filename = file1.getName();
							byte[] content =Utils.downloadFileByAWSS3Bucket(file);
							 MimeBodyPart attachmentPart = new MimeBodyPart();
							 DataSource source = new ByteArrayDataSource(content, "application/octet-stream");
					            attachmentPart.setDataHandler(new DataHandler(source));
					            attachmentPart.setFileName(filename);
					            multipart.addBodyPart(attachmentPart);
						
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					message.setContent(multipart);
				}
			mailSender.send(message);
		//	syncSentItems(message);		    
	}

	@Override
	public void sendMessageWithAttachmentForTimesheet(String to, String[] cc, String subject, String text,
			List<String> pathToAttachment,String client) throws MessagingException, Throwable {
		final String  username ;
		  final String password ;
		if(client.equalsIgnoreCase("DAH2")) {
			 username =	Utils.getHapProperty("timesheetAutomationUserName");
			 password =	Utils.getHapProperty("timesheetAutomationPassword");
		}else {
			 username =	Utils.getHapProperty("timesheetAutomationFor_Non_DAH2_UserName");
			 password =	Utils.getHapProperty("timesheetAutomationFor_Non_DAH2_Password");
		}
		
	    
	    Properties props = new Properties();
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.host", "smtp.office365.com");
	    props.put("mail.smtp.port", "587");

	    Session session = Session.getInstance(props, new Authenticator() {
			
			  @Override protected PasswordAuthentication getPasswordAuthentication() {
			  return new PasswordAuthentication(username, password); }
			 
	    });

	    MimeMessage message = new MimeMessage(session);
	    MimeMessageHelper helper = new MimeMessageHelper(message, true);
	    helper.setSubject(subject);
		helper.setText(text.toString());
		helper.setFrom(new InternetAddress(username));
		String check =	Utils.getHapProperty("hcm_testing");
		if ("yes".equalsIgnoreCase(check)) {
			to = "hap-testing@helius-tech.com";
			String[] testCC = new String[] { "hap-testing@helius-tech.com" };
			cc = testCC;
		}
		helper.setTo(to);
		if(cc!=null){
		helper.setCc(cc);
		}
		
		if((!pathToAttachment.isEmpty() || pathToAttachment.size()!=0)&&
				"no".equalsIgnoreCase(awsCheck)){
		for (String file : pathToAttachment) {
			FileSystemResource fr = new FileSystemResource(file);
			helper.addAttachment(fr.getFilename(), fr); 
		}
		}
		if ((!pathToAttachment.isEmpty() || pathToAttachment.size()!=0)&&
				"yes".equalsIgnoreCase(awsCheck)) {
           	 MimeMultipart multipart = new MimeMultipart();
   	         MimeBodyPart messageBodyPart = new MimeBodyPart();
   	         messageBodyPart.setText(text);
   	         multipart.addBodyPart(messageBodyPart);
				for (String file : pathToAttachment) {
					try {
						File file1 = new File(file);
					     String filename = file1.getName();
						byte[] content =Utils.downloadFileByAWSS3Bucket(file);
						 MimeBodyPart attachmentPart = new MimeBodyPart();
						 DataSource source = new ByteArrayDataSource(content, "application/octet-stream");
				            attachmentPart.setDataHandler(new DataHandler(source));
				            attachmentPart.setFileName(filename);
				            multipart.addBodyPart(attachmentPart);
					
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				message.setContent(multipart);
			}
		try {
			 Transport.send(message);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Throwable("Unable to send Email "+e.getMessage(), e);
		}
	//	syncSentItems(message);		    
		
	}
}
