package com.helius.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helius.managers.TimesheetAutomationManager;
import com.helius.utils.Status;

@RestController
public class AutomationTimesheetController {
	
	private org.hibernate.internal.SessionFactoryImpl sessionFactory;

	public org.hibernate.internal.SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}
	@Autowired
	Status status;
	
	@Autowired
	ApplicationContext context;
	
	@Autowired
	TimesheetAutomationManager automationManager;
	
	
	
	@CrossOrigin
	@RequestMapping(value = "client/createAutomationTimesheet", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<byte[]> createAutomationTimesheet(@RequestParam("model") String jsondata,
			MultipartHttpServletRequest request) throws JsonProcessingException {
		System.out.println("clientjsondata:" + jsondata.toString());
		ResponseEntity<byte[]> responseEntity  = automationManager.createAutomationTimesheet(jsondata, request);
	
		 return responseEntity;

		
	}
	
	@CrossOrigin
	@RequestMapping(value = "client/sendTimesheetAutomationmail", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public String sendTimesheetAutomationmail(@RequestParam("model") String jsondata,MultipartHttpServletRequest request,MultipartHttpServletRequest request2) {
		
		System.out.println("clientjsondata:" + jsondata.toString());
			
		status =automationManager.sendTimesheetAutomationmail(jsondata,request);
	
		return "{\"response\":\"" + status.getMessage() + "\"}";

		
	}
	


}
