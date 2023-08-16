package com.helius.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.helius.dao.TicketSystemDAO;
import com.helius.entities.Employee;
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
	public Status updateTicket(Employee emp, MultipartHttpServletRequest request) {
		try {
			ticketSystemDAO.updateTicket(emp, request);
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

}
