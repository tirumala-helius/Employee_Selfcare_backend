package com.helius.dao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.helius.entities.Employee;
import com.helius.entities.EmployeeCommentsList;
import com.helius.entities.EmployeeTicketingFilesCombine;
import com.helius.entities.Employee_Ticketing_System;

public interface TicketSystemDAO {
	public void saveEmpTicket(Employee emp, MultipartHttpServletRequest request) throws Throwable;

	public void updateTicket(EmployeeTicketingFilesCombine emp, MultipartHttpServletRequest request) throws Throwable;

	public String getTicketHistoryByEmpId(String empId);

	public ResponseEntity<byte[]> downloadFile(String ticketid);

	public EmployeeCommentsList getEmployeeCommentsList(String empid, String ticketNumber);

	public String getallticketfilenames(String ticketId);

}
