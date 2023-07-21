package com.helius.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
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
	public ResponseEntity<?> sendTimesheetAutomationmail(@RequestParam("model") String jsondata,MultipartHttpServletRequest request,MultipartHttpServletRequest request2) throws Throwable {
		List<String> list = null;
		System.out.println("clientjsondata:" + jsondata.toString());
		String errorresponse = 	"";
		try {
	        list = automationManager.sendTimesheetAutomationmail(jsondata, request);
	        return ResponseEntity.ok(list);
	    } catch (Throwable e) {
	    	list = new ArrayList<String>();
	    	list.add(e.getMessage());
	    	
	    	 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(list);
	    }
		
	}
	
	@CrossOrigin
	@RequestMapping(value = "client/getTimesheet", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<byte[]> getTimesheet(@RequestParam String timesheetMonth) throws JsonProcessingException {
	
		ResponseEntity<byte[]> responseEntity  = automationManager.getTimesheet(timesheetMonth);
	
		 return responseEntity;

		
	}
	
	
	
	@CrossOrigin
	@RequestMapping(value = "client/downloadsSavedTimesheet", method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadsSavedTimesheet(@RequestParam String empId, String clientId, String timesheetMonth) throws JsonProcessingException {
	
		ResponseEntity<byte[]> responseEntity  = automationManager.downloadsSavedTimesheet(empId,clientId,timesheetMonth );
	
		 return responseEntity;

		
	}
	
	// This Method is for Get Client Public Holidays 
	/*
	 * @CrossOrigin
	 * 
	 * @RequestMapping(value = "client/getPublicHolidays", method =
	 * RequestMethod.GET) public ResponseEntity<?>
	 * getPublicHolidays(@RequestParam("model") String selectedMonth, String
	 * ClientId) throws Throwable { try { Map<String, String> list =
	 * automationManager.getPublicHolidays(selectedMonth, ClientId); return
	 * ResponseEntity.ok(list); } catch (Throwable e) { return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " +
	 * e.getMessage()); }
	 * 
	 * 
	 * }
	 */
	


}
