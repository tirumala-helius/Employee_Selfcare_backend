package com.helius.dao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.helius.entities.Employee;

public interface TicketSystemDAO {
	public void saveEmpTicket(Employee emp, MultipartHttpServletRequest request) throws Throwable;

	public void updateTicket(Employee emp, MultipartHttpServletRequest request) throws Throwable;

	public String getTicketHistoryByEmpId(String empId);

	public ResponseEntity<byte[]> downloadFile(String ticketid);

}
