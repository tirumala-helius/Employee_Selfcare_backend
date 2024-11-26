package com.helius.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.helius.dao.TicketSystemDAO;
import com.helius.entities.Employee;
import com.helius.entities.EmployeeCommentsList;
import com.helius.entities.EmployeeTicketingFilesCombine;
import com.helius.entities.EmployeeTicketingSystemCommentsList;
import com.helius.entities.Employee_Ticketing_System;
import com.helius.utils.Status;

public class TicketManager {

	private TicketSystemDAO ticketSystemDAO;

	public TicketSystemDAO getTicketSystemDAO() {
		return ticketSystemDAO;
	}

	public void setTicketSystemDAO(TicketSystemDAO ticketSystemDAO) {
		this.ticketSystemDAO = ticketSystemDAO;
	}

	public Status saveEmpTicket(Employee emp, MultipartHttpServletRequest request) {
		try {
			ticketSystemDAO.saveEmpTicket(emp, request);
		} catch (Throwable e) {
			return new Status(false, "ticket details not saved " + e.getMessage());
		}
		return new Status(true, "Ticket Details Saved Successfully.!");
	}

	// update
	public Status updateTicket(EmployeeTicketingFilesCombine emp, MultipartHttpServletRequest request, String userName) {
		try {
			ticketSystemDAO.updateTicket(emp, request, userName);
		} catch (Throwable e) {
			return new Status(false, "ticket details not saved " + e.getMessage());
		}
		return new Status(true, "Ticket Details Saved Successfully.!");
	}

	public String getTicketHistoryByEmpId(String empId) {
		String ticketdetailsById = null;
		try {
			ticketdetailsById = ticketSystemDAO.getTicketHistoryByEmpId(empId);
		} catch (Throwable e) {
			return ticketdetailsById;
		}
		return ticketdetailsById;
	}
	
	public ResponseEntity<byte[]> downloadFile(String ticketid) {
		ResponseEntity<byte[]> res = null;
		try {
			 res =	ticketSystemDAO.downloadFile(ticketid);
		} catch (Throwable e) {
				return res;
		}
		return res;
	}

	public EmployeeCommentsList getEmployeeCommentsList(String empid,String ticketNumber) {
		EmployeeCommentsList empdetailsByStatus = null;
		try {
			empdetailsByStatus = ticketSystemDAO.getEmployeeCommentsList(empid,ticketNumber);
		} catch (Throwable e) {
			return empdetailsByStatus;
		}
		return empdetailsByStatus;
	}

	public String getallfilenames(String ticketId) {
		String result = "";
		try{
			result = ticketSystemDAO.getallticketfilenames(ticketId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	

}
