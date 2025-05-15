package com.helius.dao;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface AutomationTimesheetDAO {

	
	public ResponseEntity<byte[]> createAutomationTimesheet(String  clientjson, MultipartHttpServletRequest request,String work_country) throws Throwable;
	
	public List<String> sendTimesheetAutomationmail(String json,MultipartHttpServletRequest request, String work_country) throws Throwable;
	
	public ResponseEntity<byte[]> getTimesheet(String  timesheetMonth,String empId,String EmpName,String Client ) throws Throwable;

	public ResponseEntity<byte[]> downloadsTimesheet(String empId, String clientId, String timesheetMonth) throws Throwable;
	
	//public Map<String, String> getPublicHolidays(String month,String clientId) throws Throwable;

}
