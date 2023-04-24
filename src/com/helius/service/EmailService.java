package com.helius.service;

import java.io.File;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

import com.helius.dao.Mail;

public interface EmailService {
	public void sendEmail(String to,String[] cc,String[] bcc, String subject, String text )throws MessagingException;
	public void sendBulkEmail(String to,String[] cc,String[] bcc, String subject, String text) throws MessagingException;
	public void sendEmailWithAttachment(String to,String[] cc,String[] bc, String subject, String message, List<File> files) throws MessagingException;
	public void sendBulkEmailWithAttachment(String to,String[] cc,String[] bc, String subject, String message, List<File> files) throws MessagingException;
}