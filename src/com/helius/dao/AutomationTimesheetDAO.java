package com.helius.dao;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface AutomationTimesheetDAO {

	
	
	public ResponseEntity<byte[]> createAutomationTimesheet(String  clientjson, MultipartHttpServletRequest request) throws Throwable;
	
	public void sendTimesheetAutomationmail(String json,MultipartHttpServletRequest request) throws Throwable;

}
