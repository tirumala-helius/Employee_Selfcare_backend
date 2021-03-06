package com.helius.service;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.helius.utils.Utils;


@Service("emailService")
public class EmailServiceImpl implements EmailService {

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
}
