package com.helius.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.dao.AutomationTimesheetDAO;
import com.helius.utils.Status;

public class TimesheetAutomationManager {
	
	private AutomationTimesheetDAO automationTimesheetDAO;
	
	
	public AutomationTimesheetDAO getAutomationTimesheetDAO() {
		return automationTimesheetDAO;
	}

	public void setAutomationTimesheetDAO(AutomationTimesheetDAO automationTimesheetDAO) {
		this.automationTimesheetDAO = automationTimesheetDAO;
	}




	public ResponseEntity<byte[]> createAutomationTimesheet(String  clientjson, MultipartHttpServletRequest request) throws JsonProcessingException {
		ResponseEntity<byte[]> response;
		try {
			response =automationTimesheetDAO.createAutomationTimesheet(clientjson,request);
		} catch (Throwable e) {
			
			 Map<String, String> errorDetails = new HashMap<>();
			    errorDetails.put("message", "Failed to Genearet AutomationTimesheet File");
			    errorDetails.put("status", HttpStatus.NOT_FOUND.toString());
			    String json = new ObjectMapper().writeValueAsString(errorDetails);
			    response=  ResponseEntity.<byte[]>status(HttpStatus.NOT_FOUND).body(json.getBytes());
			//return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}
		return  response;
	}

	public List<String> sendTimesheetAutomationmail(String clientjson, MultipartHttpServletRequest request)throws Throwable {
		try {
			return automationTimesheetDAO.sendTimesheetAutomationmail(clientjson, request);
		} catch (Throwable e) {
			throw new Throwable("Automation TimeSheet Process Failed :" + e.getMessage(), e);
		}
	}

	public ResponseEntity<byte[]> getTimesheet(String timesheetMonth ) throws JsonProcessingException {
		ResponseEntity<byte[]> response;
		try {
			response =automationTimesheetDAO.getTimesheet(timesheetMonth);
		} catch (Throwable e) {
			
			 Map<String, String> errorDetails = new HashMap<>();
			    errorDetails.put("message", "Failed to Genearet Timesheet File");
			    errorDetails.put("status", HttpStatus.NOT_FOUND.toString());
			    String json = new ObjectMapper().writeValueAsString(errorDetails);
			    response=  ResponseEntity.<byte[]>status(HttpStatus.NOT_FOUND).body(json.getBytes());
		}
		return  response;
	}
	
	
	public ResponseEntity<byte[]> downloadsSavedTimesheet(String empId, String clientId, String timesheetMonth ) throws JsonProcessingException {
		ResponseEntity<byte[]> response;
		try {
			response =automationTimesheetDAO.downloadsTimesheet(empId,clientId,timesheetMonth );
		} catch (Throwable e) {
			
			 Map<String, String> errorDetails = new HashMap<>();
			    errorDetails.put("message", "Failed to Download Timesheet File");
			    errorDetails.put("status", HttpStatus.NOT_FOUND.toString());
			    String json = new ObjectMapper().writeValueAsString(errorDetails);
			    response=  ResponseEntity.<byte[]>status(HttpStatus.NOT_FOUND).body(json.getBytes());
		}
		return  response;
	}
	
	
	
	/*
	 * public Map<String, String> getPublicHolidays(String selectedMonth, String
	 * clientID) throws Throwable { Map<String, String> list = null; try { list =
	 * automationTimesheetDAO.getPublicHolidays(selectedMonth, clientID); } catch
	 * (Throwable e) { throw new
	 * Throwable("Failed to fetch client Public Holidays :" + e.getMessage()); }
	 * return list;
	 * 
	 * }
	 */
	
		

}
